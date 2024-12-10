package kr.merutilm.fractal.ui;

import java.awt.GridLayout;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import kr.merutilm.base.util.ConsoleUtils;

public class MTextField<T> extends JPanel {

    private transient T previousValue;

    public MTextField(String name, T defaultValue, Function<String, T> mapper, Consumer<T> enterFunction) {
        this(name, defaultValue, mapper, enterFunction, true, "");
    }

    public MTextField(String name, T defaultValue, Function<String, T> mapper, Consumer<T> enterFunction,
            boolean validCondition, String invalidConditionMessage) {
        super(new GridLayout(1, 2, 10, 0));
        JLabel label = new JLabel(name);
        label.setFont(MUI.DEFAULT_FONT);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setForeground(MUI.TEXT_COLOR);
        
        JTextField textField = new JTextField(defaultValue.toString());
        textField.setHorizontalAlignment(SwingConstants.RIGHT);
        textField.setBackground(MUI.INPUT_BACKGROUND);
        textField.setForeground(MUI.TEXT_COLOR);
        textField.setBorder(MUI.INPUT_BORDER);
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                textField.setForeground(MUI.UNSAVED_TEXT_COLOR);
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                textField.setForeground(MUI.UNSAVED_TEXT_COLOR);
            }
            
            @Override
            public void insertUpdate(DocumentEvent e) {
                textField.setForeground(MUI.UNSAVED_TEXT_COLOR);
                
            }
        });
        textField.addActionListener($ -> {
            if (!validCondition) {
                textField.setForeground(MUI.ERROR_TEXT_COLOR);
                invokeError(invalidConditionMessage);
                textField.setText(previousValue.toString());
                return;
            }
            try {
                T t = mapper.apply(textField.getText());
                previousValue = t;
                enterFunction.accept(t);
            } catch (RuntimeException e) {
                ConsoleUtils.logError(e);
                textField.setForeground(MUI.ERROR_TEXT_COLOR);
                invokeError("Invalid Value!!");
                textField.setText(previousValue.toString());
            }
            textField.setForeground(MUI.SAVED_TEXT_COLOR);
        });

        
        previousValue = defaultValue;

        setBackground(MUI.PANEL_BACKGROUND);
        setBorder(MUI.PANEL_BORDER);
 setBorder(MUI.PANEL_BORDER);
        add(label);
        add(textField);

    }

    
    private void invokeError(String message) {
        JOptionPane.showMessageDialog(null, message, "ERROR", 0);
    }
}
