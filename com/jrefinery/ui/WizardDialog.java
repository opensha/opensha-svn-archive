/* ================================================================
 * JCommon : a general purpose, open source, class library for Java
 * ================================================================
 *
 * Project Info:  http://www.object-refinery.com/jcommon/index.html
 * Project Lead:  David Gilbert (david.gilbert@object-refinery.com);
 *
 * (C) Copyright 2000-2002, by Simba Management Limited and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * -----------------
 * WizardDialog.java
 * -----------------
 * (C) Copyright 2000-2002, by Simba Management Limited.
 *
 * Original Author:  David Gilbert (for Simba Management Limited);
 * Contributor(s):   -;
 *
 * $Id$
 *
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Oct-2001 : Changed package to com.jrefinery.ui.*;
 *
 */

package com.jrefinery.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

/**
 * A dialog that presents the user with a sequence of steps for completing a task.  The dialog
 * contains "Next" and "Previous" buttons, allowing the user to navigate through the task.
 * <P>
 * When the user backs up by one or more steps, the dialog keeps the completed steps so that
 * they can be reused if the user doesn't change anything - this handles the cases where the user
 * backs up a few steps just to review what has been completed.
 * <p>
 * But if the user changes some options in an earlier step, then the dialog may have to discard
 * the later steps and have them repeated.
 * <P>
 * THIS CLASS IS NOT WORKING CORRECTLY YET.
 */
public class WizardDialog extends JDialog implements ActionListener {

    /** The end result of the wizard sequence; */
    protected Object result;

    /** The current step in the wizard process (starting at step zero); */
    protected int step;

    /** A reference to the current panel; */
    protected WizardPanel currentPanel;

    /** A list of references to the panels the user has already seen - used for navigating through
        the steps that have already been completed; */
    protected java.util.List panels;

    /** A handy reference to the "previous" button; */
    JButton previousButton;

    /** A handy reference to the "next" button; */
    JButton nextButton;

    /** A handy reference to the "finish" button; */
    JButton finishButton;

    /** A handy reference to the "help" button; */
    JButton helpButton;

    /**
     * Standard constructor - builds and returns a new WizardDialog;
     */
    public WizardDialog(JDialog owner, boolean modal,
                        String title, WizardPanel firstPanel) {
        super(owner, title+" : step 1", modal);
        this.result = null;
        this.currentPanel = firstPanel;
        this.step = 0;
        this.panels = new ArrayList();
        panels.add(firstPanel);
        this.setContentPane(createContent());
    }

    /**
     * Standard constructor - builds a new WizardDialog owned by the specified JFrame.
     */
    public WizardDialog(JFrame owner, boolean modal,
                        String title, WizardPanel firstPanel) {
        super(owner, title+" : step 1", modal);
        this.result = null;
        this.currentPanel = firstPanel;
        this.step = 0;
        this.panels = new ArrayList();
        panels.add(firstPanel);
        this.setContentPane(createContent());
    }

    /**
     * Returns the result of the wizard sequence.
     */
    public Object getResult() {
        return this.result;
    }

    /**
     * Returns the total number of steps in the wizard sequence, if this number is known.  Otherwise
     * this method returns zero.  Subclasses should override this method unless the number of steps
     * is not known.
     */
    public int getStepCount() {
        return 0;
    }

    /**
     * Returns true if it is possible to back up to the previous panel, and false otherwise.
     */
    public boolean canDoPreviousPanel() {
        return (step>0);
    }

    /**
     * Returns true if there is a 'next' panel, and false otherwise.
     */
    public boolean canDoNextPanel() {
        return currentPanel.hasNextPanel();
    }

    /**
     * Returns true if it is possible to finish the sequence at this point (possibly with defaults
     * for the remaining entries).
     */
    public boolean canFinish() {
        return currentPanel.canFinish();
    }

    /**
     * Returns the panel for the specified step (steps are numbered from zero).
     */
    public WizardPanel getWizardPanel(int step) {
        if (step<panels.size()) {
            return (WizardPanel)(panels.get(step));
        }
        else return null;
    }

    /**
     * Handles events.
     */
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        if (command.equals("nextButton")) {
            next();
        }
        else if (command.equals("previousButton")) {
            previous();
        }
        else if (command.equals("finishButton")) {
            finish();
        }
    }

    /**
     * Handles a click on the "previous" button, by displaying the previous panel in the sequence.
     */
    public void previous() {
        if (step>0) {
            WizardPanel previousPanel = getWizardPanel(step-1);
            // tell the panel that we are returning
            previousPanel.returnFromLaterStep();
            Container content = this.getContentPane();
            content.remove(currentPanel);
            content.add(previousPanel);
            step = step - 1;
            currentPanel = previousPanel;
            this.setTitle("Step "+(step+1));
            enableButtons();
            this.pack();
        }
    }

    /**
     * Displays the next step in the wizard sequence.
     */
    public void next() {

        WizardPanel nextPanel = this.getWizardPanel(step+1);
        if (nextPanel!=null) {
            if (!currentPanel.canRedisplayNextPanel()) {
                nextPanel = currentPanel.getNextPanel();
            }
        }
        else nextPanel = currentPanel.getNextPanel();

        step = step + 1;
        if (step<panels.size()) {
            panels.set(step, nextPanel);
        }
        else panels.add(nextPanel);

        Container content = this.getContentPane();
        content.remove(currentPanel);
        content.add(nextPanel);

        currentPanel = nextPanel;
        this.setTitle("Step "+(step+1));
        enableButtons();
        this.pack();

    }

    /**
     * Finishes the wizard.
     */
    public void finish() {
        this.result = currentPanel.getResult();
        hide();
    }

    /**
     * Enables/disables the buttons according to the current step.  A good idea would be to ask the
     * panels to return the status...
     */
    private void enableButtons() {
        previousButton.setEnabled(step>0);
        nextButton.setEnabled(this.canDoNextPanel());
        finishButton.setEnabled(this.canFinish());
        helpButton.setEnabled(false);
    }

    /**
     * Was the wizard cancelled?
     */
    public boolean isCancelled() {
        return false;
    }

    /**
     * Creates a panel containing the user interface for the dialog.
     */
    public JPanel createContent() {

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        content.add((JPanel)panels.get(0));
        L1R3ButtonPanel buttons = new L1R3ButtonPanel("Help", "Previous", "Next", "Finish");

        helpButton = buttons.getLeftButton();
        helpButton.setEnabled(false);

        previousButton = buttons.getRightButton1();
        previousButton.setActionCommand("previousButton");
        previousButton.addActionListener(this);
        previousButton.setEnabled(false);

        nextButton = buttons.getRightButton2();
        nextButton.setActionCommand("nextButton");
        nextButton.addActionListener(this);
        nextButton.setEnabled(true);

        finishButton = buttons.getRightButton3();
        finishButton.setActionCommand("finishButton");
        finishButton.addActionListener(this);
        finishButton.setEnabled(false);

        buttons.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        content.add(buttons, BorderLayout.SOUTH);

        return content;
    }

}