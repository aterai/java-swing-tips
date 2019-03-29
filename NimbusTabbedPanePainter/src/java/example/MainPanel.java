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

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
      NimbusTabbedPanePainterUtils.configureUI();
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

final class NimbusTabbedPanePainterUtils {
  public static final int OVERPAINT = 6;
  public static final int STROKE_SIZE = 2;
  public static final int ARC = 10;

  private NimbusTabbedPanePainterUtils() { /* HideUtilityClassConstructor */ }

  public static void configureUI() {
    UIDefaults d = UIManager.getLookAndFeelDefaults();
    d.put("TabbedPane:TabbedPaneContent.contentMargins", new Insets(0, 5, 5, 5));
    // d.put("TabbedPane:TabbedPaneTab.contentMargins", new Insets(2, 8, 3, 8));
    // d.put("TabbedPane:TabbedPaneTabArea.contentMargins", new Insets(3, 10, 4, 10));
    d.put("TabbedPane:TabbedPaneTabArea.contentMargins", new Insets(3, 10, OVERPAINT, 10));

    Painter<JComponent> tabAreaPainter = new TabAreaPainter();
    d.put("TabbedPane:TabbedPaneTabArea[Disabled].backgroundPainter", tabAreaPainter);
    d.put("TabbedPane:TabbedPaneTabArea[Enabled].backgroundPainter", tabAreaPainter);
    d.put("TabbedPane:TabbedPaneTabArea[Enabled+MouseOver].backgroundPainter", tabAreaPainter);
    d.put("TabbedPane:TabbedPaneTabArea[Enabled+Pressed].backgroundPainter", tabAreaPainter);

    d.put("TabbedPane:TabbedPaneContent.backgroundPainter", new TabContentPainter());

    Painter<JComponent> tabPainter = new TabPainter(false);
    d.put("TabbedPane:TabbedPaneTab[Enabled+MouseOver].backgroundPainter", tabPainter);
    d.put("TabbedPane:TabbedPaneTab[Enabled+Pressed].backgroundPainter", tabPainter);
    d.put("TabbedPane:TabbedPaneTab[Enabled].backgroundPainter", tabPainter);

    Painter<JComponent> selectedTabPainter = new TabPainter(true);
    d.put("TabbedPane:TabbedPaneTab[Focused+MouseOver+Selected].backgroundPainter", selectedTabPainter);
    d.put("TabbedPane:TabbedPaneTab[Focused+Pressed+Selected].backgroundPainter", selectedTabPainter);
    d.put("TabbedPane:TabbedPaneTab[Focused+Selected].backgroundPainter", selectedTabPainter);
    d.put("TabbedPane:TabbedPaneTab[MouseOver+Selected].backgroundPainter", selectedTabPainter);
    d.put("TabbedPane:TabbedPaneTab[Selected].backgroundPainter", selectedTabPainter);
    d.put("TabbedPane:TabbedPaneTab[Pressed+Selected].backgroundPainter", selectedTabPainter);
  }

  protected static class TabPainter implements Painter<JComponent> {
    private final Color color;
    private final boolean selected;

    protected TabPainter(boolean selected) {
      this.selected = selected;
      this.color = selected ? Color.WHITE : Color.ORANGE;
    }

    @Override public void paint(Graphics2D g, JComponent c, int width, int height) {
      int a = selected ? OVERPAINT : 0;
      int r = 6;
      int x = 3;
      int y = 3;
      Graphics2D g2 = (Graphics2D) g.create(0, 0, width, height + a);
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int w = width - r - 1;
      int h = height + r;
      g2.setPaint(new Color(0, 0, 0, 20));
      RoundRectangle2D rrect = new RoundRectangle2D.Double(0, 0, w, h, r, r);
      for (int i = 0; i < x; i++) {
        rrect.setFrame(x - i, y - i, w + i + i, h);
        g2.fill(rrect);
      }
      g2.setColor(color);
      g2.fill(new RoundRectangle2D.Double(x, y, w, h + OVERPAINT, r, r));
      if (selected) {
        g2.setColor(Color.GREEN);
        g2.fill(new Rectangle2D.Double(0, height + STROKE_SIZE, width, OVERPAINT));
      }
      g2.dispose();
    }
  }

  protected static class TabAreaPainter implements Painter<JComponent> {
    @Override public void paint(Graphics2D g, JComponent c, int w, int h) {
      Graphics2D g2 = (Graphics2D) g.create(0, 0, w, h);
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      Shape r = new RoundRectangle2D.Double(0, h - OVERPAINT, w - STROKE_SIZE, h - STROKE_SIZE, ARC, ARC);
      g2.setPaint(Color.CYAN);
      g2.fill(r);
      g2.setColor(Color.RED);
      g2.setStroke(new BasicStroke(STROKE_SIZE));
      g2.draw(r);
      g2.dispose();
    }
  }

  protected static class TabContentPainter implements Painter<JComponent> {
    @Override public void paint(Graphics2D g, JComponent c, int w, int h) {
      Graphics2D g2 = (Graphics2D) g.create(0, 0, w, h);
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.translate(0, -OVERPAINT);

      Shape r = new RoundRectangle2D.Double(0, 0, w - STROKE_SIZE, h - STROKE_SIZE + OVERPAINT, ARC, ARC);
      g2.setPaint(Color.WHITE);
      g2.fill(r);
      g2.setColor(Color.ORANGE);
      g2.setStroke(new BasicStroke(STROKE_SIZE));
      g2.draw(r);
      g2.dispose();
    }
  }
}
