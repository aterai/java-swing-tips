// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.RGBImageFilter;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Color bg1 = new Color(0xAE_FF_32_00, true);
    Color bg2 = new Color(0x32_FF_32_00, true).darker();

    JButton button1 = new JButton("setBackground");
    button1.setBackground(bg1);

    JButton button2 = new JButton("override paintComponent") {
      @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Rectangle r = new Rectangle(0, 0, getWidth(), getHeight());
        ButtonModel m = getModel();
        g2.setPaint(m.isArmed() || m.isPressed() ? bg1 : bg2);
        g2.fill(r);
        g2.setPaint(Color.GRAY.brighter());
        r.width -= 1;
        r.height -= 1;
        g2.draw(r);
        g2.dispose();
        super.paintComponent(g);
      }
    };
    button2.setContentAreaFilled(false);

    JButton button3 = new JButton("setIcon + setPressedIcon");
    button3.setBackground(bg1);
    button3.setBorderPainted(false);
    button3.setIcon(new ButtonBackgroundIcon(bg2));
    button3.setPressedIcon(new ButtonBackgroundIcon(bg1));
    // button3.setHorizontalTextPosition(SwingConstants.CENTER);

    JButton button4 = new JButton("JLayer + RGBImageFilter");

    Component box = makePanel(
        new JButton("Default"),
        button1,
        button2,
        button3,
        new JLayer<>(button4, new ImageFilterLayerUI<>(new ColorFilter())));

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makePanel(Component... list) {
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(20, 15, 0, 15);
    c.weightx = 1d;
    c.gridx = GridBagConstraints.REMAINDER;
    JPanel p = new JPanel(new GridBagLayout());
    Stream.of(list).forEach(cmp -> p.add(cmp, c));
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

class ButtonBackgroundIcon implements Icon {
  private final Color color;

  protected ButtonBackgroundIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(color);
    g2.fillRect(0, 0, c.getWidth(), c.getHeight());
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 0;
  }

  @Override public int getIconHeight() {
    return 0;
  }
}

class ImageFilterLayerUI<V extends Component> extends LayerUI<V> {
  private final transient ImageFilter filter;
  private transient BufferedImage buf;

  protected ImageFilterLayerUI(ImageFilter filter) {
    super();
    this.filter = filter;
  }

  @Override public void paint(Graphics g, JComponent c) {
    if (c instanceof JLayer) {
      Component view = ((JLayer<?>) c).getView();
      Dimension d = view.getSize();
      BufferedImage img = Optional.ofNullable(buf)
          .filter(bi -> bi.getWidth() == d.width && bi.getHeight() == d.height)
          .orElseGet(() -> new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB));
      Graphics2D g2 = img.createGraphics();
      view.paint(g2);
      g2.dispose();
      Image image = c.createImage(new FilteredImageSource(img.getSource(), filter));
      g.drawImage(image, 0, 0, view);
      buf = img;
    } else {
      super.paint(g, c);
    }
  }
}

class ColorFilter extends RGBImageFilter {
  @Override public int filterRGB(int x, int y, int argb) {
    int r = 0xFF; // (argb >> 16) & 0xFF;
    int g = (argb >> 8) & 0xFF;
    int b = argb & 0xFF;
    return argb & 0xFF_00_00_00 | r << 16 | g << 8 | b;
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
        Logger.getGlobal().severe(ex::getMessage);
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
