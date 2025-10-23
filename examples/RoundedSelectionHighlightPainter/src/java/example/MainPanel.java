// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.View;

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
  private final JTextField field = new JTextField("Swing");
  private final JTextArea textArea = new JTextArea(TEXT) {
    @Override public void updateUI() {
      super.updateUI();
      Caret caret = new DefaultCaret() {
        @Override protected Highlighter.HighlightPainter getSelectionPainter() {
          return new RoundedSelectionHighlightPainter();
        }
      };
      caret.setBlinkRate(UIManager.getInt("TextArea.caretBlinkRate"));
      setCaret(caret);
      // setLineWrap(true);
    }
  };

  private MainPanel() {
    super(new BorderLayout(5, 5));
    String title = "DefaultHighlighter#setDrawsLayeredHighlights";
    JCheckBox check = new JCheckBox(title, true);
    check.addActionListener(e -> {
      DefaultHighlighter dh = (DefaultHighlighter) textArea.getHighlighter();
      dh.setDrawsLayeredHighlights(((JCheckBox) e.getSource()).isSelected());
      DocumentUtils.updateHighlight(textArea, field);
      textArea.select(textArea.getSelectionStart(), textArea.getSelectionEnd());
    });

    field.getDocument().addDocumentListener(new HighlightDocumentListener());
    DocumentUtils.updateHighlight(textArea, field);

    JPanel sp = new JPanel(new BorderLayout(5, 5));
    sp.add(new JLabel("regex pattern:"), BorderLayout.WEST);
    sp.add(field);
    sp.add(Box.createVerticalStrut(2), BorderLayout.SOUTH);
    sp.setBorder(BorderFactory.createTitledBorder("Search"));

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(check);
    box.add(Box.createHorizontalStrut(2));

    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(sp, BorderLayout.NORTH);
    add(new JScrollPane(textArea));
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private final class HighlightDocumentListener implements DocumentListener {
    @Override public void insertUpdate(DocumentEvent e) {
      DocumentUtils.updateHighlight(textArea, field);
    }

    @Override public void removeUpdate(DocumentEvent e) {
      DocumentUtils.updateHighlight(textArea, field);
    }

    @Override public void changedUpdate(DocumentEvent e) {
      /* not needed */
    }
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

final class DocumentUtils {
  private static final Color WARNING_COLOR = new Color(0xFF_C8_C8);

  private DocumentUtils() {
    /* Singleton */
  }

  public static void updateHighlight(JTextComponent editor, JTextField field) {
    field.setBackground(Color.WHITE);
    String pattern = field.getText().trim();
    Highlighter highlighter = editor.getHighlighter();
    highlighter.removeAllHighlights();
    if (!pattern.isEmpty()) {
      Document doc = editor.getDocument();
      try {
        String text = doc.getText(0, doc.getLength());
        Matcher matcher = Pattern.compile(pattern).matcher(text);
        HighlightPainter highlightPainter = new RoundedHighlightPainter();
        int pos = 0;
        while (matcher.find(pos) && !matcher.group().isEmpty()) {
          int start = matcher.start();
          int end = matcher.end();
          highlighter.addHighlight(start, end, highlightPainter);
          pos = end;
        }
      } catch (BadLocationException | PatternSyntaxException ex) {
        UIManager.getLookAndFeel().provideErrorFeedback(field);
        field.setBackground(WARNING_COLOR);
      }
      field.repaint();
      editor.repaint();
    }
  }
}

class RoundedHighlightPainter extends DefaultHighlightPainter {
  protected RoundedHighlightPainter() {
    super(new Color(0x0, true));
  }

  @Override public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
    try {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      TextUI mapper = c.getUI();
      Rectangle p0 = mapper.modelToView(c, offs0);
      Rectangle p1 = mapper.modelToView(c, offs1);
      if (p0.y == p1.y) { // same line, render a rectangle
        Shape s = makeRoundRectangle(p0.union(p1));
        g2.setColor(Color.PINK);
        g2.fill(s);
        g2.setPaint(Color.RED);
        g2.draw(s);
      } else { // different lines
        Rectangle alloc = bounds.getBounds();
        int p0ToMarginWidth = alloc.x + alloc.width - p0.x;
        g2.setColor(Color.PINK);
        g2.fillRoundRect(p0.x, p0.y, p0ToMarginWidth, p0.height, 5, 5);
        g2.fillRect(p0.x + 5, p0.y, p0ToMarginWidth - 5, p0.height);
        int maxY = p0.y + p0.height;
        if (maxY != p1.y) {
          g2.fillRect(alloc.x, maxY, alloc.width, p1.y - maxY);
        }
        g2.fillRect(alloc.x, p1.y, p1.x - alloc.x - 5, p1.height);
        g2.fillRoundRect(alloc.x, p1.y, p1.x - alloc.x, p1.height, 5, 5);
      }
      g2.dispose();
    } catch (BadLocationException ex) {
      // can't render
      Logger.getGlobal().severe(ex::getMessage);
    }
  }

  @Override public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
    Shape s = super.paintLayer(g, offs0, offs1, bounds, c, view);
    if (s instanceof Rectangle) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setPaint(Color.ORANGE);
      Shape r = makeRoundRectangle(s.getBounds());
      g2.fill(r);
      g2.setPaint(Color.RED);
      g2.draw(r);
      g2.dispose();
    }
    return s;
  }

  private static RoundRectangle2D makeRoundRectangle(Rectangle r) {
    return new RoundRectangle2D.Float(r.x, r.y, r.width - 1, r.height - 1, 5f, 5f);
  }
}

