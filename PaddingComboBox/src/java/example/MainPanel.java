package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.synth.*;

public class MainPanel extends JPanel {
    private final JPanel panel = new JPanel();
    private final JCheckBox check = new JCheckBox("color");

    public MainPanel() {
        super(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        check.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                layoutComboBoxPanel(panel, initComboBoxes(check.isSelected()));
                panel.revalidate();
            }
        });
        layoutComboBoxPanel(panel, initComboBoxes(check.isSelected()));

        JMenuBar mb = new JMenuBar();
        mb.add(createLookAndFeelMenu());

        Box box = Box.createHorizontalBox();
        box.add(check); box.add(Box.createHorizontalGlue());

        add(mb, BorderLayout.NORTH);
        add(panel);
        add(box, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 320));
    }

    private void layoutComboBoxPanel(JPanel p2, ArrayList<JComboBox> list) {
        p2.removeAll();
        p2.setLayout(new GridBagLayout());
        Border inside  = BorderFactory.createEmptyBorder(10,5+2,10,10+2);
        Border outside = BorderFactory.createTitledBorder("JComboBox Padding Test");
        p2.setBorder(BorderFactory.createCompoundBorder(outside, inside));
        for(int i=0;i<list.size();i++) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridheight = 1;
            c.gridx   = 0;
            c.insets  = new Insets(5, 5, 5, 0);
            c.anchor  = GridBagConstraints.WEST;
            c.gridy   = i; p2.add(new JLabel(""+i+":"), c);
            c.gridx   = 1;
            c.weightx = 1.0;
            c.fill    = GridBagConstraints.HORIZONTAL;
            c.gridy   = i; p2.add(list.get(i), c);
        }
    }

    private ArrayList<JComboBox> initComboBoxes(boolean isColor) {
//         if(uiCheck.isSelected()) {
//             UIManager.put("ComboBox.padding", new javax.swing.plaf.InsetsUIResource(1,15,1,1));
//         }
        ArrayList<JComboBox> list = new ArrayList<JComboBox>();
        for(int i=0;i<7;i++) {
            list.add(new JComboBox(makeModel()));
        }
        Border padding = isColor
            ? BorderFactory.createMatteBorder(0,5,0,0,new Color(1f,.8f,.8f,.5f))
            : BorderFactory.createEmptyBorder(0,5,0,0);
        ListCellRenderer lcr = list.get(0).getRenderer(); ((JLabel)lcr).setBorder(padding);
        JComboBox combo;
        JTextField editor;

        // ---- 00 ----
        combo = list.get(0);
        combo.setEditable(false);
        combo.setRenderer(lcr);
        combo.setToolTipText("combo.setEditable(false);");

        // ---- 01 ----
        combo = list.get(1);
        combo.setEditable(true);
        combo.setRenderer(lcr);
        editor = (JTextField)combo.getEditor().getEditorComponent();
        editor.setBorder(BorderFactory.createCompoundBorder(editor.getBorder(), padding));
        combo.setToolTipText("editor.setBorder(BorderFactory.createCompoundBorder(editor.getBorder(), padding));");

        // ---- 02 ----
        combo = list.get(2);
        combo.setEditable(true);
        combo.setRenderer(lcr);
        editor = (JTextField)combo.getEditor().getEditorComponent();
        editor.setBorder(padding);
        combo.setToolTipText("editor.setBorder(padding);");

        // ---- 03 ----
        combo = list.get(3);
        combo.setEditable(true);
        combo.setRenderer(lcr);
        editor = (JTextField)combo.getEditor().getEditorComponent();
        Insets i = editor.getInsets();
        editor.setMargin(new Insets(i.top,i.left+5,i.bottom,i.right));
        combo.setToolTipText("Insets i = editor.getInsets(); editor.setMargin(new Insets(i.top,i.left+5,i.bottom,i.right));");

        // ---- 04 ----
        combo = list.get(4);
        combo.setEditable(true);
        combo.setRenderer(lcr);
        editor = (JTextField)combo.getEditor().getEditorComponent();
        Insets m = editor.getMargin();
        editor.setMargin(new Insets(m.top,m.left+5,m.bottom,m.right));
        combo.setToolTipText("Insets m = editor.getMargin(); editor.setMargin(new Insets(m.top,m.left+5,m.bottom,m.right));");

        // ---- 05 ----
        combo = list.get(5);
        combo.setEditable(true);
        combo.setRenderer(lcr);
        combo.setBorder(BorderFactory.createCompoundBorder(combo.getBorder(), padding));
        combo.setToolTipText("combo.setBorder(BorderFactory.createCompoundBorder(combo.getBorder(), padding));");

        // ---- 06 ----
        combo = list.get(6);
        combo.setEditable(true);
        combo.setRenderer(lcr);
        combo.setBorder(BorderFactory.createCompoundBorder(padding, combo.getBorder()));
        combo.setToolTipText("combo.setBorder(BorderFactory.createCompoundBorder(padding, combo.getBorder()));");

        if(isColor) {
            Color c = new Color(.8f,1f,.8f);
            for(JComboBox cb:list) {
                cb.setOpaque(true);
                cb.setBackground(c);
                editor = (JTextField)cb.getEditor().getEditorComponent();
                editor.setOpaque(true);
                editor.setBackground(c);
            }
        }
        return list;
    }

    //<blockquote cite="https://swingset3.dev.java.net/svn/swingset3/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java">
    private ButtonGroup lookAndFeelRadioGroup;
    private String lookAndFeel;
    protected JMenu createLookAndFeelMenu() {
        JMenu menu = new JMenu("LookAndFeel");
        lookAndFeel = UIManager.getLookAndFeel().getClass().getName();
        lookAndFeelRadioGroup = new ButtonGroup();
        for(UIManager.LookAndFeelInfo lafInfo: UIManager.getInstalledLookAndFeels()) {
            menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName()));
        }
        return menu;
    }
    protected JRadioButtonMenuItem createLookAndFeelItem(String lafName, String lafClassName) {
        JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem();
        lafItem.setSelected(lafClassName.equals(lookAndFeel));
        lafItem.setHideActionText(true);
        lafItem.setAction(new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                ButtonModel m = lookAndFeelRadioGroup.getSelection();
                try{
                    setLookAndFeel(m.getActionCommand());
                }catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        lafItem.setText(lafName);
        lafItem.setActionCommand(lafClassName);
        lookAndFeelRadioGroup.add(lafItem);
        return lafItem;
    }
    public void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        String oldLookAndFeel = this.lookAndFeel;
        if(!oldLookAndFeel.equals(lookAndFeel)) {
            UIManager.setLookAndFeel(lookAndFeel);
            this.lookAndFeel = lookAndFeel;
            updateLookAndFeel();
            firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
        }
    }
    private void updateLookAndFeel() {
        for(Window window: Frame.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }
    //</blockquote>

    private static DefaultComboBoxModel makeModel() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement("aaaaaaaaaaaaaaaaaaaaaaaaa");
        model.addElement("aaaabbb");
        model.addElement("aaaabbbcc");
        model.addElement("bbb1");
        model.addElement("bbb12");
        return model;
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

// package example;
// //-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
// //@homepage@
// import java.awt.*;
// import javax.swing.*;
// import javax.swing.border.*;
//
// class MainPanel extends JPanel {
//     private final JComboBox combo00 = new JComboBox(makeModel());
//     private final JComboBox combo01 = new JComboBox(makeModel());
//     private final JComboBox combo02 = new JComboBox(makeModel());
//     //private final Border padding = BorderFactory.createEmptyBorder(0,5,0,0);
//     private final Border padding = BorderFactory.createMatteBorder(0,5,0,0,new Color(1f,.8f,.8f,.5f));
//
//     public MainPanel() {
//         super(new BorderLayout());
//
//         final ListCellRenderer lcr = combo00.getRenderer();
//         ((JLabel)lcr).setBorder(padding);
//
//         combo00.setRenderer(lcr);
//         combo01.setRenderer(lcr);
//         combo02.setRenderer(lcr);
//
//         combo00.setEditable(false);
//         combo01.setEditable(true);
//         combo02.setEditable(true);
//
//         JTextField editor = (JTextField) combo01.getEditor().getEditorComponent();
//         editor.setBorder(padding);
//         editor.setOpaque(true);
//         //editor.setOpaque(false);
//         //editor.setBackground(new Color(.8f,1f,.8f,.5f)); // NG
//         editor.setBackground(new Color(.8f,1f,.8f));
//
//         combo02.setBorder(BorderFactory.createCompoundBorder(combo02.getBorder(), padding));
//         //combo02.setOpaque(true);
//         //combo02.setBackground(new Color(.9f,1f,.9f));
//
//         Box box = Box.createVerticalBox();
//         box.add(combo00);
//         box.add(Box.createVerticalStrut(5));
//         box.add(combo01);
//         box.add(Box.createVerticalStrut(5));
//         box.add(combo02);
//         box.add(Box.createRigidArea(new Dimension(320,0)));
//
//         setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
//         add(box);
//     }
//
//     private static DefaultComboBoxModel makeModel() {
//         DefaultComboBoxModel model = new DefaultComboBoxModel();
//         model.addElement("aaaa");
//         model.addElement("aaaabbb");
//         model.addElement("aaaabbbcc");
//         model.addElement("asdfasdfasdfasdfasdf");
//         model.addElement("bbb1");
//         model.addElement("bbb12");
//         return model;
//     }
//     public static void main(String[] args) {
//         EventQueue.invokeLater(new Runnable() {
//             public void run() {
//                 createAndShowGUI();
//             }
//         });
//     }
//     public static void createAndShowGUI() {
//         try{
//             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//         }catch(Exception e) {
//             e.printStackTrace();
//         }
//         JFrame frame = new JFrame("@title@");
//         frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//         frame.getContentPane().add(new MainPanel());
//         frame.pack();
//         frame.setResizable(false);
//         frame.setLocationRelativeTo(null);
//         frame.setVisible(true);
//     }
// }
