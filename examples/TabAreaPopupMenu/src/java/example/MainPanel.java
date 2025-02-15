// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    add(new JLayer<>(makeTabbedPane(), new DebugLayerUI()));
    setPreferredSize(new Dimension(320, 240));
  }

  private JTabbedPane makeTabbedPane() {
    // UIManager.put("TabbedPane.tabAreaInsets", new Insets(5, 10, 15, 20));
    // UIManager.put("TabbedPane.contentBorderInsets", new Insets(5, 10, 15, 20));
    JPopupMenu popup1 = makeTabPopupMenu();
    JPopupMenu popup2 = makeTabAreaPopupMenu();
    JTabbedPane tabbedPane = new JTabbedPane() {
      @Override public void updateUI() {
        super.updateUI();
        EventQueue.invokeLater(() -> {
          SwingUtilities.updateComponentTreeUI(popup1);
          SwingUtilities.updateComponentTreeUI(popup2);
          setComponentPopupMenu(popup1);
        });
      }

      @Override public Point getPopupLocation(MouseEvent e) {
        int idx = indexAtLocation(e.getX(), e.getY());
        if (idx < 0 && TabbedPaneUtils.getTabAreaBounds(this).contains(e.getPoint())) {
          setComponentPopupMenu(popup2);
        } else {
          setComponentPopupMenu(popup1);
        }
        return super.getPopupLocation(e);
      }
    };
    tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    tabbedPane.addTab("Title: 0", new JScrollPane(new JTextArea()));
    return tabbedPane;
  }

  // private static Stream<Component> descendants(Container parent) {
  //   return Stream.of(parent.getComponents())
  //       .filter(Container.class::isInstance).map(Container.class::cast)
  //       .flatMap(c -> Stream.concat(Stream.of(c), descendants(c)));
  // }

  // // @see BasicTabbedPaneUI#rotateInsets(...)
  // static void rotateInsets(Insets topInsets, Insets targetInsets, int targetPlacement) {
  //   switch(targetPlacement) {
  //     case SwingConstants.LEFT:
  //       targetInsets.top = topInsets.left;
  //       targetInsets.left = topInsets.top;
  //       targetInsets.bottom = topInsets.right;
  //       targetInsets.right = topInsets.bottom;
  //       break;
  //     case SwingConstants.BOTTOM:
  //       targetInsets.top = topInsets.bottom;
  //       targetInsets.left = topInsets.left;
  //       targetInsets.bottom = topInsets.top;
  //       targetInsets.right = topInsets.right;
  //       break;
  //     case SwingConstants.RIGHT:
  //       targetInsets.top = topInsets.left;
  //       targetInsets.left = topInsets.bottom;
  //       targetInsets.bottom = topInsets.right;
  //       targetInsets.right = topInsets.top;
  //       break;
  //     case SwingConstants.TOP:
  //     default:
  //       targetInsets.top = topInsets.top;
  //       targetInsets.left = topInsets.left;
  //       targetInsets.bottom = topInsets.bottom;
  //       targetInsets.right = topInsets.right;
  //   }
  // }

  // private static Rectangle getTabAreaBounds2(JTabbedPane tabbedPane) {
  //   JComponent c = descendants(tabbedPane)
  //       .filter(JViewport.class::isInstance)
  //       .map(JComponent.class::cast)
  //       .filter(v -> "TabbedPane.scrollableViewport".equals(v.getName()))
  //       .findFirst().orElse(null);
  //   Rectangle r = SwingUtilities.calculateInnerArea(c, null);
  //
  //   // Note: BasicTabbedPaneUI#getTabAreaInsets() causes rotation.
  //   Insets tabAreaInsets = UIManager.getInsets("TabbedPane.tabAreaInsets");
  //   Insets targetInsets = new Insets(0, 0, 0, 0);
  //   rotateInsets(tabAreaInsets, targetInsets, tabbedPane.getTabPlacement());
  //   if (r != null) {
  //     r.x += tabAreaInsets.left;
  //     r.y += tabAreaInsets.top;
  //     r.width -= tabAreaInsets.left + tabAreaInsets.right;
  //     r.height -= tabAreaInsets.top + tabAreaInsets.bottom;
  //   }
  //   return r;
  // }

  private static JPopupMenu makeTabPopupMenu() {
    JPopupMenu popup = new JPopupMenu();
    popup.add("New tab").addActionListener(e -> {
      JTabbedPane tabs = (JTabbedPane) popup.getInvoker();
      String title = "Title: " + tabs.getTabCount();
      tabs.addTab(title, new JScrollPane(new JTextArea()));
      tabs.setSelectedIndex(tabs.getTabCount() - 1);
    });
    popup.addSeparator();
    JMenuItem rename = popup.add("Rename");
    rename.addActionListener(e -> renameTab(popup));
    popup.addSeparator();
    JMenuItem close = popup.add("Close");
    close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.CTRL_DOWN_MASK));
    close.addActionListener(e -> {
      JTabbedPane tabs = (JTabbedPane) popup.getInvoker();
      tabs.remove(tabs.getSelectedIndex());
    });
    JMenuItem closeAll = popup.add("Close all");
    closeAll.addActionListener(e -> {
      JTabbedPane tabs = (JTabbedPane) popup.getInvoker();
      tabs.removeAll();
    });
    JMenuItem closeAllButActive = popup.add("Close all bat active");
    closeAllButActive.addActionListener(e -> closeAllButActiveTab(popup));
    return popup;
  }

  private static void renameTab(JPopupMenu popup) {
    JTabbedPane tabs = (JTabbedPane) popup.getInvoker();
    String name = tabs.getTitleAt(tabs.getSelectedIndex());
    JTextField textField = new JTextField(name);
    int result = JOptionPane.showConfirmDialog(
        tabs, textField, "Rename",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (result == JOptionPane.OK_OPTION) {
      String str = textField.getText().trim();
      if (!str.equals(name)) {
        tabs.setTitleAt(tabs.getSelectedIndex(), str);
      }
    }
  }

  private static void closeAllButActiveTab(JPopupMenu popup) {
    JTabbedPane tabs = (JTabbedPane) popup.getInvoker();
    int idx = tabs.getSelectedIndex();
    String title = tabs.getTitleAt(idx);
    Component cmp = tabs.getComponentAt(idx);
    tabs.removeAll();
    tabs.addTab(title, cmp);
  }

  private static JPopupMenu makeTabAreaPopupMenu() {
    JPopupMenu popup = new JPopupMenu();
    popup.add("New tab").addActionListener(e -> {
      JTabbedPane tabs = (JTabbedPane) popup.getInvoker();
      String title = "Title: " + tabs.getTabCount();
      tabs.addTab(title, new JScrollPane(new JTextArea()));
      tabs.setSelectedIndex(tabs.getTabCount() - 1);
    });
    popup.addSeparator();
    ButtonGroup group = new ButtonGroup();
    ItemListener handler = e -> {
      Component c = popup.getInvoker();
      if (e.getStateChange() == ItemEvent.SELECTED && c instanceof JTabbedPane) {
        ButtonModel m = group.getSelection();
        TabPlacement tp = TabPlacement.valueOf(m.getActionCommand());
        ((JTabbedPane) c).setTabPlacement(tp.getPlacement());
      }
    };
    Arrays.asList(TabPlacement.values()).forEach(tp -> {
      String name = tp.name();
      boolean selected = tp == TabPlacement.TOP;
      JMenuItem item = new JRadioButtonMenuItem(name, selected);
      item.addItemListener(handler);
      item.setActionCommand(name);
      popup.add(item);
      group.add(item);
    });
    return popup;
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

final class DebugLayerUI extends LayerUI<JTabbedPane> {
  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (c instanceof JLayer) {
      JLayer<?> layer = (JLayer<?>) c;
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setPaint(Color.RED);
      Rectangle r = TabbedPaneUtils.getTabAreaBounds((JTabbedPane) layer.getView());
      g2.drawRect(r.x, r.y, r.width - 1, r.height - 1);
      // g2.setPaint(Color.GREEN);
      // Rectangle r2 = getTabAreaBounds2((JTabbedPane) layer.getView());
      // g2.drawRect(r2.x, r2.y, r2.width - 1, r2.height - 1);
      g2.dispose();
    }
  }
}

final class TabbedPaneUtils {
  private TabbedPaneUtils() {
    /* Singleton */
  }

  public static Rectangle getTabAreaBounds(JTabbedPane tabbedPane) {
    Rectangle r = SwingUtilities.calculateInnerArea(tabbedPane, null);
    Rectangle cr = Optional.ofNullable(tabbedPane.getSelectedComponent())
        .map(Component::getBounds)
        .orElseGet(Rectangle::new);
    int tp = tabbedPane.getTabPlacement();
    Insets i1 = UIManager.getInsets("TabbedPane.tabAreaInsets");
    Insets i2 = UIManager.getInsets("TabbedPane.contentBorderInsets");
    if (tp == SwingConstants.TOP || tp == SwingConstants.BOTTOM) {
      r.height -= cr.height + i1.top + i1.bottom + i2.top + i2.bottom;
      // r.x += i1.left;
      r.y += tp == SwingConstants.TOP ? i1.top : cr.y + cr.height + i1.bottom + i2.bottom;
    } else {
      r.width -= cr.width + i1.top + i1.bottom + i2.left + i2.right;
      r.x += tp == SwingConstants.LEFT ? i1.top : cr.x + cr.width + i1.bottom + i2.right;
      // r.y += i1.left;
    }
    return r;
  }
}

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
        Logger.getGlobal().severe(ex::getMessage);
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
