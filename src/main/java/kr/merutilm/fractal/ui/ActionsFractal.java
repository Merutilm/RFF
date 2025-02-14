package kr.merutilm.fractal.ui;

import java.util.function.Consumer;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

import javax.swing.KeyStroke;

import kr.merutilm.fractal.struct.LWBigComplex;
import kr.merutilm.fractal.struct.LWBigDecimal;
import kr.merutilm.fractal.settings.CalculationSettings;
import kr.merutilm.fractal.settings.R3ASettings;
import kr.merutilm.fractal.settings.DecimalizeIterationMethod;
import kr.merutilm.fractal.settings.R3ASelectionMethod;
import kr.merutilm.fractal.settings.ReuseReferenceMethod;

enum ActionsFractal implements Actions {
    RRRA("RRRA", (master, name) -> new RFFSettingsWindow(name, panel -> {
        R3ASettings bla = getCalculationSettings(master).r3aSettings();
        
        Consumer<UnaryOperator<R3ASettings.Builder>> applier = e -> 
            master.setSettings(e1 -> e1.edit().setCalculationSettings(e2 -> e2.edit().setR3ASettings(e3 -> e.apply(e3.edit()).build()).build()).build());

        panel.createTextInput("Min Skip Reference", bla.minSkipReference(), Integer::parseInt, e -> 
            applier.accept(f -> f.setMinSkipReference(e))
        );
        panel.createTextInput("Max Multiplier Between Level", bla.maxMultiplierBetweenLevel(), Integer::parseInt, e -> 
            applier.accept(f -> f.setMaxMultiplierBetweenLevel(e))
        );
        panel.createTextInput("Epsilon Power", bla.epsilonPower(), Double::parseDouble, e -> 
            applier.accept(f -> f.setEpsilonPower(e))
        );

        panel.createSelectInput("Selection Method", bla.r3aSelectionMethod(), R3ASelectionMethod.values(), e -> 
            applier.accept(f -> f.setR3ASelectionMethod(e)), false
        );
    }), null),

    ITERATIONS("Iterations", (master, name) -> new RFFSettingsWindow(name, panel -> {
        CalculationSettings calc = getCalculationSettings(master);
        
        Consumer<UnaryOperator<CalculationSettings.Builder>> applier = e -> 
            master.setSettings(e1 -> e1.edit().setCalculationSettings(e2 ->  e.apply(e2.edit()).build()).build());

        panel.createTextInput("Max Iteration", calc.maxIteration(), Long::parseLong, e -> 
            applier.accept(f -> f.setMaxIteration(e)));

        panel.createTextInput("Bailout", calc.bailout(), Double::parseDouble, e -> 
            applier.accept(f -> f.setBailout(e))
        );
        panel.createSelectInput("Decimal Iteration", calc.decimalIterationSettings(), DecimalizeIterationMethod.values(), e -> 
            applier.accept(f -> f.setDecimalIterationSettings(e)), true
        );
        panel.createBoolInput("Automatic Iterations", calc.autoIteration(), e -> 
            applier.accept(f -> f.setAutoIteration(e))
        );
    }), null),


    REFERENCE("Reference", (master, name) -> new RFFSettingsWindow(name, panel -> {
        CalculationSettings calc = getCalculationSettings(master);
        
        Consumer<UnaryOperator<CalculationSettings.Builder>> applier = e -> 
            master.setSettings(e1 -> e1.edit().setCalculationSettings(e2 ->  e.apply(e2.edit()).build()).build());
        
        

        panel.createTextInput("Center:Re", calc.center().re(), s -> LWBigDecimal.valueOf(s, Math.min(-s.length(), (int)-calc.logZoom()) - 10), e -> 
            applier.accept(f -> f.setCenter(new LWBigComplex(e, calc.center().im())))
        );
        panel.createTextInput("Center:Im", calc.center().im(), s -> LWBigDecimal.valueOf(s, Math.min(-s.length(), (int)-calc.logZoom()) - 10), e -> 
            applier.accept(f -> f.setCenter(new LWBigComplex(calc.center().re(), e)))
        );
        panel.createTextInput("Log Zoom", calc.logZoom(), Double::parseDouble, e -> 
            applier.accept(f -> f.setLogZoom(e))
        );
        panel.createSelectInput("Reuse Reference",  calc.reuseReference(), ReuseReferenceMethod.values(), e -> 
            applier.accept(f -> f.setReuseReference(e)), false
        );
        
        
    }), null);
   

    private final String name;
    private final BiConsumer<RFF, String> action;
    private final KeyStroke keyStroke;

    @Override
    public KeyStroke keyStroke() {
        return keyStroke;
    }

    private ActionsFractal(String name, BiConsumer<RFF, String> generator, KeyStroke keyStroke) {
        this.name = name;
        this.action = generator;
        this.keyStroke = keyStroke;
    }


    @Override
    public String toString() {
        return name;
    }

    @Override
    public void accept(RFF master) {
        action.accept(master, name);
    }

    private static CalculationSettings getCalculationSettings(RFF master){
        return master.getSettings().calculationSettings();
    }
   
    
}
