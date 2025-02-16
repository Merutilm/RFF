package kr.merutilm.rff.ui;

import java.util.function.Consumer;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

import javax.swing.KeyStroke;

import kr.merutilm.rff.struct.LWBigComplex;
import kr.merutilm.rff.struct.LWBigDecimal;
import kr.merutilm.rff.settings.CalculationSettings;
import kr.merutilm.rff.settings.R3ASettings;
import kr.merutilm.rff.settings.DecimalizeIterationMethod;
import kr.merutilm.rff.settings.R3ASelectionMethod;
import kr.merutilm.rff.settings.ReuseReferenceMethod;

enum ActionsFractal implements Actions {
    R3A("R3A", "Recursive Reference Rebasing Approximation. Determine all of the period based on reference orbit, skips the period of iteration at once. The render speed will be significantly faster when using it recursively.", (master, name) -> new RFFSettingsWindow(name, panel -> {
        R3ASettings r3a = getCalculationSettings(master).r3aSettings();
        
        Consumer<UnaryOperator<R3ASettings.Builder>> applier = e -> 
            master.setSettings(e1 -> e1.edit().setCalculationSettings(e2 -> e2.edit().setR3ASettings(e3 -> e.apply(e3.edit()).build()).build()).build());

        panel.createTextInput("Min Skip Reference", r3a.minSkipReference(), Integer::parseInt, e ->
            applier.accept(f -> f.setMinSkipReference(e))
        );
        panel.createTextInput("Max Multiplier Between Level", r3a.maxMultiplierBetweenLevel(), Integer::parseInt, e ->
            applier.accept(f -> f.setMaxMultiplierBetweenLevel(e))
        );
        panel.createTextInput("Epsilon Power", r3a.epsilonPower(), Double::parseDouble, e ->
            applier.accept(f -> f.setEpsilonPower(e))
        );

        panel.createSelectInput("Selection Method", r3a.r3aSelectionMethod(), R3ASelectionMethod.values(), e ->
            applier.accept(f -> f.setR3ASelectionMethod(e)), false
        );
    }), null),

    ITERATIONS("Iterations", "Open the iteration settings. You can set the Max Iteration, Auto Iteration, and etc. here.", (master, name) -> new RFFSettingsWindow(name, panel -> {
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


    REFERENCE("Reference", "Open the reference settings. You can set the Location, Zoom, and etc. here", (master, name) -> new RFFSettingsWindow(name, panel -> {
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
    private final String description;

    @Override
    public KeyStroke keyStroke() {
        return keyStroke;
    }

    public String description() {
        return description;
    }

    ActionsFractal(String name, String description, BiConsumer<RFF, String> generator, KeyStroke keyStroke) {
        this.name = name;
        this.description = description;
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
