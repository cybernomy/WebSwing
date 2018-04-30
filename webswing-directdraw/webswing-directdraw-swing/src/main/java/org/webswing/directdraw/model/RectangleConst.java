package org.webswing.directdraw.model;

import java.awt.geom.Rectangle2D;

import org.webswing.directdraw.DirectDraw;
import org.webswing.directdraw.proto.Directdraw.RectangleProto;

public class RectangleConst extends MutableDrawConstantHolder<Rectangle2D, RectangleProto> {

	public RectangleConst(DirectDraw context, Rectangle2D value) {
		super(context, value);
	}

	@Override
	public String getFieldName() {
		return "rectangle";
	}

	@Override
	public RectangleProto buildMessage(Rectangle2D value) {
		RectangleProto.Builder model = RectangleProto.newBuilder();
		model.setX((int) value.getX());
		model.setY((int) value.getY());
		model.setW((int) value.getWidth());
		model.setH((int) value.getHeight());
		return model.build();
	}

	@Override
	public Rectangle2D getValue() {
		return getValue(message);
	}

	public static Rectangle2D getValue(RectangleProto proto) {
		return new Rectangle2D.Float(proto.getX(), proto.getY(), proto.getW(), proto.getH());
	}
}
