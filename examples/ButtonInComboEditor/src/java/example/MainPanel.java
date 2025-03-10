// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Image image1 = ImageUtils.makeImage("example/favicon.png");
    Image image2 = ImageUtils.makeImage("example/16x16.png");

    JComboBox<SiteItem> combo01 = new JComboBox<>(makeTestModel(image1, image2));
    initComboBox(combo01);

    JComboBox<SiteItem> combo02 = new SiteItemComboBox(makeTestModel(image1, image2));
    initComboBox(combo02);

    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createTitledBorder("setEditable(true)"));
    box.add(Box.createVerticalStrut(2));
    box.add(combo01);
    box.add(Box.createVerticalStrut(5));
    box.add(combo02);
    box.add(Box.createVerticalStrut(2));

    add(box, BorderLayout.NORTH);
    add(new JScrollPane(new JTextArea()));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static DefaultComboBoxModel<SiteItem> makeTestModel(Image i1, Image i2) {
    DefaultComboBoxModel<SiteItem> model = new DefaultComboBoxModel<>();
    model.addElement(new SiteItem("https://ateraimemo.com/", i1, true));
    model.addElement(new SiteItem("https://ateraimemo.com/Swing.html", i1, true));
    model.addElement(new SiteItem("https://ateraimemo.com/JavaWebStart.html", i1, true));
    model.addElement(new SiteItem("https://github.com/aterai/java-swing-tips", i2, true));
    model.addElement(new SiteItem("https://java-swing-tips.blogspot.com/", i2, true));
    model.addElement(new SiteItem("http://www.example.com/", i2, false));
    return model;
  }

  private static void initComboBox(JComboBox<SiteItem> combo) {
    combo.setEditable(true);
    ListCellRenderer<? super SiteItem> r = combo.getRenderer();
    combo.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
      Component c = r.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (c instanceof JLabel) {
        ((JLabel) c).setIcon(value.getFavicon());
      }
      return c;
    });
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

class SiteItemComboBox extends JComboBox<SiteItem> {
  protected SiteItemComboBox(DefaultComboBoxModel<SiteItem> model) {
    super(model);
    initComponents(model);
  }

  private void initComponents(DefaultComboBoxModel<SiteItem> model) {
    JTextField field = (JTextField) getEditor().getEditorComponent();
    JLabel faviconLabel = makeFaviconLabel(field);
    JButton feedButton = makeRssButton();
    setLayout(new SiteComboBoxLayout(faviconLabel, feedButton));
    add(feedButton);
    add(faviconLabel);

    field.addFocusListener(new FocusListener() {
      @Override public void focusGained(FocusEvent e) {
        // field.setBorder(BorderFactory.createEmptyBorder(0, 16 + 4, 0, 0));
        feedButton.setVisible(false);
      }

      @Override public void focusLost(FocusEvent e) {
        getSiteItemFromModel(model, field.getText()).ifPresent(item -> {
          model.removeElement(item);
          model.insertElementAt(item, 0);
          faviconLabel.setIcon(item.getFavicon());
          feedButton.setVisible(item.hasRss());
          setSelectedIndex(0);
        });
      }
    });
    addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        updateFavicon(model, faviconLabel);
      }
    });
    EventQueue.invokeLater(() -> updateFavicon(model, faviconLabel));
  }

  @Override public final ComboBoxEditor getEditor() {
    return super.getEditor();
  }

  @Override public final void setLayout(LayoutManager mgr) {
    super.setLayout(mgr);
  }

  @Override public final Component add(Component comp) {
    return super.add(comp);
  }

  @Override public final void addItemListener(ItemListener listener) {
    super.addItemListener(listener);
  }

  private void updateFavicon(ComboBoxModel<SiteItem> model, JLabel l) {
    getSiteItemFromModel(model, getSelectedItem())
        .map(SiteItem::getFavicon)
        .ifPresent(l::setIcon);
  }

  private static JButton makeRssButton() {
    Image rss = ImageUtils.makeImage("example/feed-icon-14x14.png"); // http://feedicons.com/
    JButton button = new JButton(new ImageIcon(rss));
    ImageProducer ip = new FilteredImageSource(rss.getSource(), new SelectedImageFilter());
    button.setRolloverIcon(new ImageIcon(button.getToolkit().createImage(ip)));
    // button.setRolloverIcon(makeFilteredImage(rss));
    // button.setRolloverIcon(makeFilteredImage2(rss));
    button.addActionListener(e -> Toolkit.getDefaultToolkit().beep());
    button.setFocusPainted(false);
    button.setBorderPainted(false);
    button.setContentAreaFilled(false);
    button.setCursor(Cursor.getDefaultCursor());
    button.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 2));
    return button;
  }

  private static JLabel makeFaviconLabel(JTextField field) {
    JLabel label = new JLabel();
    label.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        EventQueue.invokeLater(() -> {
          field.requestFocusInWindow();
          field.selectAll();
        });
      }
    });
    label.setCursor(Cursor.getDefaultCursor());
    label.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 2));
    return label;
  }

  private Optional<SiteItem> getSiteItemFromModel(ComboBoxModel<SiteItem> model, Object o) {
    Optional<SiteItem> op;
    if (o instanceof SiteItem) {
      op = Optional.of((SiteItem) o);
    } else {
      String str = Objects.toString(o, "");
      op = IntStream.range(0, model.getSize())
          .mapToObj(model::getElementAt)
          .filter(ui -> str.equals(ui.getUrl()))
          .findFirst();
    }
    return op;
  }

  // private ImageIcon getFavicon(String url) {
  //   if (url.startsWith("https://ateraimemo.com/")) {
  //     return image1;
  //   } else {
  //     return image2;
  //   }
  // }
  //
  // private boolean hasRss(String url) {
  //   return url.startsWith("https://ateraimemo.com/");
  // }
  //
  // public static ImageIcon makeFilteredImage(ImageIcon srcIcon) {
  //   ImageProducer ip = new FilteredImageSource(
  //       srcIcon.getImage().getSource(), new SelectedImageFilter());
  //   return new ImageIcon(Toolkit.getDefaultToolkit().createImage(ip));
  // }
  //
  // // Test:
  // public static ImageIcon makeFilteredImage2(ImageIcon srcIcon) {
  //   RescaleOp op = new RescaleOp(
  //       new float[] { 1.2f, 1.2f, 1.2f, 1f }, new float[] { 0f, 0f, 0f, 0f }, null);
  //   int iw = srcIcon.getIconWidth();
  //   int ih = srcIcon.getIconHeight();
  //   BufferedImage img = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_ARGB);
  //   // TEST: RescaleOp op = new RescaleOp(1.2f, 0f, null);
  //   // BufferedImage img = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_RGB);
  //   Graphics g = img.getGraphics();
  //   // g.drawImage(srcIcon.getImage(), 0, 0, null);
  //   srcIcon.paintIcon(null, g, 0, 0);
  //   g.dispose();
  //   return new ImageIcon(op.filter(img, null));
  // }
}

