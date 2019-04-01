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
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  public static final Dimension CELLSZ = new Dimension(8, 8);

  private MainPanel() {
    super(new BorderLayout());

    JPanel p = new JPanel();
    JLabel label1 = new JLabel();
    p.add(label1);
    JLabel label2 = new JLabel();
    p.add(label2);

    BufferedImage image;
    try {
      image = ImageIO.read(getClass().getResource("duke.gif"));
    } catch (IOException ex) {
      ex.printStackTrace();
      image = makeMissingImage();
    }
    label1.setIcon(new ImageIcon(image));

    ColorModel colorModel = image.getColorModel();
    // System.out.println(colorModel);
    IndexColorModel indexColorModel = null;
    int transIndex;
    if (colorModel instanceof IndexColorModel) {
      indexColorModel = (IndexColorModel) colorModel;
      transIndex = indexColorModel.getTransparentPixel();
    } else {
      transIndex = -1;
    }

    int w = image.getWidth();
    int h = image.getHeight();
    DataBuffer dataBuffer = image.getRaster().getDataBuffer();
    label2.setIcon(new ImageIcon(makeTestImage(dataBuffer, colorModel, w, h, transIndex)));

    JPanel box = new JPanel(new GridBagLayout());
    if (Objects.nonNull(indexColorModel)) {
      JList<IndexedColor> palette = new JList<IndexedColor>(new PaletteListModel(indexColorModel)) {
        @Override public void updateUI() {
          setCellRenderer(null);
          super.updateUI();
          setLayoutOrientation(JList.HORIZONTAL_WRAP);
          setVisibleRowCount(8);
          setFixedCellWidth(CELLSZ.width);
          setFixedCellHeight(CELLSZ.height);
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

  private static Image makeTestImage(DataBuffer dataBuffer, ColorModel colorModel, int w, int h, int transIdx) {
    // DataBufferByte dataBufferByte = null;
    // if (dataBuffer instanceof DataBufferByte) {
    //   dataBufferByte = (DataBufferByte) dataBuffer;
    // } else {
    //   System.out.println("No DataBufferByte");
    // }
    // byte data[] = dataBufferByte.getData();
    BufferedImage buf = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        int arrayIndex = x + y * w;
        // int colorIndex = Byte.toUnsignedInt(data[arrayIndex]);
        int colorIndex = dataBuffer.getElem(arrayIndex);
        if (transIdx == colorIndex) {
          buf.setRGB(x, y, Color.RED.getRGB()); // 0xFF_FF_00_00);
        } else {
          buf.setRGB(x, y, colorModel.getRGB(colorIndex));
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

class IndexedColor {
  public final int index;
  public final Color color;
  public final boolean isTransparentPixel;

  protected IndexedColor(int index, Color color, boolean isTransparentPixel) {
    this.index = index;
    this.color = color;
    this.isTransparentPixel = isTransparentPixel;
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

  @Override public IndexedColor getElementAt(int index) {
    return new IndexedColor(index, new Color(model.getRGB(index)), index == model.getTransparentPixel());
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
    return MainPanel.CELLSZ.width - 2;
  }

  @Override public int getIconHeight() {
    return MainPanel.CELLSZ.height - 2;
  }
}

class IndexedColorListRenderer implements ListCellRenderer<IndexedColor> {
  private final ListCellRenderer<? super IndexedColor> renderer = new DefaultListCellRenderer();

  @Override public Component getListCellRendererComponent(JList<? extends IndexedColor> list, IndexedColor value, int index, boolean isSelected, boolean cellHasFocus) {
    JLabel l = (JLabel) renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    l.setIcon(new ColorIcon(value.color));
    l.setToolTipText("index: " + value.index);
    l.setBorder(BorderFactory.createLineBorder(value.isTransparentPixel ? Color.RED : Color.WHITE));
    return l;
  }
}
