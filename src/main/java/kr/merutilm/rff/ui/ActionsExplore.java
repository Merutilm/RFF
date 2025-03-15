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
import kr.merutilm.rff.struct.LWBigComplex;
import kr.merutilm.rff.util.TextFormatter;

enum ActionsExplore implements Actions {
    RECOMPUTE("Recompute", "Recompute using current Location.", KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_DOWN_MASK), 
    (master, name, description, accelerator) ->
    Actions.createItem(name, description, accelerator, () -> Actions.getRenderer(master).recompute())
    ),
    REFRESH_COLOR("Refresh Color", "Refresh color using current Settings.", KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.ALT_DOWN_MASK), 
    (master, name, description, accelerator) ->
    Actions.createItem(name, description, accelerator, () -> master.getWindow().getRenderer().refreshColor())),
    RESET("Reset", "Reset to Initial Location. It contains \"Recompute\" operation.", KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.ALT_DOWN_MASK), 
    (master, name, description, accelerator) ->
    Actions.createItem(name, description, accelerator, () -> {
        RFFRenderPanel render = Actions.getRenderer(master);
        try {
            render.getState().cancel();
            Location def = Presets.Locations.DEFAULT.preset();
            master.setSettings(e -> e.setCalculationSettings(e1 -> e1
            .setCenter(LWBigComplex.valueOf(def.real(), def.imag()))
            .setMaxIteration(def.maxIteration())
            .setLogZoom(def.logZoom())));
            Actions.getRenderer(master).recompute();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    })),
    CANCEL("Cancel Render", "Cancels the render. If you want to continue, Use \"Recompute\" operation.", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), 
    (master, name, description, accelerator) ->
    Actions.createItem(name, description, accelerator, () -> {
                try {
            Actions.getRenderer(master).getState().cancel();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    })),
    FIND_CENTER("Find Center", "Find the Minibrot center using current period.", KeyStroke.getKeyStroke(KeyEvent.VK_9, InputEvent.CTRL_DOWN_MASK), 
    (master, name, description, accelerator) ->
    Actions.createItem(name, description, accelerator, () -> {
        RFFRenderPanel render = Actions.getRenderer(master);
        if (render.getCurrentPerturbator() == null) {
            return;
        } 
        try {
            render.getState().createThread(id -> {
                
                try{
                    LWBigComplex c = MandelbrotLocator.findCenter((MandelbrotPerturbator) render.getCurrentPerturbator());
                
                    if (c == null) {
                        sendCenterNotFoundMessage();
                    } else {
                        master.setSettings(e -> e.setCalculationSettings(e1 -> e1
                                .setCenter(c)));
                        render.compute(id);
                    }
                }catch (IllegalParallelRenderStateException e) {
                    sendCenterNotFoundMessage();
                    RFFLoggers.logCancelledMessage(name, id);
                }
                
                
            });
                
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    })),
    LOCATE_MINIBROT("Locate Minibrot", "Locate the Minibrot using current period.", KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK), 
    (master, name, description, accelerator) ->
    Actions.createItem(name, description, accelerator, () ->  {
        RFFRenderPanel render = Actions.getRenderer(master);
        if (render.getCurrentPerturbator() == null) {
            return;
        }
        try{
            render.getState().createThread(id -> {
                try{
                    int period = render.getCurrentPerturbator().getReference().longestPeriod();
                    MandelbrotLocator locator = MandelbrotLocator.locateMinibrot(render.getState(),
                                    id, (MandelbrotPerturbator) render.getCurrentPerturbator(),
                            getActionWhileFindingMinibrotCenter(master, period),
                            getActionWhileCreatingTable(master),
                            getActionWhileFindingMinibrotZoom(master)
                            );

                    master.setSettings(e -> e.setCalculationSettings(e1 -> e1
                            .setCenter(locator.center())
                            .setLogZoom(locator.logZoom())));
                    render.compute(id);

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

    public static BiConsumer<Integer, Double> getActionWhileCreatingTable(RFF master){
        RFFStatusPanel panel = master.getWindow().getStatusPanel();
        int interval = periodPanelRefreshInterval(master);
        return (p, i) -> {

            if (p % interval == 0) {
                panel.setProcess("Creating Table... "
                                 + TextFormatter.processText(i));
            }
        };
    }

    public static BiConsumer<Integer, Integer> getActionWhileFindingMinibrotCenter(RFF master, int period){
        RFFStatusPanel panel = master.getWindow().getStatusPanel();
        int interval = periodPanelRefreshInterval(master);
        return (p, i) -> {
                            
            if (p % interval == 0) {
                panel.setProcess("Locating Center... "
                        + TextFormatter
                                .processText((double) p / period)
                        + " [" + i + "]");
            }
        };
    }



    public static DoubleConsumer getActionWhileFindingMinibrotZoom(RFF master){
        RFFStatusPanel panel = master.getWindow().getStatusPanel();
        return d -> SwingUtilities.invokeLater(() -> panel.setProcess("Finding Zoom... 10^-" + String.format("%.2f", d)));
    }

    public static int periodPanelRefreshInterval(RFF master){
        return (int) (1000000 / master.getSettings().calculationSettings().logZoom());
    }

   
}
