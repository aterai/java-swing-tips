// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  public static final int FIXED_RANGE = 2;
  private static final String ES = "";
  private static final Border BORDER = BorderFactory.createMatteBorder(0, 1, 0, 0, Color.GRAY);

  private MainPanel() {
    super(new BorderLayout());
    // RowSorter<? extends TableModel> sorter = new TableRowSorter<>(model);
    JTable table = new JTable(makeModel());
    table.setAutoCreateRowSorter(true);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

    JTable fixedTable = new JTable(table.getModel());
    fixedTable.setSelectionModel(table.getSelectionModel());
    fixedTable.setRowSorter(table.getRowSorter());
    fixedTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    fixedTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    fixedTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    fixedTable.setBorder(BORDER);
    fixedTable.getTableHeader().setBorder(BORDER);
    for (int i = table.getModel().getColumnCount() - 1; i >= 0; i--) {
      if (i < FIXED_RANGE) {
        table.removeColumn(table.getColumnModel().getColumn(i));
        fixedTable.getColumnModel().getColumn(i).setResizable(false);
      } else {
        fixedTable.removeColumn(fixedTable.getColumnModel().getColumn(i));
      }
    }
    fixedTable.setPreferredScrollableViewportSize(fixedTable.getPreferredSize());

    JScrollPane scroll = new JScrollPane(table);
    scroll.setLayout(new RightFixedScrollPaneLayout());
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

    JButton addButton = new JButton("add");
    addButton.addActionListener(e -> {
      table.getRowSorter().setSortKeys(null);
      DefaultTableModel m = (DefaultTableModel) table.getModel();
      IntStream.range(0, 100)
          .mapToObj(i -> new Object[] {i, i + 1, "A" + i, "B" + i})
          .forEach(m::addRow);
    });

    add(scroll);
    add(addButton, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    Object[][] data = {
        {1, 11, "A", ES, ES, ES, ES, ES},
        {2, 22, ES, "B", ES, ES, ES, ES},
        {3, 33, ES, ES, "C", ES, ES, ES},
        {4, 1, ES, ES, ES, "D", ES, ES},
        {5, 55, ES, ES, ES, ES, "E", ES},
        {6, 66, ES, ES, ES, ES, ES, "F"}
    };
    String[] columnNames = {"fixed 1", "fixed 2", "A", "B", "C", "D", "E", "F"};
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return column < FIXED_RANGE ? Integer.class : Object.class;
      }
    };
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
  //     if (i < FIXED_RANGE) {
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
  //     Object[] o = {i, i + 1, "A" + i, "B" + i};
  //     IntStream.range(0, 100).forEach(i -> model.addRow(o));
  //   });
  //
  //   add(split);
  //   add(button, BorderLayout.SOUTH);
  //   setPreferredSize(new Dimension(320, 240));
  // }

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

