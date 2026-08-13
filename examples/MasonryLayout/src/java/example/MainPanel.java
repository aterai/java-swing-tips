// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javax.swing.*;

/**
 * Demo application for {@link MasonryLayout}.
 *
 * <p>Random height and color on masonry layout with 5px margin between cards
 * ({@link MasonryCard}) can be added and deleted. The close button is displayed only on
 * the card under the mouse cursor, and only clicking that button deletes the card. </p>
 *
 * <p>The "Auto-fill" checkbox allows for a fixed 3-column mode, and you can
 * switch the auto-fill mode where the number of columns is automatically calculated.
 * In auto-fill mode Minimum width ({@value #CARD_MIN_WIDTH}px) and maximum width
 * set for each card. The number of columns and column width are determined based
 * on ({@value #CARD_MAX_WIDTH}px). </p>
 */
public final class MainPanel extends JPanel {
  private static final int FIXED_COLUMNS = 3;
  private static final int GAP = 5;
  private static final int MIN_CARD_HEIGHT = 60;
  private static final int MAX_CARD_HEIGHT = 220;

  // Minimum width (in px) to prevent cards from being narrower in auto-fill mode.
  private static final int CARD_MIN_WIDTH = 140;
  // Maximum width (px) to prevent cards from being wider in auto-fill mode.
  private static final int CARD_MAX_WIDTH = 260;

  private static final float PASTEL_SATURATION = .45f;
  private static final float PASTEL_BRIGHTNESS = .85f;
  private static final int LUMINANCE_LIMIT = 150;
  private static final Random RANDOM = new Random();

  private final AtomicInteger nextNumber = new AtomicInteger(1);
  private final MasonryPanel masonryPanel;

  private MainPanel() {
    super(new BorderLayout());
    masonryPanel = new MasonryPanel(new MasonryLayout(FIXED_COLUMNS, GAP));
    add(createToolBar(), BorderLayout.NORTH);
    add(createScrollPane());
    for (int i = 0; i < 9; i++) {
      addCard();
    }
    setPreferredSize(new Dimension(320, 240));
  }

  private JToolBar createToolBar() {
    JButton addButton = new JButton("Add card");
    addButton.addActionListener(e -> addCard());

    JButton clearButton = new JButton("Delete all");
    clearButton.addActionListener(e -> clearCards());

    JCheckBox autoFillCheckBox = new JCheckBox("Auto-fill");
    autoFillCheckBox.addActionListener(e -> {
      boolean b = ((JCheckBox) e.getSource()).isSelected();
      int colCnt = b ? MasonryLayout.AUTO_FILL : FIXED_COLUMNS;
      masonryPanel.setLayout(new MasonryLayout(colCnt, GAP));
      masonryPanel.revalidate();
      masonryPanel.repaint();
    });

    JToolBar toolBar = new JToolBar();
    toolBar.add(addButton);
    toolBar.add(clearButton);
    toolBar.add(autoFillCheckBox);
    return toolBar;
  }

  private JScrollPane createScrollPane() {
    masonryPanel.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
    // The card itself has no mouse listener, so the mouse event is delivered
    // to this panel even if the mouse cursor is on the card.
    MouseAdapter hoverHandler = new MouseAdapter() {
      @Override public void mouseMoved(MouseEvent e) {
        setHoveredCard(masonryPanel.getComponentAt(e.getPoint()));
      }

      @Override public void mouseExited(MouseEvent e) {
        // Since mouseExited is also called when the mouse cursor moves to the
        // close button in the card, test whether it is out of the panel or not.
        if (!e.getComponent().contains(e.getPoint())) {
          setHoveredCard(null);
        }
      }
    };
    masonryPanel.addMouseListener(hoverHandler);
    masonryPanel.addMouseMotionListener(hoverHandler);

    JScrollPane scrollPane = new JScrollPane(masonryPanel);
    scrollPane.setBorder(null);
    scrollPane.getVerticalScrollBar().setUnitIncrement(GAP * 5);
    return scrollPane;
  }

  // Display the close button only on the card under the mouse cursor.
  private void setHoveredCard(Component hovered) {
    Arrays.stream(masonryPanel.getComponents())
        .filter(MasonryCard.class::isInstance)
        .map(MasonryCard.class::cast)
        .forEach(c -> c.setCloseButtonVisible(c.equals(hovered)));
  }

  private void addCard() {
    masonryPanel.add(createCard(nextNumber.getAndIncrement()));
    masonryPanel.revalidate();
    masonryPanel.repaint();
  }

