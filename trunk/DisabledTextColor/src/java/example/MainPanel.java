package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class MainPanel extends JPanel {
// RadioButton.disabledText
// ComboBox.disabledForeground
// ComboBox.disabledBackground
// Label.disabledForeground
// CheckBoxMenuItem.disabledForeground
// CheckBox.disabledText
// Label.disabledShadow
// ToggleButton.disabledText
// RadioButtonMenuItem.disabledForeground
// Button.disabledToolBarBorderBackground
// Menu.disabledForeground
// MenuItem.disabledForeground
// Button.disabledText
    private final JCheckBox cbx1   = new JCheckBox("default", true);
    private final JCheckBox cbx2   = new JCheckBox("<html>html tag</html>", true);
    private final JComboBox combo1 = new JComboBox(new String[] {"disabledForeground", "bb"});
    private final JComboBox combo2 = new JComboBox(new String[] {"<html>html</html>", "renderer"});
    private final JComboBox combo3 = new JComboBox(new String[] {"setEditable(true)", "setDisabledTextColor"});
    private final JLabel label     = new JLabel("label disabledForeground");
    private final JButton button   = new JButton("button disabledText");
    private final java.util.List<JComponent> list = Arrays.<JComponent>asList(cbx1, cbx2,
                                                             combo1, combo2, combo3,
                                                             label, button);
    public MainPanel() {
        super(new BorderLayout());
        UIManager.put("CheckBox.disabledText", Color.RED);
        UIManager.put("ComboBox.disabledForeground", Color.GREEN);
        UIManager.put("Button.disabledText", Color.YELLOW);
        UIManager.put("Label.disabledForeground", Color.ORANGE);
        final JCheckBox cbx = new JCheckBox(new AbstractAction("setEnabled") {
            @Override public void actionPerformed(ActionEvent e) {
                JCheckBox source = (JCheckBox)e.getSource();
                boolean flg = source.isSelected();
                for(JComponent c:list) {
                    c.setEnabled(flg);
                }
            }
        });
        final ListCellRenderer r = combo2.getRenderer();
        combo2.setRenderer(new ListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                Component c = r.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                if(index==-1 && !combo1.isEnabled()) {
                    JLabel l = (JLabel)c;
                    l.setText("<html><font color='red'>"+l.getText());
                    //c.setForeground(Color.RED);
                }
                return c;
            }
        });
        //combo2.setEditable(true);

        combo3.setEditable(true);
        JTextField editor = (JTextField)combo3.getEditor().getEditorComponent();
        editor.setDisabledTextColor(Color.PINK);

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5,15,5,5));
        for(JComponent c:list) {
            c.setEnabled(false);
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            int h = c.getPreferredSize().height;
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
            box.add(c);
            box.add(Box.createVerticalStrut(5));
        }
        box.add(Box.createVerticalGlue());
        add(cbx, BorderLayout.NORTH);
        add(box);
        setPreferredSize(new Dimension(320, 240));
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
        frame.setMinimumSize(new Dimension(256, 100));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
