package kr.merutilm.rff.ui;

import java.awt.event.ItemEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import kr.merutilm.rff.functions.BooleanConsumer;
import kr.merutilm.rff.selectable.Selectable;

interface ItemActions extends Selectable{
    
    static RFFRenderPanel getRenderer(RFF master) {
        return master.getWindow().getRenderer();
    }
    @FunctionalInterface
    static interface Initializer{
        JMenuItem init(RFF master, String name, String description, KeyStroke accelerator);
    }

    static JCheckBoxMenuItem createCheckBoxItem(String name, String description, KeyStroke accelerator, boolean initValue, BooleanConsumer action){
        JCheckBoxMenuItem item = new RFFCheckBoxMenuItem(name, description, initValue);
        item.setAccelerator(accelerator);
        item.addItemListener(e -> action.accept(e.getStateChange() == ItemEvent.SELECTED));
        return item;
    }

    static JMenuItem createItem(String name, String description, KeyStroke accelerator, Runnable action){
        JMenuItem item = new RFFMenuItem(name, description);
        item.setAccelerator(accelerator);
        item.addActionListener(_ -> action.run());
        return item;
    }


    static void addItem(JMenu menu, JMenuItem item){
        menu.add(item);
    }

    String description();

    KeyStroke keyStroke();

    Initializer initializer();
}
