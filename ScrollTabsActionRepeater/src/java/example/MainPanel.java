// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabs = new JTabbedPane();
    tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    IntStream.range(0, 100).forEach(i -> tabs.addTab("title" + i, new JLabel("label" + i)));

    ActionMap am = tabs.getActionMap();
    String forward = "scrollTabsForwardAction";
    String backward = "scrollTabsBackwardAction";

    JButton forwardButton = null;
    JButton backwardButton = null;
    for (Component c : tabs.getComponents()) {
      if (c instanceof JButton) {
        if (Objects.isNull(forwardButton)) {
          forwardButton = (JButton) c;
          addRepeatHandler(forwardButton, am.get(forward));
        } else if (Objects.isNull(backwardButton)) {
          backwardButton = (JButton) c;
          addRepeatHandler(backwardButton, am.get(backward));
        }
      }
    }
    add(tabs);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void addRepeatHandler(JButton button, Action action) {
    ActionRepeatHandler handler = new ActionRepeatHandler(action);
    button.addActionListener(handler);
    button.addMouseListener(handler);
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

class ActionRepeatHandler extends MouseAdapter implements ActionListener {
  private final Timer timer;
  private final Action action;
  private JButton button;

  protected ActionRepeatHandler(Action action) {
    super();
    this.action = action;
    timer = new Timer(60, this);
    timer.setInitialDelay(300);
  }

  @Override public void actionPerformed(ActionEvent e) {
    Object o = e.getSource();
    if (o instanceof Timer && button != null) {
      if (!button.getModel().isPressed() && timer.isRunning()) {
        timer.stop();
        button = null;
      } else {
        Component c = SwingUtilities.getAncestorOfClass(JTabbedPane.class, button);
        action.actionPerformed(new ActionEvent(c,
            ActionEvent.ACTION_PERFORMED, null,
            e.getWhen(), e.getModifiers()));
      }
    }
  }

  @Override public void mousePressed(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e) && e.getComponent().isEnabled()) {
      button = (JButton) e.getComponent();
      timer.start();
    }
  }

  @Override public void mouseReleased(MouseEvent e) {
    timer.stop();
    button = null;
  }

  @Override public void mouseExited(MouseEvent e) {
    if (timer.isRunning()) {
      timer.stop();
    }
  }
}
