// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabs = new JTabbedPane();
    tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    String help = "Ctrl + ScrollButton Click: scroll to first/last tabs";
    tabs.addTab("title0", new JLabel(help));
    IntStream.range(1, 100).forEach(i -> tabs.addTab("title" + i, new JLabel("label" + i)));

    ActionMap am = tabs.getActionMap();
    String forward = "scrollTabsForwardAction";
    am.put(forward, new ScrollTabsAction(tabs, forward));

    String backward = "scrollTabsBackwardAction";
    am.put(backward, new ScrollTabsAction(tabs, backward));

    add(tabs);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class ScrollTabsAction extends AbstractAction {
  private final JTabbedPane tabbedPane;
  private final String direction;
  private final int index;

  protected ScrollTabsAction(JTabbedPane tabbedPane, String direction) {
    super();
    this.tabbedPane = tabbedPane;
    this.direction = direction;
    this.index = "scrollTabsForwardAction".equals(direction) ? tabbedPane.getTabCount() - 1 : 0;
  }

  @Override public void actionPerformed(ActionEvent e) {
    Action action = tabbedPane.getActionMap().get(direction);
    if (action != null && action.isEnabled()) {
      boolean isCtrlDown = (e.getModifiers() & ActionEvent.CTRL_MASK) != 0;
      if (isCtrlDown) {
        scrollTabAt(tabbedPane, index);
      } else {
        action.actionPerformed(new ActionEvent(tabbedPane,
            ActionEvent.ACTION_PERFORMED, null,
            e.getWhen(), e.getModifiers()));
      }
    }
  }

  public static void scrollTabAt(JTabbedPane tabbedPane, int index) {
    Component cmp = null;
    for (Component c : tabbedPane.getComponents()) {
      if ("TabbedPane.scrollableViewport".equals(c.getName())) {
        cmp = c;
        break;
      }
    }
    if (cmp instanceof JViewport) {
      JViewport viewport = (JViewport) cmp;
      Dimension d = tabbedPane.getSize();
      Rectangle r = tabbedPane.getBoundsAt(index);
      int gw = (d.width - r.width) / 2;
      r.grow(gw, 0);
      viewport.scrollRectToVisible(r);
    }
  }
}
