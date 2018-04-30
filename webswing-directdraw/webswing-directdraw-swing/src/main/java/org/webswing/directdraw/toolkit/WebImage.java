package org.webswing.directdraw.toolkit;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.webswing.directdraw.DirectDraw;
import org.webswing.directdraw.model.CompositeDrawConstantHolder;
import org.webswing.directdraw.model.DrawConstant;
import org.webswing.directdraw.model.DrawInstruction;
import org.webswing.directdraw.model.FontFaceConst;
import org.webswing.directdraw.model.IntegerConst;
import org.webswing.directdraw.proto.Directdraw.DrawConstantProto;
import org.webswing.directdraw.proto.Directdraw.DrawInstructionProto.InstructionProto;
import org.webswing.directdraw.proto.Directdraw.FontFaceProto;
import org.webswing.directdraw.proto.Directdraw.WebImageProto;
import org.webswing.directdraw.util.DirectDrawUtils;
import org.webswing.directdraw.util.DrawConstantPool;
import org.webswing.directdraw.util.RenderUtil;

import sun.awt.image.SurfaceManager;
import sun.java2d.SurfaceData;

@SuppressWarnings({"unused","restriction"})
public class WebImage extends Image {

	private String id = UUID.randomUUID().toString();
	private DirectDraw context;
	private Dimension size;
	private volatile int lastGraphicsId = 0;
	private WebGraphics lastUsedG = null;
	private Set<WebGraphics> usedGraphics;
	private List<DrawInstruction> instructions;
	private boolean resetBeforeRepaint;

	public WebImage(DirectDraw dd, int w, int h) {
		this(dd, w, h, new ArrayList<DrawInstruction>());
		this.usedGraphics = new HashSet<WebGraphics>();
	}

