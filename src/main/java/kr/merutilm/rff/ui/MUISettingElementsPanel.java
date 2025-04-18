package kr.merutilm.rff.ui;


import kr.merutilm.rff.functions.BooleanConsumer;
import kr.merutilm.rff.selectable.Selectable;

import java.awt.GridLayout;
import java.util.function.Consumer;
import java.util.function.Function;


final class MUISettingElementsPanel extends RFFPanel{
    

    public MUISettingElementsPanel(){
        super(new GridLayout(1, 1));
    }


    public <T> void createTextInput(String name, String description, T defaultValue, Function<String, T> mapper, Consumer<T> enterFunction){
        add(new MUITextField<>(name, description, defaultValue, mapper, enterFunction));
    }
    public <T> void createTextInput(String name, String description,  T defaultValue, Function<String, T> mapper, Consumer<T> enterFunction, boolean validCondition, String invalidConditionMessage){
        add(new MUITextField<>(name, description, defaultValue, mapper, enterFunction, validCondition, invalidConditionMessage));
    }
    public <S extends Enum<S> & Selectable> void createSelectInput(String name, String description, S defaultValue, S[] options, Consumer<S> enterFunction, boolean useComboBox){
        if(useComboBox){
            add(new MUISelectionBoxPanel<>(name, description, defaultValue, options, enterFunction));
        }else{
            add(new MUISelectionFieldPanel<>(name, description, defaultValue, options, enterFunction));
        }
    }
    
    public void createBoolInput(String name, String description, boolean defaultValue, BooleanConsumer enterFunction){
        enum Bools implements Selectable {
            TRUE(true),
            FALSE(false);
        
            private final boolean name;
        
            public boolean bool() {
                return name;
            }
            @Override
            public String toString() {
                return String.valueOf(name);
            }
        
            Bools(boolean name) {
                this.name = name;
            }
        }
        createSelectInput(name, description, defaultValue ? Bools.TRUE : Bools.FALSE, Bools.values(), e -> enterFunction.accept(e.bool()), false);
    }


    public void refresh(){
        int count = getComponentCount();
        setLayout(new GridLayout(count, 1));
    }
    
}
