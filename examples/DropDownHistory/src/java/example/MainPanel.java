// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.JTextComponent;

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
      "https://docs.oracle.com/javase/tutorial/uiswing/learn/index.html"
  );
  private static final HighlightPainter HIGHLIGHT = new DefaultHighlightPainter(Color.YELLOW);
  private final JTextArea textArea = new JTextArea();
  private final JComboBox<String> combo = new JComboBox<>();

  private MainPanel() {
    super(new BorderLayout());
    textArea.setText(TEXT);
    textArea.setLineWrap(true);
    textArea.setEditable(false);

    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    model.addElement("swing");
    combo.setModel(model);
    combo.setEditable(true);

    JButton searchButton = new JButton("Search");
    searchButton.addActionListener(e -> {
      String pattern = Objects.toString(combo.getEditor().getItem());
      if (addItem(combo, pattern, 4)) {
        setHighlight(textArea, pattern);
      } else {
        textArea.getHighlighter().removeAllHighlights();
      }
    });
    EventQueue.invokeLater(() -> getRootPane().setDefaultButton(searchButton));

    JPanel p = new JPanel(new BorderLayout(5, 5));
    p.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
    p.add(new JLabel("Search History:"), BorderLayout.WEST);
    p.add(combo);
    p.add(searchButton, BorderLayout.EAST);

    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(textArea));
    setPreferredSize(new Dimension(320, 240));
  }

  public static boolean addItem(JComboBox<String> combo, String str, int max) {
    DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) combo.getModel();
    return Optional.ofNullable(str)
        .filter(s -> !s.isEmpty())
        .map(item -> {
          combo.setVisible(false);
          model.removeElement(item);
          model.insertElementAt(item, 0);
          if (model.getSize() > max) {
            model.removeElementAt(max);
          }
          combo.setSelectedIndex(0);
          combo.setVisible(true);
          return true;
        })
        .orElse(false);
  }

  private static void setHighlight(JTextComponent textArea, String pattern) {
    Highlighter highlighter = textArea.getHighlighter();
    highlighter.removeAllHighlights();
    try {
      Document doc = textArea.getDocument();
      String text = doc.getText(0, doc.getLength());
      Matcher matcher = Pattern.compile(pattern).matcher(text);
      int pos = 0;
      while (matcher.find(pos) && !matcher.group().isEmpty()) {
        pos = matcher.end();
        highlighter.addHighlight(matcher.start(), pos, HIGHLIGHT);
      }
      // while ((pos = text.indexOf(pattern, pos)) >= 0) {
      //   highlighter.addHighlight(pos, pos + pattern.length(), HIGHLIGHT);
      //   pos += pattern.length();
      // }
    } catch (BadLocationException | PatternSyntaxException ex) {
      UIManager.getLookAndFeel().provideErrorFeedback(textArea);
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
