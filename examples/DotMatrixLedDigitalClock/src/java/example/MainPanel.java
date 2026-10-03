// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntPredicate;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  // private static final int DIGIT_COLUMNS = 4;
  // private static final int DIGIT_ROWS = 5;
  // private static final List<Set<Integer>> DIGIT_PATTERNS = Arrays.asList(
  //     Set.of(0, 1, 2, 3, 4, 5, 9, 10, 14, 15, 16, 17, 18, 19), // 0
  //     Set.of(15, 16, 17, 18, 19), // 1
  //     Set.of(0, 2, 3, 4, 5, 7, 9, 10, 12, 14, 15, 16, 17, 19), // 2
  //     Set.of(0, 2, 4, 5, 7, 9, 10, 12, 14, 15, 16, 17, 18, 19), // 3
  //     Set.of(0, 1, 2, 7, 12, 15, 16, 17, 18, 19), // 4
  //     Set.of(0, 1, 2, 4, 5, 7, 9, 10, 12, 14, 15, 17, 18, 19), // 5
  //     Set.of(0, 1, 2, 3, 4, 5, 7, 9, 10, 12, 14, 15, 17, 18, 19), // 6
  //     Set.of(0, 1, 2, 5, 10, 15, 16, 17, 18, 19), // 7
  //     Set.of(0, 1, 2, 3, 4, 5, 7, 9, 10, 12, 14, 15, 16, 17, 18, 19), // 8
  //     Set.of(0, 1, 2, 4, 5, 7, 9, 10, 12, 14, 15, 16, 17, 18, 19)); // 9
  // private static final List<Integer> COLON_DOT_ROWS = Arrays.asList(1, 3);
  private static final int DIGIT_COLUMNS = 4;
  private static final int DIGIT_ROWS = 7;
  // Each digit is a DIGIT_COLUMNS x DIGIT_ROWS dot matrix laid out column-major,
  // i.e. the cell at (column, row) has index (column * DIGIT_ROWS + row). The set
  // below lists the indices of the dots that must be lit to draw that digit's glyph.
  private static final List<Set<Integer>> DIGIT_PATTERNS = Arrays.asList(
      immutableSetOf(0, 1, 2, 3, 4, 5, 6, 7, 13, 14, 20, 21, 22, 23, 24, 25, 26, 27), // 0
      immutableSetOf(21, 22, 23, 24, 25, 26, 27), // 1
      immutableSetOf(0, 3, 4, 5, 6, 7, 10, 13, 14, 17, 20, 21, 22, 23, 24, 27), // 2
      immutableSetOf(0, 3, 6, 7, 10, 13, 14, 17, 20, 21, 22, 23, 24, 25, 26, 27), // 3
      immutableSetOf(0, 1, 2, 3, 10, 17, 21, 22, 23, 24, 25, 26, 27), // 4
      immutableSetOf(0, 1, 2, 3, 6, 7, 10, 13, 14, 17, 20, 21, 24, 25, 26, 27), // 5
      immutableSetOf(0, 1, 2, 3, 4, 5, 6, 7, 10, 13, 14, 17, 20, 21, 24, 25, 26, 27), // 6
      immutableSetOf(0, 1, 2, 3, 7, 14, 21, 22, 23, 24, 25, 26, 27), // 7
      immutableSetOf(0, 1, 2, 3, 4, 5, 6, 7, 10, 13, 14, 17, 20, 21, 22, 23, 24, 25, 26, 27), // 8
      immutableSetOf(0, 1, 2, 3, 6, 7, 10, 13, 14, 17, 20, 21, 22, 23, 24, 25, 26, 27)); // 9
  // private static final int DIGIT_COLUMNS = 5;
  // private static final int DIGIT_ROWS = 7;
  // private static final List<Set<Integer>> DIGIT_PATTERNS = Arrays.asList(
  //     Set.of(1, 2, 3, 4, 5, 7, 9, 13, 14, 17, 20, 21, 25, 27, 29, 30, 31, 32, 33), // 0
  //     Set.of(8, 13, 14, 15, 16, 17, 18, 19, 20, 27), // 1
  //     Set.of(1, 6, 7, 12, 13, 14, 18, 20, 21, 24, 27, 29, 30, 34), // 2
  //     Set.of(0, 5, 7, 13, 14, 17, 20, 21, 23, 24, 27, 28, 29, 32, 33), // 3
  //     Set.of(3, 4, 9, 11, 15, 18, 21, 22, 23, 24, 25, 26, 27, 32), // 4
  //     Set.of(0, 1, 2, 5, 7, 9, 13, 14, 16, 20, 21, 23, 27, 28, 31, 32, 33), // 5
  //     Set.of(1, 2, 3, 4, 5, 7, 10, 13, 14, 17, 20, 21, 24, 27, 29, 32, 33), // 6
  //     Set.of(0, 7, 11, 12, 13, 14, 17, 21, 23, 28, 29), // 7
  //     Set.of(1, 2, 4, 5, 7, 10, 13, 14, 17, 20, 21, 24, 27, 29, 30, 32, 33), // 8
  //     Set.of(1, 2, 5, 7, 10, 13, 14, 17, 20, 21, 24, 27, 29, 30, 31, 32, 33)); // 9
  private static final List<Integer> COLON_DOT_ROWS = Arrays.asList(2, 4);
  private static final int COLON_COLUMNS = 1;
  private static final int RADIX = 10;
  private static final int BLOCK_GAP = 1;
  // H H : M M -> 4 digits, 1 colon and 4 gaps between the blocks
  private static final int HH_MM_COLUMNS =
      DIGIT_COLUMNS * 4 + COLON_COLUMNS + BLOCK_GAP * 4;
  // S S -> 2 digits and 1 gap
  private static final int SECONDS_COLUMNS = DIGIT_COLUMNS * 2 + BLOCK_GAP;
  private static final int TIMER_DELAY_MS = 100;
  private static final int LIST_GAP = 10;
  private static final Dimension HH_MM_DOT_SIZE = new Dimension(10, 10);
  private static final Dimension SECONDS_DOT_SIZE = new Dimension(8, 8);
  private transient HierarchyListener listener;
  private final Timer timer = new Timer(TIMER_DELAY_MS, null);
  private LocalTime time = now();

  private MainPanel() {
    super(new GridBagLayout());
    JList<Boolean> hoursMinutesList = createLedDotMatrixList(
        createDotMatrixModel(HH_MM_COLUMNS, i -> isHoursMinutesDotLit(time, i)),
        HH_MM_DOT_SIZE);
    JList<Boolean> secondsList = createLedDotMatrixList(
        createDotMatrixModel(SECONDS_COLUMNS, i -> isSecondsDotLit(time, i)),
        SECONDS_DOT_SIZE);

    timer.addActionListener(e -> {
      // The display only changes once per second, so skip redundant repaints.
      LocalTime current = now();
      if (!current.equals(time)) {
        time = current;
        hoursMinutesList.repaint();
        secondsList.repaint();
      }
    });

    hoursMinutesList.setAlignmentY(BOTTOM_ALIGNMENT);
    secondsList.setAlignmentY(BOTTOM_ALIGNMENT);
    Box box = Box.createHorizontalBox();
    box.add(hoursMinutesList);
    box.add(Box.createHorizontalStrut(LIST_GAP));
    box.add(secondsList);
    add(box);
    setBackground(Color.BLACK);
    setPreferredSize(new Dimension(320, 240));
  }

  private static LocalTime now() {
    return LocalTime.now(ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
  }

  private static ListModel<Boolean> createDotMatrixModel(int columns, IntPredicate isLit) {
    return new AbstractListModel<Boolean>() {
      @Override public int getSize() {
        return columns * DIGIT_ROWS;
      }

      @Override public Boolean getElementAt(int index) {
        return isLit.test(index);
      }
    };
  }

  // Every value in DIGIT_PATTERNS is within [0, DIGIT_COLUMNS * DIGIT_ROWS), so the
  // index relative to a block that starts at another column is either negative or
  // too large and simply misses the set: no bounds check is needed.
  private static boolean isDigitDotLit(int index, int startColumn, int digit) {
    return DIGIT_PATTERNS.get(digit).contains(index - startColumn * DIGIT_ROWS);
  }

  private static boolean isHoursMinutesDotLit(LocalTime time, int index) {
    int hour = time.getHour();
    int column = 0;
    // Blank the hour's leading zero: the tens digit only lights up when hour >= 10.
    boolean lit = hour >= RADIX && isDigitDotLit(index, column, hour / RADIX);

    column += DIGIT_COLUMNS + BLOCK_GAP;
    lit |= isDigitDotLit(index, column, hour % RADIX);

    // Blink the colon dots once per second, on for even seconds and off for odd seconds.
    column += DIGIT_COLUMNS + BLOCK_GAP;
    lit |= time.getSecond() % 2 == 0
        && COLON_DOT_ROWS.contains(index - column * DIGIT_ROWS);

    int minute = time.getMinute();
    column += COLON_COLUMNS + BLOCK_GAP;
    lit |= isDigitDotLit(index, column, minute / RADIX);

    column += DIGIT_COLUMNS + BLOCK_GAP;
    return lit || isDigitDotLit(index, column, minute % RADIX);
  }

  private static boolean isSecondsDotLit(LocalTime time, int index) {
    int second = time.getSecond();
    return isDigitDotLit(index, 0, second / RADIX)
        || isDigitDotLit(index, DIGIT_COLUMNS + BLOCK_GAP, second % RADIX);
  }

  @Override public void updateUI() {
    removeHierarchyListener(listener);
    super.updateUI();
    listener = e -> {
      if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
        if (e.getComponent().isShowing()) {
          timer.start();
        } else {
          timer.stop();
        }
      }
    };
    addHierarchyListener(listener);
  }

  private static Set<Integer> immutableSetOf(Integer... input) {
    return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(input)));
    // Java 9: return Set.of(input);
  }

  private static JList<Boolean> createLedDotMatrixList(
      ListModel<Boolean> model, Dimension size) {
    return new JList<Boolean>(model) {
      @Override public void updateUI() {
        setFixedCellWidth(size.width);
        setFixedCellHeight(size.height);
        setVisibleRowCount(DIGIT_ROWS);
        setCellRenderer(null);
        super.updateUI();
        setLayoutOrientation(VERTICAL_WRAP);
        setFocusable(false);
        setCellRenderer(new LedListCellRenderer(getCellRenderer(), size));
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        setBackground(Color.BLACK);
      }
    };
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

class LedListCellRenderer implements ListCellRenderer<Boolean> {
  private final ListCellRenderer<? super Boolean> renderer;
  private final Icon onIcon;
  private final Icon offIcon;

  protected LedListCellRenderer(
      ListCellRenderer<? super Boolean> renderer, Dimension size) {
    this.renderer = renderer;
    this.onIcon = new LedDotIcon(true, size);
    this.offIcon = new LedDotIcon(false, size);
  }

  @Override public Component getListCellRendererComponent(JList<? extends Boolean> list, Boolean value, int index, boolean isSelected, boolean cellHasFocus) {
    Component c = renderer.getListCellRendererComponent(
        list, null, index, false, false);
    if (c instanceof JLabel) {
      ((JLabel) c).setIcon(Boolean.TRUE.equals(value) ? onIcon : offIcon);
    }
    return c;
  }
}

class LedDotIcon implements Icon {
  private static final Color ON_COLOR = new Color(0x32_FF_AA);
  private final boolean lit;
  private final Dimension size;

  protected LedDotIcon(boolean lit, Dimension size) {
    this.lit = lit;
    this.size = size;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    // JList#setLayoutOrientation(VERTICAL_WRAP) + SynthLookAndFeel(Nimbus, GTK) bug???
    // g2.translate(x, y);
    g2.setPaint(lit ? ON_COLOR : c.getBackground());
    g2.fillOval(0, 0, getIconWidth() - 1, getIconHeight() - 1);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return size.width;
  }

  @Override public int getIconHeight() {
    return size.height;
  }
}
