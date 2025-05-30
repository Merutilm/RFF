package kr.merutilm.rff.ui;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.awt.GridLayout;

import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import kr.merutilm.rff.ui.HTMLStringBuilder.Tag;
import kr.merutilm.rff.util.AdvancedMath;

final class RFFStatusPanel extends RFFPanel {
    
    private final MUILabelPanel iteration;
    private final MUILabelPanel zoom;
    private final MUILabelPanel period;
    private final MUILabelPanel time;
    private final MUILabelPanel progress;
    private static final String SPACE = "  ";
    private long t = 0;
    public static final NumberFormat THOUSAND_FORMATTER = NumberFormat.getNumberInstance(Locale.US);

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
        panel.getNameLabel().setText(SPACE);
        panel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        add(panel);
    }
    public void setIterationText(long it) {
        String text = (it == 0 ? "?" : NumberFormat.getNumberInstance(Locale.US).format(it));
        iteration.getNameLabel().setText(SPACE + "Iterations : " + text);
    }

    public void setZoomText(double logZoom) {
        String text = AdvancedMath.fixDouble(Math.pow(10, logZoom % 1)) + "E-" + (int) logZoom;
        zoom.getNameLabel().setText(SPACE + "Zoom : " + text);
    }

    public void setPeriodText(long period, int length, int mpaLength) {
        String tPeriod = THOUSAND_FORMATTER.format(period);
        String tLength = THOUSAND_FORMATTER.format(length);
        String tR3ALength = THOUSAND_FORMATTER.format(mpaLength);
        this.period.getNameLabel().setText(SPACE + "Period : " + tPeriod);
        this.period.setToolTipText(new HTMLStringBuilder().wrap(Tag.BOLD,"Reference Length : ").appendln(tLength).wrap(Tag.BOLD, "MPA Table Length : ").append(tR3ALength).toString());
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