class SiteComboBoxLayout implements LayoutManager {
  private final JLabel faviconLabel;
  private final JButton feedButton;

  protected SiteComboBoxLayout(JLabel faviconLabel, JButton feedButton) {
    this.faviconLabel = faviconLabel;
    this.feedButton = feedButton;
  }

  @Override public void addLayoutComponent(String name, Component comp) {
    /* not needed */
  }

  @Override public void removeLayoutComponent(Component comp) {
    /* not needed */
  }

  @Override public Dimension preferredLayoutSize(Container parent) {
    return parent.getPreferredSize();
  }

  @Override public Dimension minimumLayoutSize(Container parent) {
    return parent.getMinimumSize();
  }

  @Override public void layoutContainer(Container parent) {
    if (!(parent instanceof JComboBox)) {
      return;
    }
    JComboBox<?> cb = (JComboBox<?>) parent;
    Rectangle r = SwingUtilities.calculateInnerArea(cb, null);

    // Arrow Icon JButton
    int arrowWidth = 0;
    JButton arrowButton = (JButton) cb.getComponent(0);
    if (Objects.nonNull(arrowButton)) {
      arrowWidth = arrowButton.getPreferredSize().width;
      arrowButton.setBounds(r.x + r.width - arrowWidth, r.y, arrowWidth, r.height);
    }

    // Favicon JLabel
    int faviconWidth = 0;
    if (Objects.nonNull(faviconLabel)) {
      faviconWidth = faviconLabel.getPreferredSize().width;
      faviconLabel.setBounds(r.x, r.y, faviconWidth, r.height);
    }

    // Feed Icon JButton
    int feedWidth = 0;
    if (Objects.nonNull(feedButton) && feedButton.isVisible()) {
      feedWidth = feedButton.getPreferredSize().width;
      feedButton.setBounds(r.x + r.width - feedWidth - arrowWidth, r.y, feedWidth, r.height);
    }

    // JComboBox Editor
    Component editor = cb.getEditor().getEditorComponent();
    if (Objects.nonNull(editor)) {
      int w = r.width - arrowWidth - faviconWidth - feedWidth;
      editor.setBounds(r.x + faviconWidth, r.y, w, r.height);
    }
  }
}

class SiteItem {
  private final String url;
  private final Icon favicon;
  private final boolean rss;

  protected SiteItem(String url, Image image, boolean rss) {
    this.url = url;
    this.favicon = new ImageIcon(image);
    this.rss = rss;
  }

  public String getUrl() {
    return url;
  }

  public Icon getFavicon() {
    return favicon;
  }

  public boolean hasRss() {
    return rss;
  }

  @Override public String toString() {
    return url;
  }
}

class SelectedImageFilter extends RGBImageFilter {
  private static final float SCALE = 1.2f;

  // public SelectedImageFilter() {
  //   canFilterIndexColorModel = false;
  // }

  @Override public int filterRGB(int x, int y, int argb) {
    // int a = (argb >> 24) & 0xFF;
    int r = Math.min(0xFF, Math.round(((argb >> 16) & 0xFF) * SCALE));
    int g = Math.min(0xFF, Math.round(((argb >> 8) & 0xFF) * SCALE));
    int b = Math.min(0xFF, Math.round((argb & 0xFF) * SCALE));
    return argb & 0xFF_00_00_00 | r << 16 | g << 8 | b;
  }
}

final class ImageUtils {
  private ImageUtils() {
    /* Singleton */
  }

  public static Image makeImage(String path) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(ImageUtils::makeMissingImage);
  }

  public static BufferedImage makeMissingImage() {
    Icon missingIcon = UIManager.getIcon("html.missingImage");
    int iw = missingIcon.getIconWidth();
    int ih = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, (16 - iw) / 2, (16 - ih) / 2);
    g2.dispose();
    return bi;
  }
}
