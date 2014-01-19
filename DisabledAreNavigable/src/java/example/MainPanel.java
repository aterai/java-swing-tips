package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;

public class MainPanel extends JPanel {
    private static final String DISABLED_ARE_NAVIGABLE = "MenuItem.disabledAreNavigable";
    private static JCheckBox check = new JCheckBox(new AbstractAction(DISABLED_ARE_NAVIGABLE) {
        @Override public void actionPerformed(ActionEvent e) {
            Boolean b = ((JCheckBox)e.getSource()).isSelected();
            UIManager.put(DISABLED_ARE_NAVIGABLE, b);
        }
    });
    public MainPanel() {
        super();
        Boolean b = UIManager.getBoolean(DISABLED_ARE_NAVIGABLE);
        System.out.println(b);
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
        m = createLookAndFeelMenu();
        mb.add(m);
        mb.add(Box.createGlue());
        m = new JMenu("Help");
        m.add("About");
        mb.add(m);

//         String[] menuKeys = {"File", "Edit", "Help"};
//         for(String key: menuKeys) {
//             JMenu m = createMenu(key);
//             if(m != null) { mb.add(m); }
//         }
        return mb;
    }
    private static JMenu createMenu(String key) {
        JMenu menu = new JMenu(key);
        String[] itemKeys = {"Cut", "Copy", "Paste", "Delete"};
        for(String k: itemKeys) {
            JMenuItem m = new JMenuItem(k);
            if(k.startsWith("C")) {
                m.setEnabled(false);
            }
            if(m != null) {
                menu.add(m);
            }
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

    //<blockquote cite="SwingSet2.java">
    protected static JMenu createLookAndFeelMenu() {
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
    private static final ButtonGroup lafMenuGroup = new ButtonGroup();
    public static JMenuItem createLafMenuItem(JMenu menu, String label, String laf) {
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
        if(currentLookAndFeel.equals(laf)) { return; }
        currentLookAndFeel = laf;
        try{
            UIManager.setLookAndFeel(currentLookAndFeel);
            updateLookAndFeel();

            Boolean b = UIManager.getBoolean(DISABLED_ARE_NAVIGABLE);
            System.out.format("%s %s: %s%n", laf, DISABLED_ARE_NAVIGABLE, b);
            check.setSelected(b);
        }catch(Exception ex) {
            ex.printStackTrace();
            System.out.println("Failed loading L&F: " + currentLookAndFeel);
        }
    }
    private static void updateLookAndFeel() {
        Window windows[] = Frame.getWindows();
        for(Window window : windows) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }
    //</blockquote>

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
            JComponent invoker = (JComponent)c.getParent();
            window = SwingUtilities.getWindowAncestor(invoker);
        }
        if(window!=null) {
            //window.dispose();
            window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
        }
    }
}
