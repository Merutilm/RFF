package kr.merutilm.fractal.ui;

import java.util.function.Consumer;

import javax.swing.KeyStroke;

import kr.merutilm.base.selectable.Selectable;

interface Actions extends Consumer<RFF>, Selectable{
    
    static RFFRenderer getRenderer(RFF master) {
        return master.getWindow().getRenderer();
    }

    KeyStroke keyStroke();
}
