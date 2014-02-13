package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.plaf.basic.*;

public class MainPanel extends JPanel {
    private MainPanel() {
        super();
        Boolean b = UIManager.getBoolean(LookAndFeelUtil.DISABLED_ARE_NAVIGABLE);
        System.out.println(b);
        JCheckBox check = LookAndFeelUtil.disabledAreNavigableCheck;
        check.setSelected(b);
        add(check);
        JPopupMenu popup = new JPopupMenu();
        initMenu(popup);
        setComponentPopupMenu(popup);
        setPreferredSize(new Dimension(320, 240));
    }
    public static JMenuBar createMenubar() {
        JMenuBar mb = new JMenuBar();
        JMenu m = new JMenu("File");
        initMenu(m);
        mb.add(m);
        m = createMenu("Edit");
        mb.add(m);
        m = LookAndFeelUtil.createLookAndFeelMenu();
        mb.add(m);
        mb.add(Box.createGlue());
        m = new JMenu("Help");
        m.add("About");
        mb.add(m);

        return mb;
    }
    private static JMenu createMenu(String key) {
        JMenu menu = new JMenu(key);
        for(String k: Arrays.asList("Cut", "Copy", "Paste", "Delete")) {
            JMenuItem m = new JMenuItem(k);
            m.setEnabled(false);
            menu.add(m);
        }
        return menu;
    }
    private static void initMenu(JComponent p) {
        JMenuItem item = new JMenuItem("Open(disabled)");
        item.setEnabled(false);
        p.add(item);
        item = new JMenuItem("Save(disabled)");
        item.setEnabled(false);
        p.add(item);
        p.add(new JSeparator());
        p.add(new JMenuItem(new ExitAction()));
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
        frame.setJMenuBar(createMenubar());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class ExitAction extends AbstractAction {
    public ExitAction() {
        super("Exit");
    }
    @Override public void actionPerformed(ActionEvent e) {
        JComponent c = (JComponent)e.getSource();
        Window window = null;
        Container parent = c.getParent();
        if(parent instanceof JPopupMenu) {
            JPopupMenu popup = (JPopupMenu)parent;
            JComponent invoker = (JComponent)popup.getInvoker();
            window = SwingUtilities.getWindowAncestor(invoker);
        }else if(parent instanceof JToolBar) {
            JToolBar toolbar = (JToolBar)parent;
            if(((BasicToolBarUI)toolbar.getUI()).isFloating()) {
                window = SwingUtilities.getWindowAncestor(toolbar).getOwner();
            }else{
                window = SwingUtilities.getWindowAncestor(toolbar);
            }
        }else{
            Component invoker = c.getParent();
            window = SwingUtilities.getWindowAncestor(invoker);
        }
        if(window!=null) {
            //window.dispose();
            window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
        }
    }
}

//@see SwingSet2.java
class LookAndFeelUtil {
    // Possible Look & Feels
    private static final String MAC     = "com.sun.java.swing.plaf.mac.MacLookAndFeel";
    private static final String METAL   = "javax.swing.plaf.metal.MetalLookAndFeel";
    private static final String MOTIF   = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
    private static final String WINDOWS = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
    private static final String GTK     = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
    private static final String NIMBUS  = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";

    // The current Look & Feel
    private static String currentLookAndFeel = METAL;

    // Test: disabledAreNavigable
    public static final String DISABLED_ARE_NAVIGABLE = "MenuItem.disabledAreNavigable";
    public static JCheckBox disabledAreNavigableCheck = new JCheckBox(new AbstractAction(DISABLED_ARE_NAVIGABLE) {
        @Override public void actionPerformed(ActionEvent e) {
            Boolean b = ((JCheckBox)e.getSource()).isSelected();
            UIManager.put(DISABLED_ARE_NAVIGABLE, b);
        }
    });

    private LookAndFeelUtil() { /* Singleton */ }

    public static JMenu createLookAndFeelMenu() {
        ButtonGroup lafMenuGroup = new ButtonGroup();
        JMenu lafMenu = new JMenu("Look&Feel");
        JMenuItem mi = createLafMenuItem(lafMenu, lafMenuGroup, "Metal", METAL);
        mi.setSelected(true); //this is the default l&f
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
        try{
            Class lnfClass = Class.forName(laf);
            LookAndFeel newLAF = (LookAndFeel)lnfClass.newInstance();
            return newLAF.isSupportedLookAndFeel();
        }catch(ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
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
    private static void setLookAndFeel(String laf) {
        if(currentLookAndFeel.equals(laf)) {
            return;
        }
        currentLookAndFeel = laf;
        try{
            UIManager.setLookAndFeel(currentLookAndFeel);
            updateLookAndFeel();
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
            System.out.println("Failed loading L&F: " + currentLookAndFeel);
        }

        // Test: disabledAreNavigable
        Boolean b = UIManager.getBoolean(DISABLED_ARE_NAVIGABLE);
        System.out.format("%s %s: %s%n", laf, DISABLED_ARE_NAVIGABLE, b);
        disabledAreNavigableCheck.setSelected(b);
    }
    private static void updateLookAndFeel() {
        for(Window window : Frame.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }
}
