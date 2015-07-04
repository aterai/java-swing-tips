package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.Locale;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        Box box = Box.createVerticalBox();
        box.add(makeSystemColorPanel(SystemColor.desktop, "desktop"));
        box.add(makeSystemColorPanel(SystemColor.activeCaption, "activeCaption"));
        box.add(makeSystemColorPanel(SystemColor.inactiveCaption, "inactiveCaption"));
        box.add(makeSystemColorPanel(SystemColor.activeCaptionText, "activeCaptionText"));
        box.add(makeSystemColorPanel(SystemColor.inactiveCaptionText, "inactiveCaptionText"));
        box.add(makeSystemColorPanel(SystemColor.activeCaptionBorder, "activeCaptionBorder"));
        box.add(makeSystemColorPanel(SystemColor.inactiveCaptionBorder, "inactiveCaptionBorder"));
        box.add(makeSystemColorPanel(SystemColor.window, "window"));
        box.add(makeSystemColorPanel(SystemColor.windowText, "windowText"));
        box.add(makeSystemColorPanel(SystemColor.menu, "menu"));
        box.add(makeSystemColorPanel(SystemColor.menuText, "menuText"));
        box.add(makeSystemColorPanel(SystemColor.text, "text"));
        box.add(makeSystemColorPanel(SystemColor.textHighlight, "textHighlight"));
        box.add(makeSystemColorPanel(SystemColor.textText, "textText"));
        box.add(makeSystemColorPanel(SystemColor.textHighlightText, "textHighlightText"));
        box.add(makeSystemColorPanel(SystemColor.control, "control"));
        box.add(makeSystemColorPanel(SystemColor.controlLtHighlight, "controlLtHighlight"));
        box.add(makeSystemColorPanel(SystemColor.controlHighlight, "controlHighlight"));
        box.add(makeSystemColorPanel(SystemColor.controlShadow, "controlShadow"));
        box.add(makeSystemColorPanel(SystemColor.controlDkShadow, "controlDkShadow"));
        box.add(makeSystemColorPanel(SystemColor.controlText, "controlText"));
//        box.add(makeSystemColorPanel(SystemColor.inactiveCaptionControlText, "inactiveControlText"));
        box.add(makeSystemColorPanel(SystemColor.control, "control"));
        box.add(makeSystemColorPanel(SystemColor.scrollbar, "scrollbar"));
        box.add(makeSystemColorPanel(SystemColor.info, "info"));
        box.add(makeSystemColorPanel(SystemColor.infoText, "infoText"));
        box.add(Box.createRigidArea(new Dimension(320, 0)));

    //    box.add(Box.createVerticalStrut(10));
    //    box.add(makeSystemColorPanel(new Color(0xFF004E98), "test"));

        add(new JScrollPane(box));
    }

    private static JPanel makeSystemColorPanel(Color color, String text) {
        JPanel p = new JPanel(new BorderLayout());
        JTextField jtext = new JTextField(text + ": 0x" + Integer.toHexString(color.getRGB()).toUpperCase(Locale.ENGLISH));
        jtext.setEditable(false);
        p.add(jtext);
        JLabel l = new JLabel() {
            @Override public Dimension getPreferredSize() {
                return new Dimension(32, 0);
            }
        };
        l.setOpaque(true);
        l.setBackground(color);
        p.add(l, BorderLayout.EAST);
        return p;
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
