package kr.merutilm.fractal.ui;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.ComboPopup;

import kr.merutilm.base.selectable.Selectable;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.function.Consumer;


public class MSelectionBoxPanel<S extends Enum<S> & Selectable> extends JPanel{

    public MSelectionBoxPanel(String name, S defaultValue, S[] options, Consumer<S> enterFunction){
        super(new GridLayout(1, 2, 10, 0));
        JLabel label = new JLabel(name);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(MUI.DEFAULT_FONT);

        JComboBox<S> comboBox = new JComboBox<>(options);
        comboBox.setSelectedIndex(defaultValue.ordinal());
        comboBox.addActionListener($ -> enterFunction.accept(options[comboBox.getSelectedIndex()]));
        comboBox.setBorder(BorderFactory.createEmptyBorder());
        comboBox.setBackground(MUI.BUTTON_BACKGROUND);
        comboBox.setForeground(MUI.TEXT_COLOR);
        comboBox.setFont(MUI.DEFAULT_FONT);
        comboBox.setUI(new BasicComboBoxUI(){

            @Override
            protected JButton createArrowButton() {
                return createNullButton();
            }

            @Override
            protected ListCellRenderer<Object> createRenderer() {
                BasicComboBoxRenderer b = new BasicComboBoxRenderer();
                b.setBorder(MUI.BUTTON_BORDER);
                b.setHorizontalAlignment(SwingConstants.CENTER);
                return b;
            }
            
        

            @Override
            protected ComboPopup createPopup() {
                return new BasicComboPopup(comboBox) {

                    @Override
                    protected void configureList() {
                        super.configureList();
                        list.setSelectionForeground(MUI.SELECTED_TEXT_COLOR);
                        list.setFont(MUI.DEFAULT_FONT);
                    }
                    @Override
                    protected void configureScroller() {
                        super.configureScroller();
                
                        scroller.getVerticalScrollBar().setUI(new BasicScrollBarUI(){
                            
                            @Override
                            protected JButton createDecreaseButton(int orientation) {
                                return createNullButton();
                            }
                            @Override    
                            protected JButton createIncreaseButton(int orientation) {
                                return createNullButton();
                            }


                        });
                        scroller.getVerticalScrollBar().setBackground(new Color(100, 100, 100));
                    }
                };
            }
        });

       
        setBackground(MUI.PANEL_BACKGROUND);
        setBorder(MUI.PANEL_BORDER);
        
        add(label);
        add(comboBox);
    }
    
    private static JButton createNullButton() {
        JButton z = new JButton(){
            @Override
            public int getWidth() {
                return 0;
            }

            @Override
            public int getHeight(){
                return 0;
            }
        };
        z.setPreferredSize(new Dimension(0, 0));
        z.setMinimumSize(new Dimension(0, 0));
        z.setMaximumSize(new Dimension(0, 0));
        z.setVisible(false);
        return z;
    }
}
