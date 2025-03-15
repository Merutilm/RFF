package kr.merutilm.rff.ui;

import java.awt.event.ItemEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import kr.merutilm.rff.functions.BooleanConsumer;
import kr.merutilm.rff.selectable.Selectable;

interface Actions extends Selectable{
    
    static RFFRenderPanel getRenderer(RFF master) {
        return master.getWindow().getRenderer();
    }
    @FunctionalInterface
    static interface Initializer{
        JMenuItem init(RFF master, String name, String description, KeyStroke accelerator);
    }

    static JCheckBoxMenuItem createCheckBoxItem(String name, String description, KeyStroke accelerator, boolean initValue, BooleanConsumer action){
        JCheckBoxMenuItem item = new JCheckBoxMenuItem(name, initValue);
        item.setToolTipText(description);
        item.setAccelerator(accelerator);
        item.setFont(MUIConstants.DEFAULT_FONT);
        item.addItemListener(e -> action.accept(e.getStateChange() == ItemEvent.SELECTED));
        return item;
    }

    static JMenuItem createItem(String name, String description, KeyStroke accelerator, Runnable action){
        JMenuItem item = new JMenuItem(name);
        item.setToolTipText(description);
        item.setAccelerator(accelerator);
        item.setFont(MUIConstants.DEFAULT_FONT);
        item.addActionListener(_ -> action.run());
        return item;
    }

    static JMenu createJMenu(String name, String description){
        JMenu menu = new JMenu(name);
        menu.setToolTipText(description);
        menu.setFont(MUIConstants.DEFAULT_FONT);
        return menu;
    }
    static JMenu createJMenu(String name){
        return createJMenu(name, null);
    }

    static void addItem(JMenu menu, JMenuItem item){
        menu.add(item);
    }

    String description();

    KeyStroke keyStroke();

    Initializer initializer();
}