	private WebImage(DirectDraw dd, int w, int h, List<DrawInstruction> instructions) {
		this.context = dd;
		this.size = new Dimension(w, h);
		this.instructions = instructions;

		SurfaceManager.setManager(this, new SurfaceManager() {

			// java 1.6
			public SurfaceData getSourceSurfaceData(sun.java2d.SurfaceData s, sun.java2d.loops.CompositeType c, java.awt.Color color, boolean b) {
				BufferedImage snapshot = WebImage.this.getSnapshot();
				SurfaceManager m = SurfaceManager.getManager(snapshot);
				try {
					return (SurfaceData) m.getClass().getDeclaredMethod("getSourceSurfaceData", sun.java2d.SurfaceData.class, sun.java2d.loops.CompositeType.class, java.awt.Color.class, Boolean.TYPE).invoke(m, s, c, color, b);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}

			// java 1.6
			public SurfaceData getDestSurfaceData() {
				BufferedImage snapshot = WebImage.this.getSnapshot();
				SurfaceManager m = SurfaceManager.getManager(snapshot);
				try {
					return (SurfaceData) m.getClass().getDeclaredMethod("getDestSurfaceData").invoke(m);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}

			public SurfaceData getPrimarySurfaceData() {// java 1.7
				BufferedImage snapshot = WebImage.this.getSnapshot();
				SurfaceManager m = SurfaceManager.getManager(snapshot);
				try {
					return (SurfaceData) m.getClass().getDeclaredMethod("getPrimarySurfaceData").invoke(m);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}

			public SurfaceData restoreContents() {// java 1.7
				BufferedImage snapshot = WebImage.this.getSnapshot();
				SurfaceManager m = SurfaceManager.getManager(snapshot);
				try {
					return (SurfaceData) m.getClass().getDeclaredMethod("restoreContents").invoke(m);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		});
	}

	@Override
	public int getWidth(ImageObserver paramImageObserver) {
		return size.width;
	}

	@Override
	public int getHeight(ImageObserver paramImageObserver) {
		return size.height;
	}

	@Override
	public ImageProducer getSource() {
		return getSnapshot().getSource();
	}

	@Override
	public Graphics getGraphics() {
		synchronized (this) {
			if (resetBeforeRepaint) {
				reset();
				resetBeforeRepaint = false;
			}
		}
		return new WebGraphics(this);
	}

	protected synchronized int getNextGraphicsId() {
		return lastGraphicsId++;
	}

	public DirectDraw getContext() {
		return context;
	}

	@Override
	public Object getProperty(String paramString, ImageObserver paramImageObserver) {
		return null;
	}

	public boolean isDirty() {
		synchronized (this) {
			return !instructions.isEmpty();
		}
	}

	public void addInstruction(WebGraphics g, DrawInstruction in) {
		if (g != null && g.isDisposed()) {
			return;
		}
		synchronized (this) {
			DrawInstructionFactory factory = context.getInstructionFactory();
			if (lastUsedG != g) {
				if (!usedGraphics.contains(g)) {
					//if dispose is the first instruction, ignore it 
					if (in.getInstruction().equals(InstructionProto.GRAPHICS_DISPOSE)) {
						return;
					}
					instructions.add(factory.createGraphics(g));
					usedGraphics.add(g);
					lastUsedG = g;
					if (in.getInstruction().equals(InstructionProto.TRANSFORM)) {
						// skip adding the instruction- transform is already
						// included in create graphics inst.
						return;
					}
				} else {
					instructions.add(factory.switchGraphics(g));
					lastUsedG = g;
				}
			}
			instructions.add(in);
		}
	}

	public void dispose(WebGraphics g) {
		synchronized (this) {
			usedGraphics.remove(g);
		}
	}

	public WebImage extractReadOnlyWebImage(boolean reset) {
		WebImage result;
		synchronized (this) {
			result = reset ? new ReadOnlyWebImage(context, size.width, size.height, instructions) : new ReadOnlyWebImage(context, size.width, size.height, new ArrayList<DrawInstruction>(instructions));
			result.id = id;
			if (reset) {
				reset();
			}
		}
		return result;
	}

	protected WebImageProto toMessageInternal(DirectDraw dd) {
		DrawConstantPool constantPool = dd.getConstantPool();

		WebImageProto.Builder webImageBuilder = WebImageProto.newBuilder();

		DirectDrawUtils.optimizeInstructions(dd, instructions);

		// build proto message
		for (DrawInstruction instruction : instructions) {
			List<FontFaceProto> fontProtos = new ArrayList<FontFaceProto>();
			for(FontFaceConst fontConst:constantPool.registerRequestedFonts()){
				fontProtos.add(fontConst.toMessage());
			}
			
			List<DrawConstantProto> constProtos = new ArrayList<DrawConstantProto>();
			for (DrawConstant<?> cons : instruction) {
				if (cons instanceof CompositeDrawConstantHolder) {
					CompositeDrawConstantHolder<?> composite = (CompositeDrawConstantHolder<?>) cons;
					composite.expandAndCacheConstants(constProtos, constantPool);
				} else if (!(cons instanceof IntegerConst) && cons != DrawConstant.nullConst) {
					int id = constantPool.addToCache(constProtos, cons);
					cons.setId(id);
				}
			}
			
			webImageBuilder.addAllFontFaces(fontProtos);
			webImageBuilder.addAllConstants(constProtos);
			webImageBuilder.addInstructions(instruction.toMessage(dd));
		}
		webImageBuilder.setWidth(size.width);
		webImageBuilder.setHeight(size.height);
		return webImageBuilder.build();
	}

	public void resetBeforeRepaint() {
		this.resetBeforeRepaint = true;
	}

	public void reset() {
		synchronized (this) {
			lastUsedG = null;
			usedGraphics.clear();
			// do not clear these collections as they are be copied to read-only instance
			instructions = new ArrayList<DrawInstruction>();
		}
	}

	public WebImageProto toMessage(DirectDraw dd) {
		// toMessageInternal is executed from anonymous subclass' overriden
		// method created in 'extractReadOnlyWebImage' method
		throw new IllegalStateException("Only read-only image can be encoded to proto message. Invoke extractReadOnlyWebImage method first to create read-only webimage.");
	}

	public BufferedImage getSnapshot() {
		return RenderUtil.render(instructions, size);
	}

	public BufferedImage getSnapshot(BufferedImage result) {
		return RenderUtil.render(result, instructions);
	}

	private static class ReadOnlyWebImage extends WebImage {

		public ReadOnlyWebImage(DirectDraw dd, int w, int h, List<DrawInstruction> instructions) {
			super(dd, w, h, instructions);
		}

		@Override
		public void addInstruction(WebGraphics g, DrawInstruction in) {
			throw new UnsupportedOperationException("This is read only instance of webimage.");
		}

		@Override
		public WebImageProto toMessage(DirectDraw dd) {
			return toMessageInternal(dd);
		}

		@Override
		public void dispose(WebGraphics g) {
			// nothing to dispose
		}

		@Override
		public void reset() {
			// do nothing
		}
	}
}
