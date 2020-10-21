package ui;
/*
 * Created by JFormDesigner on Wed Sep 22 17:08:20 EDT 2010
 */

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.*;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.border.*;

import factory.cMbTableFactory;
import org.jlab.coda.cMsg.*;
import util.PlatformConnectDialog;

/**
 * @author gurjyan
 */
public class cMbUILite extends JFrame {
    private String myName;
    private cMbUILite me;

    private String plUdl = "undefined";
    private String plName = "undefined";
    private String plHost = "undefined";
    private int plPort = cMsgNetworkConstants.nameServerTcpPort;
    private cMsg myPlatformConnection;

    private cMsgSubscriptionHandle allMsgSubscription;
    private cMsgSubscriptionHandle selectedMsgSubscription;
    private cMsgSubscriptionHandle dalogMsgSubscription;

    private cMbTableFactory messageSpaceTF = new cMbTableFactory();
    private cMbTableFactory otherMsgTF = new cMbTableFactory();
    private cMbTableFactory dalogMsgTF = new cMbTableFactory();

    private JTable messageSpaceT;
    private JTable otherMsgT;
    private JTable dalogMsgT;

    private boolean createOT = false;
    private ArrayList<String> otherTColumnNames = new ArrayList<>();

    private AtomicBoolean isUpdateActive = new AtomicBoolean();


    /**
     * fifo of the messages
     */
    private ConcurrentLinkedQueue<cMsgMessage> selectMessageQueue = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<cMsgMessage> dalogMessageQueue = new ConcurrentLinkedQueue<>();

    //  message queue size
    private int queSize = 1000;
    private int dalogMsgQueSize = 1000;

    private volatile String currentSubject = "undefined";
    private volatile String currentType = "undefined";
    private String archiveDir = "undefined";

    // filtering
    private java.util.List<String> MessageSeverityFilter = Collections.synchronizedList(new ArrayList<>());
    private String sender = null;
    private String codaClass = null;

    private boolean _selectionOn = false;

    private boolean isSelectionUpdate = true;
    private boolean isDalogUpdate = true;

    DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public cMbUILite() {
        Random r = new Random();
        myName = "cMb_" + r.nextInt(1000);
        String s = System.getenv("EXPID");
        if (s != null) {
            plName = s;
        }

        boolean ka = (new File(".cmb")).exists();
        if (!ka) {
            boolean stat = (new File(".cmb")).mkdirs();
            if (!stat) {
                System.out.println("Error: Failed to create .cmb dir");
            }
        }

        messageSpaceT = messageSpaceTF.createMessageSpaceTable();
        MessageSpaceTSelectionListener listener = new MessageSpaceTSelectionListener(messageSpaceT);
        messageSpaceT.getSelectionModel().addListSelectionListener(listener);

        dalogMsgT = dalogMsgTF.createDalogTable();

        otherMsgT = otherMsgTF.createDalogTable();

        initComponents();
        me = this;
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                dispose();
                System.exit(1);
            }
        });

        updateMessageSeverityAL();

        if (!currentSubject.equals("undefined") && !currentType.equals("undefined")) {
            currentSubjectTextField.setText(currentSubject);
            currentTypeTextField.setText((currentType));
            selectMsgScrollPane.setViewportView(otherMsgT);
        }

    }

    private void setSelectioEnabled(boolean b) {
        _selectionOn = b;
    }

    public static void main(String[] args) {
        String title = null;
        cMbUILite g = new cMbUILite();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-title")) title = args[i + 1];
            else if (args[i].equalsIgnoreCase("-host")) g.plHost = args[i + 1];
            else if (args[i].equalsIgnoreCase("-port")) g.plPort = Integer.parseInt(args[i + 1]);
            else if (args[i].equalsIgnoreCase("-subject")) {
                String s = "*";
                if (!args[i + 1].equalsIgnoreCase("all")) {
                    s = args[i + 1];
                }
                g.currentSubjectTextField.setText(s);
                g.currentSubject = s;
            } else if (args[i].equalsIgnoreCase("-type")) {
                g.currentTypeTextField.setText(args[i + 1]);
                g.currentType = args[i + 1];
            } else if (args[i].equalsIgnoreCase("-archive")) {
                g.archiveDir = args[i + 1];
            } else if (args[i].equalsIgnoreCase("-queueSize")) {
                try {
                    g.queSize = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.out.println("Error: Queue size must be integer.");
                }
            } else if (args[i].equalsIgnoreCase("-h") || args[i].equalsIgnoreCase("-help")) {
                System.out.println("cmb -<option> <value>\n" +
                        "\n" +
                        "  <option>\n" +
                        "-------------\n" +
                        "  -h or -help        :   prints help\n" +
                        "  -title             :   title of the GUI.\n" +
                        "  -host              :   platform host.\n" +
                        "  -port              :   platform port.\n" +
                        "  -subject           :   subject of the subscription.\n" +
                        "  -type              :   type of the subscription.\n" +
                        "  -archive           :   direct path to the archive dir.\n" +
                        "  -queueSize         :   stored message queue size.\n" +
                        "");
                System.exit(1);
            }
        }
        if (!g.plHost.equals("undefined")) {
            g.plConnect(g.plHost, g.plPort, g.plName);
        }
        if (title != null) {
            g.setTitle(title);
        }
        g.setVisible(true);
    }

    public String getPlName() {
        return plName;
    }

    public String getPlHost() {
        return plHost;
    }

    public int getPlPort() {
        return plPort;
    }

    public void plConnect(String host, int port, String name) {
        if (host.equals("unspecified") || host.equals("") || host.equals(" ")) {
            cMsgMessage m = null;
            try {
                String udl = "cMsg:rc://multicast/" + plName + "&multicastTO=5&connectTO=5";
                // connect to the rc domain multicast server and request platform host name
                cMsg myRcDomainConnection = new cMsg(udl, myName, "");
                // Connect to the rc domain multicast
                // server and request platform host name
                for (int i = 0; i < 100; i++) {
                    m = myRcDomainConnection.monitor(Integer.toString(300));
                    if (m != null) break;
                }
            } catch (cMsgException e) {
                e.printStackTrace();
            }
            if (m != null) {
                host = m.getSenderHost();
            }
        }
        // connect to the platform
        plUdl = "cMsg://" + host + ":" + port + "/cMsg/" + name + "?regime=low&cmsgpassword=" + plName;
        try {
            if (myPlatformConnection != null && myPlatformConnection.isConnected()) {
                myPlatformConnection.disconnect();
                myPlatformConnection = null;
            }
            myPlatformConnection = new cMsg(plUdl, myName, "cMsg Browser GUI");
            myPlatformConnection.connect();
            myPlatformConnection.start();
        } catch (cMsgException ee) {
            System.out.println("Error: connecting to the platform");
            JOptionPane.showMessageDialog(me, "Error connecting to the platform");
        }
        plName = name;
        plHost = host;
        plPort = port;

        if (myPlatformConnection != null && myPlatformConnection.isConnected()) {
            try {

                if (allMsgSubscription != null) myPlatformConnection.unsubscribe(allMsgSubscription);
                allMsgSubscription = myPlatformConnection.subscribe("*",
                        "*",
                        new MessageSpaceCallback(),
                        null);
                statusbutton.setIcon(new ImageIcon(getClass().getResource("/resources/info-on.png")));
                isUpdateActive.set(true);

                if (dalogMsgSubscription != null) myPlatformConnection.unsubscribe(dalogMsgSubscription);
                dalogMsgSubscription = myPlatformConnection.subscribe("*",
                        "rc/report/dalog",
                        new DalogMessageCallback(),
                        null);

                if (selectedMsgSubscription != null) myPlatformConnection.unsubscribe(selectedMsgSubscription);
                selectedMsgSubscription = myPlatformConnection.subscribe(currentSubject.trim(),
                        currentType.trim(),
                        new SelectedMessageCallback(),
                        null);

            } catch (cMsgException e) {
                e.printStackTrace();
            }
        }
    }

    private void plDisconnect() {
        try {
            if (myPlatformConnection != null && myPlatformConnection.isConnected()) {
                myPlatformConnection.disconnect();
                myPlatformConnection = null;
                statusbutton.setIcon(new ImageIcon(getClass().getResource("/resources/error-on.png")));
            }
        } catch (cMsgException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Converts byte array into an Object, that can be cast into pre-known class object.
     *
     * @param bytes byte array
     * @return Object
     * @throws java.io.IOException    case will return null
     * @throws ClassNotFoundException case will return null
     */
    private Object B2O(byte bytes[]) throws IOException, ClassNotFoundException {

        try {
            Object object;
            java.io.ObjectInputStream in;
            java.io.ByteArrayInputStream bs;
            bs = new java.io.ByteArrayInputStream(bytes);
            in = new java.io.ObjectInputStream(bs);
            object = in.readObject();
            in.close();
            bs.close();
            return object;
        } catch (StreamCorruptedException sce) {
            System.out.println("Stream corrupted Exception ");
            sce.printStackTrace();
            return new Object();
        } catch (java.lang.ClassCastException cce) {
            System.out.println("Class Cast Exception ");
            cce.printStackTrace();
            return new Object();
        }
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        menuBar1 = new JMenuBar();
        platformmenu = new JMenu();
        connectmenuItem = new JMenuItem();
        disconnectmenuItem = new JMenuItem();
        exitmenuItem = new JMenuItem();
        controlmenu = new JMenu();
        msgqueuemenu = new JMenu();
        menu1 = new JMenu();
        msgqclearmenuItem = new JMenuItem();
        setmsgqsizemenuItem = new JMenuItem();
        menu3 = new JMenu();
        menuItem4 = new JMenuItem();
        menuItem5 = new JMenuItem();
        dalogfiltermenu = new JMenu();
        severityfiltermenu = new JMenu();
        infoCheckBox = new JCheckBox();
        warningCheckBox = new JCheckBox();
        errorCheckBox = new JCheckBox();
        severeCheckBox = new JCheckBox();
        menuItem6 = new JMenuItem();
        menuItem7 = new JMenuItem();
        menu2 = new JMenu();
        menu4 = new JMenu();
        cleartablemenuItem = new JMenuItem();
        menuItem3 = new JMenuItem();
        menu5 = new JMenu();
        menuItem8 = new JMenuItem();
        menuItem9 = new JMenuItem();
        menuItem10 = new JMenuItem();
        menuItem11 = new JMenuItem();
        menuItem1 = new JMenuItem();
        menuItem2 = new JMenuItem();
        helpmenu = new JMenu();
        aboutmenuItem = new JMenuItem();
        panel1 = new JPanel();
        toolBar3 = new JToolBar();
        statusbutton = new JButton();
        pausebutton = new JButton();
        resumebutton = new JButton();
        label1 = new JLabel();
        currentSubjectTextField = new JTextField();
        label2 = new JLabel();
        currentTypeTextField = new JTextField();
        tabbedPane1 = new JTabbedPane();
        selectMsgScrollPane = new JScrollPane();
        dalogMsgScrollPane = new JScrollPane();
        messageSpaceScrollPane = new JScrollPane();
        action1 = new ConnectAction();
        action2 = new DisconnectAction();
        action3 = new ExitAction();
        action4 = new StatusAction();
        action5 = new PauseAction();
        action6 = new ResumeAction();
        action16 = new SeverityInfoFilterAction();
        action17 = new SeverityWarningFilterAction();
        action18 = new SeveritErrorFilterAction();
        action19 = new SeveritySevereFilterAction();
        action8 = new SetMsgQueSize();
        action9 = new ClearMsgQue();
        action10 = new ClearTableAction();
        action12 = new EnableMessageSelection();
        action7 = new DisableMsgSelectionAction();
        action11 = new ClearDalogTableAction();
        action13 = new ClearDalogMsgQueue();
        action14 = new SetDalogMsgQueueSize();
        action15 = new CodaClassFilter();
        action20 = new MessageAuthorFilter();
        action21 = new StopUpdateSelection();
        action22 = new ResumeSelectionUpdate();
        action23 = new StopDalogUpdate();
        action24 = new ResumeDalogUpdate();

        //======== this ========
        var contentPane = getContentPane();

        //======== menuBar1 ========
        {

            //======== platformmenu ========
            {
                platformmenu.setText("Platform");
                platformmenu.setSelectedIcon(null);

                //---- connectmenuItem ----
                connectmenuItem.setAction(action1);
                connectmenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
                platformmenu.add(connectmenuItem);
                platformmenu.addSeparator();

                //---- disconnectmenuItem ----
                disconnectmenuItem.setAction(action2);
                disconnectmenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK));
                platformmenu.add(disconnectmenuItem);
                platformmenu.addSeparator();

                //---- exitmenuItem ----
                exitmenuItem.setAction(action3);
                platformmenu.add(exitmenuItem);
            }
            menuBar1.add(platformmenu);

            //======== controlmenu ========
            {
                controlmenu.setAction(null);
                controlmenu.setText("Control");
                controlmenu.addSeparator();

                //======== msgqueuemenu ========
                {
                    msgqueuemenu.setText("Message Queue");

                    //======== menu1 ========
                    {
                        menu1.setText("Selection");

                        //---- msgqclearmenuItem ----
                        msgqclearmenuItem.setAction(action9);
                        msgqclearmenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_MASK));
                        msgqclearmenuItem.setText("Reset");
                        menu1.add(msgqclearmenuItem);
                        menu1.addSeparator();

                        //---- setmsgqsizemenuItem ----
                        setmsgqsizemenuItem.setAction(action8);
                        setmsgqsizemenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK));
                        menu1.add(setmsgqsizemenuItem);
                    }
                    msgqueuemenu.add(menu1);
                    msgqueuemenu.addSeparator();

                    //======== menu3 ========
                    {
                        menu3.setText("daLog");

                        //---- menuItem4 ----
                        menuItem4.setAction(action13);
                        menu3.add(menuItem4);
                        menu3.addSeparator();

                        //---- menuItem5 ----
                        menuItem5.setAction(action14);
                        menu3.add(menuItem5);
                    }
                    msgqueuemenu.add(menu3);
                }
                controlmenu.add(msgqueuemenu);
                controlmenu.addSeparator();

                //======== dalogfiltermenu ========
                {
                    dalogfiltermenu.setText("Message Filter and Archive");

                    //======== severityfiltermenu ========
                    {
                        severityfiltermenu.setText("Severity");

                        //---- infoCheckBox ----
                        infoCheckBox.setSelected(true);
                        infoCheckBox.setFont(new Font(Font.DIALOG, Font.BOLD, 12));
                        infoCheckBox.setAction(action16);
                        severityfiltermenu.add(infoCheckBox);
                        severityfiltermenu.addSeparator();

                        //---- warningCheckBox ----
                        warningCheckBox.setSelected(true);
                        warningCheckBox.setFont(new Font(Font.DIALOG, Font.BOLD, 12));
                        warningCheckBox.setAction(action17);
                        severityfiltermenu.add(warningCheckBox);
                        severityfiltermenu.addSeparator();

                        //---- errorCheckBox ----
                        errorCheckBox.setSelected(true);
                        errorCheckBox.setFont(new Font(Font.DIALOG, Font.BOLD, 12));
                        errorCheckBox.setAction(action18);
                        severityfiltermenu.add(errorCheckBox);
                        severityfiltermenu.addSeparator();

                        //---- severeCheckBox ----
                        severeCheckBox.setSelected(true);
                        severeCheckBox.setFont(new Font(Font.DIALOG, Font.BOLD, 12));
                        severeCheckBox.setAction(action19);
                        severityfiltermenu.add(severeCheckBox);
                    }
                    dalogfiltermenu.add(severityfiltermenu);
                    dalogfiltermenu.addSeparator();

                    //---- menuItem6 ----
                    menuItem6.setAction(action15);
                    dalogfiltermenu.add(menuItem6);
                    dalogfiltermenu.addSeparator();

                    //---- menuItem7 ----
                    menuItem7.setAction(action20);
                    dalogfiltermenu.add(menuItem7);
                }
                controlmenu.add(dalogfiltermenu);
                controlmenu.addSeparator();

                //======== menu2 ========
                {
                    menu2.setText("Table");

                    //======== menu4 ========
                    {
                        menu4.setText("Clear");

                        //---- cleartablemenuItem ----
                        cleartablemenuItem.setAction(action10);
                        cleartablemenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
                        cleartablemenuItem.setText("Selection Table");
                        menu4.add(cleartablemenuItem);
                        menu4.addSeparator();

                        //---- menuItem3 ----
                        menuItem3.setAction(action11);
                        menuItem3.setText("daLog Table");
                        menuItem3.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_MASK));
                        menu4.add(menuItem3);
                    }
                    menu2.add(menu4);
                    menu2.addSeparator();

                    //======== menu5 ========
                    {
                        menu5.setText("Update");

                        //---- menuItem8 ----
                        menuItem8.setAction(action21);
                        menu5.add(menuItem8);
                        menu5.addSeparator();

                        //---- menuItem9 ----
                        menuItem9.setAction(action22);
                        menu5.add(menuItem9);
                        menu5.addSeparator();

                        //---- menuItem10 ----
                        menuItem10.setAction(action23);
                        menu5.add(menuItem10);
                        menu5.addSeparator();

                        //---- menuItem11 ----
                        menuItem11.setAction(action24);
                        menu5.add(menuItem11);
                    }
                    menu2.add(menu5);
                }
                controlmenu.add(menu2);
                controlmenu.addSeparator();

                //---- menuItem1 ----
                menuItem1.setAction(action12);
                menuItem1.setText("Enable dynamic subscription");
                controlmenu.add(menuItem1);
                controlmenu.addSeparator();

                //---- menuItem2 ----
                menuItem2.setAction(action7);
                menuItem2.setText("Disable dynamic subscription");
                controlmenu.add(menuItem2);
            }
            menuBar1.add(controlmenu);

            //======== helpmenu ========
            {
                helpmenu.setText("Help");

                //---- aboutmenuItem ----
                aboutmenuItem.setText("About...");
                helpmenu.add(aboutmenuItem);
            }
            menuBar1.add(helpmenu);
        }
        setJMenuBar(menuBar1);

        //======== panel1 ========
        {
            panel1.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));

            //======== toolBar3 ========
            {

                //---- statusbutton ----
                statusbutton.setAction(action4);
                statusbutton.setToolTipText("Status");
                statusbutton.setIcon(new ImageIcon(getClass().getResource("/resources/error-on.png")));
                statusbutton.setText("status");
                statusbutton.setForeground(new Color(0, 102, 102));
                statusbutton.setFont(new Font(Font.DIALOG, Font.PLAIN, 11));
                toolBar3.add(statusbutton);

                //---- pausebutton ----
                pausebutton.setAction(action5);
                pausebutton.setToolTipText("Pause");
                pausebutton.setIcon(new ImageIcon(getClass().getResource("/resources/warning-on.png")));
                pausebutton.setText("pause");
                pausebutton.setForeground(new Color(0, 102, 102));
                pausebutton.setFont(new Font(Font.DIALOG, Font.PLAIN, 11));
                toolBar3.add(pausebutton);

                //---- resumebutton ----
                resumebutton.setAction(action6);
                resumebutton.setToolTipText("Resume");
                resumebutton.setIcon(new ImageIcon(getClass().getResource("/resources/Play-Normal-16x16.png")));
                resumebutton.setText("resume");
                resumebutton.setForeground(new Color(0, 102, 102));
                resumebutton.setFont(new Font(Font.DIALOG, Font.PLAIN, 11));
                toolBar3.add(resumebutton);
            }

            //---- label1 ----
            label1.setText("Subject");
            label1.setForeground(new Color(0, 102, 102));
            label1.setFont(new Font(Font.DIALOG, Font.PLAIN, 11));

            //---- currentSubjectTextField ----
            currentSubjectTextField.setFont(new Font(Font.DIALOG, Font.PLAIN, 11));
            currentSubjectTextField.setForeground(new Color(153, 51, 0));
            currentSubjectTextField.setEditable(false);

            //---- label2 ----
            label2.setText("Type");
            label2.setForeground(new Color(0, 102, 102));
            label2.setFont(new Font(Font.DIALOG, Font.PLAIN, 11));

            //---- currentTypeTextField ----
            currentTypeTextField.setEditable(false);
            currentTypeTextField.setFont(new Font(Font.DIALOG, Font.PLAIN, 11));
            currentTypeTextField.setForeground(new Color(153, 51, 0));

            //======== tabbedPane1 ========
            {
                tabbedPane1.addTab("Selection", selectMsgScrollPane);

                //======== dalogMsgScrollPane ========
                {
                    dalogMsgScrollPane.setViewportView(dalogMsgT);
                }
                tabbedPane1.addTab("daLog", dalogMsgScrollPane);

                //======== messageSpaceScrollPane ========
                {
                    messageSpaceScrollPane.setViewportView(messageSpaceT);
                }
                tabbedPane1.addTab("Message Space", messageSpaceScrollPane);
            }

            GroupLayout panel1Layout = new GroupLayout(panel1);
            panel1.setLayout(panel1Layout);
            panel1Layout.setHorizontalGroup(
                panel1Layout.createParallelGroup()
                    .addComponent(toolBar3, GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(panel1Layout.createParallelGroup()
                            .addGroup(panel1Layout.createSequentialGroup()
                                .addComponent(label1)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(currentSubjectTextField, GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
                                .addGap(48, 48, 48)
                                .addComponent(label2)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(currentTypeTextField, GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE))
                            .addComponent(tabbedPane1, GroupLayout.DEFAULT_SIZE, 788, Short.MAX_VALUE))
                        .addContainerGap())
            );
            panel1Layout.setVerticalGroup(
                panel1Layout.createParallelGroup()
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addComponent(toolBar3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(label1)
                            .addComponent(currentTypeTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(label2)
                            .addComponent(currentSubjectTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tabbedPane1, GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
                        .addContainerGap())
            );
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(panel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap())
        );
        contentPaneLayout.setVerticalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addComponent(panel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap())
        );
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    /**
     * Based on the UI severity checkbox status updates MessageSeverityFilter array list,
     * to be used for filtering incoming dalog messages or entire message queue.
     */
    private void updateMessageSeverityAL() {
        if (infoCheckBox.isSelected() && !MessageSeverityFilter.contains("INFO")) {
            MessageSeverityFilter.add("INFO");
        } else if (!infoCheckBox.isSelected() && MessageSeverityFilter.contains("INFO")) {
            MessageSeverityFilter.remove("INFO");
        }

        if (warningCheckBox.isSelected() && !MessageSeverityFilter.contains("WARN")) {
            MessageSeverityFilter.add("WARN");
        } else if (!warningCheckBox.isSelected() && MessageSeverityFilter.contains("WARN")) {
            MessageSeverityFilter.remove("WARN");
        }

        if (errorCheckBox.isSelected() && !MessageSeverityFilter.contains("ERROR")) {
            MessageSeverityFilter.add("ERROR");
        } else if (!errorCheckBox.isSelected() && MessageSeverityFilter.contains("ERROR")) {
            MessageSeverityFilter.remove("ERROR");
        }

        if (severeCheckBox.isSelected() && !MessageSeverityFilter.contains("SEVERE")) {
            MessageSeverityFilter.add("SEVERE");
        } else if (!severeCheckBox.isSelected() && MessageSeverityFilter.contains("SEVERE")) {
            MessageSeverityFilter.remove("SEVERE");
        }

    }


    /**
     * Filters message based on message severity
     *
     * @param msg     message
     * @param sevList list of requested severity
     * @return true if passes the filter
     */
    private boolean filterMessage(cMsgMessage msg,
                                  java.util.List<String> sevList,
                                  String _sender,
                                  String _codaClass) {
        boolean b = false;
        if (sevList.isEmpty()) return true;
        try {

            // sender
            String sender = "undefined";
            if (_sender != null) {
                sender = msg.getSender();
            }

            // codaclass
            String codaClass = "undefined";
            if (_codaClass != null) {
                if (msg.getPayloadItem("codaClass") != null) codaClass = msg.getPayloadItem("codaClass").getString();
            }

            String severity = "undefined";
            if (msg.getPayloadItem("severity") != null) severity = msg.getPayloadItem("severity").getString();

            if (severity.equals("undefined") && sender.equals("undefined") && codaClass.equals("undefined")) {
                b = true;
            } else {
                if ((severity.equals("undefined") || sevList.contains(severity)) &&
                        (sender.equals("undefined") || sender.equals(_sender)) &&
                        (codaClass.equals("undefined") || codaClass.equals(_codaClass))
                ) {
                    b = true;
                }
            }
        } catch (cMsgException e) {
            e.printStackTrace();
        }
        return b;
    }

    /**
     * Filters message based on message severity
     *
     * @param msg     message
     * @param sevList list of requested severity
     * @return true if passes the filter
     */
    private void archive(cMsgMessage msg,
                         java.util.List<String> sevList,
                         String _sender) {
        if (sevList.isEmpty()) return;
        try {

            // sender
            String sender = "undefined";
            if (_sender != null) {
                sender = msg.getSender();
            }

            // codaclass
            String codaClass = "undefined";
            if (msg.getPayloadItem("codaClass") != null) codaClass = msg.getPayloadItem("codaClass").getString();

            // severity
            String severity = "undefined";
            if (msg.getPayloadItem("severity") != null) severity = msg.getPayloadItem("severity").getString();


            if (sevList.contains(severity) && sender.equals(_sender) && !archiveDir.equals("undefined")) {
                String dirName = archiveDir + File.separator + codaClass;
                // create a dir = codaClass if it does not exists
                File dir = new File(dirName);
                if (!dir.exists()) dir.mkdirs();
                BufferedWriter writer = new BufferedWriter(new FileWriter(dirName + File.separator
                        + severity.toLowerCase() + File.separator
                        + sender + ".cmb", true));
                writer.write(LocalDateTime.now().format(dateFormat));
                writer.write(msg.toString(false, true, true));
                writer.close();
            }
        } catch (cMsgException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * based on the integer returns string describing the severity level
     * on the dalog message
     *
     * @param si dalog message severity level
     * @return String representation of the severity
     */
    private String severityToString(int si) {
        String s = "RESERVE";
        if (si >= 1 && si <= 4) {
            s = "INFO";
        } else if (si >= 5 && si <= 8) {
            s = "WARNING";
        } else if (si >= 9 && si <= 12) {
            s = "ERROR";
        } else if (si >= 13 && si <= 14) {
            s = "SEVERE";
        }
        return s;
    }


    private void filterMessageQueue() {
        otherMsgTF.clearTable();
        dalogMsgTF.clearTable();
        // filter select messages
        for (cMsgMessage msg : selectMessageQueue) {
            if (filterMessage(msg, MessageSeverityFilter, sender, codaClass)) {
                showOtherMsg(otherTColumnNames, msg);
            }
        }

        // filter dalog messages
        for (cMsgMessage msg : dalogMessageQueue) {
            if (filterMessage(msg, MessageSeverityFilter, sender, codaClass)) {
                showDalogMsg(msg);
            }
        }
        // reset filters
//        sender = null;
//        codaClass = null;
    }

    private void showOtherMsg(ArrayList<String> titles, cMsgMessage msg) {
        ArrayList<String> al = new ArrayList<String>();
        if (titles.contains("Sender")) {
            al.add(msg.getSender());
        }

        if (titles.contains("Text")) {
            if (msg.getText() != null) {
                al.add(msg.getText());
            } else {
                al.add("NA");
            }
        }
        if (titles.contains("ByteArray")) {
            if (msg.getByteArray() != null) {
                al.add("ByteArray l=" + msg.getByteArray().length);
            } else {
                al.add("NA");
            }
        }
        for (cMsgPayloadItem pi : msg.getPayloadItems().values()) {
            if (!pi.getName().contains("cMsg")) {
                if (titles.contains(pi.getName())) {
                    if (msg.getPayloadItem(pi.getName()) != null) {
                        try {
                            switch (pi.getType()) {
                                case cMsgConstants.payloadInt8:
                                case cMsgConstants.payloadInt16:
                                case cMsgConstants.payloadInt32:
                                case cMsgConstants.payloadInt64:
                                    al.add(Integer.toString(msg.getPayloadItem(pi.getName()).getInt()));
                                    break;
                                case cMsgConstants.payloadFlt:
                                    al.add(Float.toString(msg.getPayloadItem(pi.getName()).getFloat()));
                                    break;
                                case cMsgConstants.payloadDbl:
                                    al.add(Double.toString(msg.getPayloadItem(pi.getName()).getDouble()));
                                    break;
                                case cMsgConstants.payloadStr:
                                    al.add(msg.getPayloadItem(pi.getName()).getString());
                                    break;
                                case cMsgConstants.payloadInt8A:
                                case cMsgConstants.payloadInt16A:
                                case cMsgConstants.payloadInt32A:
                                case cMsgConstants.payloadInt64A:
                                    al.add("IntArray l=" + msg.getPayloadItem(pi.getName()).getIntArray().length);
                                    break;
                                case cMsgConstants.payloadFltA:
                                    al.add("FloatArray l=" + msg.getPayloadItem(pi.getName()).getFloatArray().length);
                                    break;
                                case cMsgConstants.payloadDblA:
                                    al.add("DoubleArray l=" + msg.getPayloadItem(pi.getName()).getDoubleArray().length);
                                    break;
                                case cMsgConstants.payloadStrA:
                                    al.add("StringArray l=" + msg.getPayloadItem(pi.getName()).getStringArray().length);
                                    break;
                                case cMsgConstants.payloadBin:
                                    al.add("BinaryData");
                                    break;
                                case cMsgConstants.payloadBinA:
                                    al.add("BinaryArray l=" + msg.getPayloadItem(pi.getName()).getBinaryArray().length);
                                    break;
                            }
                        } catch (cMsgException e) {
                            e.printStackTrace();
                        }
                    } else {
                        al.add("NA");
                    }
                }
            }
        }
        if (!al.isEmpty()) {
            otherMsgTF.addData(al.toArray(new String[al.size()]));
        }
    }

    private void showDalogMsg(cMsgMessage msg) {
        String[] data = new String[8];
        try {
            data[0] = msg.getSender();
            if (msg.getPayloadItem("codaClass") != null) data[1] = msg.getPayloadItem("codaClass").getString();
            else data[1] = "undefined";

            if (msg.getPayloadItem("session") != null) data[2] = msg.getPayloadItem("session").getString();
            else data[2] = "undefined";

            if (msg.getPayloadItem("config") != null) data[3] = msg.getPayloadItem("config").getString();
            else data[3] = "undefined";

            if (msg.getPayloadItem("state") != null) data[4] = msg.getPayloadItem("state").getString();
            else data[4] = "undefined";

            if (msg.getPayloadItem("dalogText") != null) data[5] = msg.getPayloadItem("dalogText").getString();
            else data[5] = "undefined";

            if (msg.getPayloadItem("severity") != null) data[6] = msg.getPayloadItem("severity").getString();
            else data[6] = "undefined";

            if (msg.getPayloadItem("tod") != null) data[7] = msg.getPayloadItem("tod").getString();
            else data[7] = "undefined";

        } catch (cMsgException e) {
            e.printStackTrace();
        }

        dalogMsgTF.addData(data);
    }

    /**
     * Creates other than daLogMsg message table
     *
     * @param msg cMsgMessage
     * @return al ArrayList of payloadItem names.
     */
    private ArrayList<String> createOtherTable(cMsgMessage msg) {
        ArrayList<String> al = new ArrayList<String>();
        al.add("Sender");
        if (msg.getText() != null) {
            al.add("Text");
        }
        if (msg.getByteArray() != null) {
            al.add("ByteArray");
        }
        for (cMsgPayloadItem pi : msg.getPayloadItems().values()) {
            if (!pi.getName().contains("cMsg")) {
                al.add(pi.getName());
            }
        }
        otherMsgTF.clearTable();
        if (msg.getType().equals("rc/report/dalg")) {
            otherMsgT = otherMsgTF.createDalogTable();
        } else {
            otherMsgT = otherMsgTF.createOtherMsgTable(al);
        }
        return al;
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JMenuBar menuBar1;
    private JMenu platformmenu;
    private JMenuItem connectmenuItem;
    private JMenuItem disconnectmenuItem;
    private JMenuItem exitmenuItem;
    private JMenu controlmenu;
    private JMenu msgqueuemenu;
    private JMenu menu1;
    private JMenuItem msgqclearmenuItem;
    private JMenuItem setmsgqsizemenuItem;
    private JMenu menu3;
    private JMenuItem menuItem4;
    private JMenuItem menuItem5;
    private JMenu dalogfiltermenu;
    private JMenu severityfiltermenu;
    private JCheckBox infoCheckBox;
    private JCheckBox warningCheckBox;
    private JCheckBox errorCheckBox;
    private JCheckBox severeCheckBox;
    private JMenuItem menuItem6;
    private JMenuItem menuItem7;
    private JMenu menu2;
    private JMenu menu4;
    private JMenuItem cleartablemenuItem;
    private JMenuItem menuItem3;
    private JMenu menu5;
    private JMenuItem menuItem8;
    private JMenuItem menuItem9;
    private JMenuItem menuItem10;
    private JMenuItem menuItem11;
    private JMenuItem menuItem1;
    private JMenuItem menuItem2;
    private JMenu helpmenu;
    private JMenuItem aboutmenuItem;
    private JPanel panel1;
    private JToolBar toolBar3;
    private JButton statusbutton;
    private JButton pausebutton;
    private JButton resumebutton;
    private JLabel label1;
    private JTextField currentSubjectTextField;
    private JLabel label2;
    private JTextField currentTypeTextField;
    private JTabbedPane tabbedPane1;
    private JScrollPane selectMsgScrollPane;
    private JScrollPane dalogMsgScrollPane;
    private JScrollPane messageSpaceScrollPane;
    private ConnectAction action1;
    private DisconnectAction action2;
    private ExitAction action3;
    private StatusAction action4;
    private PauseAction action5;
    private ResumeAction action6;
    private SeverityInfoFilterAction action16;
    private SeverityWarningFilterAction action17;
    private SeveritErrorFilterAction action18;
    private SeveritySevereFilterAction action19;
    private SetMsgQueSize action8;
    private ClearMsgQue action9;
    private ClearTableAction action10;
    private EnableMessageSelection action12;
    private DisableMsgSelectionAction action7;
    private ClearDalogTableAction action11;
    private ClearDalogMsgQueue action13;
    private SetDalogMsgQueueSize action14;
    private CodaClassFilter action15;
    private MessageAuthorFilter action20;
    private StopUpdateSelection action21;
    private ResumeSelectionUpdate action22;
    private StopDalogUpdate action23;
    private ResumeDalogUpdate action24;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    /******************************************************************
     * MessageSpaceCallBack
     */
    class MessageSpaceCallback extends cMsgCallbackAdapter {

        @Override
        public void callback(cMsgMessage msg, Object userObject) {
            if (isUpdateActive.get()) {

                ArrayList<String> al = new ArrayList<>();

                al.add(msg.getSubject());
                al.add(msg.getType());
                al.add(msg.getSender());
                if (msg.getText() != null) {
                    al.add(msg.getText());
                } else {
                    al.add("NA");
                }
                if (msg.getByteArray() != null) {
                    al.add("Length = " + Integer.toString(msg.getByteArrayLength()));
                } else {
                    al.add("NA");
                }
                int j = 0;
                for (String s : msg.getPayloadNames()) {
                    if (!s.contains("cMsg")) {
                        j++;
                    }
                }
                if (j > 0) {
                    al.add("Count = " + Integer.toString(j));
                } else {
                    al.add("NA");
                }

                if (!al.isEmpty()) {
                    // show in the message space table
                    messageSpaceTF.updateMessageSpaceTable(al.toArray(new String[al.size()]));
                }
            }
        }
    }

    /******************************************************************
     * SelectedMessageCallBack
     */
    class SelectedMessageCallback extends cMsgCallbackAdapter {

        @Override
        public void callback(cMsgMessage msg, Object userObject) {
            // create and deploy other msgTable and show other messages
            if (createOT || otherTColumnNames.isEmpty()) {
                createOT = false;
                isSelectionUpdate = true;
                otherTColumnNames = createOtherTable(msg);
                // deploy other Msg table
                selectMsgScrollPane.setViewportView(otherMsgT);
            }
            if (isSelectionUpdate) {
                if (isUpdateActive.get()) {
                    if (filterMessage(msg, MessageSeverityFilter, sender, codaClass)) {
                        showOtherMsg(otherTColumnNames, msg);
                    }

                }

                // add to the message queue
                selectMessageQueue.add(msg);
                if (selectMessageQueue.size() > queSize) {
                    selectMessageQueue.poll();
                }
            }
        }
    }


    /******************************************************************
     * DalogMessageCallBack
     */
    class DalogMessageCallback extends cMsgCallbackAdapter {

        @Override
        public void callback(cMsgMessage msg, Object userObject) {
            if (isSelectionUpdate) {
                if (isDalogUpdate) {
                    if (isUpdateActive.get()) {
                        if (filterMessage(msg, MessageSeverityFilter, sender, codaClass)) {
                            showDalogMsg(msg);
                        }
                    }
                }

                // add to the message queue
                dalogMessageQueue.add(msg);
                if (dalogMessageQueue.size() > dalogMsgQueSize) {
                    dalogMessageQueue.poll();
                }

                // archive filtered data
                archive(msg, MessageSeverityFilter, sender);

            }
        }
    }


    private class ConnectAction extends AbstractAction {
        private ConnectAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Connect");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            new PlatformConnectDialog(me).setVisible(true);
        }
    }

    private class DisconnectAction extends AbstractAction {
        private DisconnectAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Disconnect");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            if (myPlatformConnection != null && myPlatformConnection.isConnected()) {
                if ((JOptionPane.showConfirmDialog(me, "Are you sure ?", "Confirmation",
                        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION)) {
                    plDisconnect();
                    currentSubject = "undefined";
                    currentType = "undefined";
                }
            }
        }
    }

    private class ExitAction extends AbstractAction {
        private ExitAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Exit");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            plDisconnect();
            me.dispose();
            System.exit(1);
        }
    }

    private class StatusAction extends AbstractAction {
        private StatusAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "status");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            if (myPlatformConnection != null && myPlatformConnection.isConnected()) {

                String cs = "undefined";
                String ma = "undefined";
                if (codaClass != null) cs = codaClass;
                if (sender != null) ma = sender;
                JOptionPane.showMessageDialog(me, "Connected to: \n" + plUdl +
                        "\n\nShowing messages: " +
                        "\nUser selected subject = " + currentSubject +
                        "\nUser selected type     = " + currentType +
                        "\nUpdating                   = " + isUpdateActive.get() +
                        "\n\nSelect: \n" + MessageSeverityFilter +
                        "\nCoda type                 = " + cs +
                        "\nMessage author        = " + ma
                );
            } else {
                JOptionPane.showMessageDialog(me, "Disconnected.", "Status", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private class PauseAction extends AbstractAction {
        private PauseAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "pause");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            isUpdateActive.set(false);
            pausebutton.setIcon(new ImageIcon(getClass().getResource("/resources/unselected.png")));
        }
    }

    private class ResumeAction extends AbstractAction {
        private ResumeAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "resume");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            if (myPlatformConnection != null && myPlatformConnection.isConnected()) {
                isUpdateActive.set(true);
                pausebutton.setIcon(new ImageIcon(getClass().getResource("/resources/warning-on.png")));
            }
        }
    }

    /**
     * Inner class responsible for listening selections of the message space table
     */
    public class MessageSpaceTSelectionListener implements ListSelectionListener {
        JTable table;

        // It is necessary to keep the table since it is not possible
        //to determine the table from the event's source
        MessageSpaceTSelectionListener(JTable table) {
            this.table = table;
        }

        public void valueChanged(ListSelectionEvent e) {
            if (_selectionOn) {
                String subject;
                String type;
                int rowIndex = table.getSelectedRow();
                int colIndex = table.getSelectedColumn();
                if (rowIndex >= 0 && colIndex >= 0) {
                    subject = (String) table.getValueAt(rowIndex, 0);
                    type = (String) table.getValueAt(rowIndex, 1);
                    if (colIndex == 0) {
                        type = "*";
                    } else if (colIndex == 1) {
                        subject = "*";
                    }

                    if (!currentSubject.equals(subject) || !currentType.equals(type)) {
                        if ((JOptionPane.showConfirmDialog(me,
                                "Show messages ? \n\nsubject =  " + subject + "\ntype      =  " + type + "\n",
                                "Message Selection",
                                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION)) {
                            currentSubject = subject;
                            currentType = type;
                            otherMsgTF.clearTable();
                            selectMsgScrollPane.setViewportView(otherMsgT);
                            createOT = true;

                            currentSubjectTextField.setText(currentSubject);
                            currentTypeTextField.setText((currentType));

                            // subscribe to the selected subject and type
                            try {
                                if (selectedMsgSubscription != null) {
                                    myPlatformConnection.unsubscribe(selectedMsgSubscription);
                                }
                                selectedMsgSubscription = myPlatformConnection.subscribe(currentSubject,
                                        currentType,
                                        new SelectedMessageCallback(),
                                        null);
                            } catch (cMsgException e1) {
                                e1.printStackTrace();
                            }
                            selectMessageQueue.clear();
                        }
                    }
                }
            }
        }
    }

    private class SeverityInfoFilterAction extends AbstractAction {
        private SeverityInfoFilterAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Info");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            updateMessageSeverityAL();
            filterMessageQueue();
        }
    }

    private class SeverityWarningFilterAction extends AbstractAction {
        private SeverityWarningFilterAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Warning");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            updateMessageSeverityAL();
            filterMessageQueue();
        }
    }

    private class SeveritErrorFilterAction extends AbstractAction {
        private SeveritErrorFilterAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Error      ");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            updateMessageSeverityAL();
            filterMessageQueue();
        }
    }

    private class SeveritySevereFilterAction extends AbstractAction {
        private SeveritySevereFilterAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Severe   ");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            updateMessageSeverityAL();
            filterMessageQueue();
        }
    }

    private class SetMsgQueSize extends AbstractAction {
        private SetMsgQueSize() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Set Size");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            String s = JOptionPane.showInputDialog("Enter message queue size", queSize);
            try {
                queSize = Integer.parseInt(s);
                JOptionPane.showMessageDialog(me, "Message queue size is changed to be = " + queSize);
            } catch (NumberFormatException e1) {
                JOptionPane.showMessageDialog(me, "Wrong integer. Message queue size = " + queSize);
            }
        }
    }

    private class ClearMsgQue extends AbstractAction {
        private ClearMsgQue() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Reset");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            if (!selectMessageQueue.isEmpty()) {
                if ((JOptionPane.showConfirmDialog(me, "Are you sure ?", "Confirmation",
                        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION)) {
                    selectMessageQueue.clear();
                }
            }
        }
    }

    private class ClearTableAction extends AbstractAction {
        private ClearTableAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Selection Table");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            if ((JOptionPane.showConfirmDialog(me, "Are you sure ?", "Confirmation",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION)) {
                otherMsgTF.clearTable();
            }
        }
    }


    private class EnableMessageSelection extends AbstractAction {
        private EnableMessageSelection() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Enable msg selection");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            setSelectioEnabled(true);
        }
    }

    private class DisableMsgSelectionAction extends AbstractAction {
        private DisableMsgSelectionAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Disable msg selection");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            setSelectioEnabled(false);
        }
    }

    private class ClearDalogTableAction extends AbstractAction {
        private ClearDalogTableAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "daLog Table");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            if ((JOptionPane.showConfirmDialog(me, "Are you sure ?", "Confirmation",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION)) {
                dalogMsgTF.clearTable();
            }
        }
    }

    private class ClearDalogMsgQueue extends AbstractAction {
        private ClearDalogMsgQueue() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Reset");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            if (!selectMessageQueue.isEmpty()) {
                if ((JOptionPane.showConfirmDialog(me, "Are you sure ?", "Confirmation",
                        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION)) {
                    dalogMessageQueue.clear();
                }
            }
        }
    }

    private class SetDalogMsgQueueSize extends AbstractAction {
        private SetDalogMsgQueueSize() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Set Size");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            String s = JOptionPane.showInputDialog("Enter daLog message queue size", dalogMsgQueSize);
            try {
                dalogMsgQueSize = Integer.parseInt(s);
                JOptionPane.showMessageDialog(me, "daLog message queue size is changed to be = " + dalogMsgQueSize);
            } catch (NumberFormatException e1) {
                JOptionPane.showMessageDialog(me, "Wrong integer. daLog message queue size = " + dalogMsgQueSize);
            }
        }
    }

    private class CodaClassFilter extends AbstractAction {
        private CodaClassFilter() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Coda Component Type");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            String s = JOptionPane.showInputDialog("Enter the type of the Coda component", codaClass);
            if (s != null && !s.equals("") && !s.trim().contains(" ")) {
                codaClass = s;
            } else {
                codaClass = null;
            }
            filterMessageQueue();
        }
    }

    private class MessageAuthorFilter extends AbstractAction {
        private MessageAuthorFilter() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Message Author");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            String s = JOptionPane.showInputDialog("Enter the name of the Coda component", sender);
            if (s != null && !s.equals("") && !s.trim().contains(" ")) {
                sender = s;
            } else {
                sender = null;
            }
            filterMessageQueue();
        }
    }

    private class StopUpdateSelection extends AbstractAction {
        private StopUpdateSelection() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Stop Selection ");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.SHIFT_MASK));
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            isSelectionUpdate = false;
        }
    }

    private class ResumeSelectionUpdate extends AbstractAction {
        private ResumeSelectionUpdate() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Resume Selection");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK|KeyEvent.SHIFT_MASK));
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            isSelectionUpdate = true;
        }
    }

    private class StopDalogUpdate extends AbstractAction {
        private StopDalogUpdate() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Stop daLog");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.SHIFT_MASK));
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            isDalogUpdate = false;
        }
    }

    private class ResumeDalogUpdate extends AbstractAction {
        private ResumeDalogUpdate() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Resume daLog");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_MASK|KeyEvent.SHIFT_MASK));
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            isDalogUpdate = true;
        }
    }
}

