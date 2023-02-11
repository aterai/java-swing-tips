// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Optional;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout(10, 10));
    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder("Apply a blur effect if disabled"));

    JButton b0 = new JButton("<html>Default <font color='red'>JButton");
    p.add(b0);

    JButton b1 = new BlurredButton("Blurred JButton");
    p.add(b1);

    JButton b2 = new BlurButton("Blurred JButton(ConvolveOp.EDGE_NO_OP)");
    p.add(b2);

    JButton b3 = new JButton("<html>Blurred <font color='blue'>JLayer");
    p.add(new JLayer<>(b3, new BlurLayerUI<>()));

    JCheckBox button = new JCheckBox("setEnabled", true);
    button.addActionListener(e -> {
      boolean f = ((AbstractButton) e.getSource()).isSelected();
      Stream.of(b0, b1, b2, b3).forEach(b -> b.setEnabled(f));
    });

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(button);

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    add(p);
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
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

// https://www.oreilly.com/library/view/swing-hacks/0596009070/
// 9. Blur Disabled Components
// https://code.google.com/archive/p/filthy-rich-clients/
// trunk/swing-hacks-examples-20060109/
// Ch01-JComponents/09/swinghacks/ch01/JComponents/hack09/BlurJButton.java
class BlurredButton extends JButton {
  private static final Kernel KNL = new Kernel(3, 3, new float[] {
      .05f, .05f, .05f,
      .05f, .60f, .05f,
      .05f, .05f, .05f
  });
  private static final ConvolveOp CONVOLVE = new ConvolveOp(KNL);
  private transient BufferedImage buf;

  protected BlurredButton(String label) {
    super(label);
    // System.out.println(op.getEdgeCondition());
  }

  @Override protected void paintComponent(Graphics g) {
    if (isEnabled()) {
      super.paintComponent(g);
    } else {
      Dimension d = getSize();
      BufferedImage img = Optional.ofNullable(buf)
          .filter(bi -> bi.getWidth() == d.width && bi.getHeight() == d.height)
          .orElseGet(() -> new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB));
      Graphics2D g2 = img.createGraphics();
      g2.setFont(g.getFont()); // pointed out by 八ツ玉舘
      super.paintComponent(g2);
      g2.dispose();
      g.drawImage(CONVOLVE.filter(img, null), 0, 0, this);
      buf = img;
    }
  }
}

class BlurButton extends JButton {
  private static final Kernel KNL = new Kernel(3, 3, new float[] {
      .05f, .05f, .05f,
      .05f, .60f, .05f,
      .05f, .05f, .05f
  });
  private static final ConvolveOp CONVOLVE = new ConvolveOp(KNL, ConvolveOp.EDGE_NO_OP, null);
  private transient BufferedImage buf;

  protected BlurButton(String label) {
    super(label);
    // System.out.println(op.getEdgeCondition());
  }

  @Override protected void paintComponent(Graphics g) {
    if (isEnabled()) {
      super.paintComponent(g);
    } else {
      Dimension d = getSize();
      BufferedImage img = Optional.ofNullable(buf)
          .filter(bi -> bi.getWidth() == d.width && bi.getHeight() == d.height)
          .orElseGet(() -> new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB));
      Graphics2D g2 = img.createGraphics();
      g2.setFont(g.getFont()); // pointed out by 八ツ玉舘
      super.paintComponent(g2);
      g2.dispose();
      g.drawImage(CONVOLVE.filter(img, null), 0, 0, this);
      buf = img;
    }
  }

  // @Override public Dimension getPreferredSize() {
  //   Dimension d = super.getPreferredSize();
  //   d.width += 3 * 3;
  //   return d;
  // }
}

// https://ateraimemo.com/Swing/ButtonDisabledHtmlText.html
// https://github.com/aterai/java-swing-tips/tree/master/ButtonDisabledHtmlText
class BlurLayerUI<V extends AbstractButton> extends LayerUI<V> {
  private static final Kernel KNL = new Kernel(3, 3, new float[] {
      .05f, .05f, .05f,
      .05f, .60f, .05f,
      .05f, .05f, .05f
  });
  private static final ConvolveOp CONVOLVE = new ConvolveOp(KNL, ConvolveOp.EDGE_NO_OP, null);
  private transient BufferedImage buf;

  @Override public void paint(Graphics g, JComponent c) {
    if (c instanceof JLayer) {
      Component view = ((JLayer<?>) c).getView();
      if (view.isEnabled()) {
        // super.paint(g, (JComponent) view);
        view.paint(g);
      } else {
        Dimension d = view.getSize();
        BufferedImage img = Optional.ofNullable(buf)
            .filter(bi -> bi.getWidth() == d.width && bi.getHeight() == d.height)
            .orElseGet(() -> new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB));
        Graphics2D g2 = img.createGraphics();
        view.paint(g2);
        g2.dispose();
        g.drawImage(CONVOLVE.filter(img, null), 0, 0, c);
        buf = img;
      }
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
