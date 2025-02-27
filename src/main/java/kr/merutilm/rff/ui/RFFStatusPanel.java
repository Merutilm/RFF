package kr.merutilm.rff.ui;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import kr.merutilm.rff.util.AdvancedMath;

final class RFFStatusPanel extends JPanel {
    
    private final MUILabelPanel iteration;
    private final MUILabelPanel zoom;
    private final MUILabelPanel period;
    private final MUILabelPanel time;
    private final MUILabelPanel progress;
    private static final String SPACE = "  ";
    private long t = 0;

    public RFFStatusPanel() {

        setLayout(new GridLayout(1, 5));
        iteration = new MUILabelPanel();
        zoom = new MUILabelPanel();
        period = new MUILabelPanel();
        time = new MUILabelPanel();
        progress = new MUILabelPanel();

        initPanel(iteration);
        initPanel(zoom);
        initPanel(period);
        initPanel(time);
        initPanel(progress);
    }

    private void initPanel(MUILabelPanel panel) {
        panel.getNameLabel().setHorizontalAlignment(SwingConstants.LEFT);
        panel.getNameLabel().setFont(MUIConstants.DEFAULT_FONT);
        panel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        add(panel);
    }
    public void setIterationText(long it) {
        String text = (it == -1 ? "?" : NumberFormat.getNumberInstance(Locale.US).format(it));
        iteration.getNameLabel().setText(SPACE + "Iterations : " + text);
    }

    public void setZoomText(double logZoom) {
        String text = AdvancedMath.fixDouble(Math.pow(10, logZoom % 1)) + "E-" + (int) logZoom;
        zoom.getNameLabel().setText(SPACE + "Zoom : " + text);
    }

    public void setReferenceText(int period, int length) {
        String tPeriod = NumberFormat.getNumberInstance(Locale.US).format(period);
        String tLength = NumberFormat.getNumberInstance(Locale.US).format(length);
        this.period.getNameLabel().setText(SPACE + "Period : " + tPeriod + " (" + tLength + ")");
    }

    public void initTime() {
        t = System.currentTimeMillis();
    }

    public void refreshTime() {
        long ms = System.currentTimeMillis() - t;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        time.getNameLabel().setText(SPACE + "Time : " + sdf.format(ms));
    }

    public void setProcess(String v) {
        SwingUtilities.invokeLater(() -> progress.getNameLabel().setText(SPACE + v));
    }

}
