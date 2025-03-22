package kr.merutilm.rff.ui;

import java.awt.Dimension;

import javax.swing.JCheckBoxMenuItem;

class RFFCheckBoxMenuItem extends JCheckBoxMenuItem{

    public RFFCheckBoxMenuItem(String name, String description, boolean initValue){
        super(name, initValue);
        RFFMenuItemActionUtil.init(this, description);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = Math.max(MUIConstants.MENU_ITEM_MIN_WIDTH, d.width);
        return d;
    }
}
