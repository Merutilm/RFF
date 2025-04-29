package kr.merutilm.rff.ui;

import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JMenu;

import kr.merutilm.rff.preset.Presets;

enum RFFSettingsMenu {
    FILE(master -> {
        RFFMenuTree.Builder menuBuilder = RFFMenuTree.Builder.init("File");
        for(ActionsFile a : ActionsFile.values()){
            menuBuilder.createItem(a.initializer().init(master, a.toString(), a.description(), a.keyStroke()));
        }

        return (JMenu) menuBuilder.build().createUI();
    }),
    FRACTAL(master -> {
        RFFMenuTree.Builder menuBuilder = RFFMenuTree.Builder.init("Fractal");
        for(ActionsFractal a : ActionsFractal.values()){
            menuBuilder.createItem(a.initializer().init(master, a.toString(), a.description(), a.keyStroke()));
        }
        
        return (JMenu) menuBuilder.build().createUI();
    }),
    IMAGE(master -> {
        RFFMenuTree.Builder menuBuilder = RFFMenuTree.Builder.init("Image");
        for(ActionsRender a : ActionsRender.values()){
            menuBuilder.createItem(a.initializer().init(master, a.toString(), a.description(), a.keyStroke()));
        }
        
        return (JMenu) menuBuilder.build().createUI();
    }),
    SHADER(master -> {
        RFFMenuTree.Builder menuBuilder = RFFMenuTree.Builder.init("Shader");
        for(ActionsShader a : ActionsShader.values()){
            menuBuilder.createItem(a.initializer().init(master, a.toString(), a.description(), a.keyStroke()));
        }
        
        return (JMenu) menuBuilder.build().createUI();
    }),
    PRESET(master -> {
        RFFMenuTree.Builder menuBuilder = RFFMenuTree.Builder.init("Preset");
        menuBuilder.createMenu("Calculation", e -> {
            for(Presets.Calculations a : Presets.Calculations.values()){
                e.createItem(ItemActions.createItem(a.toString(), "", null, () -> master.setPreset(a.preset())));
            }
        });
        menuBuilder.createMenu("Location", e -> {
            for(Presets.Locations a : Presets.Locations.values()){
                e.createItem(ItemActions.createItem(a.toString(), "", null, () -> {
                    master.setPreset(a.preset());
                    master.getWindow().getRenderer().requestRecompute();
                }));
            }
        });
        menuBuilder.createMenu("Render", e -> {
            for(Presets.Renders a : Presets.Renders.values()){
                e.createItem(ItemActions.createItem(a.toString(), "", null, () -> {
                    master.setPreset(a.preset());
                    master.getWindow().getRenderer().requestRecompute();
                }));
            }
        });
        menuBuilder.createMenu("Shaders", e -> {
            Consumer<Presets.PresetElement<?>> shaderChangedAction = a -> {
                master.setPreset(a.preset());
                master.getWindow().getRenderer().requestColor();
            };
            e.createMenu("Palettes", e1 -> {
                for(Presets.Shaders.Palettes a : Presets.Shaders.Palettes.values()){
                    e1.createItem(ItemActions.createItem(a.toString(), "", null, () -> shaderChangedAction.accept(a)));
                }
            });
            e.createMenu("Stripes", e1 -> {
                for(Presets.Shaders.Stripes a : Presets.Shaders.Stripes.values()){
                    e1.createItem(ItemActions.createItem(a.toString(), "", null, () -> shaderChangedAction.accept(a)));
                }
            });
            e.createMenu("Slopes", e1 -> {
                for(Presets.Shaders.Slopes a : Presets.Shaders.Slopes.values()){
                    e1.createItem(ItemActions.createItem(a.toString(), "", null, () -> shaderChangedAction.accept(a)));
                }
            });
            e.createMenu("Colors", e1 -> {
                for(Presets.Shaders.Colors a : Presets.Shaders.Colors.values()){
                    e1.createItem(ItemActions.createItem(a.toString(), "", null, () -> shaderChangedAction.accept(a)));
                }
            });
            e.createMenu("Fogs", e1 -> {
                for(Presets.Shaders.Fogs a : Presets.Shaders.Fogs.values()){
                    e1.createItem(ItemActions.createItem(a.toString(), "", null, () -> shaderChangedAction.accept(a)));
                }
            });
            e.createMenu("Blooms", e1 -> {
                for(Presets.Shaders.Blooms a : Presets.Shaders.Blooms.values()){
                    e1.createItem(ItemActions.createItem(a.toString(), "", null, () -> shaderChangedAction.accept(a)));
                }
            });
        });
        menuBuilder.createMenu("Resolution", e -> {
            for(Presets.Resolutions a : Presets.Resolutions.values()){
                e.createItem(ItemActions.createItem(a.toString(), "", null, () -> {
                    master.setPreset(a.preset());
                    master.getWindow().getRenderer().requestRecompute();
                }));
            }
        });
        return (JMenu) menuBuilder.build().createUI();
    }),
    VIDEO(master -> {
        RFFMenuTree.Builder menuBuilder = RFFMenuTree.Builder.init("Video");
        for(ActionsVideo a : ActionsVideo.values()){
            menuBuilder.createItem(a.initializer().init(master, a.toString(), a.description(), a.keyStroke()));
        }
        
        return (JMenu) menuBuilder.build().createUI();
    }),
    EXPLORE(master -> {
        RFFMenuTree.Builder menuBuilder = RFFMenuTree.Builder.init("Explore");
        for(ActionsExplore a : ActionsExplore.values()){
            menuBuilder.createItem(a.initializer().init(master, a.toString(), a.description(), a.keyStroke()));
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
