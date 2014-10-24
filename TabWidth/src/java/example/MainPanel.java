package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI;

public final class MainPanel extends JPanel {
    private final JTabbedPane tab0 = new JTabbedPane();
    private final JTabbedPane tab1 = new JTabbedPane();
    public MainPanel() {
        super(new BorderLayout());
        tab0.addTab("aaaaaa",          new JLabel("aaaaaaaaaaa"));
        tab0.addTab("bbbbbbbbbbbbbbb", new JLabel("bbbbbbbbb"));
        tab0.addTab("c",               new JLabel("cccccccccc"));

        if (tab1.getUI() instanceof WindowsTabbedPaneUI) {
            tab1.setUI(new WindowsTabbedPaneUI() {
                protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
                    int i = super.calculateTabWidth(tabPlacement, tabIndex, metrics);
                    return i < 100 ? 100 : i;
                }
            });
        } else {
            tab1.setUI(new BasicTabbedPaneUI() {
                protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
                    int i = super.calculateTabWidth(tabPlacement, tabIndex, metrics);
                    return i < 100 ? 100 : i;
                }
            });
        }
        tab1.addTab("aaaaaa",          new JLabel("aaaaaaaaaaa"));
        tab1.addTab("bbbbbbbbbbbbbbb", new JLabel("bbbbbbbbb"));
        tab1.addTab("c",               new JLabel("cccccccccc"));

        add(tab0, BorderLayout.NORTH);
        add(tab1, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 200));
    }

    public String makeTitle(String title) {
        return "<html><table width='100'><tr><td align='center'>" + title + "</td></tr></table>";
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
