package kr.merutilm.rff.ui;
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

import kr.merutilm.rff.selectable.Selectable;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.function.Consumer;


final class MUISelectionBoxPanel<S extends Enum<S> & Selectable> extends RFFPanel{

    public MUISelectionBoxPanel(String name, String description, S defaultValue, S[] options, Consumer<S> enterFunction){
        super(new GridLayout(1, 2, 10, 0));
        JLabel label = new JLabel(name);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(MUIConstants.DEFAULT_FONT);

        JComboBox<S> comboBox = new JComboBox<>(options);
        comboBox.setToolTipText("<html>"+description+"</html>");
        comboBox.setSelectedIndex(defaultValue.ordinal());
        comboBox.addActionListener(_ -> enterFunction.accept(options[comboBox.getSelectedIndex()]));
        comboBox.setBorder(MUIConstants.INPUT_BORDER);
        comboBox.setBackground(MUIConstants.BUTTON_BACKGROUND);
        comboBox.setForeground(MUIConstants.TEXT_COLOR);
        comboBox.setFont(MUIConstants.DEFAULT_FONT);
        comboBox.setOpaque(false);
        comboBox.setUI(new BasicComboBoxUI(){

            @Override
            protected JButton createArrowButton() {
                return createNullButton();
            }

            @Override
            protected ListCellRenderer<Object> createRenderer() {
                BasicComboBoxRenderer b = new BasicComboBoxRenderer();
                b.setBorder(MUIConstants.BUTTON_BORDER);
                b.setHorizontalAlignment(SwingConstants.CENTER);
                return b;
            }
            
        

            @Override
            protected ComboPopup createPopup() {
                return new BasicComboPopup(comboBox) {

                    @Override
                    protected void configureList() {
                        super.configureList();
                        list.setSelectionForeground(MUIConstants.SELECTED_TEXT_COLOR);
                        list.setSelectionBackground(MUIConstants.SELECTED_BUTTON_BACKGROUND);
                        list.setFont(MUIConstants.DEFAULT_FONT);
                        list.setOpaque(false);
                        list.setBorder(MUIConstants.BUTTON_BORDER);
                        list.setFixedCellHeight(MUIConstants.UI_SELECTION_HEIGHT);
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
                            
                            
                            @Override
                            protected void configureScrollBarColors() {
                                trackColor = MUIConstants.PANEL_BACKGROUND;
                                thumbColor = MUIConstants.BUTTON_BACKGROUND;
                                minimumThumbSize = new Dimension(0, MUIConstants.SCROLL_BAR_HEIGHT);
                                scrollBarWidth = MUIConstants.SCROLL_BAR_WIDTH;

                            }

                        });
                    }
                };
            }
        });

       
        setBackground(MUIConstants.PANEL_BACKGROUND);
        setBorder(MUIConstants.PANEL_BORDER);
        
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
