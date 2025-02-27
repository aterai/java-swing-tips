// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String[] array = {
        "1111", "1111222", "111122233", "111122233444",
        "12345", "67890", "55551", "555512"};
    JComboBox<String> combo = makeComboBox(array);
    combo.setEditable(true);
    combo.setSelectedIndex(-1);
    JTextField field = (JTextField) combo.getEditor().getEditorComponent();
    field.setText("");
    field.addKeyListener(new ComboKeyHandler(combo));

    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder("Auto-Completion ComboBox"));
    p.add(combo, BorderLayout.NORTH);

    Box box = Box.createVerticalBox();
    box.add(makeHelpPanel());
    box.add(Box.createVerticalStrut(5));
    box.add(p);
    add(box, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JComboBox<String> makeComboBox(String... model) {
    HighlightPainter highlightPainter = new DefaultHighlightPainter(Color.YELLOW);
    return new JComboBox<String>(model) {
      @Override public void updateUI() {
        super.updateUI();
        JTextField field = new JTextField(" ");
        field.setOpaque(true);
        field.setBorder(BorderFactory.createEmptyBorder());
        ListCellRenderer<? super String> renderer = getRenderer();
        setRenderer((list, value, index, isSelected, cellHasFocus) -> {
          String pattern = ((JTextField) getEditor().getEditorComponent()).getText();
          if (index >= 0 && !pattern.isEmpty()) {
            Highlighter highlighter = field.getHighlighter();
            highlighter.removeAllHighlights();
            String txt = Objects.toString(value, "");
            field.setText(txt);
            addHighlight(highlighter, Pattern.compile(pattern).matcher(txt));
            field.setBackground(isSelected ? new Color(0xAA_64_AA_FF, true) : Color.WHITE);
            return field;
          }
          return renderer.getListCellRendererComponent(
              list, value, index, isSelected, cellHasFocus);
        });
      }

      private void addHighlight(Highlighter highlighter, Matcher matcher) {
        int pos = 0;
        try {
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
      }
    };
  }

  private static JPanel makeHelpPanel() {
    JPanel lp = new JPanel(new GridLayout(2, 1, 2, 2));
    lp.add(new JLabel("Char: show Popup"));
    lp.add(new JLabel("ESC: hide Popup"));

    JPanel rp = new JPanel(new GridLayout(2, 1, 2, 2));
    rp.add(new JLabel("RIGHT: Completion"));
    rp.add(new JLabel("ENTER: Add/Selection"));

    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder("Help"));

    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(0, 5, 0, 5);
    c.fill = GridBagConstraints.BOTH;
    c.weighty = 1d;

    c.weightx = 1d;
    p.add(lp, c);

    c.weightx = 0d;
    p.add(new JSeparator(SwingConstants.VERTICAL), c);

    c.weightx = 1d;
    p.add(rp, c);

    return p;
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

class ComboKeyHandler extends KeyAdapter {
  private final JComboBox<String> combo;
  private final List<String> list = new ArrayList<>();
  private boolean shouldHide;

  protected ComboKeyHandler(JComboBox<String> combo) {
    super();
    this.combo = combo;
    for (int i = 0; i < combo.getItemCount(); i++) {
      list.add(combo.getItemAt(i));
    }
  }

  @Override public void keyTyped(KeyEvent e) {
    EventQueue.invokeLater(() -> {
      String text = ((JTextField) e.getComponent()).getText();
      ComboBoxModel<String> m;
      if (text.isEmpty()) {
        m = new DefaultComboBoxModel<>(list.toArray(new String[0]));
        setSuggestionModel(m, "");
        combo.hidePopup();
      } else {
        m = getSuggestedModel(list, text);
        if (m.getSize() == 0 || shouldHide) {
          combo.hidePopup();
        } else {
          setSuggestionModel(m, text);
          combo.showPopup();
        }
      }
    });
  }

  @Override public void keyPressed(KeyEvent e) {
    JTextField textField = (JTextField) e.getComponent();
    String text = textField.getText();
    // shouldHide = false;
    switch (e.getKeyCode()) {
      case KeyEvent.VK_RIGHT:
        for (String s : list) {
          if (s.contains(text)) {
            textField.setText(s);
            shouldHide = false;
            return;
          }
        }
        shouldHide = false;
        break;
      case KeyEvent.VK_ENTER:
        if (!list.contains(text)) {
          list.add(text);
          list.sort(Comparator.naturalOrder());
          setSuggestionModel(getSuggestedModel(list, text), text);
        }
        shouldHide = true;
        break;
      case KeyEvent.VK_ESCAPE:
        shouldHide = true;
        break;
      default:
        shouldHide = false;
        break;
    }
  }

  private void setSuggestionModel(ComboBoxModel<String> model, String txt) {
    combo.setModel(model);
    combo.setSelectedIndex(-1);
    ((JTextField) combo.getEditor().getEditorComponent()).setText(txt);
  }

  private ComboBoxModel<String> getSuggestedModel(List<String> src, String txt) {
    DefaultComboBoxModel<String> dst = new DefaultComboBoxModel<>();
    for (String s : src) {
      if (s.contains(txt)) {
        dst.addElement(s);
      }
    }
    return dst;
  }
}
