package org.webswing.directdraw.toolkit;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import org.webswing.directdraw.DirectDraw;
import org.webswing.directdraw.model.ArcConst;
import org.webswing.directdraw.model.ColorConst;
import org.webswing.directdraw.model.CompositeConst;
import org.webswing.directdraw.model.DrawConstant;
import org.webswing.directdraw.model.DrawInstruction;
import org.webswing.directdraw.model.EllipseConst;
import org.webswing.directdraw.model.FontConst;
import org.webswing.directdraw.model.GlyphListConst;
import org.webswing.directdraw.model.GradientConst;
import org.webswing.directdraw.model.ImageConst;
import org.webswing.directdraw.model.IntegerConst;
import org.webswing.directdraw.model.LinearGradientConst;
import org.webswing.directdraw.model.PathConst;
import org.webswing.directdraw.model.PointsConst;
import org.webswing.directdraw.model.RadialGradientConst;
import org.webswing.directdraw.model.RectangleConst;
import org.webswing.directdraw.model.RoundRectangleConst;
import org.webswing.directdraw.model.StringConst;
import org.webswing.directdraw.model.StrokeConst;
import org.webswing.directdraw.model.TextureConst;
import org.webswing.directdraw.model.TransformConst;
import org.webswing.directdraw.proto.Directdraw.DrawInstructionProto.InstructionProto;
import org.webswing.directdraw.util.XorModeComposite;

public class DrawInstructionFactory {

	private DirectDraw ctx;

	public DrawInstructionFactory(DirectDraw ctx) {
		this.ctx = ctx;
	}

	public DrawInstruction draw(Shape s, Shape clip) {
		return new DrawInstruction(InstructionProto.DRAW, toPathConst(s), toPathConst(clip));
	}

	public DrawInstruction fill(Shape s, Shape clip) {
		return new DrawInstruction(InstructionProto.FILL, toPathConst(s), toPathConst(clip));
	}

	public DrawInstruction drawImage(BufferedImage image, AffineTransform transform, Rectangle2D crop, Color bgcolor, Shape clip) {
		DrawConstant<?> transformConst = transform != null ? new TransformConst(ctx, transform) : DrawConstant.nullConst;
		DrawConstant<?> cropConst = crop != null ? new RectangleConst(ctx, crop) : DrawConstant.nullConst;
		DrawConstant<?> bgConst = bgcolor != null ? new ColorConst(ctx, bgcolor) : DrawConstant.nullConst;
		return new DrawInstruction(InstructionProto.DRAW_IMAGE, new ImageConst(ctx, image), transformConst, cropConst, bgConst, toPathConst(clip));
	}

	public DrawInstruction drawWebImage(WebImage image, AffineTransform transform, Rectangle2D crop, Color bgcolor, Shape clip) {
		DrawConstant<?> transformConst = transform != null ? new TransformConst(ctx, transform) : DrawConstant.nullConst;
		DrawConstant<?> cropConst = crop != null ? new RectangleConst(ctx, crop) : DrawConstant.nullConst;
		DrawConstant<?> bgConst = bgcolor != null ? new ColorConst(ctx, bgcolor) : DrawConstant.nullConst;
		return new DrawInstruction(image, transformConst, cropConst, bgConst, toPathConst(clip));
	}

	public DrawInstruction drawString(String s, double x, double y, int width, Shape clip) {
		return new DrawInstruction(InstructionProto.DRAW_STRING, new StringConst(ctx, s), new PointsConst(ctx, (int) x, (int) y, width), toPathConst(clip));
	}

	public DrawInstruction drawGlyphList(String string, Font font, double x, double y, AffineTransform transform, Shape clip) {
		return new DrawInstruction(InstructionProto.DRAW_GLYPH_LIST, new GlyphListConst(ctx, string, font, x, y, transform), toPathConst(clip));
	}

	public DrawInstruction copyArea(int destX, int destY, int width, int height, int absDx, int absDy, Shape clip) {
		return new DrawInstruction(InstructionProto.COPY_AREA, new PointsConst(ctx, destX, destY, width, height, absDx, absDy), toPathConst(clip));
	}

