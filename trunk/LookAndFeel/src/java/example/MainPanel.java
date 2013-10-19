package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        JMenuBar mb = new JMenuBar();
        mb.add(createLookAndFeelMenu());
        add(mb, BorderLayout.NORTH);
        add(new JScrollPane(makeTestBox()));
        setPreferredSize(new Dimension(320, 240));
    }

    //<blockquote cite="SwingSet2.java">
    protected JMenu createLookAndFeelMenu() {
        JMenu lafMenu = new JMenu("Look&Feel");
        JMenuItem mi = createLafMenuItem(lafMenu, "Metal", metal);
        mi.setSelected(true); //this is the default l&f
        createLafMenuItem(lafMenu, "Mac", mac);
        createLafMenuItem(lafMenu, "Motif", motif);
        createLafMenuItem(lafMenu, "Windows", windows);
        createLafMenuItem(lafMenu, "GTK", gtk);
        createLafMenuItem(lafMenu, "Nimbus", nimbus);
        return lafMenu;
    }

    // Possible Look & Feels
    private static final String mac     = "com.sun.java.swing.plaf.mac.MacLookAndFeel";
    private static final String metal   = "javax.swing.plaf.metal.MetalLookAndFeel";
    private static final String motif   = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
    private static final String windows = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
    private static final String gtk     = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
    private static final String nimbus  = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";

    // The current Look & Feel
    private static String currentLookAndFeel = metal;
    private final ButtonGroup lafMenuGroup = new ButtonGroup();
    public JMenuItem createLafMenuItem(JMenu menu, String label, String laf) {
        JMenuItem mi = (JRadioButtonMenuItem) menu.add(new JRadioButtonMenuItem(label));
        lafMenuGroup.add(mi);
        mi.addActionListener(new ChangeLookAndFeelAction(laf));
        mi.setEnabled(isAvailableLookAndFeel(laf));
        return mi;
    }
    protected static boolean isAvailableLookAndFeel(String laf) {
        try{
            Class lnfClass = Class.forName(laf);
            LookAndFeel newLAF = (LookAndFeel)(lnfClass.newInstance());
            return newLAF.isSupportedLookAndFeel();
        }catch(Exception e) {
            return false;
        }
    }

    private class ChangeLookAndFeelAction extends AbstractAction {
        private final String laf;
        protected ChangeLookAndFeelAction(String laf) {
            super("ChangeTheme");
            this.laf = laf;
        }
        @Override public void actionPerformed(ActionEvent e) {
            setLookAndFeel(laf);
        }
    }
    private void setLookAndFeel(String laf) {
        if(currentLookAndFeel.equals(laf)) return;
        currentLookAndFeel = laf;
        try{
            UIManager.setLookAndFeel(currentLookAndFeel);
            updateLookAndFeel();
        }catch(Exception ex) {
            ex.printStackTrace();
            System.out.println("Failed loading L&F: " + currentLookAndFeel);
        }
    }
    private void updateLookAndFeel() {
        Window windows[] = Frame.getWindows();
        for(Window window : windows) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }
    //</blockquote>

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
        JTextField field = new JTextField(text+": RGB("+ color.getRGB() +")");
        field.setEditable(false);
        JLabel c = new JLabel();
        c.setPreferredSize(new Dimension(20, 0));
        c.setOpaque(true);
        c.setBackground(color);
        JPanel p = new JPanel(new BorderLayout());
        p.add(field);
        p.add(c, BorderLayout.EAST);
        return p;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
//         try{
//             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//         }catch(Exception e) {
//             e.printStackTrace();
//         }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
