package org.webswing.directdraw.model;

import java.util.Arrays;

import org.webswing.directdraw.DirectDraw;
import org.webswing.directdraw.proto.Directdraw.PointsProto;

public class PointsConst extends ImmutableDrawConstantHolder<int[]> {

	public PointsConst(DirectDraw context, int... value) {
		super(context, value);
	}

	@Override
	public String getFieldName() {
		return "points";
	}

	@Override
	public PointsProto toMessage() {
		PointsProto.Builder model = PointsProto.newBuilder();
		if (value != null) {
			for (int i = 0; i < value.length; i++) {
				model.addPoints(value[i]);
			}
		}
		return model.build();
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(value);
	}

	@Override
	public boolean equals(Object o) {
		return o == this || o instanceof PointsConst && Arrays.equals(value, ((PointsConst) o).value);
	}
}
