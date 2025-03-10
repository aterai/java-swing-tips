// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Arrays;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabs = new JTabbedPane() {
      @Override public void addTab(String title, Icon icon, Component component) {
        super.addTab(title, icon, component, title);
      }

      @Override public String getToolTipText(MouseEvent e) {
        String tip = super.getToolTipText(e);
        int idx = indexAtLocation(e.getX(), e.getY());
        if (idx >= 0 && isHorizontalTabPlacement()) {
          int run = getRunForTab(getTabCount(), idx);
          tip = String.format("%s: Run: %d", tip, run);
        }
        return tip;
      }

      private int getRunForTab(int tabCount, int tabIndex) {
        int runCount = getTabRunCount();
        Rectangle taRect = getTabAreaRect(tabCount);
        int runHeight = taRect.height / runCount;
        Rectangle tabRect = getBoundsAt(tabIndex);
        Point2D pt = new Point2D.Double(tabRect.getCenterX(), tabRect.getCenterY());
        Rectangle runRect = new Rectangle(taRect.x, taRect.y, taRect.width, runHeight);
        int run = -1;
        for (int i = 0; i < runCount; i++) {
          if (runRect.contains(pt)) {
            run = i;
          }
          runRect.translate(0, runHeight);
        }
        return getTabPlacement() == TOP ? runCount - run - 1 : run;
      }

      private Rectangle getTabAreaRect(int tabCount) {
        Rectangle rect = getBoundsAt(0);
        for (int i = 0; i < tabCount; i++) {
          rect.add(getBoundsAt(i));
        }
        return rect;
      }

      private boolean isHorizontalTabPlacement() {
        return getTabPlacement() == TOP || getTabPlacement() == BOTTOM;
      }
    };
    tabs.addTab("111111111111111111111111", new ColorIcon(Color.RED), new JLabel());
    tabs.addTab("2", new ColorIcon(Color.GREEN), new JLabel());
    tabs.addTab("33333333333333333333333333333", new ColorIcon(Color.BLUE), new JLabel());
    tabs.addTab("444444444444", new ColorIcon(Color.ORANGE), new JLabel());
    tabs.addTab("55555555555555555", new ColorIcon(Color.YELLOW), new JLabel());
    add(tabs);
    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    mb.add(makeTabPlacementMenu(tabs));
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JMenu makeTabPlacementMenu(JTabbedPane tabs) {
    ButtonGroup group = new ButtonGroup();
    ItemListener handler = e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        ButtonModel m = group.getSelection();
        TabPlacement tp = TabPlacement.valueOf(m.getActionCommand());
        tabs.setTabPlacement(tp.getPlacement());
      }
    };
    JMenu menu = new JMenu("TabPlacement");
    Arrays.asList(TabPlacement.values()).forEach(tp -> {
      String name = tp.name();
      boolean selected = tp == TabPlacement.TOP;
      JMenuItem item = new JRadioButtonMenuItem(name, selected);
      item.addItemListener(handler);
      item.setActionCommand(name);
      menu.add(item);
      group.add(item);
    });
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

class ColorIcon implements Icon {
  private final Color color;

  protected ColorIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(color);
    g2.fillRect(0, 0, getIconWidth(), getIconHeight());
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 12;
  }

  @Override public int getIconHeight() {
    return 12;
  }
}

enum TabPlacement {
  TOP(SwingConstants.TOP),
  LEFT(SwingConstants.LEFT),
  BOTTOM(SwingConstants.BOTTOM),
  RIGHT(SwingConstants.RIGHT);

  private final int placement;

  TabPlacement(int placement) {
    this.placement = placement;
  }

  public int getPlacement() {
    return placement;
  }
}

// @see SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = makeButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton makeButton(UIManager.LookAndFeelInfo info) {
    boolean selected = info.getClassName().equals(lookAndFeel);
    return new JRadioButtonMenuItem(info.getName(), selected);
  }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        ex.printStackTrace();
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
