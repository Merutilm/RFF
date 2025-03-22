package kr.merutilm.rff.ui;

import java.util.function.Function;

import javax.swing.JMenu;

import kr.merutilm.rff.preset.Presets;

enum RFFSettingsMenu {
    FILE(master -> {
        RFFMenuTree.Builder menuBuilder = RFFMenuTree.Builder.init("File");
        for(ActionsFile a : ActionsFile.values()){
            menuBuilder.createFruit(a.initializer().init(master, a.toString(), a.description(), a.keyStroke()));
        }

        return (JMenu) menuBuilder.build().createUI();
    }),
    FRACTAL(master -> {
        RFFMenuTree.Builder menuBuilder = RFFMenuTree.Builder.init("Fractal");
        for(ActionsFractal a : ActionsFractal.values()){
            menuBuilder.createFruit(a.initializer().init(master, a.toString(), a.description(), a.keyStroke()));
        }
        
        return (JMenu) menuBuilder.build().createUI();
    }),
    IMAGE(master -> {
        RFFMenuTree.Builder menuBuilder = RFFMenuTree.Builder.init("Image");
        for(ActionsImage a : ActionsImage.values()){
            menuBuilder.createFruit(a.initializer().init(master, a.toString(), a.description(), a.keyStroke()));
        }
        
        return (JMenu) menuBuilder.build().createUI();
    }),
    SHADER(master -> {
        RFFMenuTree.Builder menuBuilder = RFFMenuTree.Builder.init("Shader");
        for(ActionsShader a : ActionsShader.values()){
            menuBuilder.createFruit(a.initializer().init(master, a.toString(), a.description(), a.keyStroke()));
        }
        
        return (JMenu) menuBuilder.build().createUI();
    }),
    PRESET(master -> {
        RFFMenuTree.Builder menuBuilder = RFFMenuTree.Builder.init("Preset");
        menuBuilder.createBranch("Calculation", e -> {
            for(Presets.Calculations a : Presets.Calculations.values()){
                e.createFruit(ItemActions.createItem(a.toString(), "", null, () -> master.setPreset(a.preset())));
            }
        });
        menuBuilder.createBranch("Location", e -> {
            for(Presets.Locations a : Presets.Locations.values()){
                e.createFruit(ItemActions.createItem(a.toString(), "", null, () -> {
                    master.setPreset(a.preset());
                    master.getWindow().getRenderer().recompute();
                }));
            }
        });
        menuBuilder.createBranch("Render", e -> {
            for(Presets.Renders a : Presets.Renders.values()){
                e.createFruit(ItemActions.createItem(a.toString(), "", null, () -> {
                    master.setPreset(a.preset());
                    master.getWindow().getRenderer().recompute();
                }));
            }
        });
        menuBuilder.createBranch("Shader", e -> {
            for(Presets.Shaders a : Presets.Shaders.values()){
                e.createFruit(ItemActions.createItem(a.toString(), "", null, () -> {
                    master.setPreset(a.preset());
                    master.getWindow().getRenderer().refreshColorUnsafe();
                }));
            }
        });

        return (JMenu) menuBuilder.build().createUI();
    }),
    VIDEO(master -> {
        RFFMenuTree.Builder menuBuilder = RFFMenuTree.Builder.init("Video");
        for(ActionsVideo a : ActionsVideo.values()){
            menuBuilder.createFruit(a.initializer().init(master, a.toString(), a.description(), a.keyStroke()));
        }
        
        return (JMenu) menuBuilder.build().createUI();
    }),
    EXPLORE(master -> {
        RFFMenuTree.Builder menuBuilder = RFFMenuTree.Builder.init("File");
        for(ActionsExplore a : ActionsExplore.values()){
            menuBuilder.createFruit(a.initializer().init(master, a.toString(), a.description(), a.keyStroke()));
        }
        
        return (JMenu) menuBuilder.build().createUI();
    }),
    ;

    private final Function<RFF, JMenu> function;

    public JMenu getMenu(RFF master) {
        return function.apply(master);
    }


    RFFSettingsMenu(Function<RFF, JMenu> getter){
        this.function = getter;
    }
}
