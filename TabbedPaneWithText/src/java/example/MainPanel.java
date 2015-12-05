package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

// https://community.oracle.com/thread/1392495 JTabbedPane with non-tabbed text
public final class MainPanel extends JPanel {
    private static final String TEXT = "<--1234567890";
    private MainPanel() {
        super(new BorderLayout());
        JTabbedPane tab = new JTabbedPane() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                FontMetrics fm = getFontMetrics(getFont());
                int stringWidth = fm.stringWidth(TEXT) + 10;
                int x = getSize().width - stringWidth;
                Rectangle lastTab = getUI().getTabBounds(this, getTabCount() - 1);
                int tabEnd = lastTab.x + lastTab.width;
                if (x < tabEnd) {
                    x = tabEnd;
                }
                g.drawString(TEXT, x + 5, 18);
            }
        };
        tab.addTab("title1", new JLabel("tab1"));
        tab.addTab("title2", new JLabel("tab2"));
        add(tab);
        setPreferredSize(new Dimension(320, 240));
    }

    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
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
