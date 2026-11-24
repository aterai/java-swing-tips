// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.LayerUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
  private static final Color WARNING = new Color(0xFF_C8_C8);
  private static final String TEXT = String.join("\n",
      "Trail: Creating a GUI with JFC/Swing",
      "https://docs.oracle.com/javase/tutorial/uiswing/learn/index.html",
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
      " so you can test yourself on what you've learned.");
  private final JTextArea textArea = new JTextArea();
  private final JTextField field = new JTextField("Swing");
  private final JCheckBox checkCase = new JCheckBox("Match case");
  private final JCheckBox checkWord = new JCheckBox("Match whole word only");
  private final MatchCountLayerUI<JTextComponent> layerUI = new MatchCountLayerUI<>();

  private MainPanel() {
    super(new BorderLayout());
    textArea.setEditable(false);
    textArea.setText(TEXT);

    JButton prevButton = new JButton("⋀");
    prevButton.setActionCommand("prev");

    JButton nextButton = new JButton("⋁");
    nextButton.setActionCommand("next");

    HighlightHandler handler = new HighlightHandler();
    field.getDocument().addDocumentListener(handler);
    Stream.of(prevButton, nextButton, checkCase, checkWord).forEach(b -> {
      b.setFocusable(false);
      b.addActionListener(handler);
    });

    JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
    buttonPanel.add(prevButton);
    buttonPanel.add(nextButton);

    JPanel checkPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    checkPanel.add(checkCase);
    checkPanel.add(checkWord);

    JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
    searchPanel.setBorder(BorderFactory.createTitledBorder("Search"));
    searchPanel.add(new JLayer<>(field, layerUI));
    searchPanel.add(buttonPanel, BorderLayout.EAST);
    searchPanel.add(checkPanel, BorderLayout.SOUTH);

    EventQueue.invokeLater(() -> changeHighlight(0));

    add(searchPanel, BorderLayout.NORTH);
    add(new JScrollPane(textArea));
    setPreferredSize(new Dimension(320, 240));
  }

  private Optional<Pattern> getPattern(String text) {
    Optional<Pattern> pattern = Optional.empty();
    if (!text.isEmpty()) {
      // Wrap the input in a non-capturing group so that the word boundaries
      // also apply to every alternative of a pattern such as "a|b"
      String regex = checkWord.isSelected() ? "\\b(?:" + text + ")\\b" : text;
      int flags = checkCase.isSelected() ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
      try {
        pattern = Optional.of(Pattern.compile(regex, flags));
      } catch (PatternSyntaxException ex) {
        field.setBackground(WARNING);
      }
    }
    return pattern;
  }

  private int changeHighlight(int index) {
    // Restore the background color that may have been changed by an invalid pattern
    field.setBackground(UIManager.getColor("TextField.background"));
    int current = getPattern(field.getText())
        .map(pattern -> DocUtils.highlightCurrentMatch(textArea, pattern, index))
        .orElseGet(() -> {
          // Clear the stale highlights of the previous search
          textArea.getHighlighter().removeAllHighlights();
          return -1;
        });
    int hits = textArea.getHighlighter().getHighlights().length;
    JLabel hint = layerUI.getHintLabel();
    hint.setOpaque(hits == 0);
    hint.setText(String.format("%02d / %02d", current + 1, hits));
    field.repaint();
    return current;
  }

  private final class HighlightHandler implements DocumentListener, ActionListener {
    private int current;

    @Override public void changedUpdate(DocumentEvent e) {
      /* not needed */
    }

    @Override public void insertUpdate(DocumentEvent e) {
      current = changeHighlight(current);
    }

    @Override public void removeUpdate(DocumentEvent e) {
      current = changeHighlight(current);
    }

    @Override public void actionPerformed(ActionEvent e) {
      String cmd = e.getActionCommand();
      if (Objects.equals("prev", cmd)) {
        current--;
      } else if (Objects.equals("next", cmd)) {
        current++;
      }
      current = changeHighlight(current);
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

final class DocUtils {
  private static final HighlightPainter MATCH_PAINTER = new DefaultHighlightPainter(Color.YELLOW);
  private static final HighlightPainter CURRENT_PAINTER = new DefaultHighlightPainter(Color.ORANGE);

  private DocUtils() {
    /* Utility class */
  }

  // JTextComponent#modelToView(int) is kept for Java 8 compatibility
  @SuppressWarnings("deprecation")
  public static void scrollToCenter(JTextComponent tc, int pos) throws BadLocationException {
    Rectangle rect = tc.modelToView(pos);
    // Java 9: Rectangle rect = tc.modelToView2D(pos).getBounds();
    Container c = SwingUtilities.getAncestorOfClass(JViewport.class, tc);
    if (Objects.nonNull(rect) && c instanceof JViewport) {
      // Expand the rectangle to the size of the viewport around its center
      // so that the target position is scrolled to the center of the viewport
      int w = c.getWidth();
      int h = c.getHeight();
      rect.x = Math.round(rect.x + rect.width / 2f - w / 2f);
      rect.y = Math.round(rect.y + rect.height / 2f - h / 2f);
      rect.setSize(w, h);
      tc.scrollRectToVisible(rect);
    }
  }

  public static Highlighter highlightAllMatches(JTextComponent editor, Pattern pattern) {
    Highlighter highlighter = editor.getHighlighter();
    highlighter.removeAllHighlights();
    Document doc = editor.getDocument();
    try {
      Matcher matcher = pattern.matcher(doc.getText(0, doc.getLength()));
      while (matcher.find()) {
        // Skip empty matches (e.g. "a*"), Matcher#find() advances past them by itself
        if (matcher.end() > matcher.start()) {
          highlighter.addHighlight(matcher.start(), matcher.end(), MATCH_PAINTER);
        }
      }
    } catch (BadLocationException ex) {
      // should never happen
      RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
      wrap.initCause(ex);
      throw wrap;
    }
    return highlighter;
  }

  public static int highlightCurrentMatch(JTextComponent editor, Pattern pattern, int index) {
    Highlighter highlighter = highlightAllMatches(editor, pattern);
    Highlighter.Highlight[] highlights = highlighter.getHighlights();
    int hits = highlights.length;
    int current = -1;
    if (hits > 0) {
      // Wrap around in both directions (the previous button decrements the index)
      current = Math.floorMod(index, hits);
      Highlighter.Highlight hh = highlights[current];
      highlighter.removeHighlight(hh);
      int start = hh.getStartOffset();
      try {
        highlighter.addHighlight(start, hh.getEndOffset(), CURRENT_PAINTER);
        scrollToCenter(editor, start);
      } catch (BadLocationException ex) {
        // should never happen
        RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
        wrap.initCause(ex);
        throw wrap;
      }
    }
    return current;
  }
}

class MatchCountLayerUI<V extends JTextComponent> extends LayerUI<V> {
  private final JLabel hint = new JLabel() {
    @Override public void updateUI() {
      super.updateUI();
      setForeground(UIManager.getColor("TextField.inactiveForeground"));
      setBackground(Color.RED);
    }
  };

  @Override public void updateUI(JLayer<? extends V> l) {
    super.updateUI(l);
    SwingUtilities.updateComponentTreeUI(hint);
  }

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (c instanceof JLayer) {
      JTextComponent tc = (JTextComponent) ((JLayer<?>) c).getView();
      if (!tc.getText().isEmpty()) {
        Graphics2D g2 = (Graphics2D) g.create();
        Dimension d = hint.getPreferredSize();
        Rectangle r = SwingUtilities.calculateInnerArea(tc, null);
        int x = r.x + r.width - d.width - 1;
        int y = r.y + (r.height - d.height) / 2;
        SwingUtilities.paintComponent(g2, hint, tc, x, y, d.width, d.height);
        g2.dispose();
      }
    }
  }

  public JLabel getHintLabel() {
    return hint;
  }
}
