package kr.merutilm.rff.ui;

import kr.merutilm.rff.preset.Presets;

import javax.swing.JMenu;
import javax.swing.JMenuItem;



public class ActionsPreset {

    private ActionsPreset(){

    }
    public static JMenu createCalcMenu(RFF master){
        JMenu menu = Actions.createJMenu(Presets.Calculations.class.getSimpleName());
        for(Presets.Calculations shader : Presets.Calculations.values()){
            JMenuItem item = Actions.createItem(shader.toString(), "", null, () -> master.setPreset(shader.preset()));
            Actions.addItem(menu, item);
        }
        return menu;
    }
    public static JMenu createLocationMenu(RFF master){
        JMenu menu = Actions.createJMenu(Presets.Locations.class.getSimpleName());
        for(Presets.Locations shader : Presets.Locations.values()){
            JMenuItem item = Actions.createItem(shader.toString(), "", null, () -> {
                master.setPreset(shader.preset());
                master.getWindow().getRenderer().recompute();
            });
            Actions.addItem(menu, item);
        }
        return menu;
    }
    public static JMenu createRenderMenu(RFF master){
        JMenu menu = Actions.createJMenu(Presets.Renders.class.getSimpleName());
        for(Presets.Renders shader : Presets.Renders.values()){
            JMenuItem item = Actions.createItem(shader.toString(), "", null, () -> {
                master.setPreset(shader.preset());
                master.getWindow().getRenderer().recompute();
            });
            Actions.addItem(menu, item);
        }
        return menu;
    }

    public static JMenu createShaderMenu(RFF master){
        JMenu menu = Actions.createJMenu(Presets.Shaders.class.getSimpleName());
        for(Presets.Shaders shader : Presets.Shaders.values()){
            JMenuItem item = Actions.createItem(shader.toString(), "", null, () -> {
                master.setPreset(shader.preset());
                master.getWindow().getRenderer().refreshColor();
            });
            Actions.addItem(menu, item);
        }
        return menu;
    }

    
    


}
