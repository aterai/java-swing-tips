// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    ImageIcon image1 = new ImageIcon(getClass().getResource("favicon.png"));
    ImageIcon image2 = new ImageIcon(getClass().getResource("16x16.png"));
    ImageIcon rss = new ImageIcon(getClass().getResource("feed-icon-14x14.png")); // http://feedicons.com/

    JComboBox<SiteItem> combo01 = new JComboBox<>(makeTestModel(image1, image2));
    initComboBox(combo01);

    JComboBox<SiteItem> combo02 = new SiteItemComboBox(makeTestModel(image1, image2), rss);
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

  private static DefaultComboBoxModel<SiteItem> makeTestModel(ImageIcon image1, ImageIcon image2) {
    DefaultComboBoxModel<SiteItem> model = new DefaultComboBoxModel<>();
    model.addElement(new SiteItem("https://ateraimemo.com/", image1, true));
    model.addElement(new SiteItem("https://ateraimemo.com/Swing.html", image1, true));
    model.addElement(new SiteItem("https://ateraimemo.com/JavaWebStart.html", image1, true));
    model.addElement(new SiteItem("https://github.com/aterai/java-swing-tips", image2, true));
    model.addElement(new SiteItem("https://java-swing-tips.blogspot.com/", image2, true));
    model.addElement(new SiteItem("http://www.example.com/", image2, false));
    return model;
  }

  private static void initComboBox(JComboBox<SiteItem> combo) {
    combo.setEditable(true);
    combo.setRenderer(new DefaultListCellRenderer() {
      @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        c.setIcon(((SiteItem) value).favicon);
        return c;
      }
    });
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
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class SiteItemComboBox extends JComboBox<SiteItem> {
  protected SiteItemComboBox(DefaultComboBoxModel<SiteItem> model, ImageIcon rss) {
    super(model);

    JTextField field = (JTextField) getEditor().getEditorComponent();
    JButton feedButton = makeRssButton(rss);
    JLabel favicon = makeLabel(field);
    setLayout(new SiteComboBoxLayout(favicon, feedButton));
    add(feedButton);
    add(favicon);

    field.addFocusListener(new FocusListener() {
      @Override public void focusGained(FocusEvent e) {
        // field.setBorder(BorderFactory.createEmptyBorder(0, 16 + 4, 0, 0));
        feedButton.setVisible(false);
      }

      @Override public void focusLost(FocusEvent e) {
        getSiteItemFromModel(model, field.getText()).ifPresent(item -> {
          model.removeElement(item);
          model.insertElementAt(item, 0);
          favicon.setIcon(item.favicon);
          feedButton.setVisible(item.hasRss);
          setSelectedIndex(0);
        });
      }
    });
    addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        updateFavicon(model, favicon);
      }
    });
    updateFavicon(model, favicon);
  }

  private void updateFavicon(ComboBoxModel<SiteItem> model, JLabel l) {
    EventQueue.invokeLater(() -> getSiteItemFromModel(model, getSelectedItem()).ifPresent(i -> l.setIcon(i.favicon)));
  }

  private static JButton makeRssButton(ImageIcon rss) {
    JButton button = new JButton(rss);
    ImageProducer ip = new FilteredImageSource(rss.getImage().getSource(), new SelectedImageFilter());
    button.setRolloverIcon(new ImageIcon(Toolkit.getDefaultToolkit().createImage(ip)));
    // button.setRolloverIcon(makeFilteredImage(rss));
    // button.setRolloverIcon(makeFilteredImage2(rss));
    button.addActionListener(e -> System.out.println("clicked..."));
    button.setFocusPainted(false);
    button.setBorderPainted(false);
    button.setContentAreaFilled(false);
    button.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    button.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 2));
    return button;
  }

  private static JLabel makeLabel(JTextField field) {
    JLabel label = new JLabel();
    label.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        EventQueue.invokeLater(() -> {
          field.requestFocusInWindow();
          field.selectAll();
        });
      }
    });
    label.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    label.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 2));
    return label;
  }

  protected Optional<SiteItem> getSiteItemFromModel(ComboBoxModel<SiteItem> model, Object o) {
    if (o instanceof SiteItem) {
      return Optional.of((SiteItem) o);
    }
    String str = Objects.toString(o, "");
    return IntStream.range(0, model.getSize())
      .mapToObj(model::getElementAt)
      .filter(ui -> ui.url.equals(str))
      .findFirst();
    // DefaultComboBoxModel<SiteItem> model = (DefaultComboBoxModel<SiteItem>) getModel();
    // SiteItem item = null;
    // for (int i = 0; i < model.getSize(); i++) {
    //   SiteItem tmp = model.getElementAt(i);
    //   if (tmp.url.equals(text)) {
    //     item = tmp;
    //     break;
    //   }
    // }
    // if (Objects.nonNull(item)) {
    //   model.removeElement(item);
    //   model.insertElementAt(item, 0);
    // }
    // return item;
  }
  // private ImageIcon getFavicon(String url) {
  //   if (url.startsWith("https://ateraimemo.com/")) {
  //     return image1;
  //   } else {
  //     return image2;
  //   }
  // }
  // private boolean hasRss(String url) {
  //   return url.startsWith("https://ateraimemo.com/");
  // }
  // public static ImageIcon makeFilteredImage(ImageIcon srcIcon) {
  //   ImageProducer ip = new FilteredImageSource(srcIcon.getImage().getSource(), new SelectedImageFilter());
  //   return new ImageIcon(Toolkit.getDefaultToolkit().createImage(ip));
  // }
  // // Test:
  // public static ImageIcon makeFilteredImage2(ImageIcon srcIcon) {
  //   RescaleOp op = new RescaleOp(new float[] { 1.2f, 1.2f, 1.2f, 1f }, new float[] { 0f, 0f, 0f, 0f }, null);
  //   BufferedImage img = new BufferedImage(srcIcon.getIconWidth(), srcIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
  //   // TEST: RescaleOp op = new RescaleOp(1.2f, 0f, null);
  //   // BufferedImage img = new BufferedImage(srcIcon.getIconWidth(), srcIcon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
  //   Graphics g = img.getGraphics();
  //   // g.drawImage(srcIcon.getImage(), 0, 0, null);
  //   srcIcon.paintIcon(null, g, 0, 0);
  //   g.dispose();
  //   return new ImageIcon(op.filter(img, null));
  // }
}

