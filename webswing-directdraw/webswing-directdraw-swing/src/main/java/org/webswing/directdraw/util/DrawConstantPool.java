package org.webswing.directdraw.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.webswing.directdraw.model.DrawConstant;
import org.webswing.directdraw.model.FontFaceConst;
import org.webswing.directdraw.proto.Directdraw.DrawConstantProto;

public class DrawConstantPool {

    private LRUDrawConstantPoolCache pool;
    private Set<String> registeredFonts=new HashSet<String>();
    private Map<String,FontFaceConst> requestedFonts=new HashMap<String,FontFaceConst>();
    

    public DrawConstantPool(int size) {
        pool = new LRUDrawConstantPoolCache(size);
    }

    public DrawConstant<?> getCached(DrawConstant<?> constant) {
        return pool.getOrAdd(constant);
    }

    private void addToCache(DrawConstant<?> constant) {
        pool.getOrAdd(constant);
    }

    private boolean isInCache(DrawConstant<?> constant) {
        return pool.contains(constant);
    }
    
    public int addToCache(List<DrawConstantProto> protos, DrawConstant<?> cons) {
		int thisId;
		if (!isInCache(cons)) {
			DrawConstant<?> cacheEntry = cons.toCacheEntry();
			addToCache(cacheEntry);
			DrawConstantProto.Builder proto = DrawConstantProto.newBuilder();
			if (cons.getFieldName() != null) {
				proto.setField(DrawConstantProto.Builder.getDescriptor().findFieldByName(cons.getFieldName()), cons.toMessage());
			}
			proto.setId(cacheEntry.getId());
			protos.add(proto.build());
			thisId=cacheEntry.getId();
		} else {
			thisId=getCached(cons).getId();
		}
		return thisId;
	}

	public synchronized boolean isFontRegistered(String file) {
		if(requestedFonts.containsKey(file) || registeredFonts.contains(file)){
			return true;
		}
		return false;
	}

	public synchronized void requestFont(String file, FontFaceConst fontFaceConst) {
		requestedFonts.put(file, fontFaceConst);		
	}
	
	public synchronized Collection<FontFaceConst> registerRequestedFonts(){
		if(requestedFonts.size()>0){
			Collection<FontFaceConst> result = new ArrayList<FontFaceConst>(requestedFonts.values());
			registeredFonts.addAll(requestedFonts.keySet());
			requestedFonts.clear();
			return result;
		}
		return Collections.emptyList();
	}
    
}
