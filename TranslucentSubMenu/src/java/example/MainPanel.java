package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.plaf.*;

public final class MainPanel extends JPanel {
    private final JTree tree = new JTree();
    public MainPanel() {
        super(new BorderLayout());
        tree.setComponentPopupMenu(makePopupMenu());
        add(new JScrollPane(tree));
        setPreferredSize(new Dimension(320, 240));
        EventQueue.invokeLater(() -> getRootPane().setJMenuBar(createMenuBar()));
    }
    private static JPopupMenu makePopupMenu() {
        JMenu menu = new TransparentMenu("Test");
        menu.add("Undo");
        menu.add("Redo");
        JPopupMenu popup = new TranslucentPopupMenu();
        popup.add(menu);
        popup.addSeparator();
        popup.add("Cut");
        popup.add("Copy");
        popup.add("Paste");
        popup.add("Delete");
        return popup;
    }
    private JMenuBar createMenuBar() {
        JMenuBar mb = new JMenuBar();
        String[] menuKeys = {"File", "Edit", "Help"};
        for (String key: menuKeys) {
            JMenu m = createMenu(key);
            mb.add(m);
        }
        return mb;
    }
    public static JMenu createMenu(String key) {
        JMenu menu = new TransparentMenu(key);
        // menu.setForeground(new Color(200, 200, 200));
        menu.setOpaque(false); // Motif lnf
        JMenu sub = new TransparentMenu("Submenu");
        sub.add("JMenuItem");
        sub.add("Looooooooooooooooooooong");
        menu.add(sub);
        menu.add("dummy1");
        menu.add("dummy2");
        return menu;
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
        PopupFactory.setSharedInstance(new TranslucentPopupFactory());
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

// https://ateraimemo.com/Swing/TranslucentPopupMenu.html
class TranslucentPopupMenu extends JPopupMenu {
    private static final Color ALPHA_ZERO = new Color(0x0, true);
    private static final Color POPUP_BACK = new Color(250, 250, 250, 200);
    private static final Color POPUP_LEFT = new Color(230, 230, 230, 200);
    private static final int LEFT_WIDTH = 24;
    @Override public boolean isOpaque() {
        return false;
    }
    @Override public void updateUI() {
        super.updateUI();
        if (Objects.isNull(UIManager.getBorder("PopupMenu.border"))) {
            setBorder(new BorderUIResource(BorderFactory.createLineBorder(Color.GRAY)));
        }
    }
    @Override public Component add(Component c) {
        if (c instanceof JComponent) {
            ((JComponent) c).setOpaque(false);
        }
        return c;
    }
    @Override public JMenuItem add(JMenuItem menuItem) {
        menuItem.setOpaque(false);
        return super.add(menuItem);
    }
    @Override public void show(Component c, int x, int y) {
        EventQueue.invokeLater(() -> {
            Container p = getTopLevelAncestor();
            if (p instanceof JWindow) {
                System.out.println("Heavy weight");
                ((JWindow) p).setBackground(ALPHA_ZERO);
            } else {
                System.out.println("Light weight");
            }
        });
        super.show(c, x, y);
    }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(POPUP_LEFT);
        g2.fillRect(0, 0, LEFT_WIDTH, getHeight());
        g2.setPaint(POPUP_BACK);
        g2.fillRect(LEFT_WIDTH, 0, getWidth(), getHeight());
        g2.dispose();
    }
}

class TransparentMenu extends JMenu {
    private JPopupMenu popupMenu;

    protected TransparentMenu(String title) {
        super(title);
    }
    // [JDK-4688783] JPopupMenu hardcoded i JMenu - Java Bug System
    // https://bugs.openjdk.java.net/browse/JDK-4688783
    private void ensurePopupMenuCreated() {
        if (Objects.isNull(popupMenu)) {
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
        if (c instanceof JComponent) {
            ((JComponent) c).setOpaque(false);
        }
        popupMenu.add(c);
        return c;
    }
    @Override public void addSeparator() {
        ensurePopupMenuCreated();
        popupMenu.addSeparator();
    }
    @Override public void insert(String s, int pos) {
        if (pos < 0) {
            throw new IllegalArgumentException("index less than zero.");
        }
        ensurePopupMenuCreated();
        popupMenu.insert(new JMenuItem(s), pos);
    }
    @Override public JMenuItem insert(JMenuItem mi, int pos) {
        if (pos < 0) {
            throw new IllegalArgumentException("index less than zero.");
        }
        ensurePopupMenuCreated();
        popupMenu.insert(mi, pos);
        return mi;
    }
    @Override public void insertSeparator(int index) {
        if (index < 0) {
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
    @Override public Popup getPopup(Component owner, Component contents, int x, int y) { // throws IllegalArgumentException {
        return new TranslucentPopup(owner, contents, x, y);
    }
}

class TranslucentPopup extends Popup {
    private final JWindow popupWindow;
    protected TranslucentPopup(Component owner, Component contents, int ownerX, int ownerY) {
        super(owner, contents, ownerX, ownerY);
        // create a new heavyweight window
        this.popupWindow = new JWindow();
        // mark the popup with partial opacity
        // AWTUtilities.setWindowOpacity(popupWindow, (contents instanceof JToolTip) ? .8f : .95f);
        // popupWindow.setOpacity(.5f);
        // AWTUtilities.setWindowOpaque(popupWindow, false); // Java 1.6.0_10
        popupWindow.setBackground(new Color(0x0, true)); // Java 1.7.0
        // determine the popup location
        popupWindow.setLocation(ownerX, ownerY);
        // add the contents to the popup
        popupWindow.getContentPane().add(contents);
        contents.invalidate();
        // JComponent parent = (JComponent) contents.getParent();
        // set the shadow border
        // parent.setBorder(new ShadowPopupBorder());
    }
    @Override public void show() {
        System.out.println("Always Heavy weight!");
        this.popupWindow.setVisible(true);
        this.popupWindow.pack();
        // mark the window as non-opaque, so that the
        // shadow border pixels take on the per-pixel
        // translucency
        // AWTUtilities.setWindowOpaque(this.popupWindow, false);
    }
    @Override public void hide() {
        this.popupWindow.setVisible(false);
        this.popupWindow.removeAll();
        this.popupWindow.dispose();
    }
}
