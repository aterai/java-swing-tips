// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsMenuItemUI;
import com.sun.java.swing.plaf.windows.WindowsMenuUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.basic.BasicMenuItemUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(createMenuBar()));
    add(new JScrollPane(new JTextArea()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JMenuBar createMenuBar() {
    JMenu menu0 = new JMenu("Default");
    initMenu(menu0);
    // TEST: menu0.setInheritsPopupMenu(true);

    JMenu menu1 = new JMenu("DisableRightClick") {
      @Override public void updateUI() {
        super.updateUI();
        if (getUI() instanceof WindowsMenuUI) {
          setUI(new CustomWindowsMenuUI());
        }
      }

      @Override public JMenuItem add(String s) {
        JMenuItem item = new JMenuItem(s) {
          @Override public void updateUI() {
            super.updateUI();
            if (getUI() instanceof WindowsMenuItemUI) {
              setUI(new CustomWindowsMenuItemUI());
            }
          }
        };
        return add(item);
      }
    };
    initMenu(menu1);
    // TEST: menu1.setInheritsPopupMenu(true);

    JPopupMenu popup = new JPopupMenu();
    popup.add("MenuItem 1"); // .addActionListener(e -> System.out.println("PopupMenu"));
    popup.add("MenuItem 2");
    popup.add("MenuItem 3");

    JMenuBar mb = new JMenuBar();
    mb.setComponentPopupMenu(popup);
    mb.add(menu0);
    mb.add(menu1);
    return mb;
  }

  private static void initMenu(JMenu menu) {
    menu.add("MenuItem 1"); // .addActionListener(e -> System.out.println("MenuBar"));
    menu.addSeparator();
    menu.add("MenuItem 2");
    menu.add("MenuItem 3");
    menu.add("MenuItem 4");
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
      Logger.getGlobal().severe(ex::getMessage);
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

class CustomWindowsMenuUI extends WindowsMenuUI {
  @Override protected MouseInputListener createMouseInputListener(JComponent c) {
    return new BasicMenuItemUI.MouseInputHandler() {
      @Override public void mousePressed(MouseEvent e) {
        if (menuItem instanceof JMenu) {
          JMenu menu = (JMenu) menuItem;
          if (!menu.isEnabled() || SwingUtilities.isRightMouseButton(e)) {
            return;
          }
        }
        super.mousePressed(e);
      }
    };
  }
}

class CustomWindowsMenuItemUI extends WindowsMenuItemUI {
  @Override protected MouseInputListener createMouseInputListener(JComponent c) {
    return new BasicMenuItemUI.MouseInputHandler() {
      @Override public void mouseReleased(MouseEvent e) {
        if (!menuItem.isEnabled() || SwingUtilities.isRightMouseButton(e)) {
          return;
        }
        super.mouseReleased(e);
      }
    };
  }
}

/*
class CustomWindowsMenuUI extends WindowsMenuUI {
  @Override protected MouseInputListener createMouseInputListener(JComponent c) {
    return new BasicMenuItemUI.MouseInputHandler() {
      @Override public void mousePressed(MouseEvent e) {
        MenuSelectionManager manager = MenuSelectionManager.defaultManager();
        if (menu.isTopLevelMenu()) {
          if (menu.isSelected() && menu.getPopupMenu().isShowing()) {
            manager.clearSelectedPath();
          } else {
            Container cnt = menu.getParent();
            if (cnt instanceof JMenuBar) {
              MenuElement[] me = new MenuElement[2];
              me[0] = (MenuElement) cnt;
              me[1] = menu;
              manager.setSelectedPath(me);
            }
          }
        }
        MenuElement[] paths = manager.getSelectedPath();
        if (paths.length > 0 && paths[paths.length - 1] != menu.getPopupMenu()) {
          if (menu.isTopLevelMenu() || menu.getDelay() == 0) {
            MenuElement[] newPath = new MenuElement[paths.length + 1];
            System.arraycopy(paths, 0, newPath, 0, paths.length);
            newPath[paths.length] = menu.getPopupMenu();
            MenuSelectionManager.defaultManager().setSelectedPath(newPath);
          } else {
            setupPostTimer(menu);
          }
        }
      }

      @Override public void mouseReleased(MouseEvent e) {
        JMenu menu = (JMenu) menuItem;
        if (!menu.isEnabled()) {
          return;
        }
        MenuSelectionManager manager = MenuSelectionManager.defaultManager();
        manager.processMouseEvent(e);
        if (!e.isConsumed()) {
          manager.clearSelectedPath();
        }
      }

      @Override public void mouseEntered(MouseEvent e) {
        MenuSelectionManager manager = MenuSelectionManager.defaultManager();
        int modifiers = e.getModifiersEx();
        if ((modifiers & InputEvent.BUTTON1_DOWN_MASK) != 0) {
          MenuSelectionManager.defaultManager().processMouseEvent(e);
        } else {
          manager.setSelectedPath(getPath());
        }
      }

      @Override public void mouseExited(MouseEvent e) {
        MenuSelectionManager manager = MenuSelectionManager.defaultManager();
        int modifiers = e.getModifiersEx();
        if ((modifiers & InputEvent.BUTTON1_DOWN_MASK) != 0) {
          MenuSelectionManager.defaultManager().processMouseEvent(e);
        } else {
          MenuElement[] path = manager.getSelectedPath();
          if (path.length > 1 && path[path.length - 1] == menuItem) {
            MenuElement[] newPath = new MenuElement[path.length - 1];
            System.arraycopy(path, 0, newPath, 0, path.length - 1);
            manager.setSelectedPath(newPath);
          }
        }
      }

      @Override public void mouseDragged(MouseEvent e) {
        MenuSelectionManager.defaultManager().processMouseEvent(e);
      }
    };
  }
}

class CustomWindowsMenuItemUI extends WindowsMenuItemUI {
  @Override protected MouseInputListener createMouseInputListener(JComponent c) {
    return new BasicMenuItemUI.MouseInputHandler() {
      @Override public void mouseReleased(MouseEvent e) {
        MenuSelectionManager manager = MenuSelectionManager.defaultManager();
        Point p = e.getPoint();
        if (p.x >= 0 && p.x < menuItem.getWidth() && p.y >= 0 && p.y < menuItem.getHeight()) {
          doClick(manager);
        } else {
          manager.processMouseEvent(e);
        }
      }

      @Override public void mouseEntered(MouseEvent e) {
        MenuSelectionManager manager = MenuSelectionManager.defaultManager();
        int modifiers = e.getModifiersEx();
        if ((modifiers & InputEvent.BUTTON1_DOWN_MASK) != 0) {
          MenuSelectionManager.defaultManager().processMouseEvent(e);
        } else {
          manager.setSelectedPath(getPath());
        }
      }

      @Override public void mouseExited(MouseEvent e) {
        MenuSelectionManager manager = MenuSelectionManager.defaultManager();
        int modifiers = e.getModifiersEx();
        if ((modifiers & InputEvent.BUTTON1_DOWN_MASK) != 0) {
          MenuSelectionManager.defaultManager().processMouseEvent(e);
        } else {
          MenuElement[] path = manager.getSelectedPath();
          if (path.length > 1 && path[path.length - 1] == menuItem) {
            MenuElement[] newPath = new MenuElement[path.length - 1];
            System.arraycopy(path, 0, newPath, 0, path.length - 1);
            manager.setSelectedPath(newPath);
          }
        }
      }

      @Override public void mouseDragged(MouseEvent e) {
        MenuSelectionManager.defaultManager().processMouseEvent(e);
      }
    };
  }
}
*/
