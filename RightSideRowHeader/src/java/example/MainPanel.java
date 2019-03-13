// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public final class MainPanel extends JPanel {
  public static final int FIXEDCOLUMN_RANGE = 2;
  private static final String ES = "";
  private final Object[][] data = {
    {1, 11, "A",  ES,  ES,  ES,  ES,  ES},
    {2, 22,  ES, "B",  ES,  ES,  ES,  ES},
    {3, 33,  ES,  ES, "C",  ES,  ES,  ES},
    {4,  1,  ES,  ES,  ES, "D",  ES,  ES},
    {5, 55,  ES,  ES,  ES,  ES, "E",  ES},
    {6, 66,  ES,  ES,  ES,  ES,  ES, "F"}
  };
  private final String[] columnNames = {"fixed 1", "fixed 2", "A", "B", "C", "D", "E", "F"};
  private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
    @Override public Class<?> getColumnClass(int column) {
      return column < FIXEDCOLUMN_RANGE ? Integer.class : Object.class;
    }
  };
  private final transient RowSorter<? extends TableModel> sorter = new TableRowSorter<>(model);
  private final JButton addButton = new JButton("add");

  private MainPanel() {
    super(new BorderLayout());
    JTable fixedTable = new JTable(model);
    JTable table = new JTable(model);
    fixedTable.setSelectionModel(table.getSelectionModel());

    for (int i = model.getColumnCount() - 1; i >= 0; i--) {
      if (i < FIXEDCOLUMN_RANGE) {
        table.removeColumn(table.getColumnModel().getColumn(i));
        fixedTable.getColumnModel().getColumn(i).setResizable(false);
      } else {
        fixedTable.removeColumn(fixedTable.getColumnModel().getColumn(i));
      }
    }

    fixedTable.setRowSorter(sorter);
    fixedTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    fixedTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    fixedTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    fixedTable.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.GRAY));
    fixedTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.GRAY));

    table.setRowSorter(sorter);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

    JScrollPane scroll = new JScrollPane(table);
    scroll.setLayout(new RightFixedScrollPaneLayout());

    fixedTable.setPreferredScrollableViewportSize(fixedTable.getPreferredSize());
    scroll.setRowHeaderView(fixedTable);

    scroll.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, fixedTable.getTableHeader());
    // TEST:
    // table.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    // // fixedTable.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    // scroll.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    // scroll.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, fixedTable.getTableHeader());

    scroll.getViewport().setBackground(Color.WHITE);
    scroll.getRowHeader().setBackground(Color.WHITE);

    scroll.getRowHeader().addChangeListener(e -> {
      JViewport viewport = (JViewport) e.getSource();
      scroll.getVerticalScrollBar().setValue(viewport.getViewPosition().y);
    });

    addButton.addActionListener(e -> {
      sorter.setSortKeys(null);
      IntStream.range(0, 100).forEach(i -> model.addRow(new Object[] {i, i + 1, "A" + i, "B" + i}));
    });

    add(scroll);
    add(addButton, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }
  // private MainPanel() {
  //   super(new BorderLayout());
  //
  //   JTable rightTable = makeTable(model);
  //   JTable leftTable = makeTable(model);
  //
  //   leftTable.setAutoCreateRowSorter(true);
  //   rightTable.setRowSorter(leftTable.getRowSorter());
  //   rightTable.setSelectionModel(leftTable.getSelectionModel());
  //
  //   for (int i = model.getColumnCount() - 1; i >= 0; i--) {
  //     if (i < FIXED_COLUMNRANGE) {
  //       leftTable.removeColumn(leftTable.getColumnModel().getColumn(i));
  //       rightTable.getColumnModel().getColumn(i).setResizable(false);
  //     } else {
  //       rightTable.removeColumn(rightTable.getColumnModel().getColumn(i));
  //     }
  //   }
  //
  //   JScrollPane leftScroll = new JScrollPane(leftTable);
  //   leftScroll.setVerticalScrollBar(new JScrollBar(Adjustable.VERTICAL) {
  //     @Override public Dimension getPreferredSize() {
  //       Dimension d = super.getPreferredSize();
  //       d.width = 0;
  //       return d;
  //     }
  //   });
  //
  //   JScrollPane rightScroll = new JScrollPane(rightTable);
  //   rightScroll.getVerticalScrollBar().setModel(leftScroll.getVerticalScrollBar().getModel());
  //
  //   JSplitPane split = new JSplitPane();
  //   split.setResizeWeight(.7);
  //   split.setLeftComponent(leftScroll);
  //   split.setRightComponent(rightScroll);
  //
  //   JButton button = new JButton("add");
  //   button.addActionListener(e -> {
  //     leftTable.getRowSorter().setSortKeys(null);
  //     IntStream.range(0, 100).forEach(i -> model.addRow(new Object[] {i, i + 1, "A" + i, "B" + i}));
  //   });
  //
  //   add(split);
  //   add(button, BorderLayout.SOUTH);
  //   setPreferredSize(new Dimension(320, 240));
  // }

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

