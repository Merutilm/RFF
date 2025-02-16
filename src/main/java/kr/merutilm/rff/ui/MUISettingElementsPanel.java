package kr.merutilm.rff.ui;


import kr.merutilm.rff.functions.BooleanConsumer;
import kr.merutilm.rff.selectable.BooleanValue;
import kr.merutilm.rff.selectable.Selectable;

import java.awt.GridLayout;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JPanel;

final class MUISettingElementsPanel extends JPanel{
    

    public MUISettingElementsPanel(){
        super(new GridLayout(1, 1));
    }


    public <T> void createTextInput(String name, T defaultValue, Function<String, T> mapper, Consumer<T> enterFunction){
        add(new MUITextField<T>(name, defaultValue, mapper, enterFunction));
    }
    public <T> void createTextInput(String name, T defaultValue, Function<String, T> mapper, Consumer<T> enterFunction, boolean validCondition, String invalidConditionMessage){
        add(new MUITextField<T>(name, defaultValue, mapper, enterFunction, validCondition, invalidConditionMessage));
    }
    public <S extends Enum<S> & Selectable> void createSelectInput(String name, S defaultValue, S[] options, Consumer<S> enterFunction, boolean useComboBox){
        if(useComboBox){
            add(new MUISelectionBoxPanel<S>(name, defaultValue, options, enterFunction));
        }else{
            add(new MUISelectionFieldPanel<S>(name, defaultValue, options, enterFunction));
        }
    }
    
    public void createBoolInput(String name, boolean defaultValue, BooleanConsumer enterFunction){
        createSelectInput(name, BooleanValue.typeOf(defaultValue), BooleanValue.values(), e -> enterFunction.accept(e.bool()), false);
    }


    public void refresh(){
        int count = getComponentCount();
        setLayout(new GridLayout(count, 1));
    }
    
}
