// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Area;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(0, 1, 5, 5));
    add(init(new JPanel(), "Default TitledBorder"));

    JPanel p1 = new JPanel() {
      // https://stackoverflow.com/questions/72578926/how-to-set-background-within-the-titled-border
      @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(getBackground());
        g2.fill(SwingUtilities.calculateInnerArea(this, null));
        g2.dispose();
        super.paintComponent(g);
      }
    };
    add(init(p1, "Transparent TitledBorder"));

    JPanel p2 = new JPanel() {
      @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(getBackground());
        Area area = new Area(new Rectangle(getSize()));
        area.subtract(new Area(SwingUtilities.calculateInnerArea(this, null)));
        g2.fill(area);
        g2.dispose();
        super.paintComponent(g);
      }
    };
    add(init(p2, "Paint TitledBorder background"));

    JPanel p3 = new JPanel(new BorderLayout());
    p3.setBorder(new TitledBorder("Override paintBorder") {
      @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(Color.WHITE);
        Area area = new Area(new Rectangle(x, y, width, 16));
        area.subtract(new Area(SwingUtilities.calculateInnerArea(p3, null)));
        g2.fill(area);
        g2.dispose();
        super.paintBorder(c, g, x, y, width, height);
      }

      @Override public boolean isBorderOpaque() {
        return false;
      }
    });
    add(p3);

    JPanel p = new JPanel();
    JPanel p4 = makeLabelTitledBorderPanel("OverlayLayout + JLabel", p);
    add(p4);

    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    // setBackground(new Color(0xEE_EE_FF));
    setPreferredSize(new Dimension(320, 240));
  }

  private JComponent init(JComponent c, String title) {
    c.setBackground(Color.WHITE);
    c.setOpaque(false);
    c.setBorder(BorderFactory.createTitledBorder(title));
    return c;
  }

  public static JPanel makeLabelTitledBorderPanel(String title, JPanel p) {
    JLabel label = new JLabel(title, SwingConstants.LEADING);
    label.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
    label.setOpaque(true);
    label.setBackground(Color.WHITE);
    label.setAlignmentX(LEFT_ALIGNMENT);
    label.setAlignmentY(TOP_ALIGNMENT);

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalStrut(8));
    box.add(label);
    box.setAlignmentX(LEFT_ALIGNMENT);
    box.setAlignmentY(TOP_ALIGNMENT);

    int height = label.getPreferredSize().height / 2;
    Color color = new Color(0x0, true);
    Border b1 = BorderFactory.createMatteBorder(height, 2, 2, 2, color);
    Border b2 = BorderFactory.createTitledBorder("");
    p.setBorder(BorderFactory.createCompoundBorder(b1, b2));
    p.setAlignmentX(LEFT_ALIGNMENT);
    p.setAlignmentY(TOP_ALIGNMENT);

    JPanel panel = new JPanel();
    panel.setLayout(new OverlayLayout(panel));
    panel.add(box);
    panel.add(p);
    return panel;
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
