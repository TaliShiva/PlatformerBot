package util;

import model.Vec2Double;
import model.Vec2Float;

public interface Constants {
    static float EPS = 1e-6f;
    static double lengthOnOneTick = 1d / 6d;

    static double floatDistance(Vec2Float a, Vec2Float b) {
        return Math.sqrt(a.getX() - b.getX()) * (a.getX() - b.getX()) + (a.getY() - b.getY()) * (a.getY() - b.getY());
    }

    static double doubleDistance(Vec2Double a, Vec2Double b) {
        return Math.sqrt(a.getX() - b.getX()) * (a.getX() - b.getX()) + (a.getY() - b.getY()) * (a.getY() - b.getY());
    }
}
