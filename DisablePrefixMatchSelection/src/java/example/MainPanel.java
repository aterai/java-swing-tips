package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.text.Position;

public final class MainPanel extends JPanel {
    private static final String TXT = "<html>The alphanumeric keys are pressed:<br />&nbsp;&nbsp;&nbsp;&nbsp;Nothing to select";
    private final JCheckBox check = new JCheckBox(TXT);
    public MainPanel() {
        super(new BorderLayout(5, 5));

        DefaultListModel<String> model = new DefaultListModel<>();
        model.addElement("aaaaaaaaaaaa");
        model.addElement("abbbbbbbbbbbbbbbbbb");
        model.addElement("accccccccccc");
        model.addElement("bbbbbb");
        model.addElement("cccbbb");
        model.addElement("dddddddddddd");
        model.addElement("eeeeeeeeeeeeeeeeeee");
        model.addElement("fffffffffffffffffffffff");

        JList<String> list = new JList<String>(model) {
            @Override public int getNextMatch(String prefix, int startIndex, Position.Bias bias) {
                return check.isSelected() ? -1 : super.getNextMatch(prefix, startIndex, bias);
            }
        };

        add(new JScrollPane(list));
        add(check, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
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
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
