// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.swing.*;

public final class MainPanel extends JPanel {
  // private static final int COLUMN = 4;
  // private static final int ROW = 5;
  // private static final List<Set<Integer>> NUMBERS = Arrays.asList(
  //     immutableSetOf(0, 1, 2, 3, 4, 5, 9, 10, 14, 15, 16, 17, 18, 19), // 0
  //     immutableSetOf(15, 16, 17, 18, 19), // 1
  //     immutableSetOf(0, 2, 3, 4, 5, 7, 9, 10, 12, 14, 15, 16, 17, 19), // 2
  //     immutableSetOf(0, 2, 4, 5, 7, 9, 10, 12, 14, 15, 16, 17, 18, 19), // 3
  //     immutableSetOf(0, 1, 2, 7, 12, 15, 16, 17, 18, 19), // 4
  //     immutableSetOf(0, 1, 2, 4, 5, 7, 9, 10, 12, 14, 15, 17, 18, 19), // 5
  //     immutableSetOf(0, 1, 2, 3, 4, 5, 7, 9, 10, 12, 14, 15, 17, 18, 19), // 6
  //     immutableSetOf(0, 1, 2, 5, 10, 15, 16, 17, 18, 19), // 7
  //     immutableSetOf(0, 1, 2, 3, 4, 5, 7, 9, 10, 12, 14, 15, 16, 17, 18, 19), // 8
  //     immutableSetOf(0, 1, 2, 4, 5, 7, 9, 10, 12, 14, 15, 16, 17, 18, 19)); // 9
  // private static final List<Integer> DOT = Arrays.asList(1, 3);
  private static final int COLUMN = 4;
  private static final int ROW = 7;
  private static final List<Set<Integer>> NUMBERS = Arrays.asList(
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
  // private static final int COLUMN = 5;
  // private static final int ROW = 7;
  // private static final List<Set<Integer>> NUMBERS = Arrays.asList(
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
  private static final List<Integer> DOT = Arrays.asList(2, 4);
  private transient HierarchyListener listener;
  private final Timer timer = new Timer(100, null);
  private LocalTime time = LocalTime.now(ZoneId.systemDefault());

  private MainPanel() {
    super(new GridBagLayout());
    DefaultListModel<Boolean> model1 = new DefaultListModel<Boolean>() {
      @Override public Boolean getElementAt(int index) {
        return getHoursMinutesDotMatrix(time, index);
      }
    };
    model1.setSize((COLUMN * 4 + 5) * ROW);
    JList<Boolean> hoursMinutes = makeLedDotMatrixList(model1, new Dimension(10, 10));

    DefaultListModel<Boolean> model2 = new DefaultListModel<Boolean>() {
      @Override public Boolean getElementAt(int index) {
        return getSecondsDotMatrix(time, index);
      }
    };
    model2.setSize((COLUMN * 2 + 1) * ROW);
    JList<Boolean> seconds = makeLedDotMatrixList(model2, new Dimension(8, 8));

    timer.addActionListener(e -> {
      time = LocalTime.now(ZoneId.systemDefault());
      hoursMinutes.repaint();
      seconds.repaint();
    });

    hoursMinutes.setAlignmentY(BOTTOM_ALIGNMENT);
    seconds.setAlignmentY(BOTTOM_ALIGNMENT);
    Box box = Box.createHorizontalBox();
    box.add(hoursMinutes);
    box.add(Box.createHorizontalStrut(10));
    box.add(seconds);
    add(box);
    setBackground(Color.BLACK);
    setPreferredSize(new Dimension(320, 240));
  }

  private static boolean contains(int index, int start, int end, int num) {
    return index < end * ROW && NUMBERS.get(num).contains(index - start * ROW);
  }

  @SuppressWarnings("PMD.OnlyOneReturn")
  private static boolean getHoursMinutesDotMatrix(LocalTime time, int index) {
    int ten = 10;
    int hours = time.getHour();
    int h1 = hours / ten;
    int start = 0;
    int end = start + COLUMN;
    if (contains(index, start, end, h1)) {
      return hours >= ten;
    }
    int gap = 1;
    int h2 = hours - h1 * ten;
    start = end + gap;
    end = start + COLUMN;
    if (contains(index, start, end, h2)) {
      return true;
    }
    int seconds = time.getSecond();
    int s1 = seconds / ten;
    int s2 = seconds - s1 * ten;
    start = end + gap;
    end = start + gap;
    if (index < end * ROW && s2 % 2 == 0 && DOT.contains(index - start * ROW)) {
      return true;
    }
    int minutes = time.getMinute();
    int m1 = minutes / ten;
    start = end + gap;
    end = start + COLUMN;
    if (contains(index, start, end, m1)) {
      return true;
    }
    int m2 = minutes - m1 * ten;
    start = end + gap;
    end = start + COLUMN;
    return contains(index, start, end, m2);
  }

  @SuppressWarnings("PMD.OnlyOneReturn")
  private static boolean getSecondsDotMatrix(LocalTime time, int index) {
    int ten = 10;
    int seconds = time.getSecond();
    int s1 = seconds / ten;
    int start = 0;
    int end = start + COLUMN;
    if (contains(index, start, end, s1)) {
      return true;
    }
    int gap = 1;
    int s2 = seconds - s1 * ten;
    start = end + gap;
    end = start + COLUMN;
    return contains(index, start, end, s2);
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

  private static JList<Boolean> makeLedDotMatrixList(ListModel<Boolean> model, Dimension dim) {
    return new JList<Boolean>(model) {
      @Override public void updateUI() {
        setFixedCellWidth(dim.width);
        setFixedCellHeight(dim.height);
        setVisibleRowCount(ROW);
        setCellRenderer(null);
        super.updateUI();
        setLayoutOrientation(VERTICAL_WRAP);
        setFocusable(false);
        ListCellRenderer<? super Boolean> renderer = getCellRenderer();
        Icon on = new LedDotIcon(true, dim);
        Icon off = new LedDotIcon(false, dim);
        setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
          Component c = renderer.getListCellRendererComponent(
              list, null, index, false, false);
          if (c instanceof JLabel) {
            ((JLabel) c).setIcon(Objects.equals(Boolean.TRUE, value) ? on : off);
          }
          return c;
        });
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

class LedDotIcon implements Icon {
  private final Color on = new Color(0x32_FF_AA);
  private final boolean led;
  private final Dimension dim;

  protected LedDotIcon(boolean led, Dimension dim) {
    this.led = led;
    this.dim = dim;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    // JList#setLayoutOrientation(VERTICAL_WRAP) + SynthLookAndFeel(Nimbus, GTK) bug???
    // g2.translate(x, y);
    g2.setPaint(led ? on : c.getBackground());
    g2.fillOval(0, 0, getIconWidth() - 1, getIconHeight() - 1);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return dim.width;
  }

  @Override public int getIconHeight() {
    return dim.height;
  }
}