	public DrawInstruction createGraphics(WebGraphics g) {
		DrawConstant<?> id = new IntegerConst(g.getId());
		DrawConstant<?> transformConst = new TransformConst(ctx, g.getTransform());
		DrawConstant<?> compositeConst =new CompositeConst(ctx,g.getComposite());
		DrawConstant<?> strokeConst = g.getStroke() instanceof BasicStroke ? new StrokeConst(ctx, (BasicStroke) g.getStroke()) : DrawConstant.nullConst;
		DrawConstant<?> paintConst;
		try{
			paintConst = getPaintConstant(g.getPaint());
		}catch (UnsupportedOperationException e){
			paintConst = DrawConstant.nullConst;
		}
		DrawConstant<?> fontConst = ctx.requestFont(g.getFont()) ? new FontConst(ctx, g.getFont()) : DrawConstant.nullConst;
		return createGraphics(id, transformConst, strokeConst, compositeConst, paintConst, fontConst);
	}

	public DrawInstruction createGraphics(DrawConstant<?> id, DrawConstant<?> transform, DrawConstant<?> stroke, DrawConstant<?> composite, DrawConstant<?> paint, DrawConstant<?> font) {
		return new DrawInstruction(InstructionProto.GRAPHICS_CREATE, id, transform, stroke, composite, paint, font);
	}

	public DrawInstruction disposeGraphics(WebGraphics g) {
		return new DrawInstruction(InstructionProto.GRAPHICS_DISPOSE, new IntegerConst(g.getId()));
	}

	public DrawInstruction switchGraphics(WebGraphics g) {
		return new DrawInstruction(InstructionProto.GRAPHICS_SWITCH, new IntegerConst(g.getId()));
	}

	public DrawInstruction transform(AffineTransform at) {
		return new DrawInstruction(InstructionProto.TRANSFORM, new TransformConst(ctx, at));
	}

	public DrawInstruction setPaint(Paint p) {
		return new DrawInstruction(InstructionProto.SET_PAINT, getPaintConstant(p));
	}

	protected DrawConstant<?> getPaintConstant(Paint p) {
		if (p instanceof Color) {
			return new ColorConst(ctx, (Color) p);
		} else if (p instanceof GradientPaint) {
			return new GradientConst(ctx, (GradientPaint) p);
		} else if (p instanceof LinearGradientPaint) {
			return new LinearGradientConst(ctx, (LinearGradientPaint) p);
		} else if (p instanceof RadialGradientPaint) {
			return new RadialGradientConst(ctx, (RadialGradientPaint) p);
		} else if (p instanceof TexturePaint) {
			return new TextureConst(ctx, (TexturePaint) p);
		}
		throw new UnsupportedOperationException();
	}

	public DrawInstruction setFont(Font font) {
		return new DrawInstruction(InstructionProto.SET_FONT, new FontConst(ctx, font));
	}

	public DrawInstruction setStroke(BasicStroke stroke) {
		return new DrawInstruction(InstructionProto.SET_STROKE, new StrokeConst(ctx, stroke));
	}

	private DrawConstant<?> toPathConst(Shape s) {
		if (s == null) {
			return DrawConstant.nullConst;
		}
		if (s instanceof Rectangle2D) {
			return new RectangleConst(ctx, (Rectangle2D) s);
		} else if (s instanceof RoundRectangle2D) {
			return new RoundRectangleConst(ctx, (RoundRectangle2D) s);
		} else if (s instanceof Ellipse2D) {
			return new EllipseConst(ctx, (Ellipse2D) s);
		} else if (s instanceof Arc2D) {
			return new ArcConst(ctx, (Arc2D) s);
		} else {
			return new PathConst(ctx, s);
		}
	}

	public DrawInstruction setComposite(AlphaComposite ac) {
		return new DrawInstruction(InstructionProto.SET_COMPOSITE, new CompositeConst(ctx, ac));
	}

	public DrawInstruction setXorMode(Color xorColor) {
		return new DrawInstruction(InstructionProto.SET_COMPOSITE, new CompositeConst(ctx, new XorModeComposite(xorColor)));
	}
}
