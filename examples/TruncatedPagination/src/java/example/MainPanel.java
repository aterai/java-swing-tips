// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * Demo of a {@code JTable} with a compact pagination bar. Instead of listing every
 * page number, the bar always pins the first/last {@link #EDGE_PAGE_COUNT} pages and
 * a {@link #PAGE_WINDOW_DELTA}-page window around the current page, collapsing any
 * skipped run of pages into a single {@code "..."} label.
 */
public final class MainPanel extends JPanel {
  // Number of pages always pinned at the start and end (e.g. "1 2 ... 20 21").
  private static final int EDGE_PAGE_COUNT = 2;
  // Number of pages shown on each side of the current page (e.g. "10 11 (12) 13 14").
  private static final int PAGE_WINDOW_DELTA = 2;
  // Sentinel value used in the page-index list to mark where an ellipsis goes.
  private static final int ELLIPSIS = -1;
  // Page count at or below which no pagination bar is needed.
  private static final int SINGLE_PAGE_COUNT = 1;
  private final Box paginationBar = Box.createHorizontalBox();
  private final DefaultTableModel model = createTableModel();
  private final transient TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);

  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(model);
    table.setFillsViewportHeight(true);
    table.setIntercellSpacing(new Dimension());
    table.setShowGrid(false);
    table.putClientProperty("terminateEditOnFocusLost", true);
    table.setRowSorter(sorter);

    IntStream.rangeClosed(1, 2026)
        .mapToObj(i -> new Object[] {i, "Test: " + i, i % 2 == 0 ? "" : "comment..."})
        .forEach(model::addRow);

    showPage(100, 1);
    paginationBar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    add(paginationBar, BorderLayout.NORTH);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  /**
   * Creates an empty table model for this demo, with the first column ({@code Year})
   * sorted numerically rather than lexicographically.
   */
  public static DefaultTableModel createTableModel() {
    String[] columnNames = {"Year", "String", "Comment"};
    return new DefaultTableModel(columnNames, 0) {
      @Override public Class<?> getColumnClass(int column) {
        return column == 0 ? Integer.class : Object.class;
      }
    };
  }

  /**
   * Filters {@link #sorter} down to {@code currentPageIndex} and rebuilds
   * {@link #paginationBar} to match. Every pagination control invokes this method
   * with its own target page, so it doubles as the pagination bar's click handler.
   * Must be called on the EDT, since it mutates live Swing components.
   *
   * @param itemsPerPage     number of rows shown on a single page
   * @param currentPageIndex 1-based index of the page to display
   */
  @SuppressWarnings("ReturnCount")
  private void showPage(int itemsPerPage, int currentPageIndex) {
    // assert currentPageIndex > 0;
    sorter.setRowFilter(new RowFilter<TableModel, Integer>() {
      @Override public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
        int ti = currentPageIndex - 1;
        int ei = entry.getIdentifier();
        return ti * itemsPerPage <= ei && ei < ti * itemsPerPage + itemsPerPage;
      }
    });

    // "maxPageIndex" gives one blank page if the module of the division is not zero.
    //   pointed out by erServi
    // e.g. rowCount=100, maxPageIndex=100
    int rowCount = model.getRowCount();
    int extraPage = rowCount % itemsPerPage == 0 ? 0 : 1;
    int maxPageIndex = rowCount / itemsPerPage + extraPage;

    paginationBar.removeAll();
    if (maxPageIndex <= SINGLE_PAGE_COUNT) {
      // if I only have one page, Y don't want to see pagination buttons
      // suggested by erServi
      return;
    }

    boolean hasPreviousPage = currentPageIndex > 1;
    paginationBar.add(
        createPrevNextButton(itemsPerPage, currentPageIndex - 1, "<", hasPreviousPage));

    paginationBar.add(Box.createHorizontalGlue());
    ButtonGroup pageButtonGroup = new ButtonGroup();
    for (int pageIndex : createPageIndexList(currentPageIndex, maxPageIndex)) {
      if (pageIndex == ELLIPSIS) {
        paginationBar.add(createEllipsisLabel());
      } else {
        AbstractButton pageButton = createPageButton(
            itemsPerPage, currentPageIndex, pageIndex);
        paginationBar.add(pageButton);
        pageButtonGroup.add(pageButton);
      }
    }
    paginationBar.add(Box.createHorizontalGlue());

    boolean hasNextPage = currentPageIndex < maxPageIndex;
    paginationBar.add(
        createPrevNextButton(itemsPerPage, currentPageIndex + 1, ">", hasNextPage));
    paginationBar.revalidate();
    paginationBar.repaint();
  }

  /**
   * Builds the list of page numbers to render, pinning {@link #EDGE_PAGE_COUNT} pages
   * at each end and {@link #PAGE_WINDOW_DELTA} pages around {@code currentPageIndex},
   * collapsing any gap between those visible pages into a single
   * {@link #ELLIPSIS} entry.
   *
   * <p>e.g. {@code currentPageIndex=12, maxPageIndex=21} →
   * {@code 1 2 ... 10 11 12 13 14 ... 20 21}
   */
  private static List<Integer> createPageIndexList(int currentPageIndex, int maxPageIndex) {
    List<Integer> pageIndexList = new ArrayList<>();
    int previousIndex = 0;
    for (int i = 1; i <= maxPageIndex; i++) {
      boolean isVisible = i <= EDGE_PAGE_COUNT
          || i > maxPageIndex - EDGE_PAGE_COUNT
          || isWithinPageWindow(i, currentPageIndex);
      if (!isVisible) {
        continue;
      }
      if (previousIndex != 0 && i != previousIndex + 1) {
        pageIndexList.add(ELLIPSIS);
      }
      pageIndexList.add(i);
      previousIndex = i;
    }
    return pageIndexList;
  }

  // True if pageIndex is within PAGE_WINDOW_DELTA pages of currentPageIndex,
  // in either direction.
  private static boolean isWithinPageWindow(int pageIndex, int currentPageIndex) {
    return Math.abs(pageIndex - currentPageIndex) <= PAGE_WINDOW_DELTA;
  }

  // Non-interactive placeholder shown for a collapsed run of page numbers.
  private static JComponent createEllipsisLabel() {
    JLabel label = new JLabel("...");
    label.setForeground(Color.GRAY);
    label.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
    return label;
  }

  // Toggle button that jumps to targetPageIndex, selected when it is the current page.
  private AbstractButton createPageButton(
      int itemsPerPage, int currentPageIndex, int targetPageIndex) {
    AbstractButton button = new JToggleButton(Objects.toString(targetPageIndex)) {
      @Override public void updateUI() {
        super.updateUI();
        setMargin(new Insets(2, 4, 2, 4));
      }
    };
    if (targetPageIndex == currentPageIndex) {
      button.setSelected(true);
    }
    button.addActionListener(e -> showPage(itemsPerPage, targetPageIndex));
    return button;
  }

  // Plain button that jumps to targetPageIndex; disabled when enabled is false
  // (i.e. currentPageIndex is already at the first/last page).
  private AbstractButton createPrevNextButton(
      int itemsPerPage, int targetPageIndex, String label, boolean enabled) {
    AbstractButton button = new JButton(label) {
      @Override public void updateUI() {
        super.updateUI();
        setMargin(new Insets(2, 4, 2, 4));
        setFocusable(false);
      }
    };
    button.setEnabled(enabled);
    button.addActionListener(e -> showPage(itemsPerPage, targetPageIndex));
    return button;
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
