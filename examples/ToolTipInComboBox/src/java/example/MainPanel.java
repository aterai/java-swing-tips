// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    ComboBoxModel<String> model = new DefaultComboBoxModel<>(new String[] {"aaa", "bb", "c"});
    JComboBox<String> combo = makeComboBox(model);
    combo.setEditable(true);

    JPanel p = new JPanel(new GridLayout(0, 1, 5, 5));
    p.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
    p.add(new JLabel("setEditable(true)"));
    p.add(new JLayer<>(combo, new ToolTipLayerUI<>()));
    p.add(Box.createVerticalStrut(10));
    p.add(new JLabel("setEditable(false)"));
    p.add(new JLayer<>(makeComboBox(model), new ToolTipLayerUI<>()));

    add(p, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static <E> JComboBox<E> makeComboBox(ComboBoxModel<E> model) {
    return new JComboBox<E>(model) {
      @Override public void updateUI() {
        setRenderer(null);
        super.updateUI();
        ListCellRenderer<? super E> renderer = getRenderer();
        setRenderer((list, value, index, isSelected, cellHasFocus) -> {
          Component c = renderer.getListCellRendererComponent(
              list, value, index, isSelected, cellHasFocus);
          if (c instanceof JComponent) {
            ((JComponent) c).setToolTipText(String.format("Item%d: %s", index, value));
          }
          return c;
        });
      }
    };
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.put("example.SearchBarComboBox", "SearchBarComboBoxUI");
      UIManager.put("SearchBarComboBoxUI", "example.BasicSearchBarComboBoxUI");
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

class ToolTipLayerUI<V extends JComboBox<?>> extends LayerUI<V> {
  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
    super.uninstallUI(c);
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends V> l) {
    JComboBox<?> c = l.getView();
    if (e.getComponent() instanceof JButton) {
      c.setToolTipText("ArrowButton");
    } else {
      c.setToolTipText("JComboBox: " + c.getSelectedItem());
    }
    super.processMouseMotionEvent(e, l);
  }
}
