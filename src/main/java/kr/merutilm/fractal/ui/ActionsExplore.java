package kr.merutilm.fractal.ui;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.function.BiConsumer;
import java.util.function.DoubleConsumer;
import javax.annotation.Nullable;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import kr.merutilm.base.exception.IllegalRenderStateException;
import kr.merutilm.base.util.TaskManager;
import kr.merutilm.fractal.formula.MandelbrotPerturbator;
import kr.merutilm.fractal.locater.Locator;
import kr.merutilm.fractal.locater.MandelbrotLocator;
import kr.merutilm.fractal.struct.LWBigComplex;
import kr.merutilm.fractal.theme.BasicTheme;
import kr.merutilm.fractal.util.TextFormatter;

enum ActionsExplore implements Actions {
    RECOMPUTE("Recompute", (master, name) -> 
        Actions.getRenderer(master).recompute(), 
        KeyStroke.getKeyStroke(KeyEvent.VK_C, 0)),
    REFRESH_COLOR("Refresh Color", (master, name) -> {
        RFFRenderer render = Actions.getRenderer(master);
        try {
            render.getState().createThread(id -> {
                try {
                    StatusPanel panel = master.getWindow().getStatusPanel();
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
    RESET("Reset", (master, name) -> {
        RFFRenderer render = Actions.getRenderer(master);
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
    CANCEL("Cencel Render", (master, name) -> {
                try {
            Actions.getRenderer(master).getState().cancel();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)),
    FIND_CENTER("Find Center", (master, name) -> {
        RFFRenderer render = Actions.getRenderer(master);
        if (render.getCurrentPerturbator() == null) {
            return;
        } 
        try {
            render.getState().createThread(id -> {
                
                try{
                    LWBigComplex c = MandelbrotLocator.findCenter((MandelbrotPerturbator) render.getCurrentPerturbator());
                
                    if (c == null) {
                        checkNullLocator(null);
                    } else {
                        master.setSettings(e -> e.edit().setCalculationSettings(e1 -> e1.edit()
                                .setCenter(c)
                                .build()).build());
                        render.compute(id);
                    }
                }catch (IllegalRenderStateException e) {
                    RFFLoggers.logCancelledMessage(name, id);
                }
                
                
            });
                
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_9, InputEvent.CTRL_DOWN_MASK)),
    LOCATE_MINIBROT("Locate Minibrot", (master, name) -> {
        RFFRenderer render = Actions.getRenderer(master);
        if (render.getCurrentPerturbator() == null) {
            return;
        }
        try{
            render.getState().createThread(id -> {
                try{
                    int period = render.getCurrentPerturbator().getReference().period();
                    MandelbrotLocator locator = MandelbrotLocator.locateMinibrot(render.getState(),
                                    id, (MandelbrotPerturbator) render.getCurrentPerturbator(),
                            getActionWhileFindingMinibrotCenter(master, period),
                            getActionWhileFindingMinibrotZoom(master)
                            );
        
                    if (checkNullLocator(locator)) {
                        master.setSettings(e -> e.edit().setCalculationSettings(e1 -> e1.edit()
                                .setCenter(locator.center())
                                .setLogZoom(locator.logZoom())
                                .build()).build());
                        render.compute(id);
                    }
                }catch (IllegalRenderStateException e) {
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

    @Override
    public KeyStroke keyStroke() {
        return keyStroke;
    }

    private ActionsExplore(String name, BiConsumer<RFF, String> generator, KeyStroke keyStroke) {
        this.name = name;
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

    private static boolean checkNullLocator(@Nullable Locator locator) {
        if (locator == null) {
            JOptionPane.showMessageDialog(null, "Cannot find center. Zoom in a little and try again.", "Locate Minibrot", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    public static BiConsumer<Integer, Integer> getActionWhileFindingMinibrotCenter(RFF master, int period){
        StatusPanel panel = master.getWindow().getStatusPanel();
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
        StatusPanel panel = master.getWindow().getStatusPanel();
        return d -> TaskManager.runTask(() -> panel.setProcess("Finding Zoom... 10^-" + String.format("%.2f", d)));
    }

    public static int periodPanelRefreshInterval(RFF master){
        return (int) (1000000 / master.getSettings().calculationSettings().logZoom());
    }

   
}
