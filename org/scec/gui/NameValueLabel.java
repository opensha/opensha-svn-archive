package org.scec.gui;

import java.awt.*;
import java.util.*;
import javax.swing.*;

import org.scec.param.editor.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class NameValueLabel extends JPanel{
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JLabel nameLabel = new JLabel();
    JLabel valueLabel = new JLabel();

    private String keyName;
    private String value;

    public NameValueLabel() {
        try { jbInit(); }
        catch(Exception e) { e.printStackTrace(); }
    }

    public void setForground(Color c){
        //super.setForeground(c);
        nameLabel.setForeground(c);
    }

    public void setLableForground(Color c){
        nameLabel.setForeground(c);
    }

    private void jbInit() throws Exception {
        nameLabel.setFont(new java.awt.Font("Dialog", 1, 11));
        nameLabel.setMaximumSize(new Dimension(100, 16));
        nameLabel.setMinimumSize(new Dimension(100, 16));
        nameLabel.setPreferredSize(new Dimension(100, 16));
        nameLabel.setHorizontalAlignment(SwingConstants.LEFT);
        nameLabel.setText("Name");
        this.setBackground(Color.white);
        this.setLayout(gridBagLayout1);
        valueLabel.setFont(new java.awt.Font("Dialog", 0, 11));
        //valueLabel.setBorder(BorderFactory.createLineBorder(Color.black));
        valueLabel.setHorizontalAlignment(SwingConstants.LEFT);
        valueLabel.setText("N/A");
        this.add(nameLabel,    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(valueLabel,   new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    }

    public String getKeyName() { return keyName; }
    public void setKeyName(String keyName) {
        this.keyName = keyName;
        nameLabel.setText(keyName);
    }

    public String getValue() { return value; }
    public void setValue(String value) {
        this.value = value;
        valueLabel.setText(value);
    }


}
