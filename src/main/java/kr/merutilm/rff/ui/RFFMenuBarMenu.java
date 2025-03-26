package kr.merutilm.rff.ui;

import javax.swing.JMenu;


class RFFMenuBarMenu extends JMenu{
    
    public RFFMenuBarMenu(String name){
        this(name, null);
    }

    public RFFMenuBarMenu(String name, String description){
        super(name);
        RFFMenuItemActionUtil.init(this, description);
    }
    
}
