package kr.merutilm.fractal.ui;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import javax.swing.SwingUtilities;

import kr.merutilm.base.util.AdvancedMath;
import kr.merutilm.customswing.CSPanel;

class StatusPanel extends CSPanel{
    
    private final CSPanel iteration;
    private final CSPanel zoom;
    private final CSPanel period;
    private final CSPanel progress;
    private final CSPanel time;
    private long t = 0;

    public StatusPanel(RFFWindow window){
        super(window);

        iteration = new CSPanel(window);
        zoom = new CSPanel(window);
        period = new CSPanel(window);
        progress = new CSPanel(window);
        time = new CSPanel(window);    

        add(iteration);
        add(zoom);
        add(period);
        add(progress);
        add(time);
    }

    public void setIterationText(long it) {
        String text = (it == -1 ? "?" : String.valueOf(it));
        iteration.getNameLabel().setText(text);
    }

    public void setZoomText(double logZoom) {
        String text = AdvancedMath.fixDouble(Math.pow(10, logZoom % 1)) + "E-" + (int) logZoom;
        zoom.getNameLabel().setText(text);
    }

    public void setPeriodText(int period) {
        String text = String.valueOf(period);
        this.period.getNameLabel().setText(text);
    }

    public void initTime() {
        t = System.currentTimeMillis();
    }

    public void refreshTime() {
        long ms = System.currentTimeMillis() - t;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        time.getNameLabel().setText(sdf.format(ms));
    }

    public void setProcess(String v) {
        SwingUtilities.invokeLater(() -> progress.getNameLabel().setText(v));
    }


    @Override
    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
        iteration.setBounds(0, 0, w / 5, h);
        zoom.setBounds(w / 5, 0, w / 5, h);
        period.setBounds(2 * w / 5, 0, w / 5, h);
        progress.setBounds(3 * w / 5, 0, w / 5, h);
        time.setBounds(4 * w / 5, 0, w - w / 5, h);
    }
}
