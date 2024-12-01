package kr.merutilm.fractal.ui;

import static kr.merutilm.fractal.ui.StatusWindow.BAR_HEIGHT;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import javax.swing.SwingUtilities;

import kr.merutilm.base.selectable.BooleanValue;
import kr.merutilm.base.struct.HexColor;
import kr.merutilm.base.util.AdvancedMath;
import kr.merutilm.customswing.*;
import kr.merutilm.fractal.settings.BLASettings;
import kr.merutilm.fractal.settings.ReuseReferenceSettings;

final class CalcSettingsPanel extends CSPanel {

    private final transient RFF master;
    private final CSValueLabelPanel iterationPanel;
    private final CSValueTextInputPanel<Long> maxIterationPanel;
    private final CSValueLabelPanel progressPanel;
    private final CSValueLabelPanel timePanel;
    private final CSValueLabelPanel zoomPanel;
    private final CSValueLabelPanel periodPanel;

    private long t;

    public CalcSettingsPanel(RFF master, CSFrame window, Rectangle r) {
        super(window);
        this.master = master;
        setBorder(null);
        setBackground(new HexColor(70, 70, 70, 255).toAWT());
        setBounds(r);

        iterationPanel = new CSValueLabelPanel(window, "Iteration", "");
        iterationPanel.setBounds(new Rectangle(0, 0, r.width / 2, BAR_HEIGHT));

        maxIterationPanel = new CSValueTextInputPanel<>(window, new Rectangle(), "Max Iteration", master.getSettings().calculationSettings().maxIteration(), Long::parseLong, false) {
            @Override
            public void enterFunction(Long value) {
                master.setSettings(e1 -> e1.edit().setCalculationSettings(e2 -> e2.edit().setMaxIteration(value).build()).build());
                master.getFractalRender().getPainter().recompute();
            }
        };
        maxIterationPanel.setBounds(new Rectangle(r.width / 2, 0, r.width / 2, BAR_HEIGHT));

        zoomPanel = new CSValueLabelPanel(window, "Zoom", "");
        zoomPanel.setBounds(new Rectangle(0, BAR_HEIGHT, r.width / 3, BAR_HEIGHT));

        CSValueTextInputPanel<Double> bailoutPanel = createBailoutPanel(master, window, r);


        periodPanel = new CSValueLabelPanel(window, "Period", "");
        periodPanel.setBounds(new Rectangle(2 * r.width / 3, BAR_HEIGHT, r.width / 3, BAR_HEIGHT));

        CSValueSelectionInputPanel<BooleanValue> autoIterPanel = new CSValueSelectionInputPanel<>(window, this, new Rectangle(0, BAR_HEIGHT * 2, r.width / 2, BAR_HEIGHT), "Auto Iteration", BooleanValue.typeOf(master.getSettings().calculationSettings().autoIteration()), value -> {
            master.setSettings(e1 -> e1.edit().setCalculationSettings(e2 -> e2.edit().setAutoIteration(value.bool()).build()).build());
            master.getFractalRender().getPainter().recompute();
        }, false, BooleanValue.values());

        CSValueSelectionInputPanel<ReuseReferenceSettings> reusePanel = new CSValueSelectionInputPanel<>(window, this, new Rectangle(r.width / 2, BAR_HEIGHT * 2, r.width / 2, BAR_HEIGHT), "Reuse Reference", master.getSettings().calculationSettings().reuseReference(), value -> {
            master.setSettings(e1 -> e1.edit().setCalculationSettings(e2 -> e2.edit().setReuseReference(value).build()).build());
            master.getFractalRender().getPainter().recompute();
        }, false, ReuseReferenceSettings.values());

        progressPanel = new CSValueLabelPanel(window, "Progress", "");
        progressPanel.setBounds(new Rectangle(0, BAR_HEIGHT * 3, r.width / 2, BAR_HEIGHT));

        timePanel = new CSValueLabelPanel(window, "Time", "");
        timePanel.setBounds(new Rectangle(r.width / 2, BAR_HEIGHT * 3, r.width / 2, BAR_HEIGHT));



        BLASettings bla = master.getSettings().calculationSettings().blaSettings();
        Consumer<UnaryOperator<BLASettings.Builder>> applier = e -> {
            master.setSettings(e1 -> e1.edit().setCalculationSettings(e2 -> e2.edit().setBLASettings(e3 -> e.apply(e3.edit()).build()).build()).build());
            master.getFractalRender().getPainter().recompute();
        };

        CSPanel blaPanelP = new CSPanel(window);
        blaPanelP.setBounds(0, BAR_HEIGHT * 4, r.width, BAR_HEIGHT * 2);

        CSValueInputGroupPanel blaPanel = new CSValueInputGroupPanel(window, this, "BLA Settings", CSValueInputGroupPanel.InputType.VERTICAL, true);
        blaPanel.createTextInput("Epsilon power", null, bla.epsilonPower(), Double::parseDouble, e -> applier.accept(e1 -> e1.setEpsilonPower(e)));
        blaPanel.createTextInput("Minimum Level", null, bla.minLevel(), Integer::parseInt, e -> applier.accept(e1 -> e1.setMinLevel(e)));
        blaPanelP.add(blaPanel);

        setIterationText(-1);
        initTime();
        refreshTime();
        add(iterationPanel);
        add(maxIterationPanel);
        add(zoomPanel);
        add(bailoutPanel);
        add(periodPanel);
        add(autoIterPanel);
        add(reusePanel);
        add(progressPanel);
        add(timePanel);
        add(blaPanelP);
    }

    private static CSValueTextInputPanel<Double> createBailoutPanel(RFF master, CSFrame window, Rectangle r) {
        CSValueTextInputPanel<Double> bailoutPanel = new CSValueTextInputPanel<>(window, new Rectangle(), "Bailout", master.getSettings().calculationSettings().bailout(), Double::parseDouble, false) {
            @Override
            public void enterFunction(Double value) {
                master.setSettings(e1 -> e1.edit().setCalculationSettings(e2 -> e2.edit().setBailout(value).build()).build());
                master.getFractalRender().getPainter().recompute();
            }
        };
        bailoutPanel.setBounds(new Rectangle(r.width / 3, BAR_HEIGHT, r.width / 3, BAR_HEIGHT));
        return bailoutPanel;
    }

    public void refreshMaxIterationText() {
        String text = String.valueOf(master.getSettings().calculationSettings().maxIteration());
        maxIterationPanel.getTextPanel().setText(text);
    }

    public void setIterationText(long it) {
        String text = (it == -1 ? "?" : String.valueOf(it));
        iterationPanel.setText(text);
    }

    public void setZoomText(double logZoom) {
        String text = AdvancedMath.fixDouble(Math.pow(10, logZoom % 1)) + "E-" + (int) logZoom;
        zoomPanel.setText(text);
    }

    public void setPeriodText(int period) {
        String text = String.valueOf(period);
        periodPanel.setText(text);
    }

    public void initTime() {
        t = System.currentTimeMillis();
    }


    public void refreshTime() {
        long ms = System.currentTimeMillis() - t;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        timePanel.setText(sdf.format(ms));
    }

    public void setProcess(String v) {
        SwingUtilities.invokeLater(() -> progressPanel.setText(v));
    }

}
