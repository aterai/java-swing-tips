// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public final class MainPanel extends JPanel {
  public static final int DEFAULT_VOLUME = 80;

  private MainPanel() {
    super(new BorderLayout());
    JPopupMenu popup = new JPopupMenu();
    popup.setLayout(new BorderLayout());
    popup.addMouseWheelListener(InputEvent::consume);

    UIManager.put("Slider.paintValue", Boolean.TRUE);
    UIManager.put("Slider.focus", UIManager.get("Slider.background"));
    JSlider slider = new JSlider(SwingConstants.VERTICAL, 0, 100, DEFAULT_VOLUME) {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = 120;
        return d;
      }
    };
    slider.addMouseWheelListener(e -> {
      JSlider s = (JSlider) e.getComponent();
      if (s.isEnabled()) {
        BoundedRangeModel m = s.getModel();
        m.setValue(m.getValue() - e.getWheelRotation() * 2);
      }
      e.consume();
    });
    popup.add(slider);

    JToggleButton button = makeToggleButton(popup, slider);
    slider.getModel().addChangeListener(e -> {
      BoundedRangeModel m = (BoundedRangeModel) e.getSource();
      // SoundButton#setEnabled(...) also switches the icon text
      button.setEnabled(m.getValue() > m.getMinimum());
    });

    Box box = Box.createHorizontalBox();
    box.add(button);
    add(box, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
    setPreferredSize(new Dimension(320, 240));
  }

  private JToggleButton makeToggleButton(JPopupMenu popup, JSlider slider) {
    SoundButton button = new SoundButton(popup);
    button.setToolTipText("");
    button.addMouseListener(new SoundButtonHandler(button, slider, popup));
    popup.addPopupMenuListener(new PopupHandler(button));
    return button;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
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

  private static final class SoundButtonHandler extends MouseAdapter {
    private final SoundButton button;
    private final JSlider slider;
    private final JPopupMenu popup;

    private SoundButtonHandler(SoundButton button, JSlider slider, JPopupMenu popup) {
      super();
      this.button = button;
      this.slider = slider;
      this.popup = popup;
    }

    @Override public void mousePressed(MouseEvent e) {
      // A MouseListener is used instead of an ActionListener so that
      // the popup also opens when the button is disabled (muted)
      if (!button.isEnabled()) {
        slider.setValue(DEFAULT_VOLUME);
        button.setEnabled(true);
      }
      button.showPopup();
    }

    @Override public void mouseEntered(MouseEvent e) {
      enableToolTipIfPopupHidden();
    }

    @Override public void mouseExited(MouseEvent e) {
      enableToolTipIfPopupHidden();
    }

    // Restore the tooltip disabled by PopupHandler#popupMenuWillBecomeVisible(...)
    private void enableToolTipIfPopupHidden() {
      if (!popup.isVisible()) {
        ToolTipManager.sharedInstance().setEnabled(true);
      }
    }
  }

  private static final class PopupHandler implements PopupMenuListener {
    private final JToggleButton button;

    private PopupHandler(JToggleButton button) {
      this.button = button;
    }

    @Override public void popupMenuCanceled(PopupMenuEvent e) {
      /* not needed */
    }

    @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
      EventQueue.invokeLater(() -> ToolTipManager.sharedInstance().setEnabled(false));
    }

    @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
      button.setSelected(false);
    }
  }
}

class SoundButton extends JToggleButton {
  private final JPopupMenu popup;

  protected SoundButton(JPopupMenu popup) {
    super("🔊");
    this.popup = popup;
  }

  @Override public JToolTip createToolTip() {
    JToolTip tip = super.createToolTip();
    tip.addHierarchyListener(e -> {
      boolean showingChanged = (e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0;
      if (showingChanged && e.getComponent().isShowing()) {
        // Overlay the popup menu on top of the tooltip just shown
        showPopup();
      }
    });
    return tip;
  }

  public void showPopup() {
    Dimension d = popup.getPreferredSize();
    popup.show(this, (getWidth() - d.width) / 2, -d.height);
  }

  @Override public Point getToolTipLocation(MouseEvent e) {
    return new Point(getWidth() / 2, -getHeight());
  }

  @Override public void setEnabled(boolean b) {
    super.setEnabled(b);
    setText(b ? "🔊" : "🔇");
  }
}
