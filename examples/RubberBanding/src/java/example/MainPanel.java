// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
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
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    DefaultListModel<ListItem> model = new DefaultListModel<>();
    // [XP Style Icons - Download](https://xp-style-icons.en.softonic.com/)
    model.addElement(new ListItem("32x32Icon", "example/wi0054-32.png"));
    model.addElement(new ListItem("test", "example/wi0062-32.png"));
    model.addElement(new ListItem("PngImage", "example/wi0063-32.png"));
    model.addElement(new ListItem("Test", "example/wi0064-32.png"));
    model.addElement(new ListItem("3333", "example/wi0063-32.png"));
    model.addElement(new ListItem("12345", "example/wi0096-32.png"));
    model.addElement(new ListItem("111111", "example/wi0054-32.png"));
    model.addElement(new ListItem("22222", "example/wi0062-32.png"));
    model.addElement(new ListItem("Test2", "example/wi0064-32.png"));

    add(new JScrollPane(new RubberBandSelectionList<>(model)));
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

class RubberBandSelectionList<E extends ListItem> extends JList<E> {
  private transient RubberBandingListener rbl;
  private Color rubberBandColor;
  private final Path2D rubberBand = new Path2D.Double();

  protected RubberBandSelectionList(ListModel<E> model) {
    super(model);
  }

  @Override public void updateUI() {
    setSelectionForeground(null); // Nimbus
    setSelectionBackground(null); // Nimbus
    setCellRenderer(null);
    removeMouseListener(rbl);
    removeMouseMotionListener(rbl);
    super.updateUI();

    rubberBandColor = makeRubberBandColor(getSelectionBackground());
    setLayoutOrientation(HORIZONTAL_WRAP);
    setVisibleRowCount(0);
    setFixedCellWidth(74);
    setFixedCellHeight(64);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    setCellRenderer(new ListItemListCellRenderer<>());
    rbl = new RubberBandingListener();
    addMouseMotionListener(rbl);
    addMouseListener(rbl);
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(getSelectionBackground());
    g2.draw(rubberBand);
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .1f));
    g2.setPaint(rubberBandColor);
    g2.fill(rubberBand);
    g2.dispose();
  }

  private static Color makeRubberBandColor(Color c) {
    int r = c.getRed();
    int g = c.getGreen();
    int b = c.getBlue();
    int max = Math.max(Math.max(r, g), b);
    if (max == r) {
      max <<= 8;
    } else if (max == g) {
      max <<= 4;
    }
    return new Color(max);
    // return r > g ? r > b ? new Color(r, 0, 0) : new Color(0, 0, b)
    //              : g > b ? new Color(0, g, 0) : new Color(0, 0, b);
  }

  protected Path2D getRubberBand() {
    return rubberBand;
  }

  private final class RubberBandingListener extends MouseAdapter {
    private final Point srcPoint = new Point();

    @Override public void mouseDragged(MouseEvent e) {
      JList<?> l = (JList<?>) e.getComponent();
      l.setFocusable(true);
      Point dstPoint = e.getPoint();
      Path2D rb = getRubberBand();
      rb.reset();
      rb.moveTo(srcPoint.x, srcPoint.y);
      rb.lineTo(dstPoint.x, srcPoint.y);
      rb.lineTo(dstPoint.x, dstPoint.y);
      rb.lineTo(srcPoint.x, dstPoint.y);
      rb.closePath();

      // JDK 1.7.0: l.setSelectedIndices(getIntersectsIcons(l, rubberBand));
      int[] indices = IntStream.range(0, l.getModel().getSize())
          .filter(i -> rb.intersects(l.getCellBounds(i, i))).toArray();
      l.setSelectedIndices(indices);
      l.repaint();
    }

    @Override public void mouseReleased(MouseEvent e) {
      getRubberBand().reset();
      Component c = e.getComponent();
      c.setFocusable(true);
      c.repaint();
    }

    @Override public void mousePressed(MouseEvent e) {
      JList<?> l = (JList<?>) e.getComponent();
      Point pt = e.getPoint();
      int index = l.locationToIndex(pt);
      if (l.getCellBounds(index, index).contains(pt)) {
        l.setFocusable(true);
      } else {
        l.clearSelection();
        l.getSelectionModel().setAnchorSelectionIndex(-1);
        l.getSelectionModel().setLeadSelectionIndex(-1);
        l.setFocusable(false);
      }
      srcPoint.setLocation(pt);
      l.repaint();
    }

    // // JDK 1.7.0
    // private static int[] getIntersectsIcons(JList<?> l, Shape rect) {
    //   ListModel model = l.getModel();
    //   List<Integer> ll = new ArrayList<>(model.getSize());
    //   for (int i = 0; i < model.getSize(); i++) {
    //     if (rect.intersects(l.getCellBounds(i, i))) {
    //       ll.add(i);
    //     }
    //   }
    //   // JDK 1.8.0: return ll.stream().mapToInt(Integer::intValue).toArray();
    //   int[] il = new int[ll.size()];
    //   for (int i = 0; i < ll.size(); i++) {
    //     il[i] = ll.get(i);
    //   }
    //   return il;
    // }
  }
}

