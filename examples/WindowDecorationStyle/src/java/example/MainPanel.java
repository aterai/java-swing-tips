// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.plaf.basic.BasicInternalFrameUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JButton button = new JButton("close");
    button.addActionListener(e -> {
      Container c = getTopLevelAncestor();
      if (c instanceof Window) {
        Window w = (Window) c;
        w.dispatchEvent(new WindowEvent(w, WindowEvent.WINDOW_CLOSING));
      }
    });

    JPanel p = new JPanel(new BorderLayout());
    p.add(new JScrollPane(new JTree()));
    p.add(button, BorderLayout.SOUTH);

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());

    JInternalFrame f = new DraggableInternalFrame("@title@");
    f.getContentPane().add(p);
    f.setJMenuBar(mb);
    EventQueue.invokeLater(() -> f.setVisible(true));
    add(f);
    setPreferredSize(new Dimension(320, 240));
  }

  @Override public void updateUI() {
    super.updateUI();
    // Translucent resize area for mouse cursor >>>
    setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    setBackground(new Color(1f, 1f, 1f, .01f));
    // <<<
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    final JFrame frame = new JFrame();
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
      // return;
    }
    // XXX: JFrame frame = new JFrame();
    frame.setUndecorated(true);

    JRootPane root = frame.getRootPane();
    root.setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
    JLayeredPane layeredPane = root.getLayeredPane();
    Optional.ofNullable(layeredPane.getComponent(1)).ifPresent(c -> c.setVisible(false));
    // if (Objects.nonNull(c) {
    //   c.setVisible(false);
    //   // layeredPane.remove(c);
    // }
    // JComponent sampleTitlePane = new JLabel();
    // layeredPane.add(sampleTitlePane, JLayeredPane.FRAME_CONTENT_LAYER);
    // sampleTitlePane.setVisible(true);

    frame.setMinimumSize(new Dimension(300, 120));
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    GraphicsConfiguration gc = frame.getGraphicsConfiguration();
    if (gc != null && gc.isTranslucencyCapable()) {
      frame.setBackground(new Color(0x0, true)); // Java 1.7.0
    }
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class DragWindowListener extends MouseAdapter {
  private final Point startPt = new Point();

  @Override public void mousePressed(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      startPt.setLocation(e.getPoint());
    }
  }

  @Override public void mouseDragged(MouseEvent e) {
    Component c = SwingUtilities.getRoot(e.getComponent());
    if (c instanceof Window && SwingUtilities.isLeftMouseButton(e)) {
      Point pt = c.getLocation();
      c.setLocation(pt.x - startPt.x + e.getX(), pt.y - startPt.y + e.getY());
    }
  }
}

class DraggableInternalFrame extends JInternalFrame {
  protected DraggableInternalFrame(String title) {
    super(title);
    KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    focusManager.addPropertyChangeListener(e -> {
      if (Objects.equals("activeWindow", e.getPropertyName())) {
        try {
          setSelected(Objects.nonNull(e.getNewValue()));
        } catch (PropertyVetoException ex) {
          throw new IllegalStateException(ex);
        }
      }
    });
  }

  @Override public void updateUI() {
    super.updateUI();
    BasicInternalFrameUI ui = (BasicInternalFrameUI) getUI();
    Component titleBar = ui.getNorthPane();
    for (MouseMotionListener l : titleBar.getListeners(MouseMotionListener.class)) {
      titleBar.removeMouseMotionListener(l);
    }
    DragWindowListener dwl = new DragWindowListener();
    titleBar.addMouseListener(dwl);
    titleBar.addMouseMotionListener(dwl);
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
