package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.lang.reflect.*;
import java.security.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private static final Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    private static final Color BACKGROUND_COLOR = new Color(200, 100, 100, 100);
    private final JComponent glass = new JPanel(new BorderLayout()) {
        @Override protected void paintComponent(Graphics g) {
            g.setColor(BACKGROUND_COLOR);
            g.fillRect(0, 0, getWidth(), getHeight());
            super.paintComponent(g);
        }
    };
    private final JLabel label1 = makeLabel("Default: setToolTipText");
    private final JLabel label2 = makeLabel("FORCE_HEAVYWEIGHT_POPUP");
    public MainPanel() {
        super();
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            @Override public Void run() {
                try {
                    Field field;
                    if (System.getProperty("java.version").startsWith("1.6.0")) {
                        // https://community.oracle.com/thread/1357949 ComboBox scroll and selected/highlight on glasspane
                        Class clazz = Class.forName("javax.swing.PopupFactory");
                        field = clazz.getDeclaredField("forceHeavyWeightPopupKey");
                    } else { //JDK 1.7.0, 1.8.0
                        Class clazz = Class.forName("javax.swing.ClientPropertyKey");
                        field = clazz.getDeclaredField("PopupFactory_FORCE_HEAVYWEIGHT_POPUP");
                    }
                    field.setAccessible(true);
                    label2.putClientProperty(field.get(null), Boolean.TRUE);
                } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException ex) {
                    ex.printStackTrace();
                }
                return null;
            }
        });

        EventQueue.invokeLater(() -> {
            glass.setOpaque(false);
            glass.add(label1, BorderLayout.WEST);
            glass.add(label2, BorderLayout.EAST);
            glass.add(Box.createVerticalStrut(60), BorderLayout.SOUTH);
            getRootPane().setGlassPane(glass);
            getRootPane().getGlassPane().setVisible(true);
        });
        setPreferredSize(new Dimension(320, 240));
    }
    private static JLabel makeLabel(String title) {
        JLabel label = new JLabel(title);
        label.setFont(FONT);
        label.setOpaque(true);
        label.setBackground(Color.ORANGE);
        label.setToolTipText("1234567890");
        return label;
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
