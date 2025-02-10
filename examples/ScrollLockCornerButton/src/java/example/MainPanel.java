// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(16, 4);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    JScrollPane scroll = new JScrollPane(table);

    JToggleButton lock = new JToggleButton("🔓");
    lock.setBorder(BorderFactory.createEmptyBorder());
    lock.setContentAreaFilled(false);
    lock.setFocusPainted(false);
    lock.setFocusable(false);
    DisableInputLayerUI<Component> layerUI = new DisableInputLayerUI<>();
    lock.addItemListener(e -> {
      AbstractButton b = (AbstractButton) e.getItemSelectable();
      if (e.getStateChange() == ItemEvent.SELECTED) {
        b.setText("🔒");
        scrollLock(scroll, true);
        layerUI.setLocked(true);
      } else if (e.getStateChange() == ItemEvent.DESELECTED) {
        b.setText("🔓");
        scrollLock(scroll, false);
        layerUI.setLocked(false);
      }
    });

    JScrollBar verticalScrollBar = scroll.getVerticalScrollBar();
    JPanel verticalBox = new JPanel(new BorderLayout());
    verticalBox.setOpaque(false);
    verticalBox.add(new JLayer<>(verticalScrollBar, layerUI));
    verticalBox.add(lock, BorderLayout.SOUTH);
    BoundedRangeModel model = verticalScrollBar.getModel();
    model.addChangeListener(e -> {
      BoundedRangeModel m = (BoundedRangeModel) e.getSource();
      verticalBox.setVisible(m.getMaximum() - m.getMinimum() > m.getExtent());
    });
    verticalBox.setVisible(model.getMaximum() - model.getMinimum() > model.getExtent());

    JPanel panel = new JPanel(new BorderLayout(0, 0));
    panel.add(scroll);
    panel.add(verticalBox, BorderLayout.EAST);

    add(panel);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void scrollLock(JScrollPane scroll, boolean lock) {
    // scroll.getVerticalScrollBar().setEnabled(!lock);
    scroll.setWheelScrollingEnabled(!lock);
    Component c = scroll.getViewport().getView();
    c.setEnabled(!lock);
    c.setFocusable(!lock);
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

class DisableInputLayerUI<V extends Component> extends LayerUI<V> {
  private static final String CMD_REPAINT = "lock";
  private final transient MouseListener mouseBlocker = new MouseAdapter() {
    /* block mouse event */
  };
  private boolean isBlocking;

  public void setLocked(boolean flag) {
    firePropertyChange(CMD_REPAINT, isBlocking, flag);
    isBlocking = flag;
  }

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      JLayer<?> layer = (JLayer<?>) c;
      layer.getGlassPane().addMouseListener(mouseBlocker);
      layer.setLayerEventMask(
          AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK
          | AWTEvent.MOUSE_WHEEL_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      JLayer<?> layer = (JLayer<?>) c;
      layer.setLayerEventMask(0);
      layer.getGlassPane().removeMouseListener(mouseBlocker);
    }
    super.uninstallUI(c);
  }

  @Override public void eventDispatched(AWTEvent e, JLayer<? extends V> l) {
    if (isBlocking && e instanceof InputEvent) {
      ((InputEvent) e).consume();
    }
  }

  @Override public void applyPropertyChange(PropertyChangeEvent e, JLayer<? extends V> l) {
    if (CMD_REPAINT.equals(e.getPropertyName())) {
      l.getGlassPane().setVisible((Boolean) e.getNewValue());
    }
  }
}
