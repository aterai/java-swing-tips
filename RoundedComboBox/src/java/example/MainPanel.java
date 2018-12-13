package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import com.sun.java.swing.plaf.windows.WindowsComboBoxUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.metal.MetalComboBoxUI;

public final class MainPanel extends JPanel {
  public static final Color BACKGROUND = Color.BLACK; // RED;
  public static final Color FOREGROUND = Color.WHITE; // YELLOW;
  public static final Color SELECTIONFOREGROUND = Color.CYAN;

  private MainPanel() {
    super(new BorderLayout());

    JComboBox<String> combo0 = new JComboBox<>(makeModel());
    JComboBox<String> combo1 = new JComboBox<>(makeModel());
    JComboBox<String> combo2 = new JComboBox<>(makeModel());

    combo0.setBorder(new RoundedCornerBorder());
    combo1.setBorder(new KamabokoBorder());
    combo2.setBorder(new KamabokoBorder());
    if (combo2.getUI() instanceof WindowsComboBoxUI) {
      combo2.setUI(new WindowsComboBoxUI() {
        @Override protected JButton createArrowButton() {
          JButton b = new JButton(new ArrowIcon(Color.BLACK, Color.BLUE)); // .createArrowButton();
          b.setContentAreaFilled(false);
          b.setFocusPainted(false);
          b.setBorder(BorderFactory.createEmptyBorder());
          return b;
        }
      });
    }

    Box box0 = Box.createVerticalBox();
    box0.add(makeTitledPanel("RoundRectangle2D:", combo0, null));
    box0.add(Box.createVerticalStrut(5));
    box0.add(makeTitledPanel("Path2D:", combo1, null));
    box0.add(Box.createVerticalStrut(5));
    box0.add(makeTitledPanel("WindowsComboBoxUI#createArrowButton():", combo2, null));
    box0.add(Box.createVerticalStrut(5));
    box0.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // UIManager.put("TitledBorder.titleColor", FOREGROUND);
    // UIManager.put("TitledBorder.border", BorderFactory.createEmptyBorder());

    UIManager.put("ComboBox.foreground", FOREGROUND);
    UIManager.put("ComboBox.background", BACKGROUND);
    UIManager.put("ComboBox.selectionForeground", SELECTIONFOREGROUND);
    UIManager.put("ComboBox.selectionBackground", BACKGROUND);

    UIManager.put("ComboBox.buttonDarkShadow", BACKGROUND);
    UIManager.put("ComboBox.buttonBackground", FOREGROUND);
    UIManager.put("ComboBox.buttonHighlight", FOREGROUND);
    UIManager.put("ComboBox.buttonShadow", FOREGROUND);

    // UIManager.put("ComboBox.border", BorderFactory.createLineBorder(Color.WHITE));
    // UIManager.put("ComboBox.editorBorder", BorderFactory.createLineBorder(Color.GREEN));
    UIManager.put("ComboBox.border", new KamabokoBorder());

    JComboBox<String> combo00 = new JComboBox<>(makeModel());
    JComboBox<String> combo01 = new JComboBox<>(makeModel());

    UIManager.put("ComboBox.border", new KamabokoBorder());
    JComboBox<String> combo02 = new JComboBox<>(makeModel());

    combo00.setUI(new MetalComboBoxUI());
    combo01.setUI(new BasicComboBoxUI());
    combo02.setUI(new BasicComboBoxUI() {
      @Override protected JButton createArrowButton() {
        JButton b = new JButton(new ArrowIcon(BACKGROUND, FOREGROUND));
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder());
        return b;
      }
    });

    combo02.addMouseListener(new ComboRolloverHandler());

    Object o = combo00.getAccessibleContext().getAccessibleChild(0);
    ((JComponent) o).setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, FOREGROUND));
    o = combo01.getAccessibleContext().getAccessibleChild(0);
    ((JComponent) o).setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, FOREGROUND));
    o = combo02.getAccessibleContext().getAccessibleChild(0);
    ((JComponent) o).setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, FOREGROUND));

    Box box1 = Box.createVerticalBox();
    box1.add(makeTitledPanel("MetalComboBoxUI:", combo00, BACKGROUND));
    box1.add(Box.createVerticalStrut(10));
    box1.add(makeTitledPanel("BasicComboBoxUI:", combo01, BACKGROUND));
    box1.add(Box.createVerticalStrut(10));
    box1.add(makeTitledPanel("BasicComboBoxUI#createArrowButton():", combo02, BACKGROUND));
    box1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.add("Basic, Metal", makeTitledPanel(null, box1, BACKGROUND));
    tabbedPane.add("Windows", makeTitledPanel(null, box0, null));

    JCheckBox check = new JCheckBox("editable");
    check.addActionListener(e -> {
      boolean flag = ((JCheckBox) e.getSource()).isSelected();
      Stream.of(combo00, combo01, combo02, combo0, combo1, combo2).forEach(c -> c.setEditable(flag));
      repaint();
    });

    add(tabbedPane);
    add(check, BorderLayout.SOUTH);
    // setOpaque(true);
    // setBackground(BACKGROUND);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Container cmp, Color bgc) {
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

  private static DefaultComboBoxModel<String> makeModel() {
    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    model.addElement("1234");
    model.addElement("5555555555555555555555");
    model.addElement("6789000000000");
    return model;
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
    } catch (ClassNotFoundException | InstantiationException
         | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
  //   g2.setPaint(new Color(200, 150, 100, 50));
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
    int r = 12;
    int w = width - 1;
    int h = height - 1;

    Path2D p = new Path2D.Double();
    p.moveTo(x, y + h);
    p.lineTo(x, y + r);
    p.quadTo(x, y, x + r, y);
    p.lineTo(x + w - r, y);
    p.quadTo(x + w, y, x + w, y + r);
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
