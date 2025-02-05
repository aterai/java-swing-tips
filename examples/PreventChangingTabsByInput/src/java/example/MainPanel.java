// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    // No effect: UIManager.put("TabbedPane.disabledAreNavigable", Boolean.TRUE);

    JTabbedPane tabs0 = makeTabbedPane();
    tabs0.setEnabled(false);
    tabs0.setBorder(BorderFactory.createTitledBorder("setEnabled(false)"));

    // JTabbedPane tabs1 = makeTabbedPane();
    // for (int i = 0; i < tabs1.getTabCount(); i++) {
    //   tabs1.setEnabledAt(i, false);
    // }
    // tabs1.setBorder(BorderFactory.createTitledBorder("setEnabledAt(idx, false)"));

    JTabbedPane tabs2 = makeTabbedPane();
    tabs2.setEnabled(false);
    IntStream.range(0, tabs2.getTabCount())
        .forEach(i -> tabs2.setTabComponentAt(i, new JLabel(tabs2.getTitleAt(i))));
    tabs2.setBorder(BorderFactory.createTitledBorder("setTabComponentAt(...)"));

    JTabbedPane tabs3 = makeTabbedPane();
    tabs3.setBorder(BorderFactory.createTitledBorder("DisableInputLayerUI()"));

    JPanel p = new JPanel(new GridLayout(0, 1, 0, 5));
    p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    Stream.of(tabs0, tabs2).forEach(p::add);
    p.add(new JLayer<>(tabs3, new DisableInputLayerUI()));

    JButton button = new JButton("next");
    button.addActionListener(e -> {
      int next = (tabs0.getSelectedIndex() + 1) % tabs0.getTabCount();
      Stream.of(tabs0, tabs2, tabs3).forEach(t -> t.setSelectedIndex(next));
    });

    add(p, BorderLayout.NORTH);
    add(button, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTabbedPane makeTabbedPane() {
    JTabbedPane tabs = new JTabbedPane() {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = 70;
        return d;
      }
    };
    IntStream.range(0, 4)
        .mapToObj(i -> "Step" + i)
        .forEach(t -> tabs.addTab(t, new JTextField(t)));
    tabs.setFocusable(false);
    return tabs;
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

class DisableInputLayerUI extends LayerUI<Component> {
  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(
          AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK
          | AWTEvent.MOUSE_WHEEL_EVENT_MASK | AWTEvent.KEY_EVENT_MASK
          | AWTEvent.FOCUS_EVENT_MASK | AWTEvent.COMPONENT_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
    super.uninstallUI(c);
  }

  @Override public void eventDispatched(AWTEvent e, JLayer<? extends Component> l) {
    if (e instanceof InputEvent && Objects.equals(l.getView(), e.getSource())) {
      ((InputEvent) e).consume();
    }
  }
}
