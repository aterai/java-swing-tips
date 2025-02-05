// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsComboBoxUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.metal.MetalComboBoxUI;

public final class MainPanel extends JPanel {
  public static final Color BACKGROUND = Color.BLACK; // RED;
  public static final Color FOREGROUND = Color.WHITE; // YELLOW;
  public static final Color SELECTION_FGC = Color.CYAN;

  private MainPanel() {
    super(new BorderLayout());
    Container box0 = makeBox0();
    Container box1 = makeBox1();

    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab("Basic, Metal", SwingUtils.makeTitledPanel(null, box1, BACKGROUND));
    tabs.addTab("Windows", SwingUtils.makeTitledPanel(null, box0, null));

    JCheckBox check = new JCheckBox("editable");
    check.addActionListener(e -> {
      boolean flag = ((JCheckBox) e.getSource()).isSelected();
      Stream.of(box1, box0)
          .flatMap(SwingUtils::descendants)
          .filter(JComboBox.class::isInstance)
          .map(JComboBox.class::cast)
          .forEach(c -> c.setEditable(flag));
      repaint();
    });

    add(tabs);
    add(check, BorderLayout.SOUTH);
    // setOpaque(true);
    // setBackground(BACKGROUND);
    setPreferredSize(new Dimension(320, 240));
  }

  private static String[] makeModel() {
    return new String[] {
        "1234",
        "5555555555555555555555",
        "6789000000000"
    };
  }

  private static Container makeBox0() {
    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    JComboBox<String> combo0 = new JComboBox<>(makeModel());
    combo0.setBorder(new RoundedCornerBorder());
    box.add(SwingUtils.makeTitledPanel("RoundRectangle2D:", combo0, null));
    box.add(Box.createVerticalStrut(5));

    JComboBox<String> combo1 = new JComboBox<>(makeModel());
    combo1.setBorder(new KamabokoBorder());
    box.add(SwingUtils.makeTitledPanel("Path2D:", combo1, null));
    box.add(Box.createVerticalStrut(5));

    JComboBox<String> combo2 = new JComboBox<String>(makeModel()) {
      @Override public void updateUI() {
        super.updateUI();
        if (getUI() instanceof WindowsComboBoxUI) {
          setUI(new WindowsComboBoxUI() {
            @Override protected JButton createArrowButton() {
              JButton b = new JButton(new ArrowIcon(Color.BLACK, Color.BLUE));
              b.setContentAreaFilled(false);
              b.setFocusPainted(false);
              b.setBorder(BorderFactory.createEmptyBorder());
              return b;
            }
          });
        }
        setBorder(new KamabokoBorder());
      }
    };
    String title = "WindowsComboBoxUI#createArrowButton():";
    box.add(SwingUtils.makeTitledPanel(title, combo2, null));
    return box;
  }

  private static Container makeBox1() {
    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    // UIManager.put("TitledBorder.titleColor", FOREGROUND);
    // UIManager.put("TitledBorder.border", BorderFactory.createEmptyBorder());

    UIManager.put("ComboBox.foreground", FOREGROUND);
    UIManager.put("ComboBox.background", BACKGROUND);
    UIManager.put("ComboBox.selectionForeground", SELECTION_FGC);
    UIManager.put("ComboBox.selectionBackground", BACKGROUND);

    UIManager.put("ComboBox.buttonDarkShadow", BACKGROUND);
    UIManager.put("ComboBox.buttonBackground", FOREGROUND);
    UIManager.put("ComboBox.buttonHighlight", FOREGROUND);
    UIManager.put("ComboBox.buttonShadow", FOREGROUND);

    // UIManager.put("ComboBox.border", BorderFactory.createLineBorder(Color.WHITE));
    // UIManager.put("ComboBox.editorBorder", BorderFactory.createLineBorder(Color.GREEN));
    UIManager.put("ComboBox.border", new KamabokoBorder());

    JComboBox<String> combo0 = new JComboBox<String>(makeModel()) {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new MetalComboBoxUI());
        Object o = getAccessibleContext().getAccessibleChild(0);
        ((JComponent) o).setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, FOREGROUND));
      }
    };
    box.add(SwingUtils.makeTitledPanel("MetalComboBoxUI:", combo0, BACKGROUND));
    box.add(Box.createVerticalStrut(10));

    JComboBox<String> combo1 = new JComboBox<String>(makeModel()) {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new BasicComboBoxUI());
        Object o = getAccessibleContext().getAccessibleChild(0);
        ((JComponent) o).setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, FOREGROUND));
      }
    };
    box.add(SwingUtils.makeTitledPanel("BasicComboBoxUI:", combo1, BACKGROUND));
    box.add(Box.createVerticalStrut(10));

    UIManager.put("ComboBox.border", new KamabokoBorder());
    JComboBox<String> combo2 = new JComboBox<String>(makeModel()) {
      private transient MouseAdapter handler;

      @Override public void updateUI() {
        removeMouseListener(handler);
        super.updateUI();
        setUI(new BasicComboBoxUI() {
          @Override protected JButton createArrowButton() {
            JButton b = new JButton(new ArrowIcon(BACKGROUND, FOREGROUND));
            b.setContentAreaFilled(false);
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createEmptyBorder());
            return b;
          }
        });
        Object o = getAccessibleContext().getAccessibleChild(0);
        ((JComponent) o).setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, FOREGROUND));
        handler = new ComboRolloverHandler();
        addMouseListener(handler);
      }
    };
    String title = "BasicComboBoxUI#createArrowButton():";
    box.add(SwingUtils.makeTitledPanel(title, combo2, BACKGROUND));
    return box;
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