  private void removeCard(Component card) {
    masonryPanel.remove(card);
    masonryPanel.revalidate();
    masonryPanel.repaint();
    // Update the hover state after the layout of the remaining cards is updated,
    // so that the close button appears on the card that has moved under the cursor.
    EventQueue.invokeLater(() -> {
      Point pt = masonryPanel.getMousePosition();
      setHoveredCard(pt == null ? null : masonryPanel.getComponentAt(pt));
    });
  }

  private void clearCards() {
    masonryPanel.removeAll();
    masonryPanel.revalidate();
    masonryPanel.repaint();
  }

  private MasonryCard createCard(int number) {
    MasonryCard card = new MasonryCard(Integer.toString(number));
    card.setFont(card.getFont().deriveFont(Font.BOLD, 18f));

    Color background = randomPastelColor();
    card.setBackground(background);
    card.setForeground(readableForeground(background));
    card.setBorder(BorderFactory.createLineBorder(background.darker()));

    int bound = MAX_CARD_HEIGHT - MIN_CARD_HEIGHT + 1;
    int height = MIN_CARD_HEIGHT + RANDOM.nextInt(bound);
    card.setPreferredSize(new Dimension(0, height));
    // The number of columns and column width calculation in Auto-fill mode
    // is performed by detecting this minimum/maximum width.
    card.setMinimumSize(new Dimension(CARD_MIN_WIDTH, height));
    card.setMaximumSize(new Dimension(CARD_MAX_WIDTH, Integer.MAX_VALUE));

    card.addCloseActionListener(e -> removeCard(card));
    return card;
  }

  private static Color randomPastelColor() {
    return Color.getHSBColor(RANDOM.nextFloat(), PASTEL_SATURATION, PASTEL_BRIGHTNESS);
  }

  private static Color readableForeground(Color background) {
    double luminance = 0.299 * background.getRed()
        + 0.587 * background.getGreen()
        + 0.114 * background.getBlue();
    return luminance > LUMINANCE_LIMIT ? Color.BLACK : Color.WHITE;
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

/**
 * Card that displays a close button at the top right only while hovering.
 *
 * <p>The close button is a child component of this {@link JLabel}, so the
 * position of the number text does not change even if it is shown or hidden.
 * Since this card itself does not have a {@code MouseListener}, the mouse event
 * is delivered to the parent {@link MasonryPanel} and hover detection is
 * performed collectively there. </p>
 */
final class MasonryCard extends JLabel {
  private static final int PADDING = 2;
  private final JButton closeButton = new JButton(new CloseIcon()) {
    @Override public void updateUI() {
      super.updateUI();
      setBorder(BorderFactory.createEmptyBorder());
      setBorderPainted(false);
      setContentAreaFilled(false);
      setFocusPainted(false);
      setFocusable(false);
      setToolTipText("Delete this card");
      setVisible(false);
    }
  };

  /* default */ MasonryCard(String title) {
    super(title, CENTER);
    setOpaque(true);
    setLayout(new FlowLayout(FlowLayout.RIGHT, PADDING, PADDING));
    add(closeButton);
  }

  @Override public void setForeground(Color fg) {
    super.setForeground(fg);
    // This method is also called from the JLabel constructor,
    // so a null check for the closeButton field is required.
    if (closeButton != null) {
      closeButton.setForeground(fg);
    }
  }

  public void setCloseButtonVisible(boolean flag) {
    closeButton.setVisible(flag);
  }

  public void addCloseActionListener(ActionListener listener) {
    closeButton.addActionListener(listener);
  }
}

/** Cross-shaped icon drawn with the {@link Component#getForeground()} of the button. */
class CloseIcon implements Icon {
  private static final int SIZE = 12;
  private static final int MARGIN = 3;

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.translate(x, y);
    g2.setPaint(c.getForeground());
    g2.setStroke(new BasicStroke(2f));
    int end = SIZE - MARGIN - 1;
    g2.drawLine(MARGIN, MARGIN, end, end);
    g2.drawLine(end, MARGIN, MARGIN, end);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return SIZE;
  }

  @Override public int getIconHeight() {
    return SIZE;
  }
}

/**
 * Content panel using {@link MasonryLayout}.
 *
 * <p>Implemented {@link Scrollable}, width is always {@link javax.swing.JViewport}
 * By making it follow ({@link #getScrollableTracksViewportWidth()} is {@code true}),
 * Recalculate column widths as window width changes. The height does not follow,
 * A vertical scroll bar is displayed depending on the total height of the content.
 */
class MasonryPanel extends JPanel implements Scrollable {
  private static final int UNIT_INCREMENT = 24;

