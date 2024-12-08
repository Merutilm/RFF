package kr.merutilm.fractal.ui;

import java.util.function.BiConsumer;
import java.util.function.DoubleConsumer;

import javax.annotation.Nullable;
import javax.swing.JOptionPane;

import kr.merutilm.base.exception.IllegalRenderStateException;
import kr.merutilm.base.util.TaskManager;
import kr.merutilm.fractal.formula.MandelbrotPerturbator;
import kr.merutilm.fractal.locater.Locator;
import kr.merutilm.fractal.locater.MandelbrotLocator;
import kr.merutilm.fractal.struct.LWBigComplex;
import kr.merutilm.fractal.theme.BasicTheme;
import kr.merutilm.fractal.util.LabelTextUtils;

enum ActionsExplore implements Actions {
    RECOMPUTE("Recompute", (master, name) -> 
        Actions.getRenderer(master).recompute()
    ),
    REFRESH_COLOR("Refresh Color", (master, name) -> {
        RFFRenderer render = Actions.getRenderer(master);
        TaskManager.runTask(() -> {
            try {
                StatusPanel panel = master.getWindow().getStatusPanel();
                render.reloadAndPaint(render.getState().getId(), false);
                panel.setProcess("Done");
            } catch (IllegalRenderStateException | InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }),
    RESET("Reset", (master, name) -> {
        master.setSettings(e -> e.edit().setCalculationSettings(e1 -> e1.edit()
        .setCenter(BasicTheme.INIT_C)
        .setLogZoom(BasicTheme.INIT_LOG_ZOOM)
        .build()).build());
        Actions.getRenderer(master).recompute();
    }),
    CANCEL("Cencel Render", (master, name) -> 
        Actions.getRenderer(master).getState().createBreakpoint()
    ),
    FIND_CENTER("Find Center", (master, name) -> {
        RFFRenderer render = Actions.getRenderer(master);
        if (render.getCurrentPerturbator() == null) {
            return;
        }
        LWBigComplex c = MandelbrotLocator.findCenter((MandelbrotPerturbator) render.getCurrentPerturbator());

        if (c == null) {
            checkNullLocator(null);
        }else{
            master.setSettings(e -> e.edit().setCalculationSettings(e1 -> e1.edit()
                    .setCenter(c)
                    .build()).build());
            RECOMPUTE.accept(master);
        }
    }),
    LOCATE_MINIBROT("Locate Minibrot", (master, name) -> {
        RFFRenderer render = Actions.getRenderer(master);
        if (render.getCurrentPerturbator() == null) {
            return;
        }
        TaskManager.runTask(() -> {
            
            int period = render.getCurrentPerturbator().getReference().period();
            MandelbrotLocator locator = MandelbrotLocator.locateMinibrot(render.getState(), render.getState().getId(), (MandelbrotPerturbator) render.getCurrentPerturbator(),
                    getActionWhileFindingMinibrotCenter(master, period),
                    getActionWhileFindingMinibrotZoom(master)
                    );

            if (checkNullLocator(locator)) {
                master.setSettings(e -> e.edit().setCalculationSettings(e1 -> e1.edit()
                        .setCenter(locator.center())
                        .setLogZoom(locator.logZoom())
                        .build()).build());
                RECOMPUTE.accept(master);
            }
        });
    })
    ;

    private final String name;
    private final BiConsumer<RFF, String> action;

    private ActionsExplore(String name, BiConsumer<RFF, String> generator) {
        this.name = name;
        this.action = generator;
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
                        + LabelTextUtils
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
