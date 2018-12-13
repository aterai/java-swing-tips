package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.awt.event.InputEvent;
import java.beans.PropertyChangeEvent;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    DisableInputLayerUI<Component> layerUI = new DisableInputLayerUI<>();
    Timer stopper = new Timer(5000, e -> layerUI.stop());

    JButton button = new JButton("Stop 5sec");
    button.addActionListener(e -> {
      layerUI.start();
      if (!stopper.isRunning()) {
        stopper.start();
      }
    });

    JPanel p = new JPanel();
    p.add(new JCheckBox());
    p.add(new JTextField(10));
    p.add(button);
    stopper.setRepeats(false);

    add(new JLayer<>(p, layerUI), BorderLayout.NORTH);
    add(new JScrollPane(new JTextArea("dummy")));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException
         | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
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
  private static final String CMD_REPAINT = "repaint";
  private boolean running;

  public void start() {
    if (running) {
      return;
    }
    running = true;
    firePropertyChange(CMD_REPAINT, false, true);
  }

  public void stop() {
    running = false;
    firePropertyChange(CMD_REPAINT, true, false);
  }

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (!running) {
      return;
    }
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
    g2.setPaint(Color.GRAY);
    g2.fillRect(0, 0, c.getWidth(), c.getHeight());
    g2.dispose();
  }

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      JLayer<?> jlayer = (JLayer<?>) c;
      jlayer.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      jlayer.setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK
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

  @Override public void eventDispatched(AWTEvent e, JLayer<? extends V> l) {
    if (running && e instanceof InputEvent) {
      ((InputEvent) e).consume();
    }
  }

  @Override public void applyPropertyChange(PropertyChangeEvent pce, JLayer<? extends V> l) {
    String cmd = pce.getPropertyName();
    if (CMD_REPAINT.equals(cmd)) {
      l.getGlassPane().setVisible((Boolean) pce.getNewValue());
      l.repaint();
    }
  }
}