class RightFixedScrollPaneLayout extends ScrollPaneLayout {
  @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity", "PMD.ExcessiveMethodLength", "checkstyle:methodlength"})
  @Override public void layoutContainer(Container parent) {
    if (!(parent instanceof JScrollPane)) {
      return;
    }
    JScrollPane scrollPane = (JScrollPane) parent;
    vsbPolicy = scrollPane.getVerticalScrollBarPolicy();
    hsbPolicy = scrollPane.getHorizontalScrollBarPolicy();

    Rectangle availR = scrollPane.getBounds();
    availR.x = 0;
    availR.y = 0;

    Insets insets = parent.getInsets();
    availR.x = insets.left;
    availR.y = insets.top;
    availR.width -= insets.left + insets.right;
    availR.height -= insets.top + insets.bottom;

    boolean leftToRight = true; // SwingUtilities.isLeftToRight(scrollPane);

    Rectangle colHeadR = new Rectangle(0, availR.y, 0, 0);

    if (Objects.nonNull(colHead) && colHead.isVisible()) {
      int colHeadHeight = Math.min(availR.height, colHead.getPreferredSize().height);
      colHeadR.height = colHeadHeight;
      availR.y += colHeadHeight;
      availR.height -= colHeadHeight;
    }

    Rectangle rowHeadR = new Rectangle(0, 0, 0, 0);
    if (Objects.nonNull(rowHead) && rowHead.isVisible()) {
      int rowHeadWidth = Math.min(availR.width, rowHead.getPreferredSize().width);
      rowHeadR.width = rowHeadWidth;
      availR.width -= rowHeadWidth;
      // if (leftToRight) {
      //   rowHeadR.x = availR.x;
      //   availR.x += rowHeadWidth;
      // } else {
      //   rowHeadR.x = availR.x + availR.width;
      // }
      rowHeadR.x = availR.x + availR.width;
    }

    Border viewportBorder = scrollPane.getViewportBorder();
    Insets vpbInsets;
    if (Objects.nonNull(viewportBorder)) {
      vpbInsets = viewportBorder.getBorderInsets(parent);
      availR.x += vpbInsets.left;
      availR.y += vpbInsets.top;
      availR.width -= vpbInsets.left + vpbInsets.right;
      availR.height -= vpbInsets.top + vpbInsets.bottom;
    } else {
      vpbInsets = new Insets(0, 0, 0, 0);
    }

    Component view = Objects.nonNull(viewport) ? viewport.getView() : null;
    Dimension viewPrefSize = Objects.nonNull(view) ? view.getPreferredSize() : new Dimension();
    Dimension extentSize = Objects.nonNull(viewport) ? viewport.toViewCoordinates(availR.getSize()) : new Dimension();

    boolean viewTracksViewportWidth = false;
    boolean viewTracksViewportHeight = false;
    boolean isEmpty = availR.width < 0 || availR.height < 0;
    Scrollable sv;
    if (!isEmpty && view instanceof Scrollable) {
      sv = (Scrollable) view;
      viewTracksViewportWidth = sv.getScrollableTracksViewportWidth();
      viewTracksViewportHeight = sv.getScrollableTracksViewportHeight();
    } else {
      sv = null;
    }

    Rectangle vsbR = new Rectangle(0, availR.y - vpbInsets.top, 0, 0);

    boolean vsbNeeded;
    if (vsbPolicy == VERTICAL_SCROLLBAR_ALWAYS) {
      vsbNeeded = true;
    } else if (vsbPolicy == VERTICAL_SCROLLBAR_NEVER) {
      vsbNeeded = false;
    } else { // vsbPolicy == VERTICAL_SCROLLBAR_AS_NEEDED
      vsbNeeded = !viewTracksViewportHeight && viewPrefSize.height > extentSize.height;
    }

    if (Objects.nonNull(vsb) && vsbNeeded) {
      adjustForVsb(true, rowHeadR, vsbR, vpbInsets, leftToRight);
      extentSize = viewport.toViewCoordinates(availR.getSize());
    }

    Rectangle hsbR = new Rectangle(availR.x - vpbInsets.left, 0, 0, 0);
    boolean hsbNeeded;
    if (hsbPolicy == HORIZONTAL_SCROLLBAR_ALWAYS) {
      hsbNeeded = true;
    } else if (hsbPolicy == HORIZONTAL_SCROLLBAR_NEVER) {
      hsbNeeded = false;
    } else { // hsbPolicy == HORIZONTAL_SCROLLBAR_AS_NEEDED
      hsbNeeded = !viewTracksViewportWidth && viewPrefSize.width > extentSize.width;
    }

    if (Objects.nonNull(hsb) && hsbNeeded) {
      adjustForHsb(true, availR, hsbR, vpbInsets);
      if (Objects.nonNull(vsb) && !vsbNeeded && vsbPolicy != VERTICAL_SCROLLBAR_NEVER) {

        extentSize = viewport.toViewCoordinates(availR.getSize());
        vsbNeeded = viewPrefSize.height > extentSize.height;

        if (vsbNeeded) {
          // adjustForVsb(true, availR, vsbR, vpbInsets, leftToRight);
          adjustForVsb(true, rowHeadR, vsbR, vpbInsets, leftToRight);
        }
      }
    }

    if (Objects.nonNull(viewport)) {
      viewport.setBounds(availR);

      if (Objects.nonNull(sv)) {
        extentSize = viewport.toViewCoordinates(availR.getSize());

        final boolean oldHsbNeeded = hsbNeeded;
        final boolean oldVsbNeeded = vsbNeeded;
        viewTracksViewportWidth = sv.getScrollableTracksViewportWidth();
        viewTracksViewportHeight = sv.getScrollableTracksViewportHeight();
        if (Objects.nonNull(vsb) && vsbPolicy == VERTICAL_SCROLLBAR_AS_NEEDED) {
          boolean newVsbNeeded = !viewTracksViewportHeight && viewPrefSize.height > extentSize.height;
          if (newVsbNeeded != vsbNeeded) {
            vsbNeeded = newVsbNeeded;
            // adjustForVsb(vsbNeeded, availR, vsbR, vpbInsets, leftToRight);
            adjustForVsb(vsbNeeded, rowHeadR, vsbR, vpbInsets, leftToRight);
            extentSize = viewport.toViewCoordinates(availR.getSize());
          }
        }
        if (Objects.nonNull(hsb) && hsbPolicy == HORIZONTAL_SCROLLBAR_AS_NEEDED) {
          boolean newHsbNeeded = !viewTracksViewportWidth && viewPrefSize.width > extentSize.width;
          if (newHsbNeeded != hsbNeeded) {
            hsbNeeded = newHsbNeeded;
            adjustForHsb(hsbNeeded, availR, hsbR, vpbInsets);
            if (Objects.nonNull(vsb) && !vsbNeeded && vsbPolicy != VERTICAL_SCROLLBAR_NEVER) {

              extentSize = viewport.toViewCoordinates(availR.getSize());
              vsbNeeded = viewPrefSize.height > extentSize.height;

              if (vsbNeeded) {
                // adjustForVsb(true, availR, vsbR, vpbInsets, leftToRight);
                adjustForVsb(true, rowHeadR, vsbR, vpbInsets, leftToRight);
              }
            }
          }
        }
        if (oldHsbNeeded != hsbNeeded || oldVsbNeeded != vsbNeeded) {
          viewport.setBounds(availR);
        }
      }
    }

    vsbR.height = availR.height + vpbInsets.top + vpbInsets.bottom;
    hsbR.width = availR.width + vpbInsets.left + vpbInsets.right;
    rowHeadR.height = availR.height + vpbInsets.top + vpbInsets.bottom;
    rowHeadR.y = availR.y - vpbInsets.top;
    colHeadR.width = availR.width + vpbInsets.left + vpbInsets.right;
    colHeadR.x = availR.x - vpbInsets.left;

    if (Objects.nonNull(rowHead)) {
      rowHead.setBounds(rowHeadR);
    }

    if (Objects.nonNull(colHead)) {
      colHead.setBounds(colHeadR);
    }

    if (Objects.nonNull(vsb)) {
      if (vsbNeeded) {
        // if (Objects.nonNull(colHead) && UIManager.getBoolean("ScrollPane.fillUpperCorner")) {
        //   if (leftToRight && Objects.isNull(upperRight) || !leftToRight && Objects.isNull(upperLeft)) {
        //     vsbR.y = colHeadR.y;
        //     vsbR.height += colHeadR.height;
        //   }
        // }
        vsb.setVisible(true);
        vsb.setBounds(vsbR);
      } else {
        vsb.setVisible(false);
      }
    }

    if (Objects.nonNull(hsb)) {
      if (hsbNeeded) {
        // if (Objects.nonNull(rowHead) && UIManager.getBoolean("ScrollPane.fillLowerCorner")) {
        //   if (leftToRight && Objects.isNull(lowerLeft) || !leftToRight && Objects.isNull(lowerRight)) {
        //     if (leftToRight) {
        //       hsbR.x = rowHeadR.x;
        //     }
        //     hsbR.width += rowHeadR.width;
        //   }
        // }
        hsb.setVisible(true);
        hsb.setBounds(hsbR);
      } else {
        hsb.setVisible(false);
      }
    }

    if (Objects.nonNull(lowerLeft)) {
      lowerLeft.setBounds(leftToRight ? rowHeadR.x : vsbR.x, hsbR.y,
                          leftToRight ? rowHeadR.width : vsbR.width, hsbR.height);
    }

    if (Objects.nonNull(lowerRight)) {
      lowerRight.setBounds(leftToRight ? vsbR.x : rowHeadR.x, hsbR.y,
                           leftToRight ? vsbR.width : rowHeadR.width, hsbR.height);
    }

    if (Objects.nonNull(upperLeft)) {
      upperLeft.setBounds(leftToRight ? rowHeadR.x : vsbR.x, colHeadR.y,
                          leftToRight ? rowHeadR.width : vsbR.width, colHeadR.height);
    }

    if (Objects.nonNull(upperRight)) {
      upperRight.setBounds(leftToRight ? vsbR.x : rowHeadR.x, colHeadR.y,
                           leftToRight ? vsbR.width : rowHeadR.width, colHeadR.height);
    }
  }

  private void adjustForVsb(boolean wantsVsb, Rectangle available, Rectangle vsbR, Insets vpbInsets, boolean ltr) {
    int oldWidth = vsbR.width;
    if (wantsVsb) {
      int vsbWidth = Math.max(0, Math.min(vsb.getPreferredSize().width, available.width));
      available.width -= vsbWidth;
      vsbR.width = vsbWidth;

      if (ltr) { // isLeftToRight
        vsbR.x = available.x + available.width + vpbInsets.right;
      } else {
        vsbR.x = available.x - vpbInsets.left;
        available.x += vsbWidth;
      }
    } else {
      available.width += oldWidth;
    }
  }

  private void adjustForHsb(boolean wantsHsb, Rectangle available, Rectangle hsbR, Insets vpbInsets) {
    int oldHeight = hsbR.height;
    if (wantsHsb) {
      int hsbHeight = Math.max(0, Math.min(available.height, hsb.getPreferredSize().height));
      available.height -= hsbHeight;
      hsbR.y = available.y + available.height + vpbInsets.bottom;
      hsbR.height = hsbHeight;
    } else {
      available.height += oldHeight;
    }
  }
}
