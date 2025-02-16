package kr.merutilm.rff.functions;

import java.util.Arrays;

import kr.merutilm.rff.selectable.Ease;

@FunctionalInterface
public interface FunctionEase {

    double apply(double t);

    static FunctionEase merge(Ease... ease) {
        return merge(Arrays.stream(ease).map(Ease::fun).toArray(FunctionEase[]::new));
    }

    /**
     * 가감속 합성
     */
    static FunctionEase merge(FunctionEase... eases) {
        FunctionEase result = t -> t;
        for (FunctionEase ease : eases) {
            result = result.andThen(ease);
        }
        return result.multiply(eases.length);
    }

    default FunctionEase andThen(Ease ease) {
        return andThen(ease.fun());
    }

    default FunctionEase andThen(FunctionEase ease) {
        return t -> this.apply(t) + ease.apply(t);
    }

    default FunctionEase multiply(double m) {
        return t -> m * this.apply(t);
    }
}
