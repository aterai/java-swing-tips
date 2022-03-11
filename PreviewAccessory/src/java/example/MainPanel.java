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
    mb.add(LookAndFeelUtil.createLookAndFeelMenu());
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
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
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
  private File file;

  protected ImagePreview(JFileChooser chooser) {
    super();
    chooser.addPropertyChangeListener(this);
  }

  @Override public Dimension getPreferredSize() {
    return new Dimension(PREVIEW_WIDTH + PREVIEW_MARGIN * 2, 50);
  }

  private static ImageIcon getImageThumbnail(File file) {
    if (Objects.isNull(file)) {
      return null;
    }
    ImageIcon tmpIcon = new ImageIcon(file.getPath());
    if (tmpIcon.getIconWidth() > PREVIEW_WIDTH) {
      // Image img = tmpIcon.getImage().getScaledInstance(PREVIEW_WIDTH, -1, Image.SCALE_DEFAULT);
      // The Perils of Image.getScaledInstance() | Java.net
      // <del>http://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html</del>
      // The Perils of Image.getScaledInstance() Blog | Oracle Community
      // https://community.oracle.com/docs/DOC-983611
      float scale = PREVIEW_WIDTH / (float) tmpIcon.getIconWidth();
      int newW = Math.round(tmpIcon.getIconWidth() * scale);
      int newH = Math.round(tmpIcon.getIconHeight() * scale);
      BufferedImage img = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = img.createGraphics();
      g2.setRenderingHint(
          RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      g2.drawImage(tmpIcon.getImage(), 0, 0, newW, newH, null);
      g2.dispose();
      return new ImageIcon(img);
    } else {
      return tmpIcon;
    }
  }

  @Override public void propertyChange(PropertyChangeEvent e) {
    boolean update = false;
    String prop = e.getPropertyName();
    if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop)) {
      file = null;
      update = true;
    } else if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
      file = (File) e.getNewValue();
      update = true;
    }
    if (update) {
      thumbnail = null;
      if (isShowing()) {
        thumbnail = getImageThumbnail(file);
        repaint();
      }
    }
  }

  @Override protected void paintComponent(Graphics g) {
    if (Objects.isNull(thumbnail)) {
      thumbnail = getImageThumbnail(file);
    }
    if (Objects.nonNull(thumbnail)) {
      int x = Math.max(PREVIEW_MARGIN, getWidth() / 2 - thumbnail.getIconWidth() / 2);
      int y = Math.max(0, getHeight() / 2 - thumbnail.getIconHeight() / 2);
      thumbnail.paintIcon(this, g, x, y);
    }
  }
}

// @see https://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtil {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtil() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup lafGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo lafInfo : UIManager.getInstalledLookAndFeels()) {
      menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lafGroup));
    }
    return menu;
  }

  private static JMenuItem createLookAndFeelItem(String laf, String lafClass, ButtonGroup bg) {
    JMenuItem lafItem = new JRadioButtonMenuItem(laf, lafClass.equals(lookAndFeel));
    lafItem.setActionCommand(lafClass);
    lafItem.setHideActionText(true);
    lafItem.addActionListener(e -> {
      ButtonModel m = bg.getSelection();
      try {
        setLookAndFeel(m.getActionCommand());
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        UIManager.getLookAndFeel().provideErrorFeedback((Component) e.getSource());
      }
    });
    bg.add(lafItem);
    return lafItem;
  }

  private static void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
    String oldLookAndFeel = LookAndFeelUtil.lookAndFeel;
    if (!oldLookAndFeel.equals(lookAndFeel)) {
      UIManager.setLookAndFeel(lookAndFeel);
      LookAndFeelUtil.lookAndFeel = lookAndFeel;
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