class SiteComboBoxLayout implements LayoutManager {
  private final JLabel favicon;
  private final JButton feedButton;

  protected SiteComboBoxLayout(JLabel favicon, JButton feedButton) {
    this.favicon = favicon;
    this.feedButton = feedButton;
  }

  @Override public void addLayoutComponent(String name, Component comp) { /* not needed */ }

  @Override public void removeLayoutComponent(Component comp) { /* not needed */ }

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
    int width = cb.getWidth();
    int height = cb.getHeight();
    Insets insets = cb.getInsets();
    int arrowHeight = height - insets.top - insets.bottom;
    int arrowWidth = arrowHeight;
    int faviconWidth = arrowHeight;
    int feedWidth; // = arrowHeight;

    // Arrow Icon JButton
    JButton arrowButton = (JButton) cb.getComponent(0);
    if (Objects.nonNull(arrowButton)) {
      Insets arrowInsets = arrowButton.getInsets();
      arrowWidth = arrowButton.getPreferredSize().width + arrowInsets.left + arrowInsets.right;
      arrowButton.setBounds(width - insets.right - arrowWidth, insets.top, arrowWidth, arrowHeight);
    }

    // Favicon JLabel
    if (Objects.nonNull(favicon)) {
      Insets faviconInsets = favicon.getInsets();
      faviconWidth = favicon.getPreferredSize().width + faviconInsets.left + faviconInsets.right;
      favicon.setBounds(insets.left, insets.top, faviconWidth, arrowHeight);
    }

    // JButton rssButton = feedButton;
    // for (Component c: cb.getComponents()) {
    //   if ("ComboBox.rssButton".equals(c.getName())) {
    //     rssButton = (JButton) c;
    //     break;
    //   }
    // }

    // Feed Icon JButton
    if (Objects.nonNull(feedButton) && feedButton.isVisible()) {
      Insets feedInsets = feedButton.getInsets();
      feedWidth = feedButton.getPreferredSize().width + feedInsets.left + feedInsets.right;
      feedButton.setBounds(width - insets.right - feedWidth - arrowWidth, insets.top, feedWidth, arrowHeight);
    } else {
      feedWidth = 0;
    }

    // JComboBox Editor
    Component editor = cb.getEditor().getEditorComponent();
    if (Objects.nonNull(editor)) {
      editor.setBounds(insets.left + faviconWidth, insets.top,
               width - insets.left - insets.right - arrowWidth - faviconWidth - feedWidth,
               height - insets.top - insets.bottom);
    }
  }
}

class SiteItem {
  public final String url;
  public final ImageIcon favicon;
  public final boolean hasRss;

  protected SiteItem(String url, ImageIcon icon, boolean hasRss) {
    this.url = url;
    this.favicon = icon;
    this.hasRss = hasRss;
  }

  @Override public String toString() {
    return url;
  }
}

class SelectedImageFilter extends RGBImageFilter {
  // public SelectedImageFilter() {
  //   canFilterIndexColorModel = false;
  // }

  private static final float SCALE = 1.2f;

  @Override public int filterRGB(int x, int y, int argb) {
    // int a = (argb >> 24) & 0xFF;
    int r = (int) Math.min(0xFF, ((argb >> 16) & 0xFF) * SCALE);
    int g = (int) Math.min(0xFF, ((argb >> 8) & 0xFF) * SCALE);
    int b = (int) Math.min(0xFF, (argb & 0xFF) * SCALE);
    return (argb & 0xFF_00_00_00) | (r << 16) | (g << 8) | b;
  }
}
