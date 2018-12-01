package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private static final String LF = "\n";
    private final Dimension preferredSize = new Dimension(320, 240);

    public MainPanel() {
        super(new BorderLayout());
        StringBuilder buf = new StringBuilder();
        IntStream.range(0, 1000).forEach(i -> buf.append(i + LF));

        JSplitPane sp = new JSplitPane();
        sp.setLeftComponent(new JScrollPane(new JTextArea(buf.toString())));

        UIManager.put("ScrollBar.minimumThumbSize", new Dimension(32, 32));
        sp.setRightComponent(new JScrollPane(new JTextArea(buf.toString())));

        sp.setResizeWeight(.5);
        add(sp);
    }
    @Override public Dimension getPreferredSize() {
        return preferredSize;
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
