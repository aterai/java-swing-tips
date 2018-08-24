package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.ItemEvent;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JTextField leftTextField = new JTextField();
    private final JTextField rightTextField = new JTextField();
    public MainPanel() {
        super(new BorderLayout());
        leftTextField.setEditable(false);
        rightTextField.setEditable(false);

        DefaultComboBoxModel<PairItem> model = new DefaultComboBoxModel<>();
        model.addElement(new PairItem("asdfasdf", "846876"));
        model.addElement(new PairItem("bxcvzx", "asdfasd"));
        model.addElement(new PairItem("qwerqwe", "iop.ioqqadfa"));
        model.addElement(new PairItem("14234125", "64345424684"));
        model.addElement(new PairItem("hjklhjk", "asdfasdfasdfasdfasdfasd"));
        JComboBox<PairItem> combo = new JComboBox<>(model);
        combo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                initTextField((PairItem) e.getItem());
            }
        });
        initTextField(combo.getItemAt(combo.getSelectedIndex()));

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        box.add(leftTextField);
        box.add(Box.createVerticalStrut(2));
        box.add(rightTextField);
        box.add(Box.createVerticalStrut(5));
        box.add(combo);
        add(box, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private void initTextField(PairItem item) {
        leftTextField.setText(item.getLeftText());
        rightTextField.setText(item.getRightText());
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGui();
            }
        });
    }
    public static void createAndShowGui() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class PairItem {
    private final String leftText;
    private final String rightText;
    protected PairItem(String strLeft, String strRight) {
        leftText = strLeft;
        rightText = strRight;
    }
    public String getHtmlText() {
        return String.format("<html><table width='290'><tr><td align='left'>%s</td><td align='right'>%s</td></tr></table></html>", leftText, rightText);
    }
    public String getLeftText() {
        return leftText;
    }
    public String getRightText() {
        return rightText;
    }
    @Override public String toString() {
        return getHtmlText();
    }
}
