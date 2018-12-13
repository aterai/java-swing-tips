package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public final class MainPanel extends JPanel {
  private static final Color WARNING_COLOR = new Color(255, 200, 200);
  private final JTextField field = new JTextField("ab+");
  private final HighlightTableCellRenderer renderer = new HighlightTableCellRenderer();

  private final String[] columnNames = {"A", "B"};
  private final Object[][] data = {
      {"aaa", "bbaacc"}, {"bbb", "defg"},
      {"ccccbbbbaaabbbbaaeabee", "xxx"}, {"dddaaabbbbb", "ccbbaa"},
      {"cc cc bbbb aaa bbbb e", "xxx"}, {"ddd aaa b bbbb", "cc bbaa"}};
  private final TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return String.class;
      }
    };
  private final transient TableRowSorter<? extends TableModel> sorter = new TableRowSorter<>(model);
  private final JTable table = new JTable(model);

  public MainPanel() {
    super(new BorderLayout(5, 5));

    table.setFillsViewportHeight(true);
    table.setRowSorter(sorter);
    table.setDefaultRenderer(String.class, renderer);

    field.getDocument().addDocumentListener(new DocumentListener() {
      @Override public void insertUpdate(DocumentEvent e) {
        fireDocumentChangeEvent();
      }

      @Override public void removeUpdate(DocumentEvent e) {
        fireDocumentChangeEvent();
      }

      @Override public void changedUpdate(DocumentEvent e) { /* not needed */ }
    });
    fireDocumentChangeEvent();

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

  protected void fireDocumentChangeEvent() {
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

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
    } catch (ClassNotFoundException | InstantiationException
         | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

class HighlightTableCellRenderer extends DefaultTableCellRenderer {
  private static final String SPAN = "%s<span style='color:#000000; background-color:#FFFF00'>%s</span>";
  private String pattern = "";
  private String prev;

  public boolean updatePattern(String str) {
    if (Objects.equals(str, pattern)) {
      return false;
    } else {
      prev = pattern;
      pattern = str;
      return true;
    }
  }

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    String txt = Objects.toString(value, "");
    if (Objects.nonNull(pattern) && !pattern.isEmpty() && !Objects.equals(pattern, prev)) {
      Matcher matcher = Pattern.compile(pattern).matcher(txt);
      int pos = 0;
      StringBuilder buf = new StringBuilder("<html>");
      while (matcher.find(pos)) {
        int start = matcher.start();
        int end = matcher.end();
        buf.append(String.format(SPAN, txt.substring(pos, start), txt.substring(start, end)));
        pos = end;
      }
      buf.append(txt.substring(pos));
      txt = buf.toString();
    }
    super.getTableCellRendererComponent(table, txt, isSelected, hasFocus, row, column);
    return this;
  }
}
