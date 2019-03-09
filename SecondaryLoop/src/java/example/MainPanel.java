// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.InputEvent;
import java.beans.PropertyChangeEvent;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private final JTextArea logger = new JTextArea();
  private final JButton cancel = new JButton("cancel");
  private final JButton button = new JButton("Stop 5sec");
  private final DisableInputLayerUI<JComponent> layerUI = new DisableInputLayerUI<>();
  private transient Thread worker;

  private MainPanel() {
    super(new BorderLayout());

    cancel.setEnabled(false);
    cancel.addActionListener(e -> {
      if (worker != null) {
        worker.interrupt();
      }
    });

    button.addActionListener(e -> {
      setInputBlock(true);
      SecondaryLoop loop = Toolkit.getDefaultToolkit().getSystemEventQueue().createSecondaryLoop();
      worker = new Thread() {
        @Override public void run() {
          String msg = "Done";
          try {
            Thread.sleep(5000);
          } catch (InterruptedException ex) {
            msg = "Interrupted";
          }
          append(msg);
          setInputBlock(false);
          loop.exit();
        }
      };
      worker.start();
      if (!loop.enter()) {
        append("Error");
      }
    });

    JPanel p = new JPanel();
    p.add(new JCheckBox());
    p.add(new JTextField(10));
    p.add(button);
    add(new JLayer<>(p, layerUI), BorderLayout.NORTH);
    add(new JScrollPane(logger));
    add(cancel, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  public void setInputBlock(boolean flg) {
    layerUI.setInputBlock(flg);
    cancel.setEnabled(flg);
  }

  public void append(String str) {
    logger.append(str + "\n");
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
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    // frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class DisableInputLayerUI<V extends JComponent> extends LayerUI<V> {
  private static final String CMD_REPAINT = "repaint";
  private boolean running;

  public void setInputBlock(boolean block) {
    firePropertyChange(CMD_REPAINT, running, block);
    running = block;
  }

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (!running) {
      return;
    }
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
    g2.setPaint(Color.GRAY.brighter());
    g2.fillRect(0, 0, c.getWidth(), c.getHeight());
    g2.dispose();
  }

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      JLayer<?> jlayer = (JLayer<?>) c;
      jlayer.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      jlayer.setLayerEventMask(
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

  @Override public void eventDispatched(AWTEvent e, JLayer<? extends V> l) {
    if (running && e instanceof InputEvent) {
      ((InputEvent) e).consume();
    }
  }

  @Override public void applyPropertyChange(PropertyChangeEvent e, JLayer<? extends V> l) {
    if (CMD_REPAINT.equals(e.getPropertyName())) {
      l.getGlassPane().setVisible((Boolean) e.getNewValue());
      l.repaint();
    }
  }
}
