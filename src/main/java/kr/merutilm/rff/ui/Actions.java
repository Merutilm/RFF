package kr.merutilm.rff.ui;

import java.util.function.Consumer;

import javax.swing.KeyStroke;

import kr.merutilm.rff.selectable.Selectable;

interface Actions extends Consumer<RFF>, Selectable{
    
    static RFFRenderPanel getRenderer(RFF master) {
        return master.getWindow().getRenderer();
    }

    String description();

    KeyStroke keyStroke();
}
