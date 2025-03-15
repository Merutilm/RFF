package kr.merutilm.rff.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import javax.swing.KeyStroke;

import kr.merutilm.rff.formula.Perturbator;
import kr.merutilm.rff.struct.LWBigComplex;
import kr.merutilm.rff.struct.LWBigDecimal;
import kr.merutilm.rff.ui.HTMLStringBuilder.Tag;
import kr.merutilm.rff.settings.CalculationSettings;
import kr.merutilm.rff.settings.R3ASettings;
import kr.merutilm.rff.settings.DecimalizeIterationMethod;
import kr.merutilm.rff.settings.R3ASelectionMethod;
import kr.merutilm.rff.settings.ReuseReferenceMethod;
import kr.merutilm.rff.settings.R3ACompressionMethod;

enum ActionsFractal implements Actions {
    R3A("R3A", new HTMLStringBuilder().wrapln(Tag.BOLD, "Recursive Reference Rebasing Approximation.").appendln("Determine all of the period based on reference orbit, skips the period of iteration at once.").append("The render speed will be significantly faster when using it recursively.").toString(), null, 
    (master, name, description, accelerator) ->
    Actions.createItem(name, description, accelerator, () -> new RFFSettingsWindow(master.getWindow(), name, (_, panel) -> {
        R3ASettings r3a = getCalculationSettings(master).r3aSettings();

        Consumer<UnaryOperator<R3ASettings.Builder>> applier = e ->
                master.setSettings(e1 -> e1.setCalculationSettings(e2 -> e2.setR3ASettings(e::apply)));

        panel.createTextInput("Min Skip Reference", "Set minimum skipping reference iteration when creating a table.", r3a.minSkipReference(), Integer::parseInt, e ->
                applier.accept(f -> f.setMinSkipReference(e))
        );
        panel.createTextInput("Max Multiplier Between Level", new HTMLStringBuilder().wrapln(Tag.BOLD, "Set maximum multiplier between adjacent skipping levels.").appendln("This means the maximum multiplier of two adjacent periods for the new period that inserts between them,").append("So the multiplier between the two periods may in the worst case be the square of this.").toString(), r3a.maxMultiplierBetweenLevel(), Integer::parseInt, e ->
                applier.accept(f -> f.setMaxMultiplierBetweenLevel(e))
        );
        panel.createTextInput("Epsilon Power", new HTMLStringBuilder().wrapln(Tag.BOLD, "Set Epsilon power of ten.").appendln("Useful for glitch reduction. if this value is small,").appendln("The fractal will be rendered glitch-less but slow,").append("and is large, It will be fast, but maybe shown visible glitches.").toString(), r3a.epsilonPower(), Double::parseDouble, e ->
                applier.accept(f -> f.setEpsilonPower(e))
        );
        panel.createSelectInput("Selection Method", "Set the selection method of R3A.", r3a.r3aSelectionMethod(), R3ASelectionMethod.values(), e ->
                applier.accept(f -> f.setR3ASelectionMethod(e)), false
        );
        panel.createSelectInput("Compression Method", "Set the compession method of R3A.", r3a.r3aCompressionMethod(), R3ACompressionMethod.values(), e ->
                applier.accept(f -> f.setR3ACompressionMethod(e)), false
        );
    }))),

