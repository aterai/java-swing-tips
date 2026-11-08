// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(makeMenuBar()));
    add(new JScrollPane(new JTextArea()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JMenuBar makeMenuBar() {
    JMenuBar menuBar = new JMenuBar() {
      @Override public void updateUI() {
        super.updateUI();
        LayoutManager lm = getLayout();
        if (lm instanceof OverflowMenuLayout) {
          // The overflow menu may not be in the menu bar, update it explicitly
          JMenu overflowMenu = ((OverflowMenuLayout) lm).getOverflowMenu();
          SwingUtilities.updateComponentTreeUI(overflowMenu);
        }
      }
    };
    menuBar.setLayout(new OverflowMenuLayout());
    menuBar.add(makeOrientationMenu(menuBar));
    menuBar.add(makeMenu("JMenu1"));
    menuBar.add(makeMenu("JMenu2"));
    menuBar.add(makeMenu("JMenu3"));
    menuBar.add(makeMenu("JMenu4"));
    menuBar.add(makeMenu("JMenu5"));
    menuBar.add(makeMenu("JMenu6"));
    menuBar.add(makeMenu("JMenu7"));
    menuBar.add(makeMenu("JMenu8"));
    return menuBar;
  }

  private static JMenu makeOrientationMenu(JMenuBar menuBar) {
    JMenu menu = new JMenu("ComponentOrientation");
    ButtonGroup bg = new ButtonGroup();
    ItemListener handler = e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        String name = bg.getSelection().getActionCommand();
        ComponentOrientation o = Objects.equals(name, "LEFT_TO_RIGHT")
            ? ComponentOrientation.LEFT_TO_RIGHT
            : ComponentOrientation.RIGHT_TO_LEFT;
        menuBar.applyComponentOrientation(o);
        menuBar.revalidate();
      }
    };
    boolean isLeftToRight = menuBar.getComponentOrientation().isLeftToRight();
    Arrays.asList(
        new JRadioButtonMenuItem("LEFT_TO_RIGHT", isLeftToRight),
        new JRadioButtonMenuItem("RIGHT_TO_LEFT", !isLeftToRight)
    ).forEach(rb -> {
      bg.add(rb);
      menu.add(rb);
      rb.setActionCommand(rb.getText());
      rb.addItemListener(handler);
    });
    return menu;
  }

  private static JMenu makeMenu(String text) {
    JMenu menu = new JMenu(text);
    menu.add("JMenuItem1");
    menu.add("JMenuItem2");
    menu.addSeparator();
    menu.add(new JCheckBoxMenuItem("JCheckBoxMenuItem"));
    menu.add(new JRadioButtonMenuItem("JRadioButtonMenuItem"));
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

class OverflowMenuLayout extends FlowLayout {
  private final JMenu overflowMenu = new JMenu("...");

  protected OverflowMenuLayout() {
    super(LEADING, 0, 0);
  }

  public JMenu getOverflowMenu() {
    return overflowMenu;
  }

  @Override public void layoutContainer(Container target) {
    // Move all menus back to the menu bar before laying them out, even if
    // preferredLayoutSize(...) has not been called since the last layout
    restoreOverflowedMenus(target);
    super.layoutContainer(target);
    Rectangle r = SwingUtilities.calculateInnerArea((JComponent) target, null);
    int num = target.getComponentCount();
    // The menus wrapped to the second row or later overflow the menu bar
    if (num > 0 && target.getComponent(num - 1).getY() > r.y + getVgap()) {
      // Add the overflow menu before getting its preferred size, since a JMenu
      // that is not in a JMenuBar is measured as a (larger) submenu item
      target.add(overflowMenu);
      Dimension d = overflowMenu.getPreferredSize();
      int x;
      if (target.getComponentOrientation().isLeftToRight()) {
        x = r.x + r.width - d.width;
      } else {
        x = r.x;
      }
      overflowMenu.setBounds(x, r.y, d.width, d.height);
      Arrays.stream(target.getComponents())
          .filter(c -> shouldMoveToOverflowMenu(r, c))
          .forEach(overflowMenu::add);
      // `getPopupMenu().pack()` is not necessary in Java 8,
      // but if you do not do this in Java 21,
      // the JPopupMenu may not be displayed correctly.
      overflowMenu.getPopupMenu().pack();
    }
  }

  private boolean shouldMoveToOverflowMenu(Rectangle r, Component c) {
    // Rectangle#intersects(...) excludes menus that are only adjacent to the overflow menu
    boolean overlapped = !Objects.equals(c, overflowMenu)
        && c.getBounds().intersects(overflowMenu.getBounds());
    return c.getY() > r.y + getVgap() || overlapped;
  }

  private void restoreOverflowedMenus(Container target) {
    target.remove(overflowMenu);
    for (Component c : overflowMenu.getMenuComponents()) {
      target.add(c);
    }
  }

  @Override public Dimension preferredLayoutSize(Container target) {
    restoreOverflowedMenus(target);
    return super.preferredLayoutSize(target);
  }
}
