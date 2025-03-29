// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.RGBImageFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 2, 4, 4));
    String path = "example/31g.png";
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Image img = Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(MainPanel::makeMissingImage);
    ImageIcon defaultIcon = new ImageIcon(img);

    // 1
    List<ImageIcon> list1 = Arrays.asList(
        makeStarImageIcon(img, new SelectedImageFilter(1f, .5f, .5f)),
        makeStarImageIcon(img, new SelectedImageFilter(.5f, 1f, .5f)),
        makeStarImageIcon(img, new SelectedImageFilter(1f, .5f, 1f)),
        makeStarImageIcon(img, new SelectedImageFilter(.5f, .5f, 1f)),
        makeStarImageIcon(img, new SelectedImageFilter(1f, 1f, .5f)));
    add(makeStarRatingPanel("gap=0", new LevelBar(defaultIcon, list1, 0)));

    // 2
    List<ImageIcon> list2 = Arrays.asList(
        makeStarImageIcon(img, new SelectedImageFilter(.2f, .5f, .5f)),
        makeStarImageIcon(img, new SelectedImageFilter(0f, 1f, .2f)),
        makeStarImageIcon(img, new SelectedImageFilter(1f, 1f, .2f)),
        makeStarImageIcon(img, new SelectedImageFilter(.8f, .4f, .2f)),
        makeStarImageIcon(img, new SelectedImageFilter(1f, .1f, .1f)));
    add(makeStarRatingPanel("gap=1+1", new LevelBar(defaultIcon, list2, 1) {
      @Override protected void repaintIcon(int index) {
        for (int i = 0; i < getLabelList().size(); i++) {
          getLabelList().get(i).setIcon(i <= index ? getIconList().get(index) : defaultIcon);
        }
        repaint();
      }
    }));

    // 3
    List<ImageIcon> list3 = Arrays.asList(
        makeStarImageIcon(img, new SelectedImageFilter(.6f, .6f, 0f)),
        makeStarImageIcon(img, new SelectedImageFilter(.7f, .7f, 0f)),
        makeStarImageIcon(img, new SelectedImageFilter(.8f, .8f, 0f)),
        makeStarImageIcon(img, new SelectedImageFilter(.9f, .9f, 0f)),
        makeStarImageIcon(img, new SelectedImageFilter(1f, 1f, 0f)));
    add(makeStarRatingPanel("gap=2+2", new LevelBar(defaultIcon, list3, 2)));

    // 4
    ImageIcon star = makeStarImageIcon(img, new SelectedImageFilter(1f, 1f, 0f));
    List<ImageIcon> list4 = Arrays.asList(star, star, star, star, star);
    add(makeStarRatingPanel("gap=1+1", new LevelBar(defaultIcon, list4, 1)));
    setPreferredSize(new Dimension(320, 240));
  }

  private JPanel makeStarRatingPanel(String title, LevelBar label) {
    JButton button = new JButton("clear");
    button.addActionListener(e -> label.clear());

    JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(button);
    p.add(label);
    return p;
  }

  private static ImageIcon makeStarImageIcon(Image image, ImageFilter filter) {
    FilteredImageSource producer = new FilteredImageSource(image.getSource(), filter);
    return new ImageIcon(Toolkit.getDefaultToolkit().createImage(producer));
  }

  private static Image makeMissingImage() {
    Icon missingIcon = UIManager.getIcon("html.missingImage");
    int iw = missingIcon.getIconWidth();
    int ih = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, (16 - iw) / 2, (16 - ih) / 2);
    g2.dispose();
    return bi;
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

class LevelBar extends JPanel {
  private final int gap;
  private final List<ImageIcon> iconList;
  private final List<JLabel> labelList = Arrays.asList(
      new JLabel(), new JLabel(), new JLabel(), new JLabel(), new JLabel()
  );
  private final ImageIcon defaultIcon;
  private int clicked = -1;
  private transient MouseAdapter handler;

  protected LevelBar(ImageIcon defaultIcon, List<ImageIcon> list, int gap) {
    super(new GridLayout(1, 5, gap * 2, gap * 2));
    this.defaultIcon = defaultIcon;
    this.iconList = list;
    this.gap = gap;
    EventQueue.invokeLater(() -> {
      for (JLabel l : labelList) {
        l.setIcon(defaultIcon);
        add(l);
      }
    });
  }

  public List<ImageIcon> getIconList() {
    return iconList;
  }

  public List<JLabel> getLabelList() {
    return labelList;
  }

  @Override public void updateUI() {
    removeMouseListener(handler);
    removeMouseMotionListener(handler);
    super.updateUI();
    handler = new MouseAdapter() {
      @Override public void mouseMoved(MouseEvent e) {
        repaintIcon(getSelectedIconIndex(e.getPoint()));
      }

      @Override public void mouseEntered(MouseEvent e) {
        repaintIcon(getSelectedIconIndex(e.getPoint()));
      }

      @Override public void mouseClicked(MouseEvent e) {
        clicked = getSelectedIconIndex(e.getPoint());
      }

      @Override public void mouseExited(MouseEvent e) {
        repaintIcon(clicked);
      }
    };
    addMouseListener(handler);
    addMouseMotionListener(handler);
  }

  public void clear() {
    clicked = -1;
    repaintIcon(clicked);
  }

  // public int getLevel() {
  //   return clicked;
  // }

  // public void setLevel(int l) {
  //   clicked = l;
  //   repaintIcon(clicked);
  // }

  protected int getSelectedIconIndex(Point p) {
    return IntStream.range(0, labelList.size())
        .filter(i -> {
          Rectangle r = labelList.get(i).getBounds();
          r.grow(gap, gap);
          return r.contains(p);
        })
        .findFirst()
        .orElse(-1);
  }

  protected void repaintIcon(int index) {
    for (int i = 0; i < labelList.size(); i++) {
      labelList.get(i).setIcon(i <= index ? iconList.get(i) : defaultIcon);
    }
    repaint();
  }
}

class SelectedImageFilter extends RGBImageFilter {
  private final float rf;
  private final float gf;
  private final float bf;

  protected SelectedImageFilter(float rf, float gf, float bf) {
    super();
    this.rf = Math.min(1f, rf);
    this.gf = Math.min(1f, gf);
    this.bf = Math.min(1f, bf);
    canFilterIndexColorModel = false;
  }

  @Override public int filterRGB(int x, int y, int argb) {
    int r = Math.round(((argb >> 16) & 0xFF) * rf);
    int g = Math.round(((argb >> 8) & 0xFF) * gf);
    int b = Math.round((argb & 0xFF) * bf);
    return argb & 0xFF_00_00_00 | r << 16 | g << 8 | b;
  }
}
