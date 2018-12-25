// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

public class MainPanel extends JPanel {
  private final String[] columnNames = {"Year", "String", "Comment"};
  protected final DefaultTableModel model = new DefaultTableModel(null, columnNames) {
    @Override public Class<?> getColumnClass(int column) {
      return column == 0 ? Integer.class : Object.class;
    }
  };
  protected final transient TableRowSorter<? extends TableModel> sorter = new TableRowSorter<>(model);
  protected final JTable table = new JTable(model);

  private final JButton first = new JButton("|<");
  private final JButton prev = new JButton("<");
  private final JButton next = new JButton(">");
  private final JButton last = new JButton(">|");
  private final Action enterAction = new AbstractAction() {
    @Override public void actionPerformed(ActionEvent e) {
      int v = Integer.parseInt(field.getText());
      if (v > 0 && v <= maxPageIndex) {
        currentPageIndex = v;
      }
      initFilterAndButtons();
    }
  };

  protected final JTextField field = new JTextField(2);
  protected final JLabel label = new JLabel("/ 1");

  protected final int itemsPerPage;
  protected int maxPageIndex;
  protected int currentPageIndex;

  public MainPanel() {
    super(new BorderLayout());

    itemsPerPage = 100;
    currentPageIndex = 1;

    table.setFillsViewportHeight(true);
    table.setRowSorter(sorter);
    table.setEnabled(false);

    JPanel po = new JPanel();
    po.add(field);
    po.add(label);
    JPanel box = new JPanel(new GridLayout(1, 4, 2, 2));
    box.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    Stream.of(first, prev, po, next, last).forEach(box::add);

    KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    field.getInputMap(JComponent.WHEN_FOCUSED).put(enter, "Enter");
    field.getActionMap().put("Enter", enterAction);
    ((AbstractDocument) field.getDocument()).setDocumentFilter(new IntegerDocumentFilter());

    Stream.of(first, prev, next, last).forEach(b -> b.addActionListener(this::updateCurrentPageIndex));

    new TableUpdateTask(2016, itemsPerPage).execute();

    add(box, BorderLayout.NORTH);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  protected final void updateCurrentPageIndex(ActionEvent e) {
    Object c = e.getSource();
    if (first.equals(c)) {
      currentPageIndex = 1;
    } else if (prev.equals(c)) {
      currentPageIndex -= 1;
    } else if (next.equals(c)) {
      currentPageIndex += 1;
    } else if (last.equals(c)) {
      currentPageIndex = maxPageIndex;
    }
    initFilterAndButtons();
  }

  protected class TableUpdateTask extends LoadTask {
    protected TableUpdateTask(int max, int itemsPerPage) {
      super(max, itemsPerPage);
      field.setEditable(false);
    }

    @Override protected void process(List<List<Object[]>> chunks) {
      if (isCancelled()) {
        return;
      }
      if (!isDisplayable()) {
        System.out.println("process: DISPOSE_ON_CLOSE");
        cancel(true);
        return;
      }
      for (List<Object[]> list: chunks) {
        for (Object[] o: list) {
          model.addRow(o);
        }
      }
      int rowCount = model.getRowCount();
      maxPageIndex = rowCount / itemsPerPage + (rowCount % itemsPerPage == 0 ? 0 : 1);
      initFilterAndButtons();
    }

    @Override public void done() {
      if (!isDisplayable()) {
        System.out.println("done: DISPOSE_ON_CLOSE");
        cancel(true);
        return;
      }
      String text;
      try {
        text = get();
      } catch (InterruptedException | ExecutionException ex) {
        ex.printStackTrace();
        text = "Cancelled";
      }
      System.out.println(text);
      table.setEnabled(true);
      field.setEditable(true);
    }
  }

  protected final void initFilterAndButtons() {
    sorter.setRowFilter(new RowFilter<TableModel, Integer>() {
      @Override public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
        int ti = currentPageIndex - 1;
        int ei = entry.getIdentifier();
        return ti * itemsPerPage <= ei && ei < ti * itemsPerPage + itemsPerPage;
      }
    });
    first.setEnabled(currentPageIndex > 1);
    prev.setEnabled(currentPageIndex > 1);
    next.setEnabled(currentPageIndex < maxPageIndex);
    last.setEnabled(currentPageIndex < maxPageIndex);
    field.setText(Integer.toString(currentPageIndex));
    label.setText(String.format("/ %d", maxPageIndex));
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
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    // frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class LoadTask extends SwingWorker<String, List<Object[]>> {
  private final int max;
  private final int itemsPerPage;

  protected LoadTask(int max, int itemsPerPage) {
    super();
    this.max = max;
    this.itemsPerPage = itemsPerPage;
  }

  @Override public String doInBackground() {
    // File file = new File("C:/Users/(user)/AppData/Roaming/Mozilla/Firefox/Profiles/xxxxxxxx.default/places.sqlite");
    // String db = "jdbc:sqlite:/" + file.getAbsolutePath();
    // try (Connection conn = DriverManager.getConnection(db); Statement stat = conn.createStatement()) {
    int current = 1;
    int c = max / itemsPerPage;
    int i = 0;
    while (i < c && !isCancelled()) {
      try {
        Thread.sleep(500); // dummy
      } catch (InterruptedException ex) {
        // ex.printStackTrace();
        return "Interrupted";
      }
      current = makeRowListAndPublish(current, itemsPerPage);
      // current = load(stat, current, itemsPerPage);
      i++;
    }
    int surplus = max % itemsPerPage;
    if (surplus > 0) {
      makeRowListAndPublish(current, surplus);
      // load(stat, current, surplus);
    }
    // } catch (SQLException ex) {
    //   // ex.printStackTrace();
    //   return "Error";
    // }
    return "Done";
  }

  private int makeRowListAndPublish(int current, int size) {
    List<Object[]> result = IntStream.range(current, current + size)
        .mapToObj(i -> new Object[] {i, "Test: " + i, i % 2 == 0 ? "" : "comment..."})
        .collect(Collectors.toList());
    publish(result);
    return current + result.size();
  }
  // private int load(Statement stat, int current, int limit) throws SQLException {
  //   List<Object[]> result = new ArrayList<>(limit);
  //   String q = String.format("select * from moz_bookmarks limit %d offset %d", limit, current - 1);
  //   ResultSet rs = stat.executeQuery(q);
  //   int i = current;
  //   while (rs.next() && !isCancelled()) {
  //     result.add(new Object[] {i, rs.getInt("id"), rs.getString("title")});
  //     i++;
  //   }
  //   publish(result);
  //   return current + result.size();
  // }
}

class IntegerDocumentFilter extends DocumentFilter {
  // int currentValue = 0;
  @Override public void insertString(DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
    if (Objects.nonNull(text)) {
      replace(fb, offset, 0, text, attr);
    }
  }

  @Override public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException {
    replace(fb, offset, length, "", null);
  }

  @Override public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
    Document doc = fb.getDocument();
    int currentLength = doc.getLength();
    String currentContent = doc.getText(0, currentLength);
    String before = currentContent.substring(0, offset);
    String after = currentContent.substring(length + offset, currentLength);
    String newValue = before + Objects.toString(text, "") + after;
    checkInput(newValue, offset);
    fb.replace(offset, length, text, attrs);
  }

  private static int checkInput(String proposedValue, int offset) throws BadLocationException {
    if (proposedValue.isEmpty()) {
      return 0;
    } else {
      try {
        return Integer.parseInt(proposedValue);
      } catch (NumberFormatException ex) {
        throw (BadLocationException) new BadLocationException(proposedValue, offset).initCause(ex);
      }
    }
  }
}
