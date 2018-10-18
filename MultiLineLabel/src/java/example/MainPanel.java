package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public final class MainPanel extends JPanel {
    private static final String DUMMY_TEXT = "asdfasdfasdfasdfasdfasdfasd";
    // http://www.icongalore.com/ XP Style Icons - Windows Application Icon, Software XP Icons
    private final ImageIcon icon = new ImageIcon(getClass().getResource("wi0124-32.png"));

    private MainPanel() {
        super(new GridLayout(3, 1));

        JTextPane label1 = new JTextPane();
        // MutableAttributeSet attr = new SimpleAttributeSet();
        Style attr = label1.getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setLineSpacing(attr, -.2f);
        label1.setParagraphAttributes(attr, true);
        label1.setText("JTextPane\n" + DUMMY_TEXT);
        add(makeLeftIcon(label1, icon));

        JTextArea label2 = new JTextArea("JTextArea\n" + DUMMY_TEXT);
        add(makeLeftIcon(label2, icon));

        JLabel label3 = new JLabel("<html>JLabel+html<br>" + DUMMY_TEXT);
        label3.setIcon(icon);
        add(label3);

        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setPreferredSize(new Dimension(320, 240));
    }
    private static Box makeLeftIcon(JTextComponent label, ImageIcon icon) {
        label.setForeground(UIManager.getColor("Label.foreground"));
        // label.setBackground(UIManager.getColor("Label.background"));
        label.setOpaque(false);
        label.setEditable(false);
        label.setFocusable(false);
        label.setMaximumSize(label.getPreferredSize());
        label.setMinimumSize(label.getPreferredSize());

        JLabel l = new JLabel(icon);
        l.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        Box box = Box.createHorizontalBox();
        box.add(l);
        box.add(Box.createHorizontalStrut(2));
        box.add(label);
        box.add(Box.createHorizontalGlue());
        return box;
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
