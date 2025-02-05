// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Objects;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabs = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
    String help = "Ctrl + ScrollButton Click: scroll to first/last tabs";
    tabs.addTab("title0", new JLabel(help));
    IntStream.range(1, 100).forEach(i -> tabs.addTab("title" + i, new JLabel("label" + i)));

    ActionMap am = tabs.getActionMap();
    String forward = "scrollTabsForwardAction";
    am.put(forward, new ScrollTabsAction(tabs, am.get(forward)));

    String backward = "scrollTabsBackwardAction";
    am.put(backward, new ScrollTabsAction(tabs, am.get(backward)));

    add(tabs);
    setPreferredSize(new Dimension(320, 240));
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

class ScrollTabsAction extends AbstractAction {
  private final JTabbedPane tabbedPane;
  private final transient Action action;
  private final int index;

  protected ScrollTabsAction(JTabbedPane tabbedPane, Action action) {
    super();
    this.tabbedPane = tabbedPane;
    this.action = action;
    Object name = action.getValue(NAME);
    String forward = "scrollTabsForwardAction";
    this.index = Objects.equals(name, forward) ? tabbedPane.getTabCount() - 1 : 0;
  }

  @Override public void actionPerformed(ActionEvent e) {
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
      if (Objects.equals("TabbedPane.scrollableViewport", c.getName())) {
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
