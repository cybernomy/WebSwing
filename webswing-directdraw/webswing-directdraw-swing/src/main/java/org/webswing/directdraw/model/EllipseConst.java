package org.webswing.directdraw.model;

import java.awt.geom.Ellipse2D;

import org.webswing.directdraw.DirectDraw;
import org.webswing.directdraw.proto.Directdraw.EllipseProto;

public class EllipseConst extends MutableDrawConstantHolder<Ellipse2D, EllipseProto> {

	public EllipseConst(DirectDraw context, Ellipse2D value) {
		super(context, value);
	}

	@Override
	protected EllipseProto buildMessage(Ellipse2D value) {
		EllipseProto.Builder model = EllipseProto.newBuilder();
		model.setX((int) value.getX());
		model.setY((int) value.getY());
		model.setW((int) value.getWidth());
		model.setH((int) value.getHeight());
		return model.build();
	}

	@Override
	public String getFieldName() {
		return "ellipse";
	}

	@Override
	public Ellipse2D getValue() {
		return new Ellipse2D.Float(message.getX(), message.getY(), message.getW(), message.getH());
	}
}
