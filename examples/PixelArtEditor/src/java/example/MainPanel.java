// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    PixelCanvas canvas = new PixelCanvas(128, 128);

    // Center the canvas in the viewport when it is smaller than the viewport
    JPanel wrapper = new JPanel(new GridBagLayout());
    wrapper.add(canvas);
    JScrollPane scroll = new JScrollPane(wrapper);
    scroll.getVerticalScrollBar().setUnitIncrement(16);
    scroll.getHorizontalScrollBar().setUnitIncrement(16);

    add(createToolBox(canvas), BorderLayout.NORTH);
    add(scroll);
    setPreferredSize(new Dimension(320, 240));
  }

  private JPanel createToolBox(PixelCanvas canvas) {
    SpinnerNumberModel zoomModel = new SpinnerNumberModel(
        canvas.getZoom(), PixelCanvas.MIN_ZOOM, PixelCanvas.MAX_ZOOM, 1);
    zoomModel.addChangeListener(e -> canvas.setZoom(zoomModel.getNumber().intValue(), null));
    canvas.addPropertyChangeListener(PixelCanvas.ZOOM, e -> zoomModel.setValue(e.getNewValue()));

    SpinnerNumberModel penModel = new SpinnerNumberModel(canvas.getPenSize(), 1, 16, 1);
    penModel.addChangeListener(e -> canvas.setPenSize(penModel.getNumber().intValue()));

    JCheckBox grid = new JCheckBox("Grid", canvas.isGridVisible());
    grid.addActionListener(e -> canvas.setGridVisible(grid.isSelected()));

    ColorIcon icon = new ColorIcon(canvas.getPenColor());
    JButton color = new JButton("Color", icon);
    color.addActionListener(e -> {
      Color c = JColorChooser.showDialog(getRootPane(), "Pen color", icon.getColor());
      if (c != null) {
        icon.setColor(c);
        canvas.setPenColor(c);
        color.repaint();
      }
    });

    JButton clear = new JButton("Clear");
    clear.addActionListener(e -> canvas.clear());

    JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 2));
    p1.add(new JLabel("Zoom:"));
    p1.add(new JSpinner(zoomModel));
    p1.add(new JLabel("Pen size:"));
    p1.add(new JSpinner(penModel));
    addFileButtons(p1, canvas);

    JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 2));
    p2.add(grid);
    p2.add(color);
    p2.add(clear);

    JPanel box = new JPanel(new GridLayout(2, 1));
    box.add(p1);
    box.add(p2);
    return box;
  }

  // The Save and Load buttons share a JFileChooser to remember the last directory
  private void addFileButtons(JPanel p, PixelCanvas canvas) {
    JFileChooser chooser = new JFileChooser();
    chooser.setFileFilter(new FileNameExtensionFilter("PNG image (*.png)", "png"));
    JButton save = new JButton("Save");
    save.addActionListener(e -> saveImage(chooser, canvas));
    JButton load = new JButton("Load");
    load.addActionListener(e -> loadImage(chooser, canvas));
    p.add(save);
    p.add(load);
  }

  private void saveImage(JFileChooser chooser, PixelCanvas canvas) {
    if (chooser.showSaveDialog(getRootPane()) == JFileChooser.APPROVE_OPTION) {
      File file = withPngExtension(chooser.getSelectedFile());
      if (!file.exists() || confirmReplace(file)) {
        try {
          ImageIO.write(canvas.toBufferedImage(), "png", file);
        } catch (IOException ex) {
          showErrorMessage(ex.getMessage());
        }
      }
    }
  }

  private static File withPngExtension(File file) {
    String name = file.getName();
    boolean isPng = name.toLowerCase(Locale.ENGLISH).endsWith(".png");
    return isPng ? file : new File(file.getParentFile(), name + ".png");
  }

  private boolean confirmReplace(File file) {
    String msg = String.format("%s already exists.%nDo you want to replace it?", file.getName());
    int answer = JOptionPane.showConfirmDialog(
        getRootPane(), msg, "Save", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    return answer == JOptionPane.YES_OPTION;
  }

  private void loadImage(JFileChooser chooser, PixelCanvas canvas) {
    if (chooser.showOpenDialog(getRootPane()) == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      try {
        // ImageIO#read(...) returns null if no registered ImageReader can read the file
        BufferedImage img = ImageIO.read(file);
        if (img == null) {
          showErrorMessage("Unsupported image format: " + file.getName());
        } else {
          canvas.setImage(img);
        }
      } catch (IOException ex) {
        showErrorMessage(ex.getMessage());
      }
    }
  }

  private void showErrorMessage(String msg) {
    UIManager.getLookAndFeel().provideErrorFeedback(this);
    JOptionPane.showMessageDialog(getRootPane(), msg, "Error", JOptionPane.ERROR_MESSAGE);
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

@SuppressWarnings("PMD.TooManyMethods")
class PixelCanvas extends JPanel {
  public static final String ZOOM = "zoom";
  public static final int MIN_ZOOM = 1;
  public static final int MAX_ZOOM = 32;
  private static final int GRID_MIN_ZOOM = 4;
  private static final Color GRID_COLOR = new Color(0xA0_64_95_ED, true);
  private static final Paint TEXTURE = createCheckerTexture(8, Color.WHITE, new Color(0xE8_E8_E8));
  private final int imageWidth;
  private final int imageHeight;
  private final Rectangle imageBounds;
  private final int[] pixels;
  private final transient MemoryImageSource source;
  private final transient Image image;
  // The image pixel under the mouse cursor, or null if the cursor is outside
  private Point cursorPixel;
  private int zoom = 2;
  private int penSize = 1;
  private int penColor = 0xFF_00_00_00;
  private boolean gridVisible = true;
  private transient MouseAdapter handler;

  protected PixelCanvas(int width, int height) {
    super();
    imageWidth = width;
    imageHeight = height;
    imageBounds = new Rectangle(width, height);
    pixels = new int[width * height];
    source = new MemoryImageSource(width, height, pixels, 0, width);
    // Reuse a single Image and send only the changed area with newPixels(...)
    source.setAnimated(true);
    image = Toolkit.getDefaultToolkit().createImage(source);
  }

  @Override public void updateUI() {
    removeMouseListener(handler);
    removeMouseMotionListener(handler);
    removeMouseWheelListener(handler);
    super.updateUI();
    handler = new CanvasHandler();
    addMouseListener(handler);
    addMouseMotionListener(handler);
    addMouseWheelListener(handler);
  }

  @Override public Dimension getPreferredSize() {
    return new Dimension(imageWidth * zoom, imageHeight * zoom);
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    int w = imageWidth * zoom;
    int h = imageHeight * zoom;
    // The opaque checkerboard background shows the transparent pixels
    g2.setPaint(TEXTURE);
    g2.fillRect(0, 0, w, h);
    // Enlarge each image pixel into a zoom x zoom square without blurring
    g2.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    g2.drawImage(image, 0, 0, w, h, this);
    if (gridVisible && zoom >= GRID_MIN_ZOOM) {
      paintGrid(g2, g2.getClipBounds());
    }
    if (cursorPixel != null) {
      Rectangle r = toViewRect(getPenRect(cursorPixel.x, cursorPixel.y));
      g2.setPaint(Color.BLACK);
      g2.drawRect(r.x, r.y, r.width - 1, r.height - 1);
      g2.setPaint(Color.WHITE);
      g2.drawRect(r.x + 1, r.y + 1, r.width - 3, r.height - 3);
    }
    g2.dispose();
  }

  private void paintGrid(Graphics2D g2, Rectangle clip) {
    Rectangle r = clip == null ? new Rectangle(getPreferredSize()) : clip;
    int w = imageWidth * zoom;
    int h = imageHeight * zoom;
    int x0 = Math.max(0, r.x / zoom);
    int x1 = Math.min(imageWidth, (r.x + r.width) / zoom + 1);
    int y0 = Math.max(0, r.y / zoom);
    int y1 = Math.min(imageHeight, (r.y + r.height) / zoom + 1);
    g2.setPaint(GRID_COLOR);
    for (int x = x0; x <= x1; x++) {
      g2.drawLine(x * zoom, 0, x * zoom, h);
    }
    for (int y = y0; y <= y1; y++) {
      g2.drawLine(0, y * zoom, w, y * zoom);
    }
  }

  public int getZoom() {
    return zoom;
  }

  // anchor: the point in this component's coordinates that should stay
  // under the mouse cursor, or null to keep the center of the viewport
  public void setZoom(int newZoom, Point anchor) {
    int oldZoom = zoom;
    int z = Math.max(MIN_ZOOM, Math.min(newZoom, MAX_ZOOM));
    if (z != oldZoom) {
      zoom = z;
      firePropertyChange(ZOOM, oldZoom, z);
      Container viewport = SwingUtilities.getAncestorOfClass(JViewport.class, this);
      if (viewport instanceof JViewport) {
        keepAnchor((JViewport) viewport, anchor, (double) z / oldZoom);
      } else {
        revalidate();
      }
      repaint();
    }
  }

  // Scrolls the viewport so that the anchor point stays at the same location
  // in the viewport after the zoom factor has been multiplied by scale
  private void keepAnchor(JViewport viewport, Point anchor, double scale) {
    Point pt = anchor;
    if (pt == null) {
      // The layout has not been updated yet, so this is the old visible area
      Rectangle vr = getVisibleRect();
      pt = new Point((int) vr.getCenterX(), (int) vr.getCenterY());
    }
    Point vpt = SwingUtilities.convertPoint(this, pt, viewport);
    // Layout immediately so that the new view size is available
    invalidate();
    viewport.getParent().validate();
    int ax = (int) Math.round(pt.x * scale);
    int ay = (int) Math.round(pt.y * scale);
    Point p = SwingUtilities.convertPoint(this, ax, ay, viewport.getView());
    Dimension extent = viewport.getExtentSize();
    Dimension view = viewport.getViewSize();
    int x = Math.max(0, Math.min(p.x - vpt.x, view.width - extent.width));
    int y = Math.max(0, Math.min(p.y - vpt.y, view.height - extent.height));
    viewport.setViewPosition(new Point(x, y));
  }

  public int getPenSize() {
    return penSize;
  }

  public void setPenSize(int size) {
    Point pt = cursorPixel;
    setCursorPixel(null);
    penSize = Math.max(1, size);
    setCursorPixel(pt);
  }

  public Color getPenColor() {
    return new Color(penColor, true);
  }

  public void setPenColor(Color color) {
    penColor = color.getRGB();
  }

  public boolean isGridVisible() {
    return gridVisible;
  }

  public void setGridVisible(boolean visible) {
    gridVisible = visible;
    repaint();
  }

  public void clear() {
    Arrays.fill(pixels, 0x0);
    source.newPixels();
    repaint();
  }

  // Copies the pixels into a new ARGB image, keeping the transparent pixels
  public BufferedImage toBufferedImage() {
    BufferedImage img = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
    img.setRGB(0, 0, imageWidth, imageHeight, pixels, 0, imageWidth);
    return img;
  }

  // Copies the pixels of the image into the canvas, whose size is fixed.
  // BufferedImage#getRGB(...) converts any image type (indexed, gray, RGB)
  // to ARGB, and the area outside the canvas is clipped
  public void setImage(BufferedImage img) {
    Arrays.fill(pixels, 0x0);
    int w = Math.min(imageWidth, img.getWidth());
    int h = Math.min(imageHeight, img.getHeight());
    img.getRGB(0, 0, w, h, pixels, 0, imageWidth);
    source.newPixels();
    repaint();
  }

  // Converts a point in this component's coordinates to an image pixel position
  private Point toImagePoint(Point pt) {
    return new Point(Math.floorDiv(pt.x, zoom), Math.floorDiv(pt.y, zoom));
  }

  private Rectangle toViewRect(Rectangle r) {
    return new Rectangle(r.x * zoom, r.y * zoom, r.width * zoom, r.height * zoom);
  }

  // The pen tip always covers penSize x penSize image pixels,
  // regardless of the zoom factor
  private Rectangle getPenRect(int px, int py) {
    int offset = (penSize - 1) / 2;
    return new Rectangle(px - offset, py - offset, penSize, penSize);
  }

  private void setCursorPixel(Point pt) {
    if (cursorPixel != null) {
      repaint(toViewRect(getPenRect(cursorPixel.x, cursorPixel.y)));
    }
    cursorPixel = pt;
    if (pt != null) {
      repaint(toViewRect(getPenRect(pt.x, pt.y)));
    }
  }

  // Draws a line between two image pixels with the Bresenham algorithm
  private void drawLine(Point p0, Point p1, int color) {
    int dx = Math.abs(p1.x - p0.x);
    int dy = -Math.abs(p1.y - p0.y);
    int sx = p0.x < p1.x ? 1 : -1;
    int sy = p0.y < p1.y ? 1 : -1;
    int err = dx + dy;
    int x = p0.x;
    int y = p0.y;
    stamp(x, y, color);
    while (x != p1.x || y != p1.y) {
      int e2 = 2 * err;
      if (e2 >= dy) {
        err += dy;
        x += sx;
      }
      if (e2 <= dx) {
        err += dx;
        y += sy;
      }
      stamp(x, y, color);
    }
    // Every point of the line lies within the bounding box of both end points,
    // so the changed area is the union of the pen tips at both ends
    Rectangle dirty = getPenRect(p0.x, p0.y).union(getPenRect(p1.x, p1.y))
        .intersection(imageBounds);
    if (!dirty.isEmpty()) {
      source.newPixels(dirty.x, dirty.y, dirty.width, dirty.height);
      repaint(toViewRect(dirty));
    }
  }

  private void stamp(int px, int py, int color) {
    Rectangle r = getPenRect(px, py).intersection(imageBounds);
    // When the pen tip is outside the image, the intersection may have
    // a negative width, and Arrays#fill(...) would throw an exception
    if (r.isEmpty()) {
      return;
    }
    for (int y = r.y; y < r.y + r.height; y++) {
      Arrays.fill(pixels, y * imageWidth + r.x, y * imageWidth + r.x + r.width, color);
    }
  }

  public static TexturePaint createCheckerTexture(int cellSize, Color background, Color color) {
    int size = cellSize * 2;
    BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = img.createGraphics();
    g2.setPaint(background);
    g2.fillRect(0, 0, size, size);
    g2.setPaint(color);
    g2.fillRect(0, 0, cellSize, cellSize);
    g2.fillRect(cellSize, cellSize, cellSize, cellSize);
    g2.dispose();
    return new TexturePaint(img, new Rectangle(size, size));
  }

  private final class CanvasHandler extends MouseAdapter {
    private final Point prev = new Point();
    private int color;
    private boolean drawing;

    @Override public void mousePressed(MouseEvent e) {
      if (SwingUtilities.isLeftMouseButton(e)) {
        color = penColor;
      } else if (SwingUtilities.isRightMouseButton(e)) {
        color = 0x0; // eraser
      } else {
        return;
      }
      drawing = true;
      Point pt = toImagePoint(e.getPoint());
      prev.setLocation(pt);
      drawLine(pt, pt, color);
    }

    @Override public void mouseDragged(MouseEvent e) {
      Point pt = toImagePoint(e.getPoint());
      setCursorPixel(pt);
      if (drawing && !pt.equals(prev)) {
        drawLine(prev, pt, color);
        prev.setLocation(pt);
      }
    }

    @Override public void mouseReleased(MouseEvent e) {
      drawing = false;
    }

    @Override public void mouseMoved(MouseEvent e) {
      setCursorPixel(toImagePoint(e.getPoint()));
    }

    @Override public void mouseExited(MouseEvent e) {
      setCursorPixel(null);
    }

    @Override public void mouseWheelMoved(MouseWheelEvent e) {
      if (e.isControlDown()) {
        setCursorPixel(null);
        setZoom(zoom - e.getWheelRotation(), e.getPoint());
        // The mouse cursor now points to a different location in this component
        Point pt = e.getLocationOnScreen();
        SwingUtilities.convertPointFromScreen(pt, PixelCanvas.this);
        setCursorPixel(toImagePoint(pt));
      } else {
        // Forward to the JScrollPane to scroll normally
        Container parent = getParent();
        parent.dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, parent));
      }
    }
  }
}

class ColorIcon implements Icon {
  private Color color;

  protected ColorIcon(Color color) {
    this.color = color;
  }

  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(color);
    g2.fillRect(0, 0, getIconWidth(), getIconHeight());
    g2.setPaint(Color.GRAY);
    g2.drawRect(0, 0, getIconWidth() - 1, getIconHeight() - 1);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 12;
  }

  @Override public int getIconHeight() {
    return 12;
  }
}
