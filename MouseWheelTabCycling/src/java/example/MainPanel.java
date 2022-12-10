// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Optional;
import java.util.stream.IntStream;
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
        tabbedPane.setTabPlacement(((TabPlacements) e.getItem()).tabPlacement);
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

  private class TabWheelHandler implements MouseWheelListener {
    @Override public void mouseWheelMoved(MouseWheelEvent e) {
      JTabbedPane src = (JTabbedPane) e.getComponent();
      if (!getTabAreaBounds(src).contains(e.getPoint())) {
        return;
      }
      boolean dir = (e.isControlDown() ? -1 : 1) * e.getPreciseWheelRotation() > 0;
      int id = ActionEvent.ACTION_PERFORMED;
      String cmd;
      if (check.isSelected()) {
        cmd = dir ? "scrollTabsForwardAction" : "scrollTabsBackwardAction";
      } else {
        cmd = dir ? "navigateNext" : "navigatePrevious";
      }
      ActionEvent event = new ActionEvent(src, id, cmd, e.getWhen(), e.getModifiersEx());
      src.getActionMap().get(cmd).actionPerformed(event);
      // int idx = src.getSelectedIndex() + (int) dir;
      // if (idx < 0) {
      //   idx = src.getTabCount() - 1;
      // } else if (idx >= src.getTabCount()) {
      //   idx = 0;
      // }
      // src.setSelectedIndex(idx);
    }
  }

  public static Rectangle getTabAreaBounds(JTabbedPane tabbedPane) {
    Rectangle tabbedRect = tabbedPane.getBounds();
    int xx = tabbedRect.x;
    int yy = tabbedRect.y;
    Rectangle compRect = Optional.ofNullable(tabbedPane.getSelectedComponent())
        .map(Component::getBounds).orElseGet(Rectangle::new);
    int tabPlacement = tabbedPane.getTabPlacement();
    if (isTopBottomTabPlacement(tabPlacement)) {
      tabbedRect.height = tabbedRect.height - compRect.height;
      if (tabPlacement == SwingConstants.BOTTOM) {
        tabbedRect.y += compRect.y + compRect.height;
      }
    } else {
      tabbedRect.width = tabbedRect.width - compRect.width;
      if (tabPlacement == SwingConstants.RIGHT) {
        tabbedRect.x += compRect.x + compRect.width;
      }
    }
    tabbedRect.translate(-xx, -yy);
    return tabbedRect;
  }

  private static boolean isTopBottomTabPlacement(int tabPlacement) {
    return tabPlacement == SwingConstants.TOP || tabPlacement == SwingConstants.BOTTOM;
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

enum TabPlacements {
  TOP(SwingConstants.TOP),
  BOTTOM(SwingConstants.BOTTOM),
  LEFT(SwingConstants.LEFT),
  RIGHT(SwingConstants.RIGHT);
  public final int tabPlacement;

  TabPlacements(int tabPlacement) {
    this.tabPlacement = tabPlacement;
  }
}
