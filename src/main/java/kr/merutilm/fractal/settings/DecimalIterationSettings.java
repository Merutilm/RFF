package kr.merutilm.fractal.settings;

import kr.merutilm.base.selectable.Selectable;

public enum DecimalIterationSettings implements Selectable{
    LINEAR("Linear"), 
    LOG("Log"),
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
