package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private static final String TEST = "1234567890\nabcdefghijklmn";

    private MainPanel() {
        super(new BorderLayout());
        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);

        JTextPane editor1 = new JTextPane();
        editor1.setFont(font);
        editor1.setText("Default\n" + TEST);

        JTextPane editor2 = new JTextPane() {
            protected float fontSize;
            @Override public void doLayout() {
                Insets i = getInsets();
                float f = .08f * (getWidth() - i.left - i.right);
                boolean fontSizeShouldChange = Math.abs(fontSize - f) > 1.0e-1;
                if (fontSizeShouldChange) {
                    setFont(font.deriveFont(f));
                    fontSize = f;
                }
                super.doLayout();
            }
        };
        editor2.setFont(font);
        editor2.setText("doLayout + deriveFont\n" + TEST);

        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        sp.setTopComponent(editor1);
        sp.setBottomComponent(editor2);
        sp.setResizeWeight(.5);
        add(sp);
        setPreferredSize(new Dimension(320, 240));
        EventQueue.invokeLater(() -> sp.setDividerLocation(.5));
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
