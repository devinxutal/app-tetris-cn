package com.devinxutal.tetris.util;

public class MathUtil {
	public static double distance(double x1, double y1, double x2, double y2) {
		return Math.sqrt(MathUtil.distanceSquared(x1, y1, x2, y2));
	}

	public static double distanceSquared(double x1, double y1, double x2,
			double y2) {
		return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
	}

	public static double velocityScale(double v, double vx, double vy) {
		return v/Math.sqrt(vx*vx+vy*vy);
	}
}
