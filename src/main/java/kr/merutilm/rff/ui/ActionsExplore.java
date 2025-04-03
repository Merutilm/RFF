package kr.merutilm.rff.ui;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.function.BiConsumer;
import java.util.function.DoubleConsumer;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import kr.merutilm.rff.formula.MandelbrotPerturbator;
import kr.merutilm.rff.locater.MandelbrotLocator;
import kr.merutilm.rff.parallel.IllegalParallelRenderStateException;
import kr.merutilm.rff.preset.location.Location;
import kr.merutilm.rff.preset.Presets;
import kr.merutilm.rff.precision.LWBigComplex;
import kr.merutilm.rff.util.TextFormatter;
import kr.merutilm.rff.settings.Settings;

enum ActionsExplore implements ItemActions {
    RECOMPUTE("Recompute", "Recompute using current Location.", KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_DOWN_MASK), 
    (master, name, description, accelerator) ->
    ItemActions.createItem(name, description, accelerator, () -> ItemActions.getRenderer(master).requestRecompute())
    ),
    REFRESH_COLOR("Refresh Color", "Refresh color using current Settings.", KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.ALT_DOWN_MASK), 
    (master, name, description, accelerator) ->
    ItemActions.createItem(name, description, accelerator, () -> master.getWindow().getRenderer().requestColor())),
    RESET("Reset", "Reset to Initial Location. It contains \"Recompute\" operation.", KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.ALT_DOWN_MASK), 
    (master, name, description, accelerator) ->
    ItemActions.createItem(name, description, accelerator, () -> {
        RFFRenderPanel render = ItemActions.getRenderer(master);
        try {
            render.getState().cancel();
            Location def = Presets.Locations.DEFAULT.preset();
            master.setPreset(def);
            ItemActions.getRenderer(master).requestRecompute();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    })),
    CANCEL("Cancel Render", "Cancels the render. If you want to continue, Use \"Recompute\" operation.", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), 
    (master, name, description, accelerator) ->
    ItemActions.createItem(name, description, accelerator, () -> {
                try {
            ItemActions.getRenderer(master).getState().cancel();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    })),
    FIND_CENTER("Find Center", "Find the Minibrot center using current period.", KeyStroke.getKeyStroke(KeyEvent.VK_9, InputEvent.CTRL_DOWN_MASK), 
    (master, name, description, accelerator) ->
    ItemActions.createItem(name, description, accelerator, () -> {
        RFFRenderPanel render = ItemActions.getRenderer(master);
        if (render.getCurrentPerturbator() == null) {
            return;
        }

        LWBigComplex c = MandelbrotLocator.findCenter((MandelbrotPerturbator) render.getCurrentPerturbator());

        if (c == null) {
            sendCenterNotFoundMessage();
        } else {
            Settings settings = master.getSettings().edit().setCalculationSettings(e1 -> e1.setCenter(c)).build();
            master.setSettings(settings);
            render.requestRecompute();
        }
    })),
    LOCATE_MINIBROT("Locate Minibrot", "Locate the Minibrot using current period.", KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK), 
    (master, name, description, accelerator) ->
    ItemActions.createItem(name, description, accelerator, () ->  {
        RFFRenderPanel render = ItemActions.getRenderer(master);
        RFFStatusPanel panel = master.getWindow().getStatusPanel();
        Settings settings = master.getSettings();
        double logZoom = settings.calculationSettings().logZoom();

        if (render.getCurrentPerturbator() == null) {
            return;
        }
        try{


            render.getState().createThread(id -> {
                try{
                    
                    int period = render.getCurrentPerturbator().getReference().longestPeriod();
                    MandelbrotLocator locator = MandelbrotLocator.locateMinibrot(render.getState(),
                                    id, (MandelbrotPerturbator) render.getCurrentPerturbator(),
                            getActionWhileFindingMinibrotCenter(panel, logZoom, period),
                            getActionWhileCreatingTable(panel, logZoom),
                            getActionWhileFindingMinibrotZoom(panel)
                            );
                    
                    Settings modifiedSettings = settings.edit().setCalculationSettings(e1 -> e1
                    .setCenter(locator.center())
                    .setLogZoom(locator.logZoom())).build();
                    master.setSettings(modifiedSettings);
                    render.requestRecompute();

                }catch (IllegalParallelRenderStateException e) {
                    sendCenterNotFoundMessage();
                    RFFLoggers.logCancelledMessage(name, id);
                }
            });
        }catch(InterruptedException e){
            Thread.currentThread().interrupt();
        }
        

    }))
    ;

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

    ActionsExplore(String name, String description, KeyStroke accelerator, Initializer initializer) {
        this.name = name;
        this.description = description;
        this.accelerator = accelerator;
        this.initializer = initializer;
    }

    @Override
    public String toString() {
        return name;
    }

    private static void sendCenterNotFoundMessage() {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Cannot find center. Zoom in a little and try again.", "Error", JOptionPane.ERROR_MESSAGE));
    }

    public static BiConsumer<Integer, Double> getActionWhileCreatingTable(RFFStatusPanel panel, double logZoom){
        int interval = periodPanelRefreshInterval(logZoom);
        return (p, i) -> {

            if (p % interval == 0) {
                panel.setProcess("Creating Table... "
                                 + TextFormatter.processText(i));
            }
        };
    }

    public static BiConsumer<Integer, Integer> getActionWhileFindingMinibrotCenter(RFFStatusPanel panel, double logZoom, int period){
        int interval = periodPanelRefreshInterval(logZoom);
        return (p, i) -> {
                            
            if (p % interval == 0) {
                panel.setProcess("Locating Center... "
                        + TextFormatter
                                .processText((double) p / period)
                        + " [" + i + "]");
            }
        };
    }



    public static DoubleConsumer getActionWhileFindingMinibrotZoom(RFFStatusPanel panel){
        return d -> SwingUtilities.invokeLater(() -> panel.setProcess("Finding Zoom... 10^-" + String.format("%.2f", d)));
    }

    public static int periodPanelRefreshInterval(double logZoom){
        return (int) (1000000 / logZoom);
    }

   
}
