// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import javax.swing.*;

/**
 * JLayeredPane1.
 *
 * @author Taka
 */
public final class MainPanel extends JPanel {
  private static final int BACK_LAYER = 1;
  private static final Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
  private static final int[] COLORS = {
    0xDD_DD_DD, 0xAA_AA_FF, 0xFF_AA_AA, 0xAA_FF_AA, 0xFF_FF_AA, 0xFF_AA_FF, 0xAA_FF_FF
  };

  private MainPanel() {
    super(new BorderLayout());
    Image image = new ImageIcon(getClass().getResource("tokeidai.jpg")).getImage();
    JLayeredPane layer = new BackImageLayeredPane(image);
    for (int i = 0; i < 7; i++) {
      JPanel p = createPanel(layer, i);
      p.setLocation(i * 20 + 10, i * 20 + 5);
      layer.add(p, BACK_LAYER);
    }
    add(layer);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JPanel createPanel(JLayeredPane layerPane, int i) {
    String s = "<html><font color=#333333>Header:" + i + "</font></html>";

    JLabel label = new JLabel(s);
    label.setFont(FONT);
    label.setOpaque(true);
    label.setHorizontalAlignment(SwingConstants.CENTER);
    label.setBackground(getColor(COLORS[i], .85f));
    label.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

    JTextArea text = new JTextArea();
    text.setMargin(new Insets(4, 4, 4, 4));
    text.setLineWrap(true);
    text.setOpaque(false);

    JPanel p = new JPanel(new BorderLayout());
    p.setOpaque(true);
    p.setBackground(new Color(COLORS[i]));
    p.setBorder(BorderFactory.createLineBorder(getColor(COLORS[i], .5f), 1));

    // for moving the Window
    DragMouseListener li = new DragMouseListener(layerPane);
    p.addMouseListener(li);
    p.addMouseMotionListener(li);

    p.add(label, BorderLayout.NORTH);
    p.add(text);
    p.setSize(new Dimension(120, 100));
    return p;
  }

  private static Color getColor(int i, float f) {
    int r = (int) ((i >> 16 & 0xFF) * f);
    int g = (int) ((i >> 8 & 0xFF) * f);
    int b = (int) ((i & 0xFF) * f);
    return new Color(r, g, b);
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

// A mouse click on the title brings the panel to the top. It also moves by dragging.
class DragMouseListener extends MouseAdapter {
  private final JLayeredPane parent;
  private final Point origin = new Point();

  protected DragMouseListener(JLayeredPane parent) {
    super();
    this.parent = parent;
  }

  @Override public void mousePressed(MouseEvent e) {
    JComponent panel = (JComponent) e.getComponent();
    origin.setLocation(e.getPoint());
    parent.moveToFront(panel);
  }

  @Override public void mouseDragged(MouseEvent e) {
    JComponent panel = (JComponent) e.getComponent();
    int dx = e.getX() - origin.x;
    int dy = e.getY() - origin.y;
    Point pt = panel.getLocation();
    panel.setLocation(pt.x + dx, pt.y + dy);
  }
}

// Draw a background image JLayeredPane
class BackImageLayeredPane extends JLayeredPane {
  private final transient Image bgImage;

  protected BackImageLayeredPane(Image img) {
    super();
    this.bgImage = img;
  }

  @Override public boolean isOptimizedDrawingEnabled() {
    return false;
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (Objects.nonNull(bgImage)) {
      int iw = bgImage.getWidth(this);
      int ih = bgImage.getHeight(this);
      Dimension d = getSize();
      for (int h = 0; h < d.getHeight(); h += ih) {
        for (int w = 0; w < d.getWidth(); w += iw) {
          g.drawImage(bgImage, w, h, this);
        }
      }
    }
  }
}
