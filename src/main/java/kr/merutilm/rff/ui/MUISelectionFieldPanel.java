package kr.merutilm.rff.ui;

import java.awt.GridLayout;
import java.util.function.Consumer;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicToggleButtonUI;

import kr.merutilm.rff.selectable.Selectable;

final class MUISelectionFieldPanel<S extends Enum<S> & Selectable> extends JPanel{

    public MUISelectionFieldPanel(String name, S defaultValue, S[] options, Consumer<S> enterFunction){
        super(new GridLayout(1, 2, 10, 0));
        JLabel label = new JLabel(name);
        label.setFont(MUIConstants.DEFAULT_FONT);
        label.setForeground(MUIConstants.TEXT_COLOR);
        label.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, options.length));
        buttonPanel.setBorder(MUIConstants.INPUT_BORDER);
        buttonPanel.setBackground(MUIConstants.INPUT_BACKGROUND);

        ButtonGroup group = new ButtonGroup();

        for (S option : options) {
            JToggleButton button = new JToggleButton(option.toString());
            button.setFocusPainted(false);
            button.addActionListener(_ -> enterFunction.accept(option));
            button.addChangeListener(_ -> {
                if(button.getModel().isSelected()){
                    button.setForeground(MUIConstants.SELECTED_TEXT_COLOR);
                    button.setBackground(MUIConstants.SELECTED_BUTTON_BACKGROUND);
                }else{
                    button.setForeground(MUIConstants.TEXT_COLOR);
                    button.setBackground(MUIConstants.BUTTON_BACKGROUND);
                }
            });
            
            button.setUI(new BasicToggleButtonUI());
            button.setFont(MUIConstants.DEFAULT_FONT);
            button.setForeground(MUIConstants.TEXT_COLOR);
            button.setBackground(MUIConstants.BUTTON_BACKGROUND);
            button.setBorder(MUIConstants.BUTTON_BORDER);
            if(option == defaultValue){
                button.setSelected(true);
            }
            group.add(button);
            buttonPanel.add(button);
        }

        setBackground(MUIConstants.PANEL_BACKGROUND);
        setBorder(MUIConstants.PANEL_BORDER);
    

        add(label);
        add(buttonPanel);
    }
}
