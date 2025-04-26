// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

public final class MainPanel extends JPanel {
  private static final Color WARNING_COLOR = new Color(0xFF_C8_C8);
  private final JTextField field = new JTextField("ab+");
  private final HighlightTableCellRenderer renderer = new HighlightTableCellRenderer();

  private MainPanel() {
    super(new BorderLayout(5, 5));
    JTable table = new JTable(makeModel());
    table.setFillsViewportHeight(true);
    table.setDefaultRenderer(String.class, renderer);
    TableRowSorter<? extends TableModel> sorter = new TableRowSorter<>(table.getModel());
    table.setRowSorter(sorter);

    field.getDocument().addDocumentListener(new DocumentListener() {
      @Override public void insertUpdate(DocumentEvent e) {
        fireDocumentChangeEvent(sorter);
      }

      @Override public void removeUpdate(DocumentEvent e) {
        fireDocumentChangeEvent(sorter);
      }

      @Override public void changedUpdate(DocumentEvent e) {
        /* not needed */
      }
    });
    fireDocumentChangeEvent(sorter);

    JPanel sp = new JPanel(new BorderLayout(5, 5));
    sp.add(new JLabel("regex pattern:"), BorderLayout.WEST);
    sp.add(field);
    sp.add(Box.createVerticalStrut(2), BorderLayout.SOUTH);
    sp.setBorder(BorderFactory.createTitledBorder("Search"));

    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(sp, BorderLayout.NORTH);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"A", "B"};
    Object[][] data = {
        {"aaa", "bb aa cc"}, {"bbb", "def"},
        {"ccc abc aa ab bb ada eab ee", "xxx"}, {"ddd aa abb bb", "cc bb aba"},
        {"cc bac bb bb aa abc e", "xxx"}, {"ddd aa ab cab bb", "cc bab aab"}
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return String.class;
      }
    };
  }

  public void fireDocumentChangeEvent(TableRowSorter<? extends TableModel> sorter) {
    field.setBackground(Color.WHITE);
    String pattern = field.getText().trim();
    if (pattern.isEmpty()) {
      sorter.setRowFilter(null);
      renderer.updatePattern("");
    } else if (renderer.updatePattern(pattern)) {
      try {
        sorter.setRowFilter(RowFilter.regexFilter(pattern));
      } catch (PatternSyntaxException ex) {
        field.setBackground(WARNING_COLOR);
      }
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

class HighlightTableCellRenderer extends JTextField implements TableCellRenderer {
  private static final Color SELECTION_BGC = new Color(0xDC_F0_FF);
  private String pattern = "";
  private String prev;

  public boolean updatePattern(String str) {
    boolean update = !Objects.equals(str, pattern);
    if (update) {
      prev = pattern;
      pattern = str;
    }
    return update;
  }

  @Override public void updateUI() {
    super.updateUI();
    setOpaque(true);
    setBorder(BorderFactory.createEmptyBorder());
    setForeground(Color.BLACK);
    setBackground(Color.WHITE);
    setEditable(false);
  }

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    String txt = Objects.toString(value, "");
    Highlighter highlighter = getHighlighter();
    highlighter.removeAllHighlights();
    setText(txt);
    setBackground(isSelected ? SELECTION_BGC : Color.WHITE);
    if (Objects.nonNull(pattern) && !pattern.isEmpty() && !Objects.equals(pattern, prev)) {
      Matcher matcher = Pattern.compile(pattern).matcher(txt);
      HighlightPainter highlightPainter = new DefaultHighlightPainter(Color.YELLOW);
      int pos = 0;
      while (matcher.find(pos) && !matcher.group().isEmpty()) {
        int start = matcher.start();
        int end = matcher.end();
        try {
          highlighter.addHighlight(start, end, highlightPainter);
        } catch (BadLocationException | PatternSyntaxException ex) {
          UIManager.getLookAndFeel().provideErrorFeedback(this);
        }
        pos = end;
      }
    }
    return this;
  }
}
