// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final JCheckBox check = new JCheckBox("scroll tabs");

  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT) {
      private transient MouseWheelListener handler;

      @Override public void updateUI() {
        removeMouseWheelListener(handler);
        super.updateUI();
        handler = new TabWheelHandler();
        addMouseWheelListener(handler);
      }
    };
    tabbedPane.addTab("JLabel1", new JLabel("JLabel1"));
    tabbedPane.addTab("JLabel2", new JLabel("JLabel2"));
    tabbedPane.addTab("JLabel(disabled)", new JLabel("JLabel"));
    tabbedPane.setEnabledAt(2, false);
    tabbedPane.addTab("JSplitPane", new JSplitPane());
    tabbedPane.addTab("JPanel", new JLabel("JPanel"));
    tabbedPane.addTab("JTree", new JScrollPane(new JTree()));
    tabbedPane.addTab("JTextArea", new JScrollPane(new JTextArea("JTextArea")));
    IntStream.range(0, 20).forEach(i -> {
      String title = "title" + i;
      tabbedPane.addTab(title, new JScrollPane(new JLabel(title)));
    });

    JComboBox<? extends Enum<?>> comboBox = new JComboBox<>(TabPlacements.values());
    comboBox.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        tabbedPane.setTabPlacement(((TabPlacements) e.getItem()).getTabPlacement());
      }
    });
    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(check);
    box.add(new JLabel("TabPlacement: "));
    box.add(Box.createHorizontalStrut(2));
    box.add(comboBox);
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    add(tabbedPane);
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private final class TabWheelHandler implements MouseWheelListener {
    @Override public void mouseWheelMoved(MouseWheelEvent e) {
      JTabbedPane src = (JTabbedPane) e.getComponent();
      boolean policy = src.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT;
      if (policy && getTabAreaBounds(src).contains(e.getPoint())) {
        boolean dir = (e.isControlDown() ? -1 : 1) * e.getPreciseWheelRotation() > 0;
        String cmd;
        if (check.isSelected()) {
          cmd = dir ? "scrollTabsForwardAction" : "scrollTabsBackwardAction";
        } else {
          cmd = dir ? "navigateNext" : "navigatePrevious";
        }
        int id = ActionEvent.ACTION_PERFORMED;
        ActionEvent event = new ActionEvent(src, id, cmd, e.getWhen(), e.getModifiersEx());
        src.getActionMap().get(cmd).actionPerformed(event);
      }
      // int idx = src.getSelectedIndex() + (int) dir;
      // if (idx < 0) {
      //   idx = src.getTabCount() - 1;
      // } else if (idx >= src.getTabCount()) {
      //   idx = 0;
      // }
      // src.setSelectedIndex(idx);
    }
  }

  private static Stream<Component> descendants(Container parent) {
    return Stream.of(parent.getComponents())
        .filter(Container.class::isInstance).map(Container.class::cast)
        .flatMap(c -> Stream.concat(Stream.of(c), descendants(c)));
  }

  private static Rectangle getTabAreaBounds(JTabbedPane tabbedPane) {
    return descendants(tabbedPane)
        .filter(JViewport.class::isInstance)
        .map(JComponent.class::cast)
        .filter(v -> "TabbedPane.scrollableViewport".equals(v.getName()))
        .findFirst()
        .map(v -> {
          Rectangle r = SwingUtilities.calculateInnerArea(v, null);
          return SwingUtilities.convertRectangle(v, r, tabbedPane);
        })
        .orElseGet(Rectangle::new);
  }

  // private static boolean isTopBottomTabPlacement(int tabPlacement) {
  //   return tabPlacement == SwingConstants.TOP || tabPlacement == SwingConstants.BOTTOM;
  // }

  // public static Rectangle getTabAreaBounds(JTabbedPane tabbedPane) {
  //   Rectangle tabbedRect = SwingUtilities.calculateInnerArea(tabbedPane, null);
  //   Rectangle compRect = Optional.ofNullable(tabbedPane.getSelectedComponent())
  //       .map(Component::getBounds).orElseGet(Rectangle::new);
  //   int tabPlacement = tabbedPane.getTabPlacement();
  //   if (isTopBottomTabPlacement(tabPlacement)) {
  //     tabbedRect.height -= compRect.height;
  //     if (tabPlacement == SwingConstants.BOTTOM) {
  //       tabbedRect.y += compRect.y + compRect.height;
  //     }
  //   } else {
  //     tabbedRect.width -= compRect.width;
  //     if (tabPlacement == SwingConstants.RIGHT) {
  //       tabbedRect.x += compRect.x + compRect.width;
  //     }
  //   }
  //   return tabbedRect;
  // }

  // private static Rectangle getTabAreaBoundsExcludeInsets(JTabbedPane tabbedPane) {
  //   Rectangle r = SwingUtilities.calculateInnerArea(tabbedPane, null);
  //   Rectangle cr = Optional.ofNullable(tabbedPane.getSelectedComponent())
  //       .map(Component::getBounds)
  //       .orElseGet(Rectangle::new);
  //   int tp = tabbedPane.getTabPlacement();
  //   Insets i1 = UIManager.getInsets("TabbedPane.tabAreaInsets");
  //   Insets i2 = UIManager.getInsets("TabbedPane.contentBorderInsets");
  //   if (isTopBottomTabPlacement(tp)) {
  //     r.height -= cr.height + i1.top + i1.bottom + i2.top + i2.bottom;
  //     r.x += i1.left;
  //     r.y += tp == SwingConstants.TOP ? i1.top : cr.y + cr.height + i1.bottom + i2.bottom;
  //   } else {
  //     r.width -= cr.width + i1.top + i1.bottom + i2.left + i2.right;
  //     r.x += tp == SwingConstants.LEFT ? i1.top : cr.x + cr.width + i1.bottom + i2.right;
  //     r.y += i1.left;
  //   }
  //   return r;
  // }

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

enum TabPlacements {
  TOP(SwingConstants.TOP),
  BOTTOM(SwingConstants.BOTTOM),
  LEFT(SwingConstants.LEFT),
  RIGHT(SwingConstants.RIGHT);
  private final int tabPlacement;

  TabPlacements(int tabPlacement) {
    this.tabPlacement = tabPlacement;
  }

  public int getTabPlacement() {
    return tabPlacement;
  }
}
