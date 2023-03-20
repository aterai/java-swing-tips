// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.plaf.basic.BasicToolBarUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JToolBar toolBar = new JToolBar();
    toolBar.setLayout(new BorderLayout());

    JMenuBar menuBar = new JMenuBar();
    menuBar.add(makeMenu("JMenu 1"));

    JMenu menu2 = makeMenu("JMenu 2");
    menu2.addMenuListener(new MenuListener() {
      private boolean isFloating(JMenu menu) {
        Container c = SwingUtilities.getAncestorOfClass(JToolBar.class, menu);
        return c instanceof JToolBar && !((BasicToolBarUI) ((JToolBar) c).getUI()).isFloating();
      }

      @Override public void menuSelected(MenuEvent e) {
        JMenu menu = (JMenu) e.getSource();
        if (menu.isTopLevelMenu() && isFloating(menu)) {
          Dimension d = menu.getPreferredSize();
          Point p = menu.getLocation();
          Component cp = getRootPane().getContentPane();
          Point pt = SwingUtilities.convertPoint(menu.getParent(), p, cp);
          pt.y += d.height * 2;
          if (!cp.getBounds().contains(pt)) {
            EventQueue.invokeLater(() -> {
              JPopupMenu popup = menu.getPopupMenu();
              Rectangle bounds = popup.getBounds();
              Point loc = menu.getLocationOnScreen();
              // int h = bounds.height + UIManager.getInt("Menu.menuPopupOffsetY");
              loc.y -= bounds.height + UIManager.getInt("Menu.menuPopupOffsetY");
              popup.setLocation(loc);
            });
          }
        }
      }

      @Override public void menuDeselected(MenuEvent e) {
        // Do nothing
      }

      @Override public void menuCanceled(MenuEvent e) {
        // Do nothing
      }
    });
    menuBar.add(menu2);

    toolBar.add(menuBar);
    add(toolBar, BorderLayout.NORTH);
    add(Box.createRigidArea(new Dimension()), BorderLayout.WEST);
    add(Box.createRigidArea(new Dimension()), BorderLayout.EAST);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JMenu makeMenu(String title) {
    JMenu menu = new JMenu(title);
    menu.add("1");
    menu.add("22");
    menu.add("333");
    menu.addSeparator();
    menu.add("4444");
    menu.add("55555");
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
