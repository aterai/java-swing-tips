package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
//         frame.setJMenuBar(createMenuBar());
//         addComponentListener(new ComponentAdapter() {
//             @Override public void componentResized(ComponentEvent e) {
//                 System.out.println("componentResized");
//                 ((JComponent) e.getSource()).revalidate();
//             }
//         });
//         frame.addWindowStateListener(new WindowStateListener() {
//             @Override public void windowStateChanged(final WindowEvent e) {
//                 EventQueue.invokeLater(new Runnable() {
//                     @Override public void run() {
//                         System.out.println("windowStateChanged");
//                         JFrame f = (JFrame) e.getWindow();
//                         ((JComponent) f.getContentPane()).revalidate();
//                     }
//                 });
//             }
//         });
        add(createMenuBar(), BorderLayout.NORTH);
        add(new JScrollPane(new JTextArea()));
        setPreferredSize(new Dimension(320, 240));
    }
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2) {
            @Override public Dimension preferredLayoutSize(Container target) {
                synchronized (target.getTreeLock()) {
                    int targetWidth = target.getSize().width;
                    targetWidth = targetWidth == 0 ? Integer.MAX_VALUE : targetWidth;
                    Insets insets = target.getInsets();
                    int hgap      = getHgap();
                    int vgap      = getVgap();
                    int maxWidth  = targetWidth - insets.left - insets.right;
                    int height    = vgap;
                    int rowWidth  = hgap;
                    int rowHeight = 0;
                    int nmembers  = target.getComponentCount();
                    for (int i = 0; i < nmembers; i++) {
                        Component m = target.getComponent(i);
                        if (m.isVisible()) {
                            Dimension d = m.getPreferredSize();
                            if (rowWidth + d.width > maxWidth) {
                                height += rowHeight;
                                rowWidth = hgap;
                                rowHeight = 0;
                            }
                            rowWidth += d.width + hgap;
                            rowHeight = Math.max(rowHeight, d.height + vgap);
                        }
                    }
                    height += rowHeight + insets.top + insets.bottom;
                    return new Dimension(targetWidth, height);
                }
            }
//             // https://tips4java.wordpress.com/2008/11/06/wrap-layout/
//             // WrapLayout.java
//             // Rob Camick on November 6, 2008
//             private Dimension preferredLayoutSize;
//             @Override public void layoutContainer(Container target) {
//                 Dimension size = preferredLayoutSize(target);
//                 if (size.equals(preferredLayoutSize)) {
//                     super.layoutContainer(target);
//                 } else {
//                     preferredLayoutSize = size;
//                     Container top = target;
//                     while (!(top instanceof Window) && top.getParent() != null) {
//                         top = top.getParent();
//                     }
//                     top.validate();
//                 }
//             }
        });
        for (String key: new String[] {"File", "Edit", "Aaaaaaaaaaaaaaaaaaaaa", "Bbbbbbbbbbbbb", "Help"}) {
            menuBar.add(createMenu(key));
        }
        return menuBar;
    }
    private JMenu createMenu(String key) {
        JMenu menu = new JMenu(key);
        menu.add("dummy1");
        menu.add("dummy2");
        menu.add("dummy3");
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
