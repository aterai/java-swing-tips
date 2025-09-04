// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public final class MainPanel extends JPanel {
  private static final String TEXT = String.join("\n",
      "Trail: Creating a GUI with JFC/Swing",
      "Lesson: Learning Swing by Example",
      "This lesson explains the concepts you need to",
      " use Swing components in building a user interface.",
      " First we examine the simplest Swing application you can write.",
      " Then we present several progressively complicated examples of creating",
      " user interfaces using components in the javax.swing package.",
      " We cover several Swing components, such as buttons, labels, and text areas.",
      " The handling of events is also discussed,",
      " as are layout management and accessibility.",
      " This lesson ends with a set of questions and exercises",
      " so you can test yourself on what you've learned.",
      "https://docs.oracle.com/javase/tutorial/uiswing/learn/index.html");

  private MainPanel() {
    super(new BorderLayout());
    JTextArea textArea = new JTextArea(TEXT) {
      @Override public void updateUI() {
        super.updateUI();
        Caret caret = new RoundedSelectionCaret();
        caret.setBlinkRate(UIManager.getInt("TextArea.caretBlinkRate"));
        setCaret(caret);
        ((DefaultHighlighter) getHighlighter()).setDrawsLayeredHighlights(false);
        setSelectedTextColor(null);
      }
    };
    JCheckBox check = new JCheckBox("setLineWrap / setWrapStyleWord:");
    check.addActionListener(e -> {
      boolean b = ((JCheckBox) e.getSource()).isSelected();
      textArea.setLineWrap(b);
      textArea.setWrapStyleWord(b);
    });
    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    box.add(Box.createHorizontalGlue());
    box.add(check);

    JPanel p = new JPanel(new BorderLayout());
    p.add(new JScrollPane(textArea));
    p.add(box, BorderLayout.SOUTH);

    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab("JTextArea", p);
    tabs.add("JEditorPane", new JScrollPane(makeEditorPane()));
    add(tabs);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JEditorPane makeEditorPane() {
    JEditorPane editor = new JEditorPane() {
      @Override public void updateUI() {
        super.updateUI();
        // setSelectedTextColor(null);
        // setSelectionColor(new Color(0x64_88_AA_AA, true));
        setBackground(new Color(0xEE_EE_EE));
        Caret caret = new RoundedSelectionCaret();
        caret.setBlinkRate(UIManager.getInt("TextArea.caretBlinkRate"));
        setCaret(caret);
        ((DefaultHighlighter) getHighlighter()).setDrawsLayeredHighlights(false);
      }
    };
    HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
    htmlEditorKit.setStyleSheet(makeStyleSheet());
    editor.setEditorKit(htmlEditorKit);
    editor.setEditable(false);
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Optional.ofNullable(cl.getResource("example/test.html"))
        .ifPresent(url -> {
          try {
            editor.setPage(url);
          } catch (IOException ex) {
            UIManager.getLookAndFeel().provideErrorFeedback(editor);
            editor.setText(ex.getMessage());
          }
        });
    return editor;
  }

  private static StyleSheet makeStyleSheet() {
    StyleSheet styleSheet = new StyleSheet();
    styleSheet.addRule(".str{color:#008800}");
    styleSheet.addRule(".kwd{color:#000088}");
    styleSheet.addRule(".com{color:#880000}");
    styleSheet.addRule(".typ{color:#660066}");
    styleSheet.addRule(".lit{color:#006666}");
    styleSheet.addRule(".pun{color:#666600}");
    styleSheet.addRule(".pln{color:#000000}");
    styleSheet.addRule(".tag{color:#000088}");
    styleSheet.addRule(".atn{color:#660066}");
    styleSheet.addRule(".atv{color:#008800}");
    styleSheet.addRule(".dec{color:#660066}");
    return styleSheet;
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

class RoundedSelectionCaret extends DefaultCaret {
  @Override protected HighlightPainter getSelectionPainter() {
    return new RoundedSelectionHighlightPainter();
  }

  @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
  @Override protected synchronized void damage(Rectangle r) {
    super.damage(r);
    JTextComponent c = getComponent();
    int startOffset = c.getSelectionStart();
    int endOffset = c.getSelectionEnd();
    TextUI mapper = c.getUI();
    try {
      Rectangle p0 = mapper.modelToView(c, startOffset);
      Rectangle p1 = mapper.modelToView(c, endOffset);
      int w = c.getWidth();
      int h = (int) (p1.getMaxY() - p0.getMinY());
      c.repaint(new Rectangle(0, p0.y, w, h));
    } catch (BadLocationException ex) {
      UIManager.getLookAndFeel().provideErrorFeedback(c);
    }
  }
}

class RoundedSelectionHighlightPainter extends DefaultHighlightPainter {
  public static final int ARC = 3;

  protected RoundedSelectionHighlightPainter() {
    super(null);
  }

  @Override public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    // Color color = c.getSelectionColor();
    // g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 64));
    int rgba = c.getSelectionColor().getRGB() & 0xFF_FF_FF | (64 << 24);
    g2.setColor(new Color(rgba, true));
    try {
      Area area = getLinesArea(c, offs0, offs1);
      for (Area a : GeomUtils.singularization(area)) {
        List<Point2D> lst = GeomUtils.convertAreaToListOfPoint2D(a);
        GeomUtils.flatteningStepsOnRightSide(lst, ARC * 2d);
        g2.fill(GeomUtils.convertRoundedPath(lst, ARC));
      }
    } catch (BadLocationException ex) {
      // can't render
      Logger.getGlobal().severe(ex::getMessage);
    }
    g2.dispose();
  }

  private static Area getLinesArea(JTextComponent c, int offs0, int offs1)
      throws BadLocationException {
    TextUI mapper = c.getUI();
    Area area = new Area();
    int cur = offs0;
    do {
      int startOffset = Utilities.getRowStart(c, cur);
      int endOffset = Utilities.getRowEnd(c, cur);
      Rectangle p0 = mapper.modelToView(c, Math.max(startOffset, offs0));
      Rectangle p1 = mapper.modelToView(c, Math.min(endOffset, offs1));
      if (offs1 > endOffset) {
        p1.width += 6;
      }
      addRectToArea(area, p0.union(p1));
      cur = endOffset + 1;
    } while (cur < offs1);
    return area;
  }

  private static void addRectToArea(Area area, Rectangle rect) {
    area.add(new Area(rect));
  }
}

final class GeomUtils {
  private GeomUtils() {
    /* Singleton */
  }

  public static List<Point2D> convertAreaToListOfPoint2D(Area area) {
    List<Point2D> list = new ArrayList<>();
    PathIterator pi = area.getPathIterator(null);
    double[] cd = new double[6];
    while (!pi.isDone()) {
      switch (pi.currentSegment(cd)) {
        case PathIterator.SEG_MOVETO:
        case PathIterator.SEG_LINETO:
          list.add(new Point2D.Double(cd[0], cd[1]));
          break;
        default:
          break;
      }
      pi.next();
    }
    return list;
  }

  public static void flatteningStepsOnRightSide(List<Point2D> list, double arc) {
    int sz = list.size();
    for (int i = 0; i < sz; i++) {
      int i1 = (i + 1) % sz;
      int i2 = (i + 2) % sz;
      int i3 = (i + 3) % sz;
      Point2D pt0 = list.get(i);
      Point2D pt1 = list.get(i1);
      Point2D pt2 = list.get(i2);
      Point2D pt3 = list.get(i3);
      double dx1 = pt2.getX() - pt1.getX();
      if (Math.abs(dx1) > 1.0e-1 && Math.abs(dx1) < arc) {
        double max = Math.max(pt0.getX(), pt2.getX());
        replace(list, i, max, pt0.getY());
        replace(list, i1, max, pt1.getY());
        replace(list, i2, max, pt2.getY());
        replace(list, i3, max, pt3.getY());
      }
    }
  }

  private static void replace(List<Point2D> list, int i, double x, double y) {
    list.remove(i);
    list.add(i, new Point2D.Double(x, y));
  }

  /**
   * Rounding the corners of a Rectilinear Polygon.
   */
  public static Path2D convertRoundedPath(List<Point2D> list, double arc) {
    double kappa = 4d * (Math.sqrt(2d) - 1d) / 3d; // = 0.55228...;
    double akv = arc - arc * kappa;
    int sz = list.size();
    Point2D pt0 = list.get(0);
    Path2D path = new Path2D.Double();
    path.moveTo(pt0.getX() + arc, pt0.getY());
    for (int i = 0; i < sz; i++) {
      Point2D prv = list.get((i - 1 + sz) % sz);
      Point2D cur = list.get(i);
      Point2D nxt = list.get((i + 1) % sz);
      double dx0 = signum(cur.getX() - prv.getX(), arc);
      double dy0 = signum(cur.getY() - prv.getY(), arc);
      double dx1 = signum(nxt.getX() - cur.getX(), arc);
      double dy1 = signum(nxt.getY() - cur.getY(), arc);
      path.curveTo(
          cur.getX() - dx0 * akv, cur.getY() - dy0 * akv,
          cur.getX() + dx1 * akv, cur.getY() + dy1 * akv,
          cur.getX() + dx1 * arc, cur.getY() + dy1 * arc);
      path.lineTo(nxt.getX() - dx1 * arc, nxt.getY() - dy1 * arc);
    }
    path.closePath();
    return path;
  }

  private static double signum(double v, double arc) {
    return Math.abs(v) < arc ? 0d : Math.signum(v);
  }

  public static List<Area> singularization(Area rect) {
    List<Area> list = new ArrayList<>();
    Path2D path = new Path2D.Double();
    PathIterator pi = rect.getPathIterator(null);
    double[] cd = new double[6];
    while (!pi.isDone()) {
      switch (pi.currentSegment(cd)) {
        case PathIterator.SEG_MOVETO:
          path.moveTo(cd[0], cd[1]);
          break;
        case PathIterator.SEG_LINETO:
          path.lineTo(cd[0], cd[1]);
          break;
        case PathIterator.SEG_QUADTO:
          path.quadTo(cd[0], cd[1], cd[2], cd[3]);
          break;
        case PathIterator.SEG_CUBICTO:
          path.curveTo(cd[0], cd[1], cd[2], cd[3], cd[4], cd[5]);
          break;
        case PathIterator.SEG_CLOSE:
          path.closePath();
          list.add(new Area(path));
          path.reset();
          break;
        default:
          break;
      }
      pi.next();
    }
    return list;
  }
}
