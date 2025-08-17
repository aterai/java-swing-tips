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
          JMenu popupButton = ((OverflowMenuLayout) lm).overflowMenu;
          SwingUtilities.updateComponentTreeUI(popupButton);
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
    boolean b = menuBar.getComponentOrientation().isLeftToRight();
    Arrays.asList(
        new JRadioButtonMenuItem("LEFT_TO_RIGHT", b),
        new JRadioButtonMenuItem("RIGHT_TO_LEFT", !b)
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
  public final JMenu overflowMenu = new JMenu("...");

  protected OverflowMenuLayout() {
    super(LEADING, 0, 0);
  }

  @Override public void layoutContainer(Container target) {
    super.layoutContainer(target);
    Rectangle r = SwingUtilities.calculateInnerArea((JComponent) target, null);
    int num = target.getComponentCount();
    if (target.getComponent(num - 1).getY() > r.y + getVgap()) {
      target.add(overflowMenu);
      overflowMenu.setSize(overflowMenu.getPreferredSize());
      int popupX;
      if (target.getComponentOrientation().isLeftToRight()) {
        popupX = r.x + r.width - overflowMenu.getSize().width;
      } else {
        popupX = r.x;
      }
      overflowMenu.setLocation(popupX, r.y);
      overflowMenu.setVisible(true);
    }
    if (target.isAncestorOf(overflowMenu)) {
      Arrays.stream(target.getComponents())
          .filter(c -> shouldMoveToPopup(target, c))
          .forEach(overflowMenu::add);
      // `getPopupMenu().pack()` is not necessary in Java 8,
      // but if you do not do this in Java 21,
      // the JPopupMenu may not be displayed correctly.
      overflowMenu.getPopupMenu().pack();
    }
  }

  private boolean shouldMoveToPopup(Container target, Component c) {
    Insets insets = target.getInsets();
    int y = insets.top + getVgap();
    Point pt = overflowMenu.getLocation();
    if (!target.getComponentOrientation().isLeftToRight()) {
      pt.x += overflowMenu.getWidth();
    }
    boolean b = !Objects.equals(c, overflowMenu) && c.getBounds().contains(pt);
    return c.getY() > y || b;
  }

  @Override public Dimension preferredLayoutSize(Container target) {
    overflowMenu.setVisible(false);
    target.remove(overflowMenu);
    for (Component c : overflowMenu.getPopupMenu().getComponents()) {
      target.add(c);
    }
    overflowMenu.removeAll();
    return super.preferredLayoutSize(target);
  }
}
