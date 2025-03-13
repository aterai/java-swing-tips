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
    String html = "<html>html <span style='color:#0000ff'>tag";
    JButton button0 = makeButton(html);
    DisableInputLayerUI<AbstractButton> layer1 = new DisableInputLayerUI<>();
    JButton button2 = makeButton(html);

    JPanel p = new JPanel(new GridLayout(0, 1, 8, 8));
    p.add(makeTitledPanel("Default", button0));
    p.add(makeTitledPanel("JLayer1", new JLayer<>(makeButton(html), layer1)));
    p.add(makeTitledPanel("JLayer2", new JLayer<>(button2, new DisabledHtmlTextLayerUI<>())));

    JCheckBox check = new JCheckBox("setEnabled", true);
    check.addActionListener(e -> {
      boolean isSelected = ((JCheckBox) e.getSource()).isSelected();
      button0.setEnabled(isSelected);
      layer1.setLocked(!isSelected);
      button2.setEnabled(isSelected);
    });
    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(check);

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    add(p, BorderLayout.NORTH);
    add(box, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JButton makeButton(String title) {
    JPopupMenu pop = new JPopupMenu();
    pop.add(title);
    JButton button = new JButton(title);
    if (!title.isEmpty()) {
      button.setMnemonic(title.codePointAt(0));
    }
    button.setIcon(UIManager.getIcon("FileView.directoryIcon"));
    button.setToolTipText(title);
    button.setComponentPopupMenu(pop);
    return button;
  }

  private static Component makeTitledPanel(String title, Component c) {
    Box box = Box.createHorizontalBox();
    box.add(new JLabel(title + ": "));
    box.add(c);
    box.add(Box.createHorizontalGlue());
    return box;
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
        // super.paint(g, (JComponent) view);
      }
    }
  }
}

class DisabledHtmlTextLayerUI<V extends AbstractButton> extends LayerUI<V> {
  private final JLabel label = new JLabel();
  private final JPanel canvas = new JPanel();

  @Override public void updateUI(JLayer<? extends V> l) {
    super.updateUI(l);
    SwingUtilities.updateComponentTreeUI(label);
  }

  @Override public void paint(Graphics g, JComponent c) {
    if (c instanceof JLayer) {
      Component view = ((JLayer<?>) c).getView();
      super.paint(g, (JComponent) view);
      if (!view.isEnabled()) {
        paintDisabledText(g, view);
      }
    }
  }

  private void paintDisabledText(Graphics g, Component c) {
    if (c instanceof AbstractButton) {
      AbstractButton b = (AbstractButton) c;
      label.setFont(b.getFont());
      label.setText(b.getText());
      label.setIcon(b.getIcon());
      label.setVerticalAlignment(b.getVerticalAlignment());
      label.setHorizontalAlignment(b.getHorizontalAlignment());
      label.setVerticalTextPosition(b.getVerticalTextPosition());
      label.setHorizontalTextPosition(b.getHorizontalTextPosition());
      label.setForeground(UIManager.getColor("Button.disabledText"));
      label.setOpaque(false);
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .25f));
      Rectangle r = b.getBounds();
      SwingUtilities.paintComponent(g2, label, canvas, r.x, r.y, r.width, r.height);
      g2.dispose();
    }
  }
}

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
