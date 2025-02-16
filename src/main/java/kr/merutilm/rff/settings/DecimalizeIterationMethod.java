package kr.merutilm.rff.settings;

import kr.merutilm.rff.selectable.Selectable;

public enum DecimalizeIterationMethod implements Selectable{
    /**
     * Do Not Use Decimal Iterations.
     */
    NONE("None"),
    /**
     * Use triangle inequality once.
     */
    LINEAR("Linear"),
    /**
     * Calculates <b>Sqrt(Linear)</b>.
     */
    SQUARE_ROOT("Square root"),
    /**
     * Calculates <b>Log(Linear + 1)</b>.
     */
    LOG("Log"),
    /**
     * Calculates <b>Log(Log(Linear + 1) + 1)</b>.
     */
    LOG_LOG("LogLog");

    private final String name;

    @Override
    public String toString() {
        return name;
    }

    DecimalizeIterationMethod(String name){
        this.name = name;
    }
}
