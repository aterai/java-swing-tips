package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel{
    private final JComboBox combo = new JComboBox();
    private final JTextField leftTextField  = new JTextField();
    private final JTextField rightTextField = new JTextField();
    public MainPanel() {
        super(new BorderLayout());
        leftTextField.setEditable(false);
        rightTextField.setEditable(false);
        combo.setModel(makeModel());
        combo.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()==ItemEvent.SELECTED) {
                    initTextField(e.getItem());
                }
            }
        });
        initTextField(combo.getSelectedItem());

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        box.add(leftTextField);
        box.add(Box.createVerticalStrut(2));
        box.add(rightTextField);
        box.add(Box.createVerticalStrut(5));
        box.add(combo);
        add(box, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 200));
    }
    private void initTextField(Object obj) {
        LRItem item = (LRItem)obj;
        leftTextField.setText(item.getLeftText());
        rightTextField.setText(item.getRightText());
    }
    static class LRItem{
        private final String leftText;
        private final String rightText;
        public LRItem(String strLeft, String strRight) {
            leftText = strLeft;
            rightText = strRight;
        }
        public String getHtmlText() {
            String str = "<html><table width='290'><tr><td align='left'>" + leftText;
            str = str + "</td><td align='right'>" + rightText + "</td></tr></table></html>";
            return str;
        }
        public String getLeftText() {
            return leftText;
        }
        public String getRightText() {
            return rightText;
        }
        public String toString() {
            return getHtmlText();
        }
    }
    private static DefaultComboBoxModel makeModel() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement(new LRItem("asdfasdf", "846876"));
        model.addElement(new LRItem("bxcvzx", "asdfasd"));
        model.addElement(new LRItem("qwerqwe", "iop.ioqqadfa"));
        model.addElement(new LRItem("14234125", "64345424684"));
        model.addElement(new LRItem("hjklhjk", "asdfasdfasdfasdfasdfasd"));
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
