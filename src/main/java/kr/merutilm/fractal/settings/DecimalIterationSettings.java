package kr.merutilm.fractal.settings;

import kr.merutilm.base.selectable.Selectable;

public enum DecimalIterationSettings implements Selectable{
    /**
     * Do Not Use Decimal Iterations.
     */
    NONE("None"),
    /**
     * Use triangle inequation once.
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

    private DecimalIterationSettings(String name){
        this.name = name;
    }
}
