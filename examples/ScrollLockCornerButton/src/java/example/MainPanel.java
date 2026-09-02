// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(16, 4);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    JScrollPane scroll = new JScrollPane(table);
    JToggleButton lockButton = new LockToggleButton();
    DisableInputLayerUI<Component> layerUI = new DisableInputLayerUI<>();
    lockButton.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        setScrollLocked(scroll, true);
        layerUI.setLocked(true);
      } else if (e.getStateChange() == ItemEvent.DESELECTED) {
        setScrollLocked(scroll, false);
        layerUI.setLocked(false);
      }
    });
    JScrollBar verticalScrollBar = scroll.getVerticalScrollBar();
    JPanel scrollBarPanel = new JPanel(new BorderLayout());
    scrollBarPanel.setOpaque(false);
    scrollBarPanel.add(new JLayer<>(verticalScrollBar, layerUI));
    scrollBarPanel.add(lockButton, BorderLayout.SOUTH);
    BoundedRangeModel model = verticalScrollBar.getModel();
    model.addChangeListener(e -> {
      BoundedRangeModel m = (BoundedRangeModel) e.getSource();
      scrollBarPanel.setVisible(m.getMaximum() - m.getMinimum() > m.getExtent());
    });
    scrollBarPanel.setVisible(model.getMaximum() - model.getMinimum() > model.getExtent());
    JPanel panel = new JPanel(new BorderLayout(0, 0));
    panel.add(scroll);
    panel.add(scrollBarPanel, BorderLayout.EAST);
    add(panel);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void setScrollLocked(JScrollPane scroll, boolean locked) {
    // scroll.getVerticalScrollBar().setEnabled(!locked);
    scroll.setWheelScrollingEnabled(!locked);
    Component view = scroll.getViewport().getView();
    view.setEnabled(!locked);
    view.setFocusable(!locked);
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

class LockToggleButton extends JToggleButton {
  private transient ItemListener listener;

  protected LockToggleButton() {
    super("🔓");
  }

  @Override public void updateUI() {
    removeItemListener(listener);
    super.updateUI();
    listener = e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        setText("🔒");
      } else if (e.getStateChange() == ItemEvent.DESELECTED) {
        setText("🔓");
      }
    };
    addItemListener(listener);
    setBorder(BorderFactory.createEmptyBorder());
    setContentAreaFilled(false);
    setFocusPainted(false);
    setFocusable(false);
  }
}

class DisableInputLayerUI<V extends Component> extends LayerUI<V> {
  private static final String LOCKED_PROPERTY = "locked";
  private final transient MouseListener mouseBlocker = new MouseAdapter() {
    /* block mouse event */
  };
  private boolean locked;

  public void setLocked(boolean locked) {
    firePropertyChange(LOCKED_PROPERTY, this.locked, locked);
    this.locked = locked;
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
    if (locked && e instanceof InputEvent) {
      ((InputEvent) e).consume();
    }
  }

  @Override public void applyPropertyChange(PropertyChangeEvent e, JLayer<? extends V> l) {
    if (LOCKED_PROPERTY.equals(e.getPropertyName())) {
      l.getGlassPane().setVisible((Boolean) e.getNewValue());
    }
  }
}
