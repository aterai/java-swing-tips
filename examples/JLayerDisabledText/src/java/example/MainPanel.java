// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.util.Optional;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    UIManager.put("Button.disabledText", Color.RED);
    JButton button1 = makeButton("Default");
    JButton button2 = makeButton("setForeground");
    DisableInputLayerUI<AbstractButton> layer3 = new DisableInputLayerUI<>();
    JButton button4 = makeButton("<html>html <font color='red'>tag");
    DisableInputLayerUI<AbstractButton> layer5 = new DisableInputLayerUI<>();

    JCheckBox check = new JCheckBox("setEnabled", true);
    check.addActionListener(e -> {
      boolean isSelected = ((JCheckBox) e.getSource()).isSelected();
      button1.setEnabled(isSelected);
      button2.setEnabled(isSelected);
      button2.setForeground(isSelected ? Color.BLACK : Color.RED);
      layer3.setLocked(!isSelected);
      button4.setEnabled(isSelected);
      layer5.setLocked(!isSelected);
    });

    JPanel p1 = new JPanel();
    p1.setBorder(BorderFactory.createTitledBorder("setEnabled"));
    p1.add(button1);
    p1.add(button2);
    p1.add(new JLayer<>(makeButton("JLayer"), layer3));

    JPanel p2 = new JPanel();
    p2.setBorder(BorderFactory.createTitledBorder("html"));
    p2.add(button4);
    p2.add(new JLayer<>(makeButton("<html>JLayer <font color='#0000ff'>html"), layer5));

    // JPanel p3 = new JPanel();
    // p3.setBorder(BorderFactory.createTitledBorder("Focus test"));
    // p3.add(new JTextField(16));
    // p3.add(new JButton("JButton"));

    JPanel panel = new JPanel(new GridLayout(0, 1));
    panel.add(p1);
    panel.add(p2);
    // panel.add(p3);
    add(panel, BorderLayout.NORTH);

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(check);
    add(box, BorderLayout.SOUTH);

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JButton makeButton(String title) {
    JPopupMenu pop = new JPopupMenu();
    pop.add(title);
    JButton button = new JButton(title);
    if (!title.isEmpty()) {
      button.setMnemonic(title.codePointAt(0));
    }
    button.setToolTipText(title);
    button.setComponentPopupMenu(pop);
    return button;
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

class DisableInputLayerUI<V extends AbstractButton> extends LayerUI<V> {
  private static final String CMD_BLOCKING = "lock";
  private static final boolean DEBUG_POPUP_BLOCK = false;
  private final transient MouseListener mouseBlocker = new MouseAdapter() {
    /* Do nothing */
  };
  private final transient KeyListener keyBlocker = new KeyAdapter() {
    /* Do nothing */
  };
  private boolean isBlocking;
  private transient BufferedImage buf;

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      JLayer<?> layer = (JLayer<?>) c;
      if (DEBUG_POPUP_BLOCK) {
        layer.getGlassPane().addMouseListener(mouseBlocker);
        layer.getGlassPane().addKeyListener(keyBlocker);
      }
      layer.setLayerEventMask(
          AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK
          | AWTEvent.MOUSE_WHEEL_EVENT_MASK | AWTEvent.KEY_EVENT_MASK
          | AWTEvent.FOCUS_EVENT_MASK | AWTEvent.COMPONENT_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      JLayer<?> layer = (JLayer<?>) c;
      layer.setLayerEventMask(0);
      if (DEBUG_POPUP_BLOCK) {
        layer.getGlassPane().removeMouseListener(mouseBlocker);
        layer.getGlassPane().removeKeyListener(keyBlocker);
      }
    }
    super.uninstallUI(c);
  }

  // @Override protected void processComponentEvent(ComponentEvent e, JLayer<? extends V> l) {
  //   System.out.println("processComponentEvent");
  // }

  // @Override protected void processKeyEvent(KeyEvent e, JLayer<? extends V> l) {
  //   System.out.println("processKeyEvent");
  // }

  // @Override protected void processFocusEvent(FocusEvent e, JLayer<? extends V> l) {
  //   System.out.println("processFocusEvent");
  // }

  @Override public void eventDispatched(AWTEvent e, JLayer<? extends V> l) {
    if (isBlocking && e instanceof InputEvent) {
      ((InputEvent) e).consume();
    }
  }

  public void setLocked(boolean flag) {
    boolean old = isBlocking;
    isBlocking = flag;
    firePropertyChange(CMD_BLOCKING, old, isBlocking);
  }

  @Override public void applyPropertyChange(PropertyChangeEvent e, JLayer<? extends V> l) {
    if (CMD_BLOCKING.equals(e.getPropertyName())) {
      AbstractButton b = l.getView();
      b.setFocusable(!isBlocking);
      b.setMnemonic(isBlocking ? 0 : b.getText().codePointAt(0));
      b.setForeground(isBlocking ? Color.RED : Color.BLACK);
      l.getGlassPane().setVisible((Boolean) e.getNewValue());
    }
  }

  @Override public void paint(Graphics g, JComponent c) {
    if (c instanceof JLayer) {
      Component view = ((JLayer<?>) c).getView();
      if (isBlocking) {
        Dimension d = view.getSize();
        BufferedImage img = Optional.ofNullable(buf)
            .filter(bi -> bi.getWidth() == d.width && bi.getHeight() == d.height)
            .orElseGet(() -> new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB));

        Graphics2D g2 = img.createGraphics();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .25f));
        // NimbusLookAndFeel bug???: super.paint(g2, c);
        view.paint(g2);
        g2.dispose();

        g.drawImage(img, 0, 0, c);
        buf = img;
      } else {
        // NimbusLookAndFeel bug???: super.paint(g, c);
        view.paint(g);
        // super.paint(g2, (JComponent) view);
      }
    }
  }
}