  protected MasonryPanel(MasonryLayout layout) {
    super(layout);
  }

  @Override public Dimension getPreferredScrollableViewportSize() {
    return getPreferredSize();
  }

  @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
    return UNIT_INCREMENT;
  }

  @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
    return orientation == SwingConstants.VERTICAL ? visibleRect.height : visibleRect.width;
  }

  @Override public boolean getScrollableTracksViewportWidth() {
    return true;
  }

  @Override public boolean getScrollableTracksViewportHeight() {
    return false;
  }
}

/**
 * CSS {@code grid-template-columns: repeat(n, 1fr)} or
 * {@code repeat(auto-fill, minmax(min, max))} Masonry equivalent
 * {@link LayoutManager} that realizes a (masonry/Pinterest style) layout.
 *
 * <p>Each component is added to the bottom of the currently lowest column.
 * The width of the component is aligned to the column width, and the height
 * is The value of {@link Component#getPreferredSize()} is used as is.
 *
 * <h2>Fixed number of columns mode</h2>
 *
 * <p>If you specify 1 or more for the number of columns, it will always
 * be arranged with that number of columns.
 *
 * <p>This layout is immutable, so to switch modes, set a new instance with
 * {@link Container#setLayout(LayoutManager)}.
 *
 * <h2>Auto-fill mode</h2>
 *
 * <p>If you specify {@code 0} for the number of columns, it becomes Auto-fill mode.
 * In this mode Maximum width of {@link Component#getMinimumSize()} child component
 * (= width that no card should fall below) to the current width of the parent
 * container Automatically calculates the "maximum number of columns that can fit without
 * collapsing" during each layout. Additionally, {@link Component#getMaximumSize()} uses
 * the minimum width as the upper limit of the column width, If the equal division width
 * for the calculated number of columns exceeds that, the column width will be capped at
 * that upper limit. (The extra width remains as the right margin). This is CSS Grid Same
 * idea as {@code repeat(auto-fill, minmax(min, max))}.
 */
final class MasonryLayout implements LayoutManager {
  /** Column count value representing auto-fill mode. */
  public static final int AUTO_FILL = 0;

  /**
   * Temporary column width (px) to use when the width of the parent container
   * is not yet determined.
   */
  private static final int FALLBACK_WIDTH = 150;

  /**
   * In auto-fill mode, the expected number of columns used for provisional
   * calculation when the width is not determined.
   */
  private static final int FALLBACK_COLUMNS = 3;

  // Immutable so that the layout state is never rewritten
  // while the layout methods are reading it under the tree lock.
  private final int columnCount;
  private final int gap;

  /**
   * Creates a {@link MasonryLayout} with the specified number of columns and gap.
   *
   * @param columnCount Number of columns. 1 or more for fixed number of columns,
   *     {@link #AUTO_FILL} (0) for auto-fill mode
   * @param gap Space between cards (px, 0 or more). Use the same value for the top,
   *     bottom, left and right
   */
  /* default */ MasonryLayout(int columnCount, int gap) {
    if (columnCount < 0) {
      throw new IllegalArgumentException("columnCount must be >= 0: " + columnCount);
    }
    if (gap < 0) {
      throw new IllegalArgumentException("gap must be >= 0: " + gap);
    }
    this.columnCount = columnCount;
    this.gap = gap;
  }

  /**
   * Returns whether this layout is in Auto-fill mode, that is, the mode in which
   * the number of columns is determined dynamically.
   *
   * @return {@code true} if Auto-fill mode
   */
  public boolean isAutoFill() {
    return columnCount == AUTO_FILL;
  }

  @Override public void addLayoutComponent(String name, Component comp) {
    // Do nothing because add without constraints
  }

  @Override public void removeLayoutComponent(Component comp) {
    // Does nothing because no cache is maintained
  }

  @SuppressWarnings("PMD.AvoidSynchronizedStatement")
  @Override public Dimension preferredLayoutSize(Container parent) {
    synchronized (parent.getTreeLock()) {
      int[] columnHeights = placeComponents(parent, false);
      Insets insets = parent.getInsets();
      int maxHeight = 0;
      for (int height : columnHeights) {
        maxHeight = Math.max(maxHeight, height);
      }
      int width = resolveAvailableWidth(parent) + insets.left + insets.right;
      return new Dimension(width, maxHeight + insets.top + insets.bottom);
    }
  }

