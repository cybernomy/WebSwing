package org.webswing.directdraw.model;

import java.awt.AlphaComposite;
import java.awt.Composite;

import org.webswing.directdraw.DirectDraw;
import org.webswing.directdraw.proto.Directdraw.CompositeProto;
import org.webswing.directdraw.proto.Directdraw.CompositeProto.CompositeTypeProto;
import org.webswing.directdraw.util.XorModeComposite;

public class CompositeConst extends ImmutableDrawConstantHolder<Composite> {

	public CompositeConst(DirectDraw context, Composite value) {
		super(context, value);
	}

	@Override
	public String getFieldName() {
		return "composite";
	}

	@Override
	public CompositeProto toMessage() {
		CompositeProto.Builder model = CompositeProto.newBuilder();
		if(value instanceof AlphaComposite) {
			AlphaComposite avalue=(AlphaComposite) value;
			model.setType(CompositeTypeProto.valueOf(avalue.getRule()));
			if (avalue.getAlpha() != 1f) {
				model.setAlpha(avalue.getAlpha());
			}
		}else if(value instanceof XorModeComposite){
			model.setType(CompositeTypeProto.XOR_MODE);
			model.setColor(ColorConst.toRGBA(((XorModeComposite) value).getXorColor()));
		}
		return model.build();
	}
}
