package kr.merutilm.rff.ui;

import javax.swing.JMenu;
import java.awt.Dimension;


class RFFMenu extends JMenu{
    
    public RFFMenu(String name){
        this(name, null);
    }

    public RFFMenu(String name, String description){
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
