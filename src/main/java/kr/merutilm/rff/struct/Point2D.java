package kr.merutilm.rff.struct;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import kr.merutilm.rff.functions.FunctionEase;
import kr.merutilm.rff.util.AdvancedMath;

import java.util.Objects;


public record Point2D(double x, double y) implements Struct<Point2D> {

    public static final Point2D ORIGIN = new Point2D(0.0, 0.0);

    @Nullable
    public static Point2D convert(String value) {
        if (value == null) {
            return null;
        }
        String[] arr = value.replace(" ", "").split(",");
        if (arr.length == 1) {
            double v = Double.parseDouble(Objects.equals(arr[0], "null") ? "NaN" : arr[0]);
            return new Point2D(v, v);
        }

        double x = Double.parseDouble(Objects.equals(arr[0], "null") ? "NaN" : arr[0]);
        double y = Double.parseDouble(Objects.equals(arr[1], "null") ? "NaN" : arr[1]);
        return new Point2D(x, y);
    }

    @Override
    public Builder edit() {
        return new Builder(x, y);
    }

    public static final class Builder implements StructBuilder<Point2D> {
        private double x;
        private double y;

        public Builder(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public Builder setX(double x) {
            this.x = x;
            return this;
        }

        public Builder setY(double y) {
            this.y = y;
            return this;
        }

        @Override
        public Point2D build() {
            return new Point2D(x, y);
        }

    }

    public double distance(Point2D target) {
        return distance(target.x, target.y);
    }

    public double distance(double x, double y) {
        return AdvancedMath.hypot(x - this.x, y - this.y);
    }

    public static Point2D ratioDivide(Point2D start, Point2D end, double ratio) {
        return new Point2D(AdvancedMath.ratioDivide(start.x, end.x, ratio), AdvancedMath.ratioDivide(start.y, end.y, ratio));
    }

    public static Point2D ratioDivide(Point2D start, Point2D end, double ratio, FunctionEase ease) {
        return new Point2D(AdvancedMath.ratioDivide(start.x, end.x, ratio, ease), AdvancedMath.ratioDivide(start.y, end.y, ratio, ease));
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Point2D p){
            return p.x == x && p.y == y;
        }
        return false;
    }

    @Nonnull
    @Override
    public String toString() {
        return (Double.isNaN(x) ? "null" : AdvancedMath.fixDouble(x)) + ", " + (Double.isNaN(y) ? "null" : AdvancedMath.fixDouble(y));
    }
}
