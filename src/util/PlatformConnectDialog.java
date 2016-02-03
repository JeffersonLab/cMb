package util;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import org.jlab.coda.cMsg.cMsgNetworkConstants;
import ui.cMbUILite;

/**
 * JSA
 * Thomas Jefferson National Accelerator Facility
 * *
 * This software was developed under a United States
 * Government license, described in the NOTICE file
 * included as part of this distribution.
 * *
 * Copyright (c)
 *
 * @author gurjyan
 *
 * The Dalog Browser application's main frame.
 */

public class PlatformConnectDialog extends JDialog {
    private String  plName = "undefined";
    private String  plHost = "unspecified";
    private int     plPort = cMsgNetworkConstants.nameServerTcpPort;
    private cMbUILite owner;
    private boolean isLite;

    public PlatformConnectDialog(cMbUILite owner) {
        super(owner);
        this.owner = owner;
        this.plName = owner.getPlName();
        this.plPort = owner.getPlPort();
        this.plHost = owner.getPlHost();
        initComponents();
        isLite = true;
    }

    public void dExit(){
        dispose();
    }
    

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        nameTextField = new JTextField();
        label1 = new JLabel();
        label2 = new JLabel();
        separator1 = new JSeparator();
        label3 = new JLabel();
        hostTextField = new JTextField();
        label4 = new JLabel();
        portTextField = new JTextField();
        buttonBar = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();
        action1 = new OkAction();
        action2 = new CancelAction();

        //======== this ========
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {

                //---- nameTextField ----
                nameTextField.setText(plName);

                //---- label1 ----
                label1.setText("Name");
                label1.setForeground(new Color(0, 102, 102));

                //---- label2 ----
                label2.setText("Connect To The Platform");
                label2.setHorizontalAlignment(SwingConstants.CENTER);
                label2.setFont(new Font("Dialog", Font.BOLD, 12));
                label2.setForeground(new Color(0, 102, 102));

                //---- label3 ----
                label3.setText("Host");
                label3.setForeground(new Color(0, 102, 102));

                //---- hostTextField ----
                hostTextField.setText(plHost);

                //---- label4 ----
                label4.setText("Port");
                label4.setForeground(new Color(0, 102, 102));

                //---- portTextField ----
                portTextField.setText(Integer.toString(plPort));

                GroupLayout contentPanelLayout = new GroupLayout(contentPanel);
                contentPanel.setLayout(contentPanelLayout);
                contentPanelLayout.setHorizontalGroup(
                    contentPanelLayout.createParallelGroup()
                        .addGroup(contentPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(contentPanelLayout.createParallelGroup()
                                        .addComponent(separator1, GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
                                        .addGroup(contentPanelLayout.createSequentialGroup()
                                                .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                                        .addComponent(label4, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(label3, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(label1, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(contentPanelLayout.createParallelGroup()
                                                        .addComponent(hostTextField, GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                                                        .addComponent(nameTextField, GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                                                        .addComponent(portTextField, GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)))
                                        .addComponent(label2, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE))
                                .addContainerGap())
                );
                contentPanelLayout.setVerticalGroup(
                    contentPanelLayout.createParallelGroup()
                        .addGroup(contentPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(label2)
                                .addGap(7, 7, 7)
                                .addComponent(separator1, GroupLayout.PREFERRED_SIZE, 5, GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(label1)
                                        .addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(label3)
                                        .addComponent(hostTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(label4)
                                        .addComponent(portTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(13, Short.MAX_VALUE))
                );
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 85, 85, 80};
                ((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0, 0.0};

                //---- okButton ----
                okButton.setAction(action1);
                buttonBar.add(okButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- cancelButton ----
                cancelButton.setAction(action2);
                buttonBar.add(cancelButton, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JTextField nameTextField;
    private JLabel label1;
    private JLabel label2;
    private JSeparator separator1;
    private JLabel label3;
    private JTextField hostTextField;
    private JLabel label4;
    private JTextField portTextField;
    private JPanel buttonBar;
    private JButton okButton;
    private JButton cancelButton;
    private OkAction action1;
    private CancelAction action2;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private class OkAction extends AbstractAction {
        private OkAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "OK");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            plName = nameTextField.getText();
            plHost = hostTextField.getText();
            try {
                plPort = Integer.parseInt(portTextField.getText());
            } catch (NumberFormatException e1) {
                e1.printStackTrace();
            }
            if(isLite){
                owner.plConnect(plHost, plPort, plName);
            } else {
            owner.plConnect(plHost,plPort,plName);
            }
            dExit();
        }
    }

    private class CancelAction extends AbstractAction {
        private CancelAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Cancel");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            dExit();
        }
    }
}
