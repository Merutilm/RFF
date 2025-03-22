package kr.merutilm.rff.ui;

import kr.merutilm.rff.preset.Presets;

import javax.swing.JMenu;
import javax.swing.JMenuItem;



class ActionsPreset {

    private ActionsPreset(){

    }

    public static JMenu createMenus(RFF master, JMenu menu){
        
        menu.add(createCalcMenu(master));
        menu.add(createLocationMenu(master));
        menu.add(createRenderMenu(master));
        menu.add(createShaderMenu(master));
        return menu;
    }

    public static JMenu createCalcMenu(RFF master){
        JMenu menu = new RFFMenu(Presets.Calculations.class.getSimpleName());
        for(Presets.Calculations shader : Presets.Calculations.values()){
            JMenuItem item = ItemActions.createItem(shader.toString(), "", null, () -> master.setPreset(shader.preset()));
            ItemActions.addItem(menu, item);
        }
        return menu;
    }
    public static JMenu createLocationMenu(RFF master){
        JMenu menu = new RFFMenu(Presets.Locations.class.getSimpleName());
        for(Presets.Locations shader : Presets.Locations.values()){
            JMenuItem item = ItemActions.createItem(shader.toString(), "", null, () -> {
                master.setPreset(shader.preset());
                master.getWindow().getRenderer().recompute();
            });
            ItemActions.addItem(menu, item);
        }
        return menu;
    }
    public static JMenu createRenderMenu(RFF master){
        JMenu menu = new RFFMenu(Presets.Renders.class.getSimpleName());
        for(Presets.Renders shader : Presets.Renders.values()){
            JMenuItem item = ItemActions.createItem(shader.toString(), "", null, () -> {
                master.setPreset(shader.preset());
                master.getWindow().getRenderer().recompute();
            });
            ItemActions.addItem(menu, item);
        }
        return menu;
    }

    public static JMenu createShaderMenu(RFF master){
        JMenu menu = new RFFMenu(Presets.Shaders.class.getSimpleName(), null);
        for(Presets.Shaders shader : Presets.Shaders.values()){
            JMenuItem item = ItemActions.createItem(shader.toString(), "", null, () -> {
                master.setPreset(shader.preset());
                master.getWindow().getRenderer().refreshColor();
            });
            ItemActions.addItem(menu, item);
        }
        return menu;
    }

    
    


}
