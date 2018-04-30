package org.webswing.directdraw.model;

import java.awt.geom.RoundRectangle2D;

import org.webswing.directdraw.DirectDraw;
import org.webswing.directdraw.proto.Directdraw.RoundRectangleProto;

public class RoundRectangleConst extends MutableDrawConstantHolder<RoundRectangle2D, RoundRectangleProto> {

	public RoundRectangleConst(DirectDraw context, RoundRectangle2D value) {
		super(context, value);
	}

	@Override
	public String getFieldName() {
		return "roundRectangle";
	}

	@Override
	public RoundRectangleProto buildMessage(RoundRectangle2D value) {
		RoundRectangleProto.Builder model = RoundRectangleProto.newBuilder();
		model.setX((int) value.getX());
		model.setY((int) value.getY());
		model.setW((int) value.getWidth());
		model.setH((int) value.getHeight());
		model.setArcH((int) value.getArcHeight());
		model.setArcW((int) value.getArcWidth());
		return model.build();
	}

	@Override
	public RoundRectangle2D getValue() {
		return new RoundRectangle2D.Float(message.getX(), message.getY(), message.getW(), message.getH(), message.getArcW(), message.getArcH());
	}
}