class RoundedSelectionHighlightPainter extends DefaultHighlightPainter {
  protected RoundedSelectionHighlightPainter() {
    super(null);
  }

  @Override public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
    Rectangle alloc = bounds.getBounds();
    try {
      // --- determine locations ---
      TextUI mapper = c.getUI();
      Rectangle p0 = mapper.modelToView(c, offs0);
      Rectangle p1 = mapper.modelToView(c, offs1);
      // --- render ---
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      Color color = getColor();
      if (color == null) {
        g2.setColor(c.getSelectionColor().brighter());
      } else {
        g2.setColor(color);
      }
      if (p0.y == p1.y) { // same line, render a rectangle
        Rectangle r = p0.union(p1);
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 5, 5);
      } else { // different lines
        int p0ToMarginWidth = alloc.x + alloc.width - p0.x;
        g2.fillRoundRect(p0.x, p0.y, p0ToMarginWidth, p0.height, 5, 5);
        g2.fillRect(p0.x + 5, p0.y, p0ToMarginWidth - 5, p0.height);
        int maxY = p0.y + p0.height;
        if (maxY != p1.y) {
          g2.fillRect(alloc.x, maxY, alloc.width, p1.y - maxY);
        }
        g2.fillRect(alloc.x, p1.y, p1.x - alloc.x - 5, p1.height);
        g2.fillRoundRect(alloc.x, p1.y, p1.x - alloc.x, p1.height, 5, 5);
      }
      g2.dispose();
    } catch (BadLocationException ex) {
      // can't render
      Logger.getGlobal().severe(ex::getMessage);
    }
  }

  @Override public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Color color = getColor();
    if (color == null) {
      g2.setColor(c.getSelectionColor());
    } else {
      g2.setColor(color);
    }
    Rectangle r = null;
    if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()) {
      // Contained in view, can just use bounds.
      if (bounds instanceof Rectangle) {
        r = (Rectangle) bounds;
      } else {
        r = bounds.getBounds();
      }
    } else {
      // Should only render part of View.
      try {
        // --- determine locations ---
        Shape shape = view.modelToView(
            offs0, Position.Bias.Forward, offs1, Position.Bias.Backward, bounds);
        r = shape instanceof Rectangle ? (Rectangle) shape : shape.getBounds();
      } catch (BadLocationException ex) {
        // can't render
        Logger.getGlobal().severe(ex::getMessage);
      }
    }
    if (r != null) {
      // If we are asked to highlight, we should draw something even
      // if the model-to-view projection is of zero width (6340106).
      r.width = Math.max(r.width, 1);
      g2.fillRoundRect(r.x, r.y, r.width - 1, r.height - 1, 5, 5);
      g2.setColor(c.getSelectionColor().darker());
      g2.drawRoundRect(r.x, r.y, r.width - 1, r.height - 1, 5, 5);
    }
    g2.dispose();
    return r;
  }
}
