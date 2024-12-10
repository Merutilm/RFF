package kr.merutilm.fractal.ui;

import java.awt.GridLayout;
import java.util.function.Consumer;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import kr.merutilm.base.selectable.Selectable;

public class MSelectionFieldPanel<S extends Enum<S> & Selectable> extends JPanel{

    public MSelectionFieldPanel(String name, S defaultValue, S[] options, Consumer<S> enterFunction){
        super(new GridLayout(1, 2, 10, 0));
        JLabel label = new JLabel(name);
        label.setFont(MUI.DEFAULT_FONT);
        label.setForeground(MUI.TEXT_COLOR);
        label.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, options.length));
        buttonPanel.setBorder(MUI.INPUT_BORDER);
        buttonPanel.setBackground(MUI.INPUT_BACKGROUND);

        ButtonGroup group = new ButtonGroup();

        for (S option : options) {
            JToggleButton button = new JToggleButton(option.toString());
            button.setFocusPainted(false);
            button.addActionListener(e -> enterFunction.accept(option));
            button.addChangeListener(e -> {
                if(button.getModel().isSelected()){
                    button.setForeground(MUI.SELECTED_TEXT_COLOR);
                }else{
                    button.setForeground(MUI.TEXT_COLOR);
                }
            });
            button.setFont(MUI.DEFAULT_FONT);
            button.setForeground(MUI.TEXT_COLOR);
            button.setBackground(MUI.BUTTON_BACKGROUND);
            button.setBorder(MUI.BUTTON_BORDER);
            if(option == defaultValue){
                button.setSelected(true);
            }
            group.add(button);
            buttonPanel.add(button);
        }

        setBackground(MUI.PANEL_BACKGROUND);
        setBorder(MUI.PANEL_BORDER);
    

        add(label);
        add(buttonPanel);
    }
}
