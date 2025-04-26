// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JList<ListItem> list = new RubberBandSelectionList<>(makeModel());
    list.setOpaque(false);
    list.setBackground(new Color(0x0, true));
    list.setForeground(Color.WHITE);
    // list.addListSelectionListener(e -> SwingUtilities.getUnwrappedParent(list).repaint());

    JScrollPane scroll = new JScrollPane(list);
    scroll.setBackground(new Color(0x0, true));
    scroll.setOpaque(false);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.setViewportBorder(BorderFactory.createEmptyBorder());
    // scroll.getViewport().addChangeListener(e -> ((Component) e.getSource()).repaint());
    scroll.getViewport().setOpaque(false);

    JPanel panel = new JPanel(new BorderLayout()) {
      private transient Paint texture;
      @Override public void updateUI() {
        super.updateUI();
        texture = TextureUtils.createCheckerTexture(6);
        setOpaque(false);
      }

      @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(texture);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
      }
    };
    panel.add(scroll);
    add(panel);
    setPreferredSize(new Dimension(320, 240));
  }

  private static ListModel<ListItem> makeModel() {
    DefaultListModel<ListItem> model = new DefaultListModel<>();
    model.addElement(new ListItem("red", new ColorIcon(Color.RED)));
    model.addElement(new ListItem("green", new ColorIcon(Color.GREEN)));
    model.addElement(new ListItem("blue", new ColorIcon(Color.BLUE)));
    model.addElement(new ListItem("cyan", new ColorIcon(Color.CYAN)));
    model.addElement(new ListItem("darkGray", new ColorIcon(Color.DARK_GRAY)));
    model.addElement(new ListItem("gray", new ColorIcon(Color.GRAY)));
    model.addElement(new ListItem("lightGray", new ColorIcon(Color.LIGHT_GRAY)));
    model.addElement(new ListItem("magenta", new ColorIcon(Color.MAGENTA)));
    model.addElement(new ListItem("orange", new ColorIcon(Color.ORANGE)));
    model.addElement(new ListItem("pink", new ColorIcon(Color.PINK)));
    model.addElement(new ListItem("yellow", new ColorIcon(Color.YELLOW)));
    model.addElement(new ListItem("black", new ColorIcon(Color.BLACK)));
    model.addElement(new ListItem("white", new ColorIcon(Color.WHITE)));
    return model;
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
    setFixedCellWidth(62);
    setFixedCellHeight(62);
    setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

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
      int index = l.locationToIndex(e.getPoint());
      if (l.getCellBounds(index, index).contains(e.getPoint())) {
        l.setFocusable(true);
      } else {
        l.clearSelection();
        l.getSelectionModel().setAnchorSelectionIndex(-1);
        l.getSelectionModel().setLeadSelectionIndex(-1);
        l.setFocusable(false);
      }
      srcPoint.setLocation(e.getPoint());
      l.repaint();
    }
  }
}

class ListItemListCellRenderer<E extends ListItem> implements ListCellRenderer<E> {
  protected static final Color SELECTED_COLOR = new Color(0x40_32_64_FF, true);
  private final JLabel label = new JLabel("", null, SwingConstants.CENTER) {
    @Override protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (SELECTED_COLOR.equals(getBackground())) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(SELECTED_COLOR);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
      }
    }
  };
  private final JPanel renderer = new JPanel(new BorderLayout());
  private final Border focusBorder = UIManager.getBorder("List.focusCellHighlightBorder");
  private final Border noFocusBorder; // = UIManager.getBorder("List.noFocusBorder");

  protected ListItemListCellRenderer() {
    Border b = UIManager.getBorder("List.noFocusBorder");
    if (Objects.isNull(b)) { // Nimbus???
      Insets i = focusBorder.getBorderInsets(renderer);
      b = BorderFactory.createEmptyBorder(i.top, i.left, i.bottom, i.right);
    }
    noFocusBorder = b;
    label.setVerticalTextPosition(SwingConstants.BOTTOM);
    label.setHorizontalTextPosition(SwingConstants.CENTER);
    label.setForeground(renderer.getForeground());
    label.setBackground(renderer.getBackground());
    label.setBorder(noFocusBorder);
    label.setOpaque(false);
    renderer.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    renderer.add(label);
    renderer.setOpaque(false);
  }

  @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
    label.setText(value.getTitle());
    label.setBorder(cellHasFocus ? focusBorder : noFocusBorder);
    label.setIcon(value.getIcon());
    if (isSelected) {
      label.setForeground(list.getSelectionForeground());
      label.setBackground(SELECTED_COLOR);
    } else {
      label.setForeground(list.getForeground());
      label.setBackground(list.getBackground());
    }
    return renderer;
  }
}

class ListItem {
  private final String title;
  private final Icon icon;

  protected ListItem(String title, Icon icon) {
    this.title = title;
    this.icon = icon;
  }

  public String getTitle() {
    return title;
  }

  public Icon getIcon() {
    return icon;
  }
}

final class TextureUtils {
  private static final Color DEFAULT_COLOR = new Color(0xEE_32_32_32, true);

  private TextureUtils() {
    /* HideUtilityClassConstructor */
  }

  public static TexturePaint createCheckerTexture(int cs, Color color) {
    int size = cs * cs;
    BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = img.createGraphics();
    g2.setPaint(color);
    g2.fillRect(0, 0, size, size);
    for (int i = 0; i * cs < size; i++) {
      for (int j = 0; j * cs < size; j++) {
        if ((i + j) % 2 == 0) {
          g2.fillRect(i * cs, j * cs, cs, cs);
        }
      }
    }
    g2.dispose();
    return new TexturePaint(img, new Rectangle(size, size));
  }

  public static TexturePaint createCheckerTexture(int cs) {
    return createCheckerTexture(cs, DEFAULT_COLOR);
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
    return 32;
  }

  @Override public int getIconHeight() {
    return 32;
  }
}
