package kr.merutilm.rff.ui;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.function.BiConsumer;
import java.util.function.DoubleConsumer;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import kr.merutilm.rff.shader.IllegalRenderStateException;
import kr.merutilm.rff.util.TaskManager;
import kr.merutilm.rff.formula.MandelbrotPerturbator;
import kr.merutilm.rff.locater.MandelbrotLocator;
import kr.merutilm.rff.struct.LWBigComplex;
import kr.merutilm.rff.theme.BasicTheme;
import kr.merutilm.rff.util.TextFormatter;

enum ActionsExplore implements Actions {
    RECOMPUTE("Recompute", "Recompute using current Location.", (master, _) ->
        Actions.getRenderer(master).recompute(), 
        KeyStroke.getKeyStroke(KeyEvent.VK_C, 0)),
    REFRESH_COLOR("Refresh Color", "Refresh color using current Settings.", (master, _) -> {
        RFFRenderPanel render = Actions.getRenderer(master);
        try {
            render.getState().createThread(id -> {
                try {
                    RFFStatusPanel panel = master.getWindow().getStatusPanel();
                    render.reloadAndPaint(id, false);
                    panel.setProcess("Done");
                } catch (IllegalRenderStateException | InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_R, 0)),
    RESET("Reset", "Reset to Initial Location. It contains \"Recompute\" operation.", (master, _) -> {
        RFFRenderPanel render = Actions.getRenderer(master);
        try {
            render.getState().cancel();
            master.setSettings(e -> e.edit().setCalculationSettings(e1 -> e1.edit()
            .setCenter(BasicTheme.INIT_C)
            .setLogZoom(BasicTheme.INIT_LOG_ZOOM)
            .build()).build());
            Actions.getRenderer(master).recompute();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_N, 0)),
    CANCEL("Cancel Render", "Cancels the render. If you want to continue, Use \"Recompute\" operation.", (master, _) -> {
                try {
            Actions.getRenderer(master).getState().cancel();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)),
    FIND_CENTER("Find Center", "Find the Minibrot center using current period.", (master, name) -> {
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
                        master.setSettings(e -> e.edit().setCalculationSettings(e1 -> e1.edit()
                                .setCenter(c)
                                .build()).build());
                        render.compute(id);
                    }
                }catch (IllegalRenderStateException e) {
                    sendCenterNotFoundMessage();
                    RFFLoggers.logCancelledMessage(name, id);
                }
                
                
            });
                
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_9, InputEvent.CTRL_DOWN_MASK)),
    LOCATE_MINIBROT("Locate Minibrot", "Locate the Minibrot using current period.", (master, name) -> {
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

                    master.setSettings(e -> e.edit().setCalculationSettings(e1 -> e1.edit()
                            .setCenter(locator.center())
                            .setLogZoom(locator.logZoom())
                            .build()).build());
                    render.compute(id);

                }catch (IllegalRenderStateException e) {
                    sendCenterNotFoundMessage();
                    RFFLoggers.logCancelledMessage(name, id);
                }
            });
        }catch(InterruptedException e){
            Thread.currentThread().interrupt();
        }
        

    }, KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK))
    ;

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

    ActionsExplore(String name, String description, BiConsumer<RFF, String> generator, KeyStroke keyStroke) {
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

    private static void sendCenterNotFoundMessage() {
        TaskManager.runTask(() -> JOptionPane.showMessageDialog(null, "Cannot find center. Zoom in a little and try again.", "Error", JOptionPane.ERROR_MESSAGE));
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
        return d -> TaskManager.runTask(() -> panel.setProcess("Finding Zoom... 10^-" + String.format("%.2f", d)));
    }

    public static int periodPanelRefreshInterval(RFF master){
        return (int) (1000000 / master.getSettings().calculationSettings().logZoom());
    }

   
}
