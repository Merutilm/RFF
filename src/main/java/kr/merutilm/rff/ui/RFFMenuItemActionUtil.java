package kr.merutilm.rff.ui;

import java.awt.Dimension;

import javax.swing.JMenuItem;

class RFFMenuItemActionUtil {
    
    private RFFMenuItemActionUtil(){

    }

    public static void init(JMenuItem item, String description){
        if(description != null && !description.isBlank()){
            item.setToolTipText(description);
        }
        item.setFont(MUIConstants.DEFAULT_FONT);
    }


    public static Dimension getMinimumWidth(JMenuItem item){
        Dimension d = item.getPreferredSize();
        d.width = Math.max(MUIConstants.MENU_ITEM_MIN_WIDTH, d.width);
        return d;
    }

}