class SelectedImageFilter extends RGBImageFilter {
  // public SelectedImageFilter() {
  //   canFilterIndexColorModel = false;
  // }

  @Override public int filterRGB(int x, int y, int argb) {
    // Color color = new Color(argb, true);
    // float[] array = new float[4];
    // color.getComponents(array);
    // return new Color(array[0], array[1], array[2] * .5f, array[3]).getRGB();
    return argb & 0xFF_FF_FF_00 | (argb & 0xFF) >> 1;
  }
}

// class DotBorder extends EmptyBorder {
//   protected DotBorder(Insets borderInsets) {
//     super(borderInsets);
//   }
//
//   protected DotBorder(int top, int left, int bottom, int right) {
//     super(top, left, bottom, right);
//   }
//
//   @Override public boolean isBorderOpaque() {
//     return true;
//   }
//
//   @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
//     Graphics2D g2 = (Graphics2D) g.create();
//     g2.translate(x, y);
//     g2.setPaint(new Color(~SystemColor.activeCaption.getRGB()));
//     // new Color(200, 150, 150));
//     // g2.setStroke(dashed);
//     // g2.drawRect(0, 0, w - 1, h - 1);
//     BasicGraphicsUtils.drawDashedRect(g2, 0, 0, w, h);
//     g2.dispose();
//   }
// }

class ListItemListCellRenderer<E extends ListItem> implements ListCellRenderer<E> {
  private final JPanel renderer = new JPanel(new BorderLayout());
  private final JLabel icon = new JLabel((Icon) null, SwingConstants.CENTER);
  private final JLabel label = new JLabel("", SwingConstants.CENTER);
  // private final Border dotBorder = new DotBorder(2, 2, 2, 2);
  // private final Border empBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
  private final Border focusBorder = UIManager.getBorder("List.focusCellHighlightBorder");
  private final Border noFocusBorder; // = UIManager.getBorder("List.noFocusBorder");

  protected ListItemListCellRenderer() {
    Border b = UIManager.getBorder("List.noFocusBorder");
    if (Objects.isNull(b)) { // Nimbus???
      Insets i = focusBorder.getBorderInsets(label);
      b = BorderFactory.createEmptyBorder(i.top, i.left, i.bottom, i.right);
    }
    noFocusBorder = b;
    icon.setOpaque(false);
    label.setForeground(renderer.getForeground());
    label.setBackground(renderer.getBackground());
    label.setBorder(noFocusBorder);

    renderer.setOpaque(false);
    renderer.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    renderer.add(icon);
    renderer.add(label, BorderLayout.SOUTH);
  }

  @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
    label.setText(value.getTitle());
    label.setBorder(cellHasFocus ? focusBorder : noFocusBorder);
    if (isSelected) {
      icon.setIcon(value.getSelectedIcon());
      label.setForeground(list.getSelectionForeground());
      label.setBackground(list.getSelectionBackground());
      label.setOpaque(true);
    } else {
      icon.setIcon(value.getIcon());
      label.setForeground(list.getForeground());
      label.setBackground(list.getBackground());
      label.setOpaque(false);
    }
    return renderer;
  }
}

class ListItem {
  private final ImageIcon icon;
  private final ImageIcon selectedIcon;
  private final String title;

  protected ListItem(String title, String path) {
    this.title = title;
    Image img = makeImage(path);
    this.icon = new ImageIcon(img);
    ImageProducer ip = new FilteredImageSource(img.getSource(), new SelectedImageFilter());
    this.selectedIcon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(ip));
  }

  public static Image makeImage(String path) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(ListItem::makeMissingImage);
  }

  private static BufferedImage makeMissingImage() {
    Icon missingIcon = UIManager.getIcon("OptionPane.errorIcon");
    int iw = missingIcon.getIconWidth();
    int ih = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, (32 - iw) / 2, (32 - ih) / 2);
    g2.dispose();
    return bi;
  }

  public ImageIcon getIcon() {
    return icon;
  }

  public ImageIcon getSelectedIcon() {
    return selectedIcon;
  }

  public String getTitle() {
    return title;
  }
}
