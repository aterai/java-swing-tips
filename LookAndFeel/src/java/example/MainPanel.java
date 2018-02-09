package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        add(new JScrollPane(makeTestBox()));
        setPreferredSize(new Dimension(320, 240));
    }

    private static Box makeTestBox() {
        Box box = Box.createVerticalBox();
        box.add(makeSystemColor(SystemColor.desktop, "desktop"));
        box.add(makeSystemColor(SystemColor.activeCaption, "activeCaption"));
        box.add(makeSystemColor(SystemColor.inactiveCaption, "inactiveCaption"));
        box.add(makeSystemColor(SystemColor.activeCaptionText, "activeCaptionText"));
        box.add(makeSystemColor(SystemColor.inactiveCaptionText, "inactiveCaptionText"));
        box.add(makeSystemColor(SystemColor.activeCaptionBorder, "activeCaptionBorder"));
        box.add(makeSystemColor(SystemColor.inactiveCaptionBorder, "inactiveCaptionBorder"));
        box.add(makeSystemColor(SystemColor.window, "window"));
        box.add(makeSystemColor(SystemColor.windowText, "windowText"));
        box.add(makeSystemColor(SystemColor.menu, "menu"));
        box.add(makeSystemColor(SystemColor.menuText, "menuText"));
        box.add(makeSystemColor(SystemColor.text, "text"));
        box.add(makeSystemColor(SystemColor.textHighlight, "textHighlight"));
        box.add(makeSystemColor(SystemColor.textText, "textText"));
        box.add(makeSystemColor(SystemColor.textHighlightText, "textHighlightText"));
        box.add(makeSystemColor(SystemColor.control, "control"));
        box.add(makeSystemColor(SystemColor.controlLtHighlight, "controlLtHighlight"));
        box.add(makeSystemColor(SystemColor.controlHighlight, "controlHighlight"));
        box.add(makeSystemColor(SystemColor.controlShadow, "controlShadow"));
        box.add(makeSystemColor(SystemColor.controlDkShadow, "controlDkShadow"));
        box.add(makeSystemColor(SystemColor.controlText, "controlText"));
//        box.add(makeSystemColor(SystemColor.inactiveCaptionControlText, "inactiveControlText"));
        box.add(makeSystemColor(SystemColor.control, "control"));
        box.add(makeSystemColor(SystemColor.scrollbar, "scrollbar"));
        box.add(makeSystemColor(SystemColor.info, "info"));
        box.add(makeSystemColor(SystemColor.infoText, "infoText"));
        box.add(Box.createVerticalGlue());
        return box;
    }
    private static JComponent makeSystemColor(Color color, String text) {
        JTextField field = new JTextField(String.format("%s RGB(#%06X)", text, color.getRGB() & 0xFFFFFF));
        field.setEditable(false);
        JLabel c = new JLabel() {
            @Override public Dimension getPreferredSize() {
                return new Dimension(20, 0);
            }
        };
        c.setOpaque(true);
        c.setBackground(color);
        JPanel p = new JPanel(new BorderLayout());
        p.add(field);
        p.add(c, BorderLayout.EAST);
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
//         try {
//             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//         } catch (ClassNotFoundException | InstantiationException
//                | IllegalAccessException | UnsupportedLookAndFeelException ex) {
//             ex.printStackTrace();
//         }
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(LookAndFeelUtil.createLookAndFeelMenu());

        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.setJMenuBar(menuBar);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

// @see SwingSet2.java
final class LookAndFeelUtil {
    // Possible Look & Feels
    private static final String MAC = "com.sun.java.swing.plaf.mac.MacLookAndFeel";
    private static final String METAL = "javax.swing.plaf.metal.MetalLookAndFeel";
    private static final String MOTIF = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
    private static final String WINDOWS = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
    private static final String GTK = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
    // private static final String NIMBUS = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"; // JDK 1.6.0_10
    private static final String NIMBUS = "javax.swing.plaf.nimbus.NimbusLookAndFeel"; // JDK 1.7.0

    // The current Look & Feel
    private static String currentLaf = METAL;

    private LookAndFeelUtil() { /* Singleton */ }

    public static JMenu createLookAndFeelMenu() {
        ButtonGroup lafMenuGroup = new ButtonGroup();
        JMenu lafMenu = new JMenu("Look&Feel");
        JMenuItem mi = createLafMenuItem(lafMenu, lafMenuGroup, "Metal", METAL);
        mi.setSelected(true); // this is the default l&f
        createLafMenuItem(lafMenu, lafMenuGroup, "Mac",     MAC);
        createLafMenuItem(lafMenu, lafMenuGroup, "Motif",   MOTIF);
        createLafMenuItem(lafMenu, lafMenuGroup, "Windows", WINDOWS);
        createLafMenuItem(lafMenu, lafMenuGroup, "GTK",     GTK);
        createLafMenuItem(lafMenu, lafMenuGroup, "Nimbus",  NIMBUS);
        return lafMenu;
    }
    private static JMenuItem createLafMenuItem(JMenu menu, ButtonGroup lafMenuGroup, String label, String laf) {
        JMenuItem mi = menu.add(new JRadioButtonMenuItem(label));
        lafMenuGroup.add(mi);
        mi.addActionListener(new ChangeLookAndFeelAction(laf));
        mi.setEnabled(isAvailableLookAndFeel(laf));
        return mi;
    }
    private static boolean isAvailableLookAndFeel(String laf) {
        try {
            Class<?> lnfClass = Class.forName(laf);
            LookAndFeel newLnF = (LookAndFeel) lnfClass.getConstructor().newInstance();
            return newLnF.isSupportedLookAndFeel();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
            return false;
        }
    }
    private static class ChangeLookAndFeelAction extends AbstractAction {
        private final String laf;
        protected ChangeLookAndFeelAction(String laf) {
            super("ChangeTheme");
            this.laf = laf;
        }
        @Override public void actionPerformed(ActionEvent e) {
            setLookAndFeel(laf);
        }
    }
    public static void setLookAndFeel(String laf) {
        if (currentLaf.equals(laf)) {
            return;
        }
        currentLaf = laf;
        try {
            UIManager.setLookAndFeel(currentLaf);
            updateLookAndFeel();
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
            System.out.println("Failed loading L&F: " + currentLaf);
        }
    }
    private static void updateLookAndFeel() {
        for (Window window: Frame.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }
}