// class LockingGlassPane extends JPanel {
//   protected LockingGlassPane() {
//     super();
//     setOpaque(false);
//     setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
//       @Override public boolean accept(Component c) {
//         return false;
//       }
//     });
//     addKeyListener(new KeyAdapter() {});
//     addMouseListener(new MouseAdapter() {});
//     requestFocusInWindow();
//     setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//   }
//
//   @Override public void setVisible(boolean flag) {
//     super.setVisible(flag);
//     setFocusTraversalPolicyProvider(flag);
//   }
// }

// class LockingGlassPane extends JPanel {
//   protected LockingGlassPane() {
//     super();
//     setOpaque(false);
//     setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//   }
//
//   @Override public void setVisible(boolean isVisible) {
//     boolean oldVisible = isVisible();
//     super.setVisible(isVisible);
//     JRootPane rootPane = SwingUtilities.getRootPane(this);
//     if (rootPane != null && isVisible() != oldVisible) {
//       rootPane.getLayeredPane().setVisible(!isVisible);
//     }
//   }
//
//   @Override protected void paintComponent(Graphics g) {
//     JRootPane rootPane = SwingUtilities.getRootPane(this);
//     if (rootPane != null) {
//       // http://weblogs.java.net/blog/alexfromsun/archive/2008/01/
//       // it is important to call print() instead of paint() here
//       // because print() doesn't affect the frame's double buffer
//       rootPane.getLayeredPane().print(g);
//     }
//     super.paintComponent(g);
//   }
// }

// class PrintGlassPane extends JPanel {
//   // TexturePaint texture = TextureFactory.createCheckerTexture(4);
//   protected PrintGlassPane() {
//     super((LayoutManager) null);
//     setOpaque(false);
//   }
//
//   @Override public void setVisible(boolean isVisible) {
//     boolean oldVisible = isVisible();
//     super.setVisible(isVisible);
//     JRootPane rootPane = SwingUtilities.getRootPane(this);
//     if (rootPane != null && isVisible() != oldVisible) {
//       rootPane.getLayeredPane().setVisible(!isVisible);
//     }
//   }
//
//   @Override protected void paintComponent(Graphics g) {
//     super.paintComponent(g);
//     JRootPane rootPane = SwingUtilities.getRootPane(this);
//     if (rootPane != null) {
//       // http://weblogs.java.net/blog/alexfromsun/archive/2008/01/
//       // it is important to call print() instead of paint() here
//       // because print() doesn't affect the frame's double buffer
//       rootPane.getLayeredPane().print(g);
//     }
//     // Graphics2D g2 = (Graphics2D) g.create();
//     // g2.setPaint(texture);
//     // g2.fillRect(0, 0, getWidth(), getHeight());
//     // g2.dispose();
//   }
// }

// @see SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = makeButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton makeButton(UIManager.LookAndFeelInfo info) {
    boolean selected = info.getClassName().equals(lookAndFeel);
    return new JRadioButtonMenuItem(info.getName(), selected);
  }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        ex.printStackTrace();
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