    ITERATIONS("Iteration", "Open the iteration settings. You can set the Max Iteration, Auto Iteration, and etc. here.", null, 
    (master, name, description, accelerator) ->
    Actions.createItem(name, description, accelerator, () -> new RFFSettingsWindow(master.getWindow(), name, (_, panel) -> {
        CalculationSettings calc = getCalculationSettings(master);

        Consumer<UnaryOperator<CalculationSettings.Builder>> applier = e ->
                master.setSettings(e1 -> e1.setCalculationSettings(e::apply));

        panel.createTextInput("Max Iteration", "Set maximum iteration. It is disabled when Auto iteration is enabled.", calc.maxIteration(), Long::parseLong, e ->
                applier.accept(f -> f.setMaxIteration(e)));

        panel.createTextInput("Bailout", "Set the bailout radius.", calc.bailout(), Double::parseDouble, e ->
                applier.accept(f -> f.setBailout(e))
        );
        panel.createSelectInput("Decimalize Iteration Method", "Iteration decimalization method", calc.decimalizeIterationMethod(), DecimalizeIterationMethod.values(), e ->
                applier.accept(f -> f.setDecimalizeIterationMethod(e)), true
        );
    }))),
    AUTOMATIC_ITERATIONS("Automatic Iterations", "Set max iteration automatic.", null, 
    (master, name, description, accelerator) -> 
    Actions.createCheckBoxItem(name, description, accelerator, getCalculationSettings(master).autoIteration(), b -> 
        master.setSettings(e1 -> e1.setCalculationSettings(e2 -> e2.setAutoIteration(b)))
    )),
    ABSOLUTE_ITERATION_MODE("Absolute Iteration Mode", new HTMLStringBuilder().wrapln(Tag.BOLD, "Absolute Iteration Mode").appendln("Define the iteration as while-loop count instead of the perturbation.").toString(), null, 
    (master, name, description, accelerator) -> 
    Actions.createCheckBoxItem(name, description, accelerator, getCalculationSettings(master).absoluteIterationMode(), b -> 
        master.setSettings(e1 -> e1.setCalculationSettings(e2 -> e2.setAbsoluteIterationMode(b)))
    )),
    REFERENCE("Reference", "Open the reference settings. You can set the Location, Zoom, and etc. here.", null, 
    (master, name, description, accelerator) ->
    Actions.createItem(name, description, accelerator, () -> new RFFSettingsWindow(master.getWindow(), name, (window, panel) -> {
        CalculationSettings calc = getCalculationSettings(master);

        Consumer<UnaryOperator<CalculationSettings.Builder>> applier = e ->
                master.setSettings(e1 -> e1.setCalculationSettings(e::apply));
        AtomicReference<String> realStr = new AtomicReference<>(calc.center().re().toString());
        AtomicReference<String> imagStr = new AtomicReference<>(calc.center().im().toString());
        AtomicReference<Double> zoomStr = new AtomicReference<>(calc.logZoom());


        panel.createTextInput("Center:Re", "Real part of center, The changes will be applied when this window is closed.", calc.center().re().toString(), s -> s, realStr::set);
        panel.createTextInput("Center:Im", "Imaginary part of center, The changes will be applied when this window is closed.", calc.center().im().toString(), s -> s, imagStr::set);
        panel.createTextInput("Log Zoom", "The logarithm by 10 of zoom, The changes will be applied when this window is closed.", calc.logZoom(), Double::parseDouble, zoomStr::set);
        panel.createSelectInput("Reuse Reference", "Set the method of reference reusing.", calc.reuseReference(), ReuseReferenceMethod.values(), e ->
                applier.accept(f -> f.setReuseReference(e)), false
        );
        panel.createTextInput("Reference Compress Criteria", new HTMLStringBuilder().wrapln(Tag.BOLD, "Reference Compress Criteria").appendln("When compressing references, sets the minimum amount of references to compress at one time.").appendln("Reference compression slows down the calculation but frees up memory space.").append("To disable, write -1.").toString(), calc.referenceCompressionSettings().compressCriteria(), Integer::parseInt, e ->
                applier.accept(f -> f.setReferenceCompressionSettings(g -> g.setCompressCriteria(e)))
        );
        panel.createTextInput("Reference Compress Threshold", new HTMLStringBuilder().wrapln(Tag.BOLD, "Reference Compress Threshold").appendln("When compressing references, sets the negative exponents of ten of minimum error to be considered equal.").appendln("Reference compression slows down the calculation but frees up memory space.").append("To disable, write -1.").toString(), calc.referenceCompressionSettings().compressionThresholdPower(), Integer::parseInt, e ->
        applier.accept(f -> f.setReferenceCompressionSettings(g -> g.setCompressionThresholdPower(e)))
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

    })));


    private final String name;
    private final String description;
    private final KeyStroke accelerator;
    private final Initializer initializer;

    @Override
    public KeyStroke keyStroke() {
        return accelerator;
    }

    public String description() {
        return description;
    }

    public Initializer initializer() {
        return initializer;
    }

    ActionsFractal(String name, String description, KeyStroke accelerator, Initializer initializer) {
        this.name = name;
        this.description = description;
        this.accelerator = accelerator;
        this.initializer = initializer;
    }


    @Override
    public String toString() {
        return name;
    }

    private static CalculationSettings getCalculationSettings(RFF master) {
        return master.getSettings().calculationSettings();
    }


}
