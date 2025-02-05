// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import java.util.Optional;
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
import javax.swing.text.View;

public final class MainPanel extends JPanel {
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
      " so you can test yourself on what you've learned.",
      "https://docs.oracle.com/javase/tutorial/uiswing/learn/index.html"
  );
  private static final Color WARNING_COLOR = new Color(0xFF_C8_C8);
  private final JTextArea textArea = new JTextArea();
  private final JTextField field = new JTextField("Swing");
  private final JCheckBox checkCase = new JCheckBox("Match case");
  private final JCheckBox checkWord = new JCheckBox("Match whole word only");
  private final PlaceholderLayerUI<JTextComponent> layerUI = new PlaceholderLayerUI<>();

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

    JPanel bp = new JPanel(new GridLayout(1, 2));
    bp.add(prevButton);
    bp.add(nextButton);

    JPanel cp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    cp.add(checkCase);
    cp.add(checkWord);

    JPanel sp = new JPanel(new BorderLayout(5, 5));
    sp.setBorder(BorderFactory.createTitledBorder("Search"));
    sp.add(new JLayer<>(field, layerUI));
    sp.add(bp, BorderLayout.EAST);
    sp.add(cp, BorderLayout.SOUTH);

    EventQueue.invokeLater(() -> changeHighlight(0));

    add(sp, BorderLayout.NORTH);
    add(new JScrollPane(textArea));
    setPreferredSize(new Dimension(320, 240));
  }

  private static void scrollToCenter(JTextComponent tc, int pos) throws BadLocationException {
    Rectangle rect = tc.modelToView(pos);
    // Java 9: Rectangle rect = tc.modelToView2D(pos).getBounds();
    Container c = SwingUtilities.getAncestorOfClass(JViewport.class, tc);
    if (Objects.nonNull(rect) && c instanceof JViewport) {
      rect.x = Math.round(rect.x - c.getWidth() / 2f);
      rect.width = c.getWidth();
      rect.height = Math.round(c.getHeight() / 2f);
      tc.scrollRectToVisible(rect);
    }
  }

  private Optional<Pattern> getPattern() {
    return Optional.ofNullable(field.getText())
        .filter(txt -> !txt.isEmpty())
        .map(txt -> {
          String cw = checkWord.isSelected() ? "\\b" : "";
          String fmt = String.format("%s%s%s", cw, txt, cw);
          boolean b = checkCase.isSelected();
          int flags = b ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
          Pattern pattern = null;
          try {
            pattern = Pattern.compile(fmt, flags);
          } catch (PatternSyntaxException ex) {
            field.setBackground(WARNING_COLOR);
          }
          return pattern;
        });
  }

  public int changeHighlight(int index) {
    field.setBackground(Color.WHITE);
    Highlighter highlighter = textArea.getHighlighter();
    highlighter.removeAllHighlights();
    Document doc = textArea.getDocument();
    getPattern().ifPresent(pattern -> {
      try {
        Matcher matcher = pattern.matcher(doc.getText(0, doc.getLength()));
        HighlightPainter highlightPainter = new DefaultHighlightPainter(Color.ORANGE);
        int pos = 0;
        while (matcher.find(pos) && !matcher.group().isEmpty()) {
          int start = matcher.start();
          int end = matcher.end();
          highlighter.addHighlight(start, end, highlightPainter);
          pos = end;
        }
      } catch (BadLocationException ex) {
        // should never happen
        RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
        wrap.initCause(ex);
        throw wrap;
      }
    });
    JLabel label = layerUI.getHintLabel();
    Highlighter.Highlight[] array = highlighter.getHighlights();
    int hits = array.length;
    int idx = index;
    if (hits == 0) {
      idx = -1;
      label.setOpaque(true);
    } else {
      idx = (idx + hits) % hits;
      label.setOpaque(false);
      Highlighter.Highlight hh = highlighter.getHighlights()[idx];
      highlighter.removeHighlight(hh);
      HighlightPainter currentPainter = new DefaultHighlightPainter(Color.ORANGE) {
        @Override public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
          Shape s = super.paintLayer(g, offs0, offs1, bounds, c, view);
          if (s instanceof Rectangle) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(Color.RED);
            Rectangle r = s.getBounds();
            // g2.draw(r);
            g2.drawRect(r.x, r.y, r.width - 1, r.height - 1);
            g2.dispose();
          }
          return s;
        }
      };
      try {
        highlighter.addHighlight(hh.getStartOffset(), hh.getEndOffset(), currentPainter);
        scrollToCenter(textArea, hh.getStartOffset());
      } catch (BadLocationException ex) {
        // should never happen
        RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
        wrap.initCause(ex);
        throw wrap;
      }
    }
    label.setText(String.format("%02d / %02d%n", idx + 1, hits));
    field.repaint();
    return idx;
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
      // Object o = e.getSource();
      // if (o instanceof AbstractButton) {
      //   String cmd = ((AbstractButton) o).getActionCommand();
      //   if (Objects.equals("prev", cmd)) {
      //     current--;
      //   } else if (Objects.equals("next", cmd)) {
      //     current++;
      //   }
      // }
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

class PlaceholderLayerUI<V extends JTextComponent> extends LayerUI<V> {
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
        g2.setPaint(hint.getForeground());
        Dimension d = hint.getPreferredSize();
        // Insets i = tc.getInsets();
        // int x = tc.getWidth() - i.right - d.width - 1;
        // int y = (tc.getHeight() - d.height) / 2;
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