  // The card height is fixed regardless of the column width,
  // so the minimum size is the same as the preferred size.
  @Override public Dimension minimumLayoutSize(Container parent) {
    return preferredLayoutSize(parent);
  }

  @SuppressWarnings("PMD.AvoidSynchronizedStatement")
  @Override public void layoutContainer(Container parent) {
    synchronized (parent.getTreeLock()) {
      placeComponents(parent, true);
    }
  }

  /**
   * Determine the available layout width. If the parent container does not yet have a real
   * size (e.g., before {@code pack()}) uses temporary values based on the temporary
   * number of columns and column width. Do not call {@code parent.getPreferredSize()},
   * as this would result in a recursive call to the method itself.
   */
  private int resolveAvailableWidth(Container parent) {
    Insets insets = parent.getInsets();
    int actualWidth = parent.getWidth() - insets.left - insets.right;
    int assumedColumns = isAutoFill() ? FALLBACK_COLUMNS : columnCount;
    int assumedWidth = assumedColumns * FALLBACK_WIDTH + gap * (assumedColumns - 1);
    return actualWidth > 0 ? actualWidth : assumedWidth;
  }

  /**
   * Determine the number of columns to use at the current width. In fixed number
   * of columns mode, always returns that value. In Auto-fill mode,
   * the {@code getMinimumSize()} width of visible child components Based on the maximum
   * value, find the maximum number of columns that does not exceed {@code availableWidth}
   * even if you include margins. If there are no children, there will be one column.
   */
  private int resolveColumnCount(Container parent, int availableWidth) {
    int columns = columnCount;
    if (isAutoFill()) {
      // The minimum width is at least 1px to avoid division by zero.
      int minWidth = 1;
      boolean hasVisible = false;
      for (Component comp : parent.getComponents()) {
        if (comp.isVisible()) {
          hasVisible = true;
          minWidth = Math.max(minWidth, comp.getMinimumSize().width);
        }
      }
      columns = hasVisible ? (availableWidth + gap) / (minWidth + gap) : 1;
    }
    return Math.max(1, columns);
  }

  /**
   * Returns the minimum {@code getMaximumSize()} width of visible child components.
   * Do not make the column width larger than this (upper limit in Auto-fill mode).
   */
  private int resolveMaxColumnWidth(Container parent) {
    int maxWidth = Integer.MAX_VALUE;
    for (Component comp : parent.getComponents()) {
      if (!comp.isVisible()) {
        continue;
      }
      maxWidth = Math.min(maxWidth, comp.getMaximumSize().width);
    }
    return maxWidth;
  }

  /**
   * Calculate masonry placement. Only if {@code apply} is {@code true}, Actually
   * set {@code bounds} of the component.
   *
   * @return Height of each column after placement (value excluding extra gap at the end)
   */
  private int[] placeComponents(Container parent, boolean apply) {
    Insets insets = parent.getInsets();
    int availableWidth = resolveAvailableWidth(parent);
    int resolvedColCnt = resolveColumnCount(parent, availableWidth);
    int cw = (availableWidth - gap * (resolvedColCnt - 1)) / resolvedColCnt;
    int columnWidth = Math.max(0, cw);
    if (isAutoFill()) {
      // If the equal division width exceeds the maximum width of each card,
      // it will be capped at that upper limit.
      // (The excess amount is not used and remains as a margin on the right side.)
      columnWidth = Math.min(columnWidth, resolveMaxColumnWidth(parent));
    }

    int[] columnHeights = new int[resolvedColCnt];

    for (Component comp : parent.getComponents()) {
      if (!comp.isVisible()) {
        continue;
      }
      int col = shortestColumn(columnHeights);
      int x = insets.left + col * (columnWidth + gap);
      int y = insets.top + columnHeights[col];
      int height = comp.getPreferredSize().height;

      if (apply) {
        comp.setBounds(x, y, columnWidth, height);
      }
      columnHeights[col] += height + gap;
    }

    for (int i = 0; i < columnHeights.length; i++) {
      if (columnHeights[i] > 0) {
        columnHeights[i] -= gap;
      }
    }
    return columnHeights;
  }

  private int shortestColumn(int... columnHeights) {
    int minIndex = 0;
    for (int i = 1; i < columnHeights.length; i++) {
      if (columnHeights[i] < columnHeights[minIndex]) {
        minIndex = i;
      }
    }
    return minIndex;
  }
}
