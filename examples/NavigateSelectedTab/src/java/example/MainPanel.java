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
    IntStream.range(0, 20).forEach(i -> {
      String title = "title" + i;
      tabs.addTab(title, new JScrollPane(new JTextArea(title)));
    });

    JCheckBox layout = new JCheckBox("SCROLL_TAB_LAYOUT", true);
    layout.setFocusable(false);
    layout.addActionListener(e -> {
      boolean b = ((JCheckBox) e.getSource()).isSelected();
      tabs.setTabLayoutPolicy(b ? JTabbedPane.SCROLL_TAB_LAYOUT : JTabbedPane.WRAP_TAB_LAYOUT);
    });

    JCheckBox placement = new JCheckBox("TOP", true);
    placement.setFocusable(false);
    placement.addActionListener(e -> {
      boolean b = ((JCheckBox) e.getSource()).isSelected();
      tabs.setTabPlacement(b ? SwingConstants.TOP : SwingConstants.LEFT);
    });

    InputMap im0 = tabs.getInputMap(WHEN_FOCUSED);
    InputMap im1 = tabs.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    ActionMap am = tabs.getActionMap();

    String prev = "navigatePrevious";
    am.put(prev, new TabNavigateAction(tabs, am.get(prev)));
    im0.put(KeyStroke.getKeyStroke("LEFT"), prev);
    im1.put(KeyStroke.getKeyStroke("alt LEFT"), prev);
    im0.put(KeyStroke.getKeyStroke("UP"), prev);
    im1.put(KeyStroke.getKeyStroke("alt UP"), prev);

    String next = "navigateNext";
    am.put(next, new TabNavigateAction(tabs, am.get(next)));
    im0.put(KeyStroke.getKeyStroke("RIGHT"), next);
    im1.put(KeyStroke.getKeyStroke("alt RIGHT"), next);
    im0.put(KeyStroke.getKeyStroke("DOWN"), next);
    im1.put(KeyStroke.getKeyStroke("alt DOWN"), next);

    Box box = Box.createHorizontalBox();
    box.add(layout);
    box.add(placement);

    add(tabs);
    add(box, BorderLayout.SOUTH);
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

class TabNavigateAction extends AbstractAction {
  private final JTabbedPane tabs;
  private final transient Action action;

  protected TabNavigateAction(JTabbedPane tabbedPane, Action action) {
    super();
    this.tabs = tabbedPane;
    this.action = action;
  }

  @Override public void actionPerformed(ActionEvent e) {
    if (action != null && action.isEnabled()) {
      boolean isWrap = tabs.getTabLayoutPolicy() == JTabbedPane.WRAP_TAB_LAYOUT;
      boolean isAltDown = (e.getModifiers() & ActionEvent.ALT_MASK) != 0;
      Object name = action.getValue(NAME);
      int base = tabs.getSelectedIndex();
      boolean prev = Objects.equals(name, "navigatePrevious") && base != 0;
      boolean next = Objects.equals(name, "navigateNext") && base != tabs.getTabCount() - 1;
      if (isWrap || isAltDown || prev || next) {
        action.actionPerformed(new ActionEvent(tabs,
            ActionEvent.ACTION_PERFORMED, null,
            e.getWhen(), e.getModifiers()));
      }
    }
  }
}
