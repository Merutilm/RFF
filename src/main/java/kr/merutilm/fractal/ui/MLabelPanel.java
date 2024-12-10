package kr.merutilm.fractal.ui;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class MLabelPanel extends JPanel{

    private final JLabel label;

    public MLabelPanel(){
        this.label = new JLabel();
        setLayout(new BorderLayout(0, 0));
        
        add(label, BorderLayout.CENTER);
    }


    public JLabel getNameLabel(){
        return label;
    }
    
}
