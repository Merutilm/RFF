package kr.merutilm.rff.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

import javax.swing.KeyStroke;

import kr.merutilm.rff.formula.Perturbator;
import kr.merutilm.rff.struct.LWBigComplex;
import kr.merutilm.rff.struct.LWBigDecimal;
import kr.merutilm.rff.settings.CalculationSettings;
import kr.merutilm.rff.settings.R3ASettings;
import kr.merutilm.rff.settings.DecimalizeIterationMethod;
import kr.merutilm.rff.settings.R3ASelectionMethod;
import kr.merutilm.rff.settings.ReuseReferenceMethod;
import kr.merutilm.rff.settings.R3ACompressionMethod;

enum ActionsFractal implements Actions {
    R3A("R3A", "<b> Recursive Reference Rebasing Approximation. </b> <br> Determine all of the period based on reference orbit, skips the period of iteration at once. <br>  The render speed will be significantly faster when using it recursively.", (master, name) -> new RFFSettingsWindow(master.getWindow(), name, (_, panel) -> {
        R3ASettings r3a = getCalculationSettings(master).r3aSettings();

        Consumer<UnaryOperator<R3ASettings.Builder>> applier = e ->
                master.setSettings(e1 -> e1.edit().setCalculationSettings(e2 -> e2.edit().setR3ASettings(e3 -> e.apply(e3.edit()).build()).build()).build());

        panel.createTextInput("Min Skip Reference", "Set minimum skipping reference iteration when creating a table.", r3a.minSkipReference(), Integer::parseInt, e ->
                applier.accept(f -> f.setMinSkipReference(e))
        );
        panel.createTextInput("Max Multiplier Between Level", "Set maximum multiplier between adjacent skipping levels. <br> This means the maximum multiplier of two adjacent periods for the new period that inserts between them, <br> So the multiplier between the two periods may in the worst case be the square of this.", r3a.maxMultiplierBetweenLevel(), Integer::parseInt, e ->
                applier.accept(f -> f.setMaxMultiplierBetweenLevel(e))
        );
        panel.createTextInput("Epsilon Power", "Set Epsilon power of ten. Useful for glitch reduction. if this value is small, <br>The fractal will be rendered glitch-less but slow, <br>and is large, It will be fast, but maybe shown visible glitches.", r3a.epsilonPower(), Double::parseDouble, e ->
                applier.accept(f -> f.setEpsilonPower(e))
        );

        panel.createSelectInput("Selection Method", "Set the selection method of R3A.", r3a.r3aSelectionMethod(), R3ASelectionMethod.values(), e ->
                applier.accept(f -> f.setR3ASelectionMethod(e)), false
        );
        panel.createSelectInput("Compression Method", "Set the compession method of R3A.", r3a.r3aCompressionMethod(), R3ACompressionMethod.values(), e ->
                applier.accept(f -> f.setR3ACompressionMethod(e)), false
        );
    }), null),

    ITERATIONS("Iterations", "Open the iteration settings. You can set the Max Iteration, Auto Iteration, and etc. here.", (master, name) -> new RFFSettingsWindow(master.getWindow(), name, (_, panel) -> {
        CalculationSettings calc = getCalculationSettings(master);

        Consumer<UnaryOperator<CalculationSettings.Builder>> applier = e ->
                master.setSettings(e1 -> e1.edit().setCalculationSettings(e2 -> e.apply(e2.edit()).build()).build());

        panel.createTextInput("Max Iteration", "Set maximum iteration. It is disabled when Auto iteration is enabled.", calc.maxIteration(), Long::parseLong, e ->
                applier.accept(f -> f.setMaxIteration(e)));

        panel.createTextInput("Bailout", "Set the bailout radius.", calc.bailout(), Double::parseDouble, e ->
                applier.accept(f -> f.setBailout(e))
        );
        panel.createSelectInput("Decimal Iteration", "Iteration decimalization method", calc.decimalIterationSettings(), DecimalizeIterationMethod.values(), e ->
                applier.accept(f -> f.setDecimalIterationSettings(e)), true
        );
        panel.createBoolInput("Automatic Iterations", "Set max iteration automatic.", calc.autoIteration(), e ->
                applier.accept(f -> f.setAutoIteration(e))
        );
    }), null),


    REFERENCE("Reference", "Open the reference settings. You can set the Location, Zoom, and etc. here.", (master, name) -> new RFFSettingsWindow(master.getWindow(), name, (window, panel) -> {
        CalculationSettings calc = getCalculationSettings(master);

        Consumer<UnaryOperator<CalculationSettings.Builder>> applier = e ->
                master.setSettings(e1 -> e1.edit().setCalculationSettings(e2 -> e.apply(e2.edit()).build()).build());
        AtomicReference<String> realStr = new AtomicReference<>(calc.center().re().toString());
        AtomicReference<String> imagStr = new AtomicReference<>(calc.center().im().toString());
        AtomicReference<Double> zoomStr = new AtomicReference<>(calc.logZoom());


        panel.createTextInput("Center:Re", "Real part of center, The changes will be applied when this window is closed.", calc.center().re().toString(), s -> s, realStr::set);
        panel.createTextInput("Center:Im", "Imaginary part of center, The changes will be applied when this window is closed.", calc.center().im().toString(), s -> s, imagStr::set);
        panel.createTextInput("Log Zoom", "The logarithm by 10 of zoom, The changes will be applied when this window is closed.", calc.logZoom(), Double::parseDouble, zoomStr::set);
        panel.createSelectInput("Reuse Reference", "Set the method of reference reusing.", calc.reuseReference(), ReuseReferenceMethod.values(), e ->
                applier.accept(f -> f.setReuseReference(e)), false
        );
        panel.createTextInput("Reference Compress Criteria", "When compressing references, sets the minimum amount of references to compress at one time. <br> Reference compression slows down the calculation but frees up memory space. <br> To disable, write -1.", calc.compressCriteria(), Integer::parseInt, e ->
                applier.accept(f -> f.setCompressCriteria(e))
        );
        panel.createTextInput("Reference Compress Threshold", "When compressing references, sets the negative exponents of ten of minimum error to be considered equal. <br> Reference compression slows down the calculation but frees up memory space. <br> To disable, write -1.", calc.compressionThresholdPower(), Integer::parseInt, e ->
        applier.accept(f -> f.setCompressionThresholdPower(e))
);
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                applier.accept(f -> {
                    double zoom = zoomStr.get();
                    int precision = Perturbator.precision(zoom);

                    LWBigDecimal re = LWBigDecimal.valueOf(realStr.get(), precision);
                    LWBigDecimal im = LWBigDecimal.valueOf(imagStr.get(), precision);

                    return f.setCenter(new LWBigComplex(re, im)).setLogZoom(zoom);
                });
            }
        });

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

    private static CalculationSettings getCalculationSettings(RFF master) {
        return master.getSettings().calculationSettings();
    }


}
