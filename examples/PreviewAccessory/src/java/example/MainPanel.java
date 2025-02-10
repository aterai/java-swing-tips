// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final JFileChooser fileChooser = new JFileChooser() {
    @Override public void updateUI() {
      super.updateUI();
      setAccessory(new ImagePreview(this));
    }
  };

  private MainPanel() {
    super(new GridBagLayout());
    JButton button = new JButton("Open JFileChooser");
    button.addActionListener(e -> fileChooser.showOpenDialog(getRootPane()));

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    add(button);
    setPreferredSize(new Dimension(320, 240));
  }

  @Override public void updateUI() {
    super.updateUI();
    if (Objects.nonNull(fileChooser)) {
      SwingUtilities.updateComponentTreeUI(fileChooser);
    }
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

// How to Use File Choosers (The Java™ Tutorials > ... > Using Swing Components)
// https://docs.oracle.com/javase/tutorial/uiswing/components/filechooser.html
// https://docs.oracle.com/javase/tutorial/uiswing/examples/components/index.html#FileChooserDemo2
// https://docs.oracle.com/javase/tutorial/uiswing/examples/components/FileChooserDemo2Project/src/components/ImagePreview.java
class ImagePreview extends JComponent implements PropertyChangeListener {
  private static final int PREVIEW_WIDTH = 90;
  private static final int PREVIEW_MARGIN = 5;
  private ImageIcon thumbnail;

  protected ImagePreview(JFileChooser chooser) {
    super();
    chooser.addPropertyChangeListener(this);
  }

  @Override public Dimension getPreferredSize() {
    return new Dimension(PREVIEW_WIDTH + PREVIEW_MARGIN * 2, 50);
  }

  private static ImageIcon getImageThumbnail(File file) {
    return Optional.ofNullable(file)
        .map(File::getPath)
        .map(ImageIcon::new)
        .map(i -> {
          int w = i.getIconWidth();
          return w > PREVIEW_WIDTH ? getScaledImageIcon(i, PREVIEW_WIDTH / (float) w) : i;
        })
        .orElse(null);
  }

  private static ImageIcon getScaledImageIcon(ImageIcon icon, float scale) {
    // Image img = icon.getImage().getScaledInstance(PREVIEW_WIDTH, -1, Image.SCALE_DEFAULT);
    // The Perils of Image.getScaledInstance() | Java.net
    // <del>http://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html</del>
    // The Perils of Image.getScaledInstance() Blog | Oracle Community
    // https://community.oracle.com/docs/DOC-983611
    // float scale = PREVIEW_WIDTH / (float) icon.getIconWidth();
    int newW = Math.round(icon.getIconWidth() * scale);
    int newH = Math.round(icon.getIconHeight() * scale);
    BufferedImage img = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = img.createGraphics();
    g2.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2.drawImage(icon.getImage(), 0, 0, newW, newH, null);
    g2.dispose();
    return new ImageIcon(img);
  }

  @Override public void propertyChange(PropertyChangeEvent e) {
    String name = e.getPropertyName();
    boolean update = false;
    File file = null;
    if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(name)) {
      file = (File) e.getNewValue();
      update = true;
    } else if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(name)) {
      update = true;
    }
    if (update) {
      thumbnail = getImageThumbnail(file);
      if (isShowing()) {
        repaint();
      }
    }
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (Objects.nonNull(thumbnail)) {
      int x = Math.max(PREVIEW_MARGIN, getWidth() / 2 - thumbnail.getIconWidth() / 2);
      int y = Math.max(0, getHeight() / 2 - thumbnail.getIconHeight() / 2);
      thumbnail.paintIcon(this, g, x, y);
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
