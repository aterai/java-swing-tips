// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addTab("JTree", new JScrollPane(new JTree()));
    tabbedPane.addTab("JSplitPane", new JSplitPane());
    tabbedPane.addTab("JTextArea", new JScrollPane(new JTextArea()));
    add(tabbedPane);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
      NimbusTabbedPanePainterUtils.configureUI();
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

final class NimbusTabbedPanePainterUtils {
  public static final int OVER_PAINT = 6;
  public static final float STROKE_SIZE = 2f;
  public static final int ARC = 10;
  public static final Color CONTENT_BGC = Color.LIGHT_GRAY;
  public static final Color CONTENT_BORDER = Color.ORANGE; // Color.GRAY;
  public static final Color TAB_TABAREA_MASK = Color.GREEN; // CONTENT_BGC;
  public static final Color TAB_BGC = Color.PINK; // CONTENT_BORDER;
  public static final Color TABAREA_BGC = Color.CYAN; // CONTENT_BGC;
  public static final Color TABAREA_BORDER = Color.RED; // CONTENT_BORDER;

  private NimbusTabbedPanePainterUtils() {
    /* HideUtilityClassConstructor */
  }

  public static void configureUI() {
    String content = "TabbedPane:TabbedPaneContent";
    String tabArea = "TabbedPane:TabbedPaneTabArea";
    String tab = "TabbedPane:TabbedPaneTab";

    UIDefaults d = UIManager.getLookAndFeelDefaults();
    d.put(content + ".contentMargins", new Insets(0, 5, 5, 5));
    // d.put(tabArea + ".contentMargins", new Insets(3, 10, 4, 10));
    d.put(tabArea + ".contentMargins", new Insets(3, 10, OVER_PAINT, 10));
    // d.put(tab + ".contentMargins", new Insets(2, 8, 3, 8));

    d.put(content + ".backgroundPainter", new TabbedPaneContentPainter());

    Painter<JComponent> tabAreaPainter = new TabAreaPainter();
    d.put(tabArea + "[Disabled].backgroundPainter", tabAreaPainter);
    d.put(tabArea + "[Enabled].backgroundPainter", tabAreaPainter);
    d.put(tabArea + "[Enabled+MouseOver].backgroundPainter", tabAreaPainter);
    d.put(tabArea + "[Enabled+Pressed].backgroundPainter", tabAreaPainter);

    Painter<JComponent> tabPainter = new TabPainter(false);
    d.put(tab + "[Enabled+MouseOver].backgroundPainter", tabPainter);
    d.put(tab + "[Enabled+Pressed].backgroundPainter", tabPainter);
    d.put(tab + "[Enabled].backgroundPainter", tabPainter);

    Painter<JComponent> selTabPainter = new TabPainter(true);
    d.put(tab + "[Focused+MouseOver+Selected].backgroundPainter", selTabPainter);
    d.put(tab + "[Focused+Pressed+Selected].backgroundPainter", selTabPainter);
    d.put(tab + "[Focused+Selected].backgroundPainter", selTabPainter);
    d.put(tab + "[MouseOver+Selected].backgroundPainter", selTabPainter);
    d.put(tab + "[Selected].backgroundPainter", selTabPainter);
    d.put(tab + "[Pressed+Selected].backgroundPainter", selTabPainter);
  }

  protected static class TabPainter implements Painter<JComponent> {
    private final Color color;
    private final boolean selected;

    protected TabPainter(boolean selected) {
      this.selected = selected;
      this.color = selected ? CONTENT_BGC : TAB_BGC;
    }

    @Override public void paint(Graphics2D g, JComponent c, int width, int height) {
      int a = selected ? OVER_PAINT : 0;
      int r = 6;
      int x = 3;
      int y = 3;
      Graphics2D g2 = (Graphics2D) g.create(0, 0, width, height + a);
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      int w = width - x;
      int h = height + a;

      // Paint tab shadow
      if (selected) {
        g2.setPaint(new Color(0, 0, 0, 20));
        RoundRectangle2D rect = new RoundRectangle2D.Double(0d, 0d, w, h, r, r);
        for (int i = 0; i < x; i++) {
          rect.setFrame((double) x - i, (double) y - i, w + i * 2d, h);
          g2.fill(rect);
        }
      }

      // Fill tab background
      g2.setColor(color);
      g2.fill(new RoundRectangle2D.Double(x, y, w - 1d, (double) h + a, r, r));

      if (selected) {
        // Draw a border
        g2.setStroke(new BasicStroke(STROKE_SIZE));
        g2.setPaint(TABAREA_BORDER);
        g2.draw(new RoundRectangle2D.Double(x, y, w - 1d, (double) h + a, r, r));

        // Over paint the overexposed area with the background color
        g2.setColor(TAB_TABAREA_MASK);
        g2.fill(new Rectangle2D.Double(0d, height + STROKE_SIZE, width, OVER_PAINT));
      }
      g2.dispose();
    }
  }

  protected static class TabAreaPainter implements Painter<JComponent> {
    @Override public void paint(Graphics2D g, JComponent c, int w, int h) {
      Graphics2D g2 = (Graphics2D) g.create(0, 0, w, h);
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      double y = (double) h - OVER_PAINT;
      double width = w - STROKE_SIZE;
      double height = h - STROKE_SIZE;
      Shape r = new RoundRectangle2D.Double(0d, y, width, height, ARC, ARC);
      g2.setPaint(TABAREA_BGC);
      g2.fill(r);
      g2.setColor(TABAREA_BORDER);
      g2.setStroke(new BasicStroke(STROKE_SIZE));
      g2.draw(r);
      g2.dispose();
    }
  }

  protected static class TabbedPaneContentPainter implements Painter<JComponent> {
    @Override public void paint(Graphics2D g, JComponent c, int w, int h) {
      Graphics2D g2 = (Graphics2D) g.create(0, 0, w, h);
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.translate(0, -OVER_PAINT);
      double width = w - STROKE_SIZE;
      double height = h - STROKE_SIZE + OVER_PAINT;
      Shape r = new RoundRectangle2D.Double(0d, 0d, width, height, ARC, ARC);
      g2.setPaint(CONTENT_BGC);
      g2.fill(r);
      g2.setColor(CONTENT_BORDER);
      g2.setStroke(new BasicStroke(STROKE_SIZE));
      g2.draw(r);
      g2.dispose();
    }
  }
}
