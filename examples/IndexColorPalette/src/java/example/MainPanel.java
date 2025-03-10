// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  public static final Dimension CELL_SIZE = new Dimension(8, 8);

  private MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel();
    JLabel label1 = new JLabel();
    p.add(label1);
    JLabel label2 = new JLabel();
    p.add(label2);

    String path = "example/duke.gif";
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    BufferedImage image = Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(MainPanel::makeMissingImage);
    label1.setIcon(new ImageIcon(image));

    ColorModel colorModel = image.getColorModel();
    IndexColorModel idxColorModel = null;
    int transIndex;
    if (colorModel instanceof IndexColorModel) {
      idxColorModel = (IndexColorModel) colorModel;
      transIndex = idxColorModel.getTransparentPixel();
    } else {
      transIndex = -1;
    }

    int w = image.getWidth();
    int h = image.getHeight();
    DataBuffer dataBuffer = image.getRaster().getDataBuffer();
    label2.setIcon(new ImageIcon(makeImage(dataBuffer, colorModel, w, h, transIndex)));

    JPanel box = new JPanel(new GridBagLayout());
    if (Objects.nonNull(idxColorModel)) {
      JList<IndexedColor> palette = new JList<IndexedColor>(new PaletteListModel(idxColorModel)) {
        @Override public void updateUI() {
          setCellRenderer(null);
          super.updateUI();
          setLayoutOrientation(HORIZONTAL_WRAP);
          setVisibleRowCount(8);
          setFixedCellWidth(CELL_SIZE.width);
          setFixedCellHeight(CELL_SIZE.height);
          setCellRenderer(new IndexedColorListRenderer());
          getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
          setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        }
      };
      box.add(new JScrollPane(palette), new GridBagConstraints());
    } else {
      box.add(new JLabel("No IndexColorModel"), new GridBagConstraints());
    }

    add(p, BorderLayout.NORTH);
    add(box);
    setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Image makeImage(DataBuffer buffer, ColorModel model, int w, int h, int idx) {
    // DataBufferByte dataBufferByte = null;
    // if (buffer instanceof DataBufferByte) {
    //   dataBufferByte = (DataBufferByte) buffer;
    // } else {
    //   System.out.println("No DataBufferByte");
    // }
    // byte data[] = dataBufferByte.getData();
    BufferedImage buf = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        int arrayIndex = x + y * w;
        // int colorIndex = Byte.toUnsignedInt(data[arrayIndex]);
        int colorIndex = buffer.getElem(arrayIndex);
        if (idx == colorIndex) {
          buf.setRGB(x, y, Color.RED.getRGB());
        } else {
          buf.setRGB(x, y, model.getRGB(colorIndex));
        }
      }
    }
    return buf;
  }

  private static BufferedImage makeMissingImage() {
    Icon missingIcon = UIManager.getIcon("OptionPane.errorIcon");
    int w = missingIcon.getIconWidth();
    int h = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, 0, 0);
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

class IndexedColor {
  private final int index;
  private final Color color;
  private final boolean transparent;

  protected IndexedColor(int index, Color color, boolean transparent) {
    this.index = index;
    this.color = color;
    this.transparent = transparent;
  }

  public int getIndex() {
    return index;
  }

  public Color getColor() {
    return color;
  }

  public boolean isTransparent() {
    return transparent;
  }
}

class PaletteListModel extends AbstractListModel<IndexedColor> {
  private final transient IndexColorModel model;

  protected PaletteListModel(IndexColorModel model) {
    super();
    this.model = model;
  }

  @Override public int getSize() {
    return model.getMapSize();
  }

  @Override public IndexedColor getElementAt(int idx) {
    Color color = new Color(model.getRGB(idx));
    boolean transparent = idx == model.getTransparentPixel();
    return new IndexedColor(idx, color, transparent);
  }
}

class ColorIcon implements Icon {
  private final Color color;

  protected ColorIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(color);
    g2.fillRect(0, 0, getIconWidth(), getIconHeight());
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return MainPanel.CELL_SIZE.width - 2;
  }

  @Override public int getIconHeight() {
    return MainPanel.CELL_SIZE.height - 2;
  }
}

class IndexedColorListRenderer implements ListCellRenderer<IndexedColor> {
  private final ListCellRenderer<? super IndexedColor> renderer = new DefaultListCellRenderer();

  @Override public Component getListCellRendererComponent(JList<? extends IndexedColor> list, IndexedColor value, int index, boolean isSelected, boolean cellHasFocus) {
    Component c = renderer.getListCellRendererComponent(
        list, value, index, isSelected, cellHasFocus);
    if (c instanceof JLabel) {
      JLabel l = (JLabel) c;
      l.setIcon(new ColorIcon(value.getColor()));
      l.setToolTipText("index: " + value.getIndex());
      Color borderColor = value.isTransparent() ? Color.RED : Color.WHITE;
      l.setBorder(BorderFactory.createLineBorder(borderColor));
    }
    return c;
  }
}
