package kr.merutilm.rff.ui;

import java.awt.Dimension;

import javax.swing.JMenuItem;

class RFFMenuItem extends JMenuItem {

    public RFFMenuItem(String name, String description){
        super(name);
        RFFMenuItemActionUtil.init(this, description);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = Math.max(MUIConstants.MENU_ITEM_MIN_WIDTH, d.width);
        return d;
    }
}
