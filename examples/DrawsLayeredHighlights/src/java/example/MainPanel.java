// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

public final class MainPanel extends JPanel {
  private static final Color WARNING_COLOR = new Color(0xFF_C8_C8);
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
  private final JTextArea textArea = new JTextArea(TEXT);

  private MainPanel() {
    super(new BorderLayout(5, 5));
    textArea.setEditable(false);

    JCheckBox check = new JCheckBox("DefaultHighlighter#setDrawsLayeredHighlights", true);
    check.setFocusable(false);
    check.addActionListener(e -> {
      JCheckBox cb = (JCheckBox) e.getSource();
      int start = textArea.getSelectionStart();
      int end = textArea.getSelectionEnd();
      DefaultHighlighter dh = (DefaultHighlighter) textArea.getHighlighter();
      dh.setDrawsLayeredHighlights(cb.isSelected());
      fireDocumentChangeEvent();
      textArea.select(start, end);
    });

    field.getDocument().addDocumentListener(new DocumentListener() {
      @Override public void insertUpdate(DocumentEvent e) {
        fireDocumentChangeEvent();
      }

      @Override public void removeUpdate(DocumentEvent e) {
        fireDocumentChangeEvent();
      }

      @Override public void changedUpdate(DocumentEvent e) {
        /* not needed */
      }
    });
    fireDocumentChangeEvent();
    EventQueue.invokeLater(() -> {
      textArea.requestFocusInWindow();
      textArea.selectAll();
    });

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

  public void fireDocumentChangeEvent() {
    field.setBackground(Color.WHITE);
    String pattern = field.getText().trim();
    Highlighter highlighter = textArea.getHighlighter();
    highlighter.removeAllHighlights();
    if (pattern.isEmpty()) {
      return;
    }
    Document doc = textArea.getDocument();
    try {
      String text = doc.getText(0, doc.getLength());
      Matcher matcher = Pattern.compile(pattern).matcher(text);
      HighlightPainter highlightPainter = new DefaultHighlightPainter(Color.YELLOW);
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
    repaint();
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
