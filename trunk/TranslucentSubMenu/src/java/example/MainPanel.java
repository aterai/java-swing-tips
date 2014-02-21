package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private final JTree tree = new JTree();
    public MainPanel(JFrame frame) {
        super(new BorderLayout());
        frame.setJMenuBar(createMenubar());
        tree.setComponentPopupMenu(makePopupMenu());
        add(new JScrollPane(tree));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JPopupMenu makePopupMenu() {
        JMenu menu = new TransparentMenu("Test");
        menu.add(new JMenuItem("Undo"));
        menu.add(new JMenuItem("Redo"));
        JPopupMenu popup = new TranslucentPopupMenu();
        popup.add(menu);
        popup.addSeparator();
        popup.add(new JMenuItem("Cut"));
        popup.add(new JMenuItem("Copy"));
        popup.add(new JMenuItem("Paste"));
        popup.add(new JMenuItem("Delete"));
        return popup;
    }
    private JMenuBar createMenubar() {
        JMenuBar mb = new JMenuBar();
        String[] menuKeys = {"File", "Edit", "Help"};
        for(String key: menuKeys) {
            JMenu m = createMenu(key);
            //if(m != null)
            mb.add(m);
        }
        return mb;
    }
    private JMenu createMenu(String key) {
        JMenu menu = new TransparentMenu(key);
        menu.setOpaque(false); // Motif lnf
        JMenu sub = new TransparentMenu("Submenu");
        sub.add(new JMenuItem("JMenuItem"));
        sub.add(new JMenuItem("Looooooooooooooooooooong"));
        menu.add(sub);
        menu.add("dummy1");
        menu.add("dummy2");
        return menu;
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        PopupFactory.setSharedInstance(new TranslucentPopupFactory());
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel(frame));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

//http://terai.xrea.jp/Swing/TranslucentPopupMenu.html
class TranslucentPopupMenu extends JPopupMenu {
    private static final Color POPUP_BACK = new Color(250,250,250,200);
    private static final Color POPUP_LEFT = new Color(230,230,230,200);
    private static final int LEFT_WIDTH = 24;
    @Override public boolean isOpaque() {
        return false;
    }
    @Override public Component add(Component c) {
        if(c instanceof JComponent) {
            ((JComponent)c).setOpaque(false);
        }
        return c;
    }
    @Override public JMenuItem add(JMenuItem menuItem) {
        menuItem.setOpaque(false);
        return super.add(menuItem);
    }
//     private static final Color ALPHA_ZERO = new Color(0, true);
//     @Override public void show(Component c, int x, int y) {
//         EventQueue.invokeLater(new Runnable() {
//             @Override public void run() {
//                 Window p = SwingUtilities.getWindowAncestor(TranslucentPopupMenu.this);
//                 if(p!=null && p instanceof JWindow) {
//                     System.out.println("Heavy weight");
//                     JWindow w = (JWindow)p;
//                     w.setBackground(ALPHA_ZERO);
//                 }else{
//                     System.out.println("Light weight");
//                 }
//             }
//         });
//         super.show(c, x, y);
//     }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setPaint(POPUP_LEFT);
        g2.fillRect(0,0,LEFT_WIDTH,getHeight());
        g2.setPaint(POPUP_BACK);
        g2.fillRect(LEFT_WIDTH,0,getWidth(),getHeight());
        g2.dispose();
    }
}

class TransparentMenu extends JMenu {
    private JPopupMenu popupMenu;

    public TransparentMenu(String title) {
        super(title);
    }
    // Bug ID: JDK-4688783 JPopupMenu hardcoded i JMenu
    // http://bugs.sun.com/view_bug.do?bug_id=4688783
    private void ensurePopupMenuCreated() {
        if(popupMenu == null) {
            this.popupMenu = new TranslucentPopupMenu();
            popupMenu.setInvoker(this);
            popupListener = createWinListener(popupMenu);
        }
    }
    @Override public JPopupMenu getPopupMenu() {
        ensurePopupMenuCreated();
        return popupMenu;
    }
    @Override public JMenuItem add(JMenuItem menuItem) {
        ensurePopupMenuCreated();
        menuItem.setOpaque(false);
        return popupMenu.add(menuItem);
    }
    @Override public Component add(Component c) {
        ensurePopupMenuCreated();
        if(c instanceof JComponent) {
            ((JComponent)c).setOpaque(false);
        }
        popupMenu.add(c);
        return c;
    }
    @Override public void addSeparator() {
        ensurePopupMenuCreated();
        popupMenu.addSeparator();
    }
    @Override public void insert(String s, int pos) {
        if(pos < 0) {
            throw new IllegalArgumentException("index less than zero.");
        }
        ensurePopupMenuCreated();
        popupMenu.insert(new JMenuItem(s), pos);
    }
    @Override public JMenuItem insert(JMenuItem mi, int pos) {
        if(pos < 0) {
            throw new IllegalArgumentException("index less than zero.");
        }
        ensurePopupMenuCreated();
        popupMenu.insert(mi, pos);
        return mi;
    }
    @Override public void insertSeparator(int index) {
        if(index < 0) {
            throw new IllegalArgumentException("index less than zero.");
        }
        ensurePopupMenuCreated();
        popupMenu.insert(new JPopupMenu.Separator(), index);
    }
    @Override public boolean isPopupMenuVisible() {
        ensurePopupMenuCreated();
        return popupMenu.isVisible();
    }
}

/*
<a href="http://today.java.net/pub/a/today/2008/03/18/translucent-and-shaped-swing-windows.html">
Translucent and Shaped Swing Windows | Java.net
</a>
*/
class TranslucentPopupFactory extends PopupFactory {
    @Override public Popup getPopup(Component owner, Component contents, int x, int y) { //throws IllegalArgumentException {
        return new TranslucentPopup(owner, contents, x, y);
    }
}

class TranslucentPopup extends Popup {
    private final JWindow popupWindow;
    public TranslucentPopup(Component owner, Component contents, int ownerX, int ownerY) {
        super(owner, contents, ownerX, ownerY);
        // create a new heavyweight window
        this.popupWindow = new JWindow();
        // mark the popup with partial opacity
        //com.sun.awt.AWTUtilities.setWindowOpacity(popupWindow, (contents instanceof JToolTip) ? 0.8f : 0.95f);
        //popupWindow.setOpacity(.5f);
        //com.sun.awt.AWTUtilities.setWindowOpaque(popupWindow, false); //Java 1.6.0_10
        popupWindow.setBackground(new Color(0, true)); //Java 1.7.0
        // determine the popup location
        popupWindow.setLocation(ownerX, ownerY);
        // add the contents to the popup
        popupWindow.getContentPane().add(contents);
        contents.invalidate();
        //JComponent parent = (JComponent) contents.getParent();
        // set the shadow border
        //parent.setBorder(new ShadowPopupBorder());
    }
    @Override public void show() {
        System.out.println("Always Heavy weight!");
        this.popupWindow.setVisible(true);
        this.popupWindow.pack();
        // mark the window as non-opaque, so that the
        // shadow border pixels take on the per-pixel
        // translucency
        //com.sun.awt.AWTUtilities.setWindowOpaque(this.popupWindow, false);
    }
    @Override public void hide() {
        this.popupWindow.setVisible(false);
        this.popupWindow.removeAll();
        this.popupWindow.dispose();
    }
}
