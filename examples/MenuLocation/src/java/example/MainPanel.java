// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    // System.out.println(UIManager.getInt("Menu.submenuPopupOffsetX"));
    UIManager.put("Menu.submenuPopupOffsetX", -16);
    // System.out.println(UIManager.getInt("Menu.submenuPopupOffsetY"));
    UIManager.put("Menu.submenuPopupOffsetY", -3);

    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(createMenuBar()));
    add(new JScrollPane(new JTextArea()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JMenuBar createMenuBar() {
    JMenuBar mb = new JMenuBar();
    JMenu m1 = createMenu("File");
    mb.add(m1);
    mb.add(Box.createHorizontalGlue());
    JMenu m2 = createMenu("☰"); // \u2630
    mb.add(m2);
    return mb;
  }

  private static JMenu createMenu(String key) {
    JMenu menu = new JMenu(key) {
      @Override public void setPopupMenuVisible(boolean b) {
        if (isTopLevelMenu()) {
          Point p = getLocation();
          Rectangle r = getRootPane().getBounds();
          Dimension d1 = getPopupMenu().getPreferredSize();
          if (p.x + d1.width > r.width) {
            Dimension d2 = getPreferredSize();
            setMenuLocation(d2.width - d1.width, d2.height);
          }
        }
        super.setPopupMenuVisible(b);
      }
    };
    menu.add(Box.createHorizontalStrut(200));

    JMenu sub = createTitledMenu("Bookmarks");
    sub.add("Item 1");
    sub.add("Item 2");
    sub.add(Box.createHorizontalStrut(200));

    menu.add(sub);

    JMenu sub2 = new JMenu("submenuPopupOffsetX");

    sub2.add("Item 3");
    sub2.add("Item 4");
    menu.add(sub2);

    menu.add("Item 5");
    menu.add("Item 6");

    JMenu sub3 = createTitledMenu("Help");
    sub3.add("Help 1");
    sub3.add("Help 2");
    sub3.add("Help 3");
    sub3.add("Help 4");
    menu.add(sub3);

    return menu;
  }

  private static JMenu createTitledMenu(String title) {
    JMenu menu = new JMenu(title) {
      @Override public void setPopupMenuVisible(boolean b) {
        JPopupMenu popup = getPopupMenu();
        popup.setPopupSize(getParent().getPreferredSize());
        Point p = getLocation();
        setMenuLocation(-p.x, -p.y);
        super.setPopupMenuVisible(b);
      }

      @Override public JMenuItem add(JMenuItem item) {
        item.setMaximumSize(new Dimension(Short.MAX_VALUE, item.getPreferredSize().height));
        return super.add(item);
      }
    };
    menu.setDelay(100_000);
    menu.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        ((AbstractButton) e.getComponent()).doClick();
        menu.getPopupMenu().setVisible(true);
      }
    });
    JButton button = new JButton(" < ");
    button.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    button.setContentAreaFilled(false);
    button.setFocusPainted(false);
    button.setBackground(Color.WHITE);
    button.setOpaque(false);
    button.addMouseMotionListener(new MouseAdapter() {
      @Override public void mouseMoved(MouseEvent e) {
        e.getComponent().getParent().repaint();
      }
    });
    button.addActionListener(e -> menu.getPopupMenu().setVisible(false));
    JMenuItem titleBar = new JMenuItem();
    // titleBar.setBackground(new Color(0xAA_DD_FF));
    titleBar.setOpaque(true);
    titleBar.setEnabled(false);
    titleBar.setFocusable(false);
    titleBar.setLayout(new BorderLayout(0, 0));
    titleBar.add(button, BorderLayout.WEST);
    titleBar.add(new JLabel(title, SwingConstants.CENTER));
    titleBar.add(Box.createHorizontalStrut(button.getPreferredSize().width), BorderLayout.EAST);
    // titleBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY.darker()));
    titleBar.setPreferredSize(new Dimension(200, 24));
    menu.add(titleBar);
    // menu.addSeparator();
    return menu;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
      return;
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
