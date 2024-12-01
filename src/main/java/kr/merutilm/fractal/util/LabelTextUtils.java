package kr.merutilm.fractal.util;

import javax.annotation.Nullable;

public class LabelTextUtils {
    private LabelTextUtils(){

    }
    public static String processText(double progressRatio){
        return processText(progressRatio, null);
    }
    public static String processText(double progressRatio, Parentheses parentheses){
        return wrap(String.format("%.2f", progressRatio * 100) + "%", parentheses);
    }
    public static String frac(int numerator, int denominator){
        return frac(numerator, denominator, null);
    }
    public static String frac(int numerator, int denominator, Parentheses parentheses){
        return wrap(numerator + "/" + denominator, parentheses);
    }

    private static String wrap(String a, @Nullable Parentheses parentheses){
        if(parentheses == null){
            return a;
        }
        return switch (parentheses){
            case PARENTHESES -> "(" + a + ")";
            case CURLY -> "{" + a + "}";
            case SQUARE -> "[" + a + "]";
            case ANGLE -> "<" + a + ">";
            default -> a;
        };
    }


    public enum Parentheses{
        PARENTHESES, CURLY, SQUARE, ANGLE
    }
}
