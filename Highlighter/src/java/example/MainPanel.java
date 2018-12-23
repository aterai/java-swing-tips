// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
  private static final String PATTERN = "Swing";
  private static final String INIT_TXT = "Trail: Creating a GUI with JFC/Swing\n"
      + "Lesson: Learning Swing by Example\n"
      + "This lesson explains the concepts you need to\n"
      + " use Swing components in building a user interface.\n"
      + " First we examine the simplest Swing application you can write.\n"
      + " Then we present several progressively complicated examples of creating\n"
      + " user interfaces using components in the javax.swing package.\n"
      + " We cover several Swing components, such as buttons, labels, and text areas.\n"
      + " The handling of events is also discussed,\n"
      + " as are layout management and accessibility.\n"
      + " This lesson ends with a set of questions and exercises\n"
      + " so you can test yourself on what you've learned.\n"
      + "https://docs.oracle.com/javase/tutorial/uiswing/learn/index.html\n";
  private static final Highlighter.HighlightPainter HIGHLIGHT = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);

  private MainPanel() {
    super(new BorderLayout());

    JTextArea jta = new JTextArea();
    jta.setLineWrap(true);
    jta.setText(INIT_TXT);

    JButton highlight = new JButton("highlight: " + PATTERN);
    highlight.addActionListener(e -> {
      jta.setEditable(false);
      setHighlight(jta, PATTERN);
    });

    JButton clear = new JButton("clear");
    clear.addActionListener(e -> {
      jta.setEditable(true);
      jta.getHighlighter().removeAllHighlights();
    });

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(highlight);
    box.add(clear);
    add(new JScrollPane(jta));
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  protected static void setHighlight(JTextComponent jtc, String pattern) {
    jtc.getHighlighter().removeAllHighlights();
    try {
      Highlighter highlighter = jtc.getHighlighter();
      Document doc = jtc.getDocument();
      String text = doc.getText(0, doc.getLength());
      Matcher matcher = Pattern.compile(pattern).matcher(text);
      int pos = 0;
      while (matcher.find(pos)) {
        pos = matcher.end();
        highlighter.addHighlight(matcher.start(), pos, HIGHLIGHT);
      }
      // int pos = text.indexOf(pattern);
      // while (pos >= 0) {
      //   int nextp = pos + pattern.length();
      //   jtc.getHighlighter().addHighlight(pos, nextp, HIGHLIGHT);
      //   pos = text.indexOf(pattern, nextp);
      // }
    } catch (BadLocationException | PatternSyntaxException ex) {
      ex.printStackTrace();
    }
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
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
