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
  private MainPanel() {
    super(new BorderLayout());
    JPopupMenu popup = new JPopupMenu();
    popup.setLayout(new BorderLayout());
    popup.addMouseWheelListener(InputEvent::consume);

    UIManager.put("Slider.paintValue", Boolean.TRUE);
    UIManager.put("Slider.focus", UIManager.get("Slider.background"));
    JSlider slider = new JSlider(SwingConstants.VERTICAL, 0, 100, 80) {
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
      button.setEnabled(m.getValue() > m.getMinimum());
      button.repaint();
    });

    Box box = Box.createHorizontalBox();
    box.add(button);
    add(box, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
    setPreferredSize(new Dimension(320, 240));
  }

  private JToggleButton makeToggleButton(JPopupMenu popup, JSlider slider) {
    JToggleButton button = new JToggleButton("🔊") {
      @Override public JToolTip createToolTip() {
        JToolTip tip = super.createToolTip();
        tip.addHierarchyListener(e -> {
          long flg = e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED;
          if (flg != 0 && e.getComponent().isShowing()) {
            Dimension d = popup.getPreferredSize();
            popup.show(this, (getWidth() - d.width) / 2, -d.height);
          }
        });
        return tip;
      }

      @Override public Point getToolTipLocation(MouseEvent e) {
        return new Point(getWidth() / 2, -getHeight());
      }

      @Override public void setEnabled(boolean b) {
        super.setEnabled(b);
        setText(b ? "🔊" : "🔇");
      }
    };
    button.setToolTipText("");
    button.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        Component btn = e.getComponent();
        if (!btn.isEnabled()) {
          slider.setValue(80);
          btn.setEnabled(true);
        }
        Dimension d = popup.getPreferredSize();
        popup.show(btn, (btn.getWidth() - d.width) / 2, -d.height);
      }

      @Override public void mouseEntered(MouseEvent e) {
        if (!popup.isVisible()) {
          ToolTipManager.sharedInstance().setEnabled(true);
        }
      }

      @Override public void mouseExited(MouseEvent e) {
        if (!popup.isVisible()) {
          ToolTipManager.sharedInstance().setEnabled(true);
        }
      }
    });
    popup.addPopupMenuListener(new PopupMenuListener() {
      @Override public void popupMenuCanceled(PopupMenuEvent e) {
        /* not needed */
      }

      @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        EventQueue.invokeLater(() -> ToolTipManager.sharedInstance().setEnabled(false));
      }

      @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        button.setSelected(false);
      }
    });
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
}
