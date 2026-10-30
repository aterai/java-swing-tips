// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public final class MainPanel extends JPanel {
  private final JTextArea log = new JTextArea();

  private MainPanel() {
    super(new BorderLayout());
    log.setEditable(false);
    JToolBar toolBar = new JToolBar();
    Icon listIcon = UIManager.getIcon("FileChooser.listViewIcon");
    toolBar.add(createButton(listIcon, createExitClosePopupMenu()));
    toolBar.addSeparator(new Dimension(25, 25));
    Icon detailsIcon = UIManager.getIcon("FileChooser.detailsViewIcon");
    toolBar.add(createButton(detailsIcon, createAutoClosePopupMenu()));
    add(toolBar, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));
  }

  private JPopupMenu createExitClosePopupMenu() {
    JPopupMenu popup = new JPopupMenu();
    initPopupMenu(popup);
    popup.addMouseListener(new MouseAdapter() {
      @Override public void mouseExited(MouseEvent e) {
        // mouseExited is also fired when the cursor moves onto a child JMenuItem,
        // so wait until the armed state is updated and close only if no item is armed
        EventQueue.invokeLater(() -> {
          boolean isArmed = Stream.of(popup.getSubElements())
              .filter(AbstractButton.class::isInstance)
              .map(AbstractButton.class::cast)
              .map(AbstractButton::getModel)
              .anyMatch(ButtonModel::isArmed);
          if (!isArmed) {
            popup.setVisible(false);
          }
        });
      }
    });
    return popup;
  }

  private JPopupMenu createAutoClosePopupMenu() {
    JPopupMenu popup = new AutoClosePopupMenu();
    initPopupMenu(popup);
    return popup;
  }

  private static JButton createButton(Icon icon, JPopupMenu popup) {
    JButton button = new JButton(icon);
    button.setFocusPainted(false);
    button.addActionListener(e -> {
      popup.show(button, 0, button.getHeight());
      popup.requestFocusInWindow();
    });
    button.addMouseListener(new MouseAdapter() {
      @Override public void mouseEntered(MouseEvent e) {
        button.doClick();
      }
    });
    return button;
  }

  private void initPopupMenu(JPopupMenu popup) {
    ButtonGroup group = new ButtonGroup();
    createMenuList().stream()
        .map(MainPanel::createMenuItem)
        .forEach(item -> {
          item.addActionListener(e -> {
            String cmd = e.getActionCommand();
            log.append(String.format("Selected JRadioButton command: %s%n", cmd));
          });
          popup.add(item);
          group.add(item);
        });
  }

  private static AbstractButton createMenuItem(MenuContext context) {
    JRadioButtonMenuItem item = new JRadioButtonMenuItem(context.getCommand());
    item.setActionCommand(context.getCommand());
    item.setForeground(context.getColor());
    item.setBorder(BorderFactory.createEmptyBorder());
    return item;
  }

  private static List<MenuContext> createMenuList() {
    return Arrays.asList(
        new MenuContext("BLUE", Color.BLUE),
        new MenuContext("CYAN", Color.CYAN),
        new MenuContext("GREEN", Color.GREEN),
        new MenuContext("MAGENTA", Color.MAGENTA),
        new MenuContext("ORANGE", Color.ORANGE),
        new MenuContext("PINK", Color.PINK),
        new MenuContext("RED", Color.RED),
        new MenuContext("YELLOW", Color.YELLOW));
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

class AutoClosePopupMenu extends JPopupMenu {
  private transient PopupMenuListener listener;

  @Override public void updateUI() {
    removePopupMenuListener(listener);
    super.updateUI();
    listener = new AwtPopupMenuListener();
    addPopupMenuListener(listener);
  }

  private void checkAutoClose(MouseEvent e) {
    Component c = e.getComponent();
    // The mouse point is converted to this popup's coordinate system,
    // so compare it with the local bounds rather than getBounds(),
    // which depends on the container created by the PopupFactory.
    Rectangle r = new Rectangle(getSize());
    // Allow a small vertical gap between the invoker and this popup
    r.grow(0, 5);
    Point pt = SwingUtilities.convertPoint(c, e.getPoint(), this);
    if (!r.contains(pt) && !(c instanceof JButton)) {
      setVisible(false);
    }
  }

  private final class AwtPopupMenuListener implements PopupMenuListener {
    private final AWTEventListener handler = e -> {
      if (e instanceof MouseEvent) {
        int id = e.getID();
        if (id == MouseEvent.MOUSE_MOVED || id == MouseEvent.MOUSE_EXITED) {
          checkAutoClose((MouseEvent) e);
        }
      }
    };

    @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
      long mask = AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK;
      Toolkit.getDefaultToolkit().addAWTEventListener(handler, mask);
    }

    @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
      Toolkit.getDefaultToolkit().removeAWTEventListener(handler);
    }

    @Override public void popupMenuCanceled(PopupMenuEvent e) {
      /* not needed */
    }
  }
}

class MenuContext {
  private final String command;
  private final Color color;

  protected MenuContext(String cmd, Color c) {
    command = cmd;
    color = c;
  }

  public String getCommand() {
    return command;
  }

  public Color getColor() {
    return color;
  }
}
