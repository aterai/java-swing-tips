// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String hint1 = "Please enter your E-mail address";
    PlaceholderFocusListener listener1 = new PlaceholderFocusListener(hint1);
    JTextField field1 = new JTextField();
    field1.addFocusListener(listener1);
    listener1.update(field1);

    JTextField field2 = new JTextField() {
      private transient PlaceholderFocusListener listener;

      @Override public void updateUI() {
        removeFocusListener(listener);
        super.updateUI();
        String hint = "History Search";
        listener = new PlaceholderFocusListener(hint);
        addFocusListener(listener);
        EventQueue.invokeLater(() -> listener.update(this));
      }
    };

    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    box.add(makeTitledPanel("E-mail", field1));
    box.add(Box.createVerticalStrut(10));
    box.add(makeTitledPanel("Search", field2));
    box.add(Box.createVerticalStrut(10));
    LayerUI<JTextComponent> layerUI = new PlaceholderLayerUI<>("JLayer version");
    box.add(makeTitledPanel("JLayer", new JLayer<>(new JTextField(), layerUI)));

    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
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

class PlaceholderFocusListener implements FocusListener {
  private final String hintMessage;

  protected PlaceholderFocusListener(String hintMessage) {
    this.hintMessage = hintMessage;
  }

  @Override public void focusGained(FocusEvent e) {
    update((JTextComponent) e.getComponent());
  }

  @Override public void focusLost(FocusEvent e) {
    update((JTextComponent) e.getComponent());
  }

  public void update(JTextComponent c) {
    String txt = c.getText().trim();
    if (txt.isEmpty()) {
      c.setForeground(UIManager.getColor("TextField.inactiveForeground"));
      c.setText(hintMessage);
    } else {
      c.setForeground(UIManager.getColor("TextField.foreground"));
      if (Objects.equals(txt, hintMessage)) {
        c.setText("");
      }
    }
  }
}

class PlaceholderLayerUI<V extends JTextComponent> extends LayerUI<V> {
  private final JLabel hint = new JLabel() {
    @Override public void updateUI() {
      super.updateUI();
      setForeground(UIManager.getColor("TextField.inactiveForeground"));
    }
  };

  protected PlaceholderLayerUI(String hintMessage) {
    super();
    hint.setText(hintMessage);
  }

  @Override public void updateUI(JLayer<? extends V> l) {
    super.updateUI(l);
    SwingUtilities.updateComponentTreeUI(hint);
  }

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    Optional.ofNullable(c)
        .filter(JLayer.class::isInstance).map(JLayer.class::cast)
        .map(JLayer::getView)
        .filter(JTextComponent.class::isInstance).map(JTextComponent.class::cast)
        .filter(tc -> tc.getText().isEmpty() && !tc.hasFocus())
        .ifPresent(tc -> paintHint(g, tc));
    // if (c instanceof JLayer) {
    //   JTextComponent tc = (JTextComponent) ((JLayer<?>) c).getView();
    //   if (tc.getText().isEmpty() && !tc.hasFocus()) {
    //     paintHint(g, tc);
    //   }
    // }
  }

  private void paintHint(Graphics g, JTextComponent tc) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(hint.getBackground());
    Rectangle r = SwingUtilities.calculateInnerArea(tc, null);
    Dimension d = hint.getPreferredSize();
    int yy = (int) (r.getCenterY() - d.height / 2d);
    SwingUtilities.paintComponent(g2, hint, tc, r.x, yy, d.width, d.height);
    // int baseline = tc.getBaseline(tc.getWidth(), tc.getHeight());
    // Font font = tc.getFont();
    // FontRenderContext frc = g2.getFontRenderContext();
    // TextLayout tl = new TextLayout(hintMessage, font, frc);
    // tl.draw(g2, i.left + 2, baseline);
    g2.dispose();
  }

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(AWTEvent.FOCUS_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    super.uninstallUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
  }

  @Override protected void processFocusEvent(FocusEvent e, JLayer<? extends V> l) {
    l.getView().repaint();
  }
}
