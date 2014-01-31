package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

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

        Box box = Box.createHorizontalBox();
        box.add(check); box.add(Box.createHorizontalGlue());

        add(panel);
        add(box, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }

    private void layoutComboBoxPanel(JPanel p2, List<JComboBox> list) {
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
            c.gridy   = i; p2.add(new JLabel(String.format("%d:", i)), c);
            c.gridx   = 1;
            c.weightx = 1.0;
            c.fill    = GridBagConstraints.HORIZONTAL;
            c.gridy   = i; p2.add(list.get(i), c);
        }
        p2.revalidate(); //??? JDK 1.7.0 Nimbus ???
    }

    private List<JComboBox> initComboBoxes(boolean isColor) {
//         if(uiCheck.isSelected()) {
//             // Bug ID: JDK-7158712 Synth Property "ComboBox.popupInsets" is ignored
//             // http://bugs.sun.com/view_bug.do?bug_id=7158712
//             UIManager.put("ComboBox.padding", new javax.swing.plaf.InsetsUIResource(1,15,1,1));
//         }
        List<JComboBox> list = new ArrayList<>();
        for(int i=0;i<7;i++) {
            list.add(makeComboBox());
        }
        JComboBox combo;
        JTextField editor;

        // ---- 00 ----
        combo = list.get(0);
        combo.setEditable(false);
        combo.setToolTipText("combo.setEditable(false);");

        // ---- 01 ----
        combo = list.get(1);
        combo.setEditable(true);
        editor = (JTextField)combo.getEditor().getEditorComponent();
        editor.setBorder(BorderFactory.createCompoundBorder(editor.getBorder(), getPaddingBorder(isColor)));
        combo.setToolTipText("editor.setBorder(BorderFactory.createCompoundBorder(editor.getBorder(), padding));");

        // ---- 02 ----
        combo = list.get(2);
        combo.setEditable(true);
        editor = (JTextField)combo.getEditor().getEditorComponent();
        editor.setBorder(getPaddingBorder(isColor));
        combo.setToolTipText("editor.setBorder(padding);");

        // ---- 03 ----
        combo = list.get(3);
        combo.setEditable(true);
        editor = (JTextField)combo.getEditor().getEditorComponent();
        Insets i = editor.getInsets();
        editor.setMargin(new Insets(i.top,i.left+5,i.bottom,i.right));
        combo.setToolTipText("Insets i = editor.getInsets(); editor.setMargin(new Insets(i.top,i.left+5,i.bottom,i.right));");

        // ---- 04 ----
        combo = list.get(4);
        combo.setEditable(true);
        editor = (JTextField)combo.getEditor().getEditorComponent();
        Insets m = editor.getMargin();
        editor.setMargin(new Insets(m.top,m.left+5,m.bottom,m.right));
        combo.setToolTipText("Insets m = editor.getMargin(); editor.setMargin(new Insets(m.top,m.left+5,m.bottom,m.right));");

        // ---- 05 ----
        combo = list.get(5);
        combo.setEditable(true);
        combo.setBorder(BorderFactory.createCompoundBorder(combo.getBorder(), getPaddingBorder(isColor)));
        combo.setToolTipText("combo.setBorder(BorderFactory.createCompoundBorder(combo.getBorder(), padding));");

        // ---- 06 ----
        combo = list.get(6);
        combo.setEditable(true);
        combo.setBorder(BorderFactory.createCompoundBorder(getPaddingBorder(isColor), combo.getBorder()));
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

    private static Border getPaddingBorder(boolean isColor) {
        return isColor ? BorderFactory.createMatteBorder(0,5,0,0,new Color(1f,.8f,.8f,.5f))
                       : BorderFactory.createEmptyBorder(0,5,0,0);
    }

    private static JComboBox<String> makeComboBox() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("aaaaaaaaaaaaaaaaaaaaaaaaa");
        model.addElement("aaaabbb");
        model.addElement("aaaabbbcc");
        model.addElement("bbb1");
        model.addElement("bbb12");

        JComboBox<String> combo = new JComboBox<String>(model) {
            @Override public void updateUI() {
                setRenderer(null);
                super.updateUI();
                final ListCellRenderer<? super String> lcr = getRenderer();
                setRenderer(new ListCellRenderer<String>() {
                    @Override public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean hasFocus) {
                        JLabel l = (JLabel)lcr.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
                        l.setBorder(getPaddingBorder(false));
                        return l;
                    }
                });
                //???: UIManager.put("ComboBox.editorBorder", BorderFactory.createEmptyBorder(0,5,0,0));
                //???: ((JLabel)lcr).setBorder(getPaddingBorder(false));
            }
        };
        return combo;
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
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JMenuBar mb = new JMenuBar();
        mb.add(LookAndFeelUtil.createLookAndFeelMenu());

        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.setJMenuBar(mb);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

//http://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java
class LookAndFeelUtil {
    private static String lookAndFeel;
    private LookAndFeelUtil() {}
    public static JMenu createLookAndFeelMenu() {
        JMenu menu = new JMenu("LookAndFeel");
        lookAndFeel = UIManager.getLookAndFeel().getClass().getName();
        ButtonGroup lookAndFeelRadioGroup = new ButtonGroup();
        for(UIManager.LookAndFeelInfo lafInfo: UIManager.getInstalledLookAndFeels()) {
            menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lookAndFeelRadioGroup));
        }
        return menu;
    }
    private static JRadioButtonMenuItem createLookAndFeelItem(String lafName, String lafClassName, final ButtonGroup lookAndFeelRadioGroup) {
        JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem();
        lafItem.setSelected(lafClassName.equals(lookAndFeel));
        lafItem.setHideActionText(true);
        lafItem.setAction(new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                ButtonModel m = lookAndFeelRadioGroup.getSelection();
                try{
                    setLookAndFeel(m.getActionCommand());
                }catch(ClassNotFoundException | InstantiationException |
                       IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }
            }
        });
        lafItem.setText(lafName);
        lafItem.setActionCommand(lafClassName);
        lookAndFeelRadioGroup.add(lafItem);
        return lafItem;
    }
    private static void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        String oldLookAndFeel = LookAndFeelUtil.lookAndFeel;
        if(!oldLookAndFeel.equals(lookAndFeel)) {
            UIManager.setLookAndFeel(lookAndFeel);
            LookAndFeelUtil.lookAndFeel = lookAndFeel;
            updateLookAndFeel();
            //firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
        }
    }
    private static void updateLookAndFeel() {
        for(Window window: Frame.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }
}
