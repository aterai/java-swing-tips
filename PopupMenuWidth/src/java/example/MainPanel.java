package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.DefaultMenuLayout;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JMenuBar mb = new JMenuBar();
        JMenu menu = makeMenu(mb.add(new JMenu("Default")));
        // menu.getPopupMenu().setPreferredSize(new Dimension(200, 0));

        menu = makeMenu(mb.add(new JMenu("BoxHStrut")));
        menu.add(Box.createHorizontalStrut(200));

        menu = makeMenu(mb.add(new JMenu("Override")));
        menu.add(new JMenuItem("PreferredSize") {
            @Override public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width = 200;
                return d;
            }
        });

        menu = makeMenu(mb.add(new JMenu("Layout")));
        JPopupMenu popup = menu.getPopupMenu();
        popup.setLayout(new DefaultMenuLayout(popup, BoxLayout.Y_AXIS) {
            @Override public Dimension preferredLayoutSize(Container target) {
                Dimension d = super.preferredLayoutSize(target);
                d.width = Math.max(200, d.width);
                return d;
            }
        });

        menu = mb.add(new JMenu("Html"));
        JMenuItem item = menu.add("<html><table cellpadding='0' cellspacing='0' style='width:200'>Table");
        item.setMnemonic('T');
        //item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
        makeMenu(menu);

        add(mb, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private JMenu makeMenu(JMenu menu) {
        menu.add("Open").setMnemonic('O');
        menu.addSeparator();
        menu.add("Exit").setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        return menu;
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
