package kr.merutilm.rff.ui;

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

import kr.merutilm.rff.util.ConsoleUtils;

final class MUITextField<T> extends JPanel {

    private transient T previousValue;

    public MUITextField(String name, String description, T defaultValue, Function<String, T> mapper, Consumer<T> enterFunction) {
        this(name, description, defaultValue, mapper, enterFunction, true, "");
    }

    public MUITextField(String name, String description, T defaultValue, Function<String, T> mapper, Consumer<T> enterFunction,
            boolean validCondition, String invalidConditionMessage) {
        super(new GridLayout(1, 2, 10, 0));
        JLabel label = new JLabel(name);
        label.setFont(MUIConstants.DEFAULT_FONT);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setForeground(MUIConstants.TEXT_COLOR);
        
        JTextField textField = new JTextField(defaultValue.toString());
        textField.setToolTipText("<html>"+description+"</html>");
        textField.setHorizontalAlignment(SwingConstants.RIGHT);
        textField.setBackground(MUIConstants.INPUT_BACKGROUND);
        textField.setFont(MUIConstants.DEFAULT_FONT);
        textField.setForeground(MUIConstants.TEXT_COLOR);
        textField.setBorder(MUIConstants.INPUT_BORDER);
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                textField.setForeground(MUIConstants.UNSAVED_TEXT_COLOR);
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                textField.setForeground(MUIConstants.UNSAVED_TEXT_COLOR);
            }
            
            @Override
            public void insertUpdate(DocumentEvent e) {
                textField.setForeground(MUIConstants.UNSAVED_TEXT_COLOR);
                
            }
        });
        textField.addActionListener($ -> {
            if (!validCondition) {
                textField.setForeground(MUIConstants.ERROR_TEXT_COLOR);
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
                textField.setForeground(MUIConstants.ERROR_TEXT_COLOR);
                invokeError("Invalid Value!!");
                textField.setText(previousValue.toString());
            }
            textField.setForeground(MUIConstants.SAVED_TEXT_COLOR);
        });

        
        previousValue = defaultValue;

        setBackground(MUIConstants.PANEL_BACKGROUND);
        setBorder(MUIConstants.PANEL_BORDER);
        add(label);
        add(textField);

    }

    
    private void invokeError(String message) {
        JOptionPane.showMessageDialog(null, message, "ERROR", JOptionPane.ERROR_MESSAGE);
    }
}