class ComboRolloverHandler extends MouseAdapter {
  private static ButtonModel getButtonModel(MouseEvent e) {
    Container c = (Container) e.getComponent();
    JButton b = (JButton) c.getComponent(0);
    return b.getModel();
  }

  @Override public void mouseEntered(MouseEvent e) {
    getButtonModel(e).setRollover(true);
  }

  @Override public void mouseExited(MouseEvent e) {
    getButtonModel(e).setRollover(false);
  }

  @Override public void mousePressed(MouseEvent e) {
    getButtonModel(e).setPressed(true);
  }

  @Override public void mouseReleased(MouseEvent e) {
    getButtonModel(e).setPressed(false);
  }
}

class ArrowIcon implements Icon {
  private final Color color;
  private final Color rollover;

  protected ArrowIcon(Color color, Color rollover) {
    this.color = color;
    this.rollover = rollover;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(color);
    int shift = 0;
    if (c instanceof AbstractButton) {
      ButtonModel m = ((AbstractButton) c).getModel();
      if (m.isPressed()) {
        shift = 1;
      } else {
        if (m.isRollover()) {
          g2.setPaint(rollover);
        }
      }
    }
    g2.translate(x, y + shift);
    g2.drawLine(2, 3, 6, 3);
    g2.drawLine(3, 4, 5, 4);
    g2.drawLine(4, 5, 4, 5);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 9;
  }

  @Override public int getIconHeight() {
    return 9;
  }
}

class RoundedCornerBorder extends AbstractBorder {
  @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    int r = 12;
    int w = width - 1;
    int h = height - 1;

    Area round = new Area(new RoundRectangle2D.Double(x, y, w, h, r, r));

    Container parent = c.getParent();
    if (Objects.nonNull(parent)) {
      g2.setPaint(parent.getBackground());
      Area corner = new Area(new Rectangle2D.Double(x, y, width, height));
      corner.subtract(round);
      g2.fill(corner);
    }
    g2.setPaint(c.getForeground());
    g2.draw(round);
    g2.dispose();
  }

  @Override public Insets getBorderInsets(Component c) {
    return new Insets(4, 8, 4, 8);
  }

  @Override public Insets getBorderInsets(Component c, Insets insets) {
    insets.set(4, 8, 4, 8);
    return insets;
  }
}

class KamabokoBorder extends RoundedCornerBorder {
  // private static TexturePaint makeCheckerTexture() {
  //   int cs = 6;
  //   int sz = cs * cs;
  //   BufferedImage bi = new BufferedImage(sz, sz, BufferedImage.TYPE_INT_ARGB);
  //   Graphics2D g2 = bi.createGraphics();
  //   g2.setPaint(new Color(0x32_C8_96_64, true));
  //   g2.fillRect(0, 0, sz, sz);
  //   for (int i = 0; i * cs < sz; i++) {
  //     for (int j = 0; j * cs < sz; j++) {
  //       if ((i + j) % 2 == 0) {
  //         g2.fillRect(i * cs, j * cs, cs, cs);
  //       }
  //     }
  //   }
  //   g2.dispose();
  //   return new TexturePaint(bi, new Rectangle(sz, sz));
  // }
  // private static TexturePaint tp = makeCheckerTexture();
  @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    double r = 12d / 2d;
    double rr = r * 4d * (Math.sqrt(2d) - 1d) / 3d; // = r * .5522;
    double w = width - 1d;
    double h = height - 1d;

    Path2D p = new Path2D.Double();
    p.moveTo(x, y + h);
    p.lineTo(x, y + r);
    p.curveTo(x, y + r - rr, x + r - rr, y, x + r, y);
    p.lineTo(x + w - r, y);
    p.curveTo(x + w - r + rr, y, x + w, y + r - rr, x + w, y + r);
    p.lineTo(x + w, y + h);
    p.closePath();
    Area round = new Area(p);

    // Area round = new Area(new RoundRectangle2D.Double(x, y, w, h, r, r));
    // Rectangle b = round.getBounds();
    // b.setBounds(b.x, b.y + r, b.width, b.height - r);
    // round.add(new Area(b));

    Container parent = c.getParent();
    if (Objects.nonNull(parent)) {
      g2.setPaint(parent.getBackground());
      Area corner = new Area(new Rectangle2D.Double(x, y, width, height));
      corner.subtract(round);
      g2.fill(corner);
    }
    // g2.setPaint(tp);
    // g2.fill(round);
    g2.setPaint(c.getForeground());
    g2.draw(round);
    g2.dispose();
  }
}

final class SwingUtils {
  private SwingUtils() {
    /* Singleton */
  }

  public static Stream<Component> descendants(Container parent) {
    return Stream.of(parent.getComponents())
        .filter(Container.class::isInstance).map(Container.class::cast)
        .flatMap(c -> Stream.concat(Stream.of(c), descendants(c)));
  }

  public static Component makeTitledPanel(String title, Container cmp, Color bgc) {
    JPanel p = new JPanel(new BorderLayout());
    if (cmp.getLayout() instanceof BoxLayout) {
      p.add(cmp, BorderLayout.NORTH);
    } else {
      p.add(cmp);
    }
    if (Objects.nonNull(title)) {
      TitledBorder b = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), title);
      if (Objects.nonNull(bgc)) {
        b.setTitleColor(new Color(~bgc.getRGB()));
      }
      p.setBorder(b);
    }
    if (Objects.nonNull(bgc)) {
      p.setOpaque(true);
      p.setBackground(bgc);
    }
    return p;
  }
}
