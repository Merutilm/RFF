package kr.merutilm.fractal.ui;

import java.util.function.Consumer;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

import kr.merutilm.fractal.struct.LWBigComplex;
import kr.merutilm.fractal.struct.LWBigDecimal;
import kr.merutilm.fractal.settings.CalculationSettings;
import kr.merutilm.fractal.settings.BLASettings;
import kr.merutilm.fractal.settings.DecimalIterationSettings;
import kr.merutilm.fractal.settings.ReuseReferenceSettings;

enum ActionsFractal implements Actions {
    BLA("BLA", (master, name) -> new SettingsWindow(name, panel -> {
        BLASettings bla = getCalculationSettings(master).blaSettings();
        
        Consumer<UnaryOperator<BLASettings.Builder>> applier = e -> 
            master.setSettings(e1 -> e1.edit().setCalculationSettings(e2 -> e2.edit().setBLASettings(e3 -> e.apply(e3.edit()).build()).build()).build());

        panel.createTextInput("Epsilon Power", null, bla.epsilonPower(), Double::parseDouble, e -> 
            applier.accept(f -> f.setEpsilonPower(e))
        );

        panel.createTextInput("Minimum Level", null, bla.minLevel(), Integer::parseInt, e -> 
            applier.accept(f -> f.setMinLevel(e))
        );
    })),

    ITERATIONS("Iterations", (master, name) -> new SettingsWindow(name, panel -> {
        CalculationSettings calc = getCalculationSettings(master);
        
        Consumer<UnaryOperator<CalculationSettings.Builder>> applier = e -> 
            master.setSettings(e1 -> e1.edit().setCalculationSettings(e2 ->  e.apply(e2.edit()).build()).build());

        panel.createTextInput("Max Iteration", null, calc.maxIteration(), Long::parseLong, e -> 
            applier.accept(f -> f.setMaxIteration(e)));

        panel.createTextInput("Bailout", null, calc.bailout(), Double::parseDouble, e -> 
            applier.accept(f -> f.setBailout(e))
        );
        panel.createSelectInput("Decimal Iteration", null, calc.decimalIterationSettings(), DecimalIterationSettings.values(), e -> 
            applier.accept(f -> f.setDecimalIterationSettings(e))
        );
        panel.createBoolInput("Automatic Iterations", null, calc.autoIteration(), e -> 
            applier.accept(f -> f.setAutoIteration(e))
        );
    })),


    REFERENCE("Reference", (master, name) -> new SettingsWindow(name, panel -> {
        CalculationSettings calc = getCalculationSettings(master);
        
        Consumer<UnaryOperator<CalculationSettings.Builder>> applier = e -> 
            master.setSettings(e1 -> e1.edit().setCalculationSettings(e2 ->  e.apply(e2.edit()).build()).build());
        
        panel.createTextInput("Center:Re", null, calc.center().re(), s -> LWBigDecimal.valueOf(s, -s.length()), e -> 
            applier.accept(f -> f.setCenter(new LWBigComplex(e, calc.center().im())))
        );
        panel.createTextInput("Center:Im", null, calc.center().im(), s -> LWBigDecimal.valueOf(s, -s.length()), e -> 
            applier.accept(f -> f.setCenter(new LWBigComplex(calc.center().re(), e)))
        );
        panel.createTextInput("Log Zoom", null, calc.logZoom(), Double::parseDouble, e -> 
            applier.accept(f -> f.setLogZoom(e))
        );
        panel.createSelectInput("Reuse Reference", null, calc.reuseReference(), ReuseReferenceSettings.values(), e -> 
            applier.accept(f -> f.setReuseReference(e))
        );
        
        
    }));
   

    private final String name;
    private final BiConsumer<RFF, String> generator;

    private ActionsFractal(String name, BiConsumer<RFF, String> generator){
        this.name = name;
        this.generator = generator;
    }


    @Override
    public String toString() {
        return name;
    }

    @Override
    public void accept(RFF master) {
        generator.accept(master, name);
    }

    private static CalculationSettings getCalculationSettings(RFF master){
        return master.getSettings().calculationSettings();
    }
   
    
}
