package kr.merutilm.rff.ui;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

final class MUILabelPanel extends RFFPanel{

    private final JLabel label;

    public MUILabelPanel(){
        this.label = new JLabel();
        setLayout(new BorderLayout(0, 0));
        add(label, BorderLayout.CENTER);
    }


    public JLabel getNameLabel(){
        return label;
    }
    
}