@SuppressWarnings("PMD.GodClass")
class RightFixedScrollPaneLayout extends ScrollPaneLayout {
  @SuppressWarnings({
      "PMD.CyclomaticComplexity",
      "PMD.NPathComplexity",
      "PMD.NcssCount",
      "PMD.CognitiveComplexity",
      "CyclomaticComplexity",
      "NPathComplexity",
      "MethodLength",
      "JavaNCSS"
  })
  @Override public void layoutContainer(Container parent) {
    if (!(parent instanceof JScrollPane)) {
      return;
    }
    JScrollPane scrollPane = (JScrollPane) parent;
    vsbPolicy = scrollPane.getVerticalScrollBarPolicy();
    hsbPolicy = scrollPane.getHorizontalScrollBarPolicy();
    Rectangle availR = SwingUtilities.calculateInnerArea(scrollPane, null);

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

    Component view = Optional.ofNullable(viewport)
        .map(JViewport::getView)
        .orElse(null);
    Dimension viewPrefSize = Optional.ofNullable(view)
        .map(Component::getPreferredSize)
        .orElseGet(Dimension::new);
    Dimension extentSize = Optional.ofNullable(viewport)
        .map(v -> v.toViewCoordinates(availR.getSize()))
        .orElseGet(Dimension::new);

    boolean scrollableWidth = false;
    boolean scrollableHeight = false;
    boolean isEmpty = availR.width < 0 || availR.height < 0;
    Scrollable sv = null;
    if (!isEmpty && view instanceof Scrollable) {
      sv = (Scrollable) view;
      scrollableWidth = sv.getScrollableTracksViewportWidth();
      scrollableHeight = sv.getScrollableTracksViewportHeight();
    }

    Rectangle vsbR = new Rectangle(0, availR.y - vpbInsets.top, 0, 0);

    boolean vsbNeeded;
    if (vsbPolicy == VERTICAL_SCROLLBAR_ALWAYS) {
      vsbNeeded = true;
    } else if (vsbPolicy == VERTICAL_SCROLLBAR_NEVER) {
      vsbNeeded = false;
    } else { // vsbPolicy == VERTICAL_SCROLLBAR_AS_NEEDED
      vsbNeeded = !scrollableHeight && viewPrefSize.height > extentSize.height;
    }

    boolean leftToRight = scrollPane.getComponentOrientation().isLeftToRight();
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
      hsbNeeded = !scrollableWidth && viewPrefSize.width > extentSize.width;
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
        scrollableWidth = sv.getScrollableTracksViewportWidth();
        scrollableHeight = sv.getScrollableTracksViewportHeight();
        if (Objects.nonNull(vsb) && vsbPolicy == VERTICAL_SCROLLBAR_AS_NEEDED) {
          boolean newVsbNeeded = !scrollableHeight && viewPrefSize.height > extentSize.height;
          if (newVsbNeeded != vsbNeeded) {
            vsbNeeded = newVsbNeeded;
            // adjustForVsb(vsbNeeded, availR, vsbR, vpbInsets, leftToRight);
            adjustForVsb(vsbNeeded, rowHeadR, vsbR, vpbInsets, leftToRight);
            extentSize = viewport.toViewCoordinates(availR.getSize());
          }
        }
        if (Objects.nonNull(hsb) && hsbPolicy == HORIZONTAL_SCROLLBAR_AS_NEEDED) {
          boolean newHsbNeeded = !scrollableWidth && viewPrefSize.width > extentSize.width;
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

    Optional.ofNullable(rowHead).ifPresent(rh -> rh.setBounds(rowHeadR));

    Optional.ofNullable(colHead).ifPresent(ch -> ch.setBounds(colHeadR));

    if (Objects.nonNull(vsb)) {
      if (vsbNeeded) {
        // if (Objects.nonNull(colHead) && UIManager.getBoolean("ScrollPane.fillUpperCorner")) {
        //   if (leftToRight && upperRight == null || !leftToRight && upperLeft == null) {
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
        //   if (leftToRight && lowerLeft == null || !leftToRight && lowerRight == null) {
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

    if (leftToRight) {
      setLtrCorner(colHeadR, rowHeadR, vsbR, hsbR);
    } else {
      setRtlCorner(colHeadR, rowHeadR, vsbR, hsbR);
    }
  }

  private void setLtrCorner(
      Rectangle colHeadR, Rectangle rowHeadR, Rectangle vsbR, Rectangle hsbR) {
    Optional.ofNullable(lowerLeft)
        .ifPresent(c -> c.setBounds(rowHeadR.x, hsbR.y, rowHeadR.width, hsbR.height));
    Optional.ofNullable(lowerRight)
        .ifPresent(c -> c.setBounds(vsbR.x, hsbR.y, vsbR.width, hsbR.height));
    Optional.ofNullable(upperLeft)
        .ifPresent(c -> c.setBounds(rowHeadR.x, colHeadR.y, rowHeadR.width, colHeadR.height));
    Optional.ofNullable(upperRight)
        .ifPresent(c -> c.setBounds(vsbR.x, colHeadR.y, vsbR.width, colHeadR.height));
  }

  private void setRtlCorner(
      Rectangle colHeadR, Rectangle rowHeadR, Rectangle vsbR, Rectangle hsbR) {
    Optional.ofNullable(lowerLeft)
        .ifPresent(c -> c.setBounds(vsbR.x, hsbR.y, vsbR.width, hsbR.height));
    Optional.ofNullable(lowerRight)
        .ifPresent(c -> c.setBounds(rowHeadR.x, hsbR.y, rowHeadR.width, hsbR.height));
    Optional.ofNullable(upperLeft)
        .ifPresent(c -> c.setBounds(vsbR.x, colHeadR.y, vsbR.width, colHeadR.height));
    Optional.ofNullable(upperRight)
        .ifPresent(c -> c.setBounds(rowHeadR.x, colHeadR.y, rowHeadR.width, colHeadR.height));
  }

  private void adjustForVsb(
      boolean wantsVsb, Rectangle avr, Rectangle vsbR, Insets vpbIns, boolean ltr) {
    int oldWidth = vsbR.width;
    if (wantsVsb) {
      int vsbWidth = Math.max(0, Math.min(vsb.getPreferredSize().width, avr.width));
      avr.width -= vsbWidth;
      vsbR.width = vsbWidth;

      if (ltr) { // isLeftToRight
        vsbR.x = avr.x + avr.width + vpbIns.right;
      } else {
        vsbR.x = avr.x - vpbIns.left;
        avr.x += vsbWidth;
      }
    } else {
      avr.width += oldWidth;
    }
  }

  private void adjustForHsb(
      boolean wantsHsb, Rectangle available, Rectangle hsbR, Insets vpbInsets) {
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
