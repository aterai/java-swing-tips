package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane() {
            private transient MouseMotionListener hoverHandler;
            @Override public void updateUI() {
                removeMouseMotionListener(hoverHandler);
                super.updateUI();
                hoverHandler = new MouseAdapter() {
                    @Override public void mouseMoved(MouseEvent e) {
                        JTabbedPane source = (JTabbedPane) e.getComponent();
                        int num = source.indexAtLocation(e.getX(), e.getY());
                        for (int i = 0; i < source.getTabCount(); i++) {
                            source.setForegroundAt(i, i == num ? Color.GREEN : Color.BLACK);
                        }
                    }
                };
                addMouseMotionListener(hoverHandler);
            }
        };
        tabbedPane.addTab("11111", new JScrollPane(new JTree()));
        tabbedPane.addTab("22222", new JScrollPane(new JLabel("asdfasdfsadf")));
        tabbedPane.addTab("33333", new JScrollPane(new JTree()));
        tabbedPane.addTab("44444", new JScrollPane(new JLabel("qerwqerqwerqwe")));
        tabbedPane.addTab("55555", new JScrollPane(new JTree()));

        add(tabbedPane);
        setPreferredSize(new Dimension(320, 240));
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
