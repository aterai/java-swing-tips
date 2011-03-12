package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel{
    private final JComboBox combo1 = new JComboBox();
    private final JComboBox combo2 = new JComboBox();
    public MainPanel() {
        super(new BorderLayout());
        combo1.setRenderer(new LRComboCellRenderer(combo1, 80, 16));
        combo1.setModel(makeModel());
        combo2.setModel(makeModel());
        add(makeTitledBox("ClippedLRComboBox", combo1), BorderLayout.NORTH);
        add(makeTitledBox("NomalComboBox", combo2), BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 200));
    }
    private static Box makeTitledBox(String title, JComboBox combo) {
        final JTextField leftTextField  = new JTextField();
        final JTextField rightTextField = new JTextField();
        leftTextField.setEditable(false);
        rightTextField.setEditable(false);
        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createTitledBorder(title));
        box.add(Box.createVerticalStrut(2));
        box.add(combo);
        box.add(Box.createVerticalStrut(5));
        box.add(new JSeparator());
        box.add(leftTextField);
        box.add(Box.createVerticalStrut(2));
        box.add(rightTextField);
        combo.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()==ItemEvent.SELECTED) {
                    JComboBox c = (JComboBox)e.getSource();
                    LRItem item = (LRItem)c.getSelectedItem();
                    leftTextField.setText(item.getLeftText());
                    rightTextField.setText(item.getRightText());
                }
            }
        });
        return box;
    }
    private static DefaultComboBoxModel makeModel() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement(new LRItem("asdfasdf", "846876"));
        model.addElement(new LRItem("bxcvzx", "asdfaasdfasdfasdfasdfsasd"));
        model.addElement(new LRItem("asdfasdf.1234567890.1234567890.1234567890.e",
                                    "qwerqwer.1234567890.1234567890.1234567890"));
        model.addElement(new LRItem("14234125", "64345424543523452345234523684"));
        model.addElement(new LRItem("hjklhjk", "addElement"));
        model.addElement(new LRItem("aaaaaaaa", "ddd"));
        model.addElement(new LRItem("bbbbbbbb", "eeeee"));
        model.addElement(new LRItem("cccccccc", "fffffff"));
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
        frame.setMinimumSize(new Dimension(256, 100));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
class LRComboCellRenderer extends JPanel implements ListCellRenderer {
    private final JLabel leftLabel  = new JLabel();
    private final JLabel rightLabel = new JLabel();
    private final JComboBox combo;
    private int prevwidth = -1;
    public LRComboCellRenderer(JComboBox combo, int rightWidth, int labelHeight) {
        super(new BorderLayout());
        this.combo = combo;
        this.setBorder(BorderFactory.createEmptyBorder());
        this.setOpaque(true);
        leftLabel.setOpaque(true);
        leftLabel.setBorder(BorderFactory.createEmptyBorder(0,2,0,0));
        rightLabel.setOpaque(true);
        rightLabel.setBorder(BorderFactory.createEmptyBorder(0,2,0,2));
        rightLabel.setPreferredSize(new Dimension(rightWidth, labelHeight));
        rightLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        this.add(leftLabel);
        this.add(rightLabel, BorderLayout.EAST);
    }
    @Override public Component getListCellRendererComponent(JList list, Object value,
                                                  int index, boolean isSelected,
                                                  boolean cellHasFocus) {
        LRItem item = (LRItem)value;
        leftLabel.setText(item.getLeftText());
        rightLabel.setText(item.getRightText());

        leftLabel.setBackground(isSelected?  list.getSelectionBackground():list.getBackground());
        rightLabel.setBackground(isSelected? list.getSelectionBackground():list.getBackground());
        this.setBackground(isSelected?       list.getSelectionBackground():list.getBackground());

        leftLabel.setForeground(isSelected?  list.getSelectionForeground():list.getForeground());
        rightLabel.setForeground(Color.GRAY);

        if(index<0) {
            Dimension d = combo.getSize();
            if(d.width!=prevwidth) {
                Insets i = combo.getInsets();
                int w = d.width-i.left-i.right;
                list.setPreferredSize(new Dimension(w, 0));
                prevwidth = d.width;
            }
        }
        return this;
    }
    @Override public void updateUI() {
        prevwidth = -1;
        super.updateUI();
    }
}
class LRItem{
    private final String leftText;
    private final String rightText;
    public LRItem(String strLeft, String strRight) {
        this.leftText = strLeft;
        this.rightText = strRight;
    }
    public String getLeftText() {
        return leftText;
    }
    public String getRightText() {
        return rightText;
    }
    @Override public String toString() {
        return leftText + " / " + rightText;
    }
}
