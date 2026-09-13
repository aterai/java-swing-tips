// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.text.ParseException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.MaskFormatter;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new FlowLayout(FlowLayout.LEFT, 8, 8));
    add(new JLabel("Select time:"));
    add(new TimePickerField());
    setPreferredSize(new Dimension(320, 240));
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

// TimePickerField – composite widget: masked text field + dropdown button
// HH:mm format (24 hours)
class TimePickerField extends JPanel {
  private final JFormattedTextField timeField;
  private final TimePickerPopup popup;

  protected TimePickerField() {
    super();
    setLayout(new OverlayLayout(this));

    JButton dropdownButton = new DropdownButton();
    dropdownButton.setAlignmentX(RIGHT_ALIGNMENT);
    dropdownButton.setAlignmentY(CENTER_ALIGNMENT);
    dropdownButton.addActionListener(e -> togglePopup());
    add(dropdownButton);

    popup = new TimePickerPopup(this);
    dropdownButton.setComponentPopupMenu(popup);

    MaskFormatter mask = TimePickerUtils.createMaskFormatter();
    timeField = mask != null
        ? new JFormattedTextField(mask)
        : new JFormattedTextField();
    timeField.setHorizontalAlignment(JTextField.LEFT);
    timeField.setFocusLostBehavior(JFormattedTextField.PERSIST);
    timeField.setText(TimePickerUtils.getNowString());
    timeField.setAlignmentX(RIGHT_ALIGNMENT);
    timeField.setColumns(10);
    add(timeField);
  }

  @Override public boolean isOptimizedDrawingEnabled() {
    return false;
  }

  @Override public boolean isOpaque() {
    return false;
  }

  @Override public final Component add(Component comp) {
    return super.add(comp);
  }

  @Override public final void setLayout(LayoutManager mgr) {
    super.setLayout(mgr);
  }

  // Getter method to retrieve the text from the text field
  public String getTimeText() {
    return timeField.getText();
  }

  public void applyTime(String text) {
    timeField.setText(text);
  }

  private void togglePopup() {
    if (popup.isVisible()) {
      popup.setVisible(false);
    } else {
      // Since the common synchronization is consolidated in the PopupMenuListener,
      // just show it at the specified position here
      popup.show(this, 0, getHeight());
    }
  }
}

// TimePickerPopup – JPopupMenu with hour / minute columns
class TimePickerPopup extends JPopupMenu {
  private final TimePickerField owner;
  private final TimePickerPopupPanel panel;
  private transient PopupMenuListener handler;

  protected TimePickerPopup(TimePickerField owner) {
    super();
    this.owner = owner;
    List<String> hourModel = IntStream.range(0, 24)
        .mapToObj(h -> String.format("%02d", h))
        .collect(Collectors.toList()); // Java 16: .toList();
    List<String> minModel = IntStream.range(0, 60)
        .mapToObj(m -> String.format("%02d", m))
        .collect(Collectors.toList()); // Java 16: .toList();
    panel = new TimePickerPopupPanel(owner, hourModel, minModel);
    add(panel);
  }

  @Override public void updateUI() {
    removePopupMenuListener(handler);
    super.updateUI();
    // Add a listener to monitor events right before the popup becomes visible
    handler = new TimePickerPopupListener();
    addPopupMenuListener(handler);
  }

  // Override the default placement position determined by ComponentPopupMenu
  // in environments like Windows LookAndFeel
  @Override public void show(Component invoker, int x, int y) {
    setInvoker(invoker);
    Point p = popupMenuLocation();
    if (p != null) {
      // Pass screen coordinates directly to setLocation
      setLocation(p.x, p.y);
      setVisible(true);
    } else {
      super.show(invoker, x, y);
    }
  }

  @Override public final void setLayout(LayoutManager mgr) {
    super.setLayout(mgr);
  }

  @Override public final Component add(Component comp) {
    return super.add(comp);
  }

  // Calculates the appropriate screen coordinates where the popup should be displayed.
  private Point popupMenuLocation() {
    Point p = null;
    Component invoker = getInvoker();
    if (invoker != null && invoker.isShowing()) {
      // Regardless of which component is the invoker,
      // always base it on the bottom-left edge of the TimePickerField
      p = owner.getLocationOnScreen();
      p.y += owner.getHeight();
    }
    return p;
  }

  private final class TimePickerPopupListener implements PopupMenuListener {
    @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
      // Always synchronize with the field time before showing
      panel.synchronizeFromField(owner.getTimeText());

      // Force correction of unexpected display position shifts
      // caused by right-clicks, etc.
      Point p = popupMenuLocation();
      if (p != null) {
        setInvoker(owner);
        setLocation(p.x, p.y);
      }
    }

    @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
      // No operation needed
    }

    @Override public void popupMenuCanceled(PopupMenuEvent e) {
      // No operation needed
    }
  }
}

/** Panel that contains the drum roll pickers and footer actions extracted from the popup. */
class TimePickerPopupPanel extends JPanel {
  private final TimePickerField owner;
  private final DrumRollPicker hourPicker;
  private final DrumRollPicker minPicker;

  protected TimePickerPopupPanel(
      TimePickerField owner, List<String> hourModel, List<String> minModel) {
    super(new BorderLayout(0, 0));
    this.owner = owner;

    hourPicker = new DrumRollPicker(hourModel);
    minPicker = new DrumRollPicker(minModel);

    JPanel pickers = new JPanel(new GridLayout(1, 2, 2, 2));
    pickers.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    Locale loc = Locale.getDefault();
    String hourName = ChronoField.HOUR_OF_DAY.getDisplayName(loc);
    pickers.add(createColumn(hourName, hourPicker));
    String minName = ChronoField.MINUTE_OF_HOUR.getDisplayName(loc);
    pickers.add(createColumn(minName, minPicker));

    add(pickers, BorderLayout.CENTER);
    add(buildFooter(), BorderLayout.SOUTH);
  }

  private static JPanel createColumn(String label, DrumRollPicker picker) {
    JLabel lbl = new JLabel(label, SwingConstants.CENTER);
    lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
    JPanel col = new JPanel(new BorderLayout(0, 1));
    col.setOpaque(false);
    col.add(lbl, BorderLayout.NORTH);
    col.add(picker);
    return col;
  }

  @Override public final void add(Component comp, Object constraints) {
    super.add(comp, constraints);
  }

  private JPanel buildFooter() {
    JButton resetBtn = new JButton("Now");
    resetBtn.addActionListener(e -> synchronizeFromField(TimePickerUtils.getNowString()));
    JButton okBtn = new JButton("OK");
    okBtn.addActionListener(e -> applyAndClose());
    JPanel footer = new JPanel(new FlowLayout(FlowLayout.TRAILING, 6, 1));
    footer.add(resetBtn);
    footer.add(okBtn);
    return footer;
  }

  public void synchronizeFromField(String text) {
    int[] t = TimePickerUtils.parseTime(text);
    int hour = t[0]; // 0..23
    int min = t[1]; // 0..59
    // DrumRollPicker#setSelectedIndex clamps or wraps the index by itself,
    // so no extra scrolling is required
    hourPicker.setSelectedIndex(hour);
    minPicker.setSelectedIndex(min);
  }

  private void applyAndClose() {
    String hour = hourPicker.getSelectedItem();
    String min = minPicker.getSelectedItem();
    owner.applyTime(hour + ":" + min);
    Container popup = SwingUtilities.getAncestorOfClass(JPopupMenu.class, this);
    if (popup instanceof JPopupMenu) {
      popup.setVisible(false);
    }
  }
}

// Remaining helper classes (unchanged)
class DropdownButton extends JButton {
  @Override public void updateUI() {
    super.updateUI();
    Color c1 = UIManager.getColor("ComboBox.foreground");
    Color c2 = UIManager.getColor("ComboBox.selectionBackground");
    setIcon(new CharIcon("⏰", c1, c2, 10));
    setBorderPainted(false);
    setContentAreaFilled(false);
    setFocusPainted(false);
    setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }
}

// Static utility methods shared by TimePickerField and TimePickerPopup
final class TimePickerUtils {
  private static final Pattern TIME_DELIMITER = Pattern.compile("[:\\s]+");

  private TimePickerUtils() {
    // utility
  }

  /** Returns current time as "HH:mm" (24-hour). */
  public static String getNowString() {
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
    return LocalTime.now(ZoneId.systemDefault()).format(fmt);
  }

  /** Parses "HH:mm" text into {hour, minute}. */
  public static int[] parseTime(String text) {
    // String[] parts = text.trim().split("[:\\s]+");
    String[] parts = TIME_DELIMITER.split(text.trim());
    // List<String> parts = Splitter.on(TIME_DELIMITER).splitToList(text.trim());
    int hour = Integer.parseInt(parts[0].trim());
    int min = Integer.parseInt(parts[1].trim());
    return new int[] {hour, min};
  }

  /** Creates a {@link MaskFormatter} for the "##:##" pattern. */
  public static MaskFormatter createMaskFormatter() {
    MaskFormatter mask = null;
    try {
      mask = new MaskFormatter("##:##");
      mask.setPlaceholderCharacter('_');
      mask.setCommitsOnValidEdit(false);
    } catch (ParseException ex) {
      Logger.getGlobal().severe(ex::getMessage);
    }
    return mask;
  }
}

class CharIcon implements Icon {
  private final String name;
  private final Color color;
  private final Color rollover;
  private final int size;

  protected CharIcon(String name, Color color, Color rollover, int size) {
    this.name = name;
    this.color = color;
    this.rollover = rollover;
    this.size = size;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(color);
    if (c instanceof AbstractButton) {
      ButtonModel m = ((AbstractButton) c).getModel();
      if (m.isRollover()) {
        g2.setPaint(rollover);
      }
    }
    FontMetrics fontMetrics = g2.getFontMetrics();
    g2.translate(x, y);
    int tx = (size - fontMetrics.stringWidth(name)) / 2;
    int ty = (size - fontMetrics.getHeight()) / 2 + fontMetrics.getAscent();
    g2.drawString(name, tx, ty);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return size;
  }

  @Override public int getIconHeight() {
    return size;
  }
}

/**
 * Custom component for selecting a value with a drum roll effect.
 * The value is changed by the mouse wheel, dragging, clicking a neighbor item,
 * or the up/down arrow keys. Every change except dragging is animated with a
 * cubic ease-out so the drum decelerates into the selection band.
 */
class DrumRollPicker extends JPanel {
  private static final int ITEM_HEIGHT = 26;
  private static final int VISIBLE_ROWS = 5; // odd number: center item + neighbors
  private static final int COLUMN_WIDTH = 24;
  // Number of items painted on each side of the center: one extra row for dragging
  private static final int PAINT_ROWS = VISIBLE_ROWS / 2 + 1;
  private static final float ALPHA_STEP = .35f; // alpha decrease per item
  private static final float MAX_SCALE = 1.5f; // font scale of the center item
  private static final float SCALE_STEP = .25f; // font scale decrease per item
  private static final float MIN_SCALE = .8f;
  private static final int BAND_ALPHA = 48;
  private static final int DRAG_THRESHOLD = 4;
  private static final int MAX_ALPHA = 255;
  // Upper bound of the animated distance: a fast wheel spin catches up instead of
  // sliding through every item
  private static final int MAX_ANIM_ROWS = 2;

  private final List<String> items;
  private int index;
  private int pressedY;
  private boolean dragging;
  private transient PickerHandler handler;
  private transient DrumRollScroller scroller;

  protected DrumRollPicker(List<String> items) {
    super();
    this.items = new ArrayList<>(items);
  }

  @Override public void updateUI() {
    removeMouseListener(handler);
    removeMouseMotionListener(handler);
    removeMouseWheelListener(handler);
    removeFocusListener(handler);
    if (scroller != null) {
      scroller.setOffset(0);
    }
    super.updateUI();
    setOpaque(true);
    setFocusable(true);
    setBackground(UIManager.getColor("List.background"));
    setForeground(UIManager.getColor("List.foreground"));
    setFont(UIManager.getFont("List.font"));
    handler = new PickerHandler();
    addMouseListener(handler);
    addMouseMotionListener(handler);
    addMouseWheelListener(handler);
    addFocusListener(handler);
    scroller = new DrumRollScroller(this);
    InputMap im = getInputMap(WHEN_FOCUSED);
    im.put(KeyStroke.getKeyStroke("UP"), "previous");
    im.put(KeyStroke.getKeyStroke("DOWN"), "next");
    ActionMap am = getActionMap();
    am.put("previous", new PickerAction(-1));
    am.put("next", new PickerAction(1));
  }

  @Override public Dimension getPreferredSize() {
    return isPreferredSizeSet()
        ? super.getPreferredSize()
        : new Dimension(COLUMN_WIDTH, ITEM_HEIGHT * VISIBLE_ROWS);
  }

  // Stop the timer when the popup is closed so it does not keep ticking while hidden.
  // Jump to the end of the animation: the index is already final, so only the
  // offset needs clearing to avoid reappearing with a stale drum position
  @Override public void removeNotify() {
    scroller.setOffset(0);
    super.removeNotify();
  }

  public String getSelectedItem() {
    return items.get(index);
  }

  public int getSelectedIndex() {
    return index;
  }

  /**
   * Selects the item at the given index without animation:
   * wraps around or clamps it if out of range.
   */
  public void setSelectedIndex(int idx) {
    index = isCyclic() ? Math.floorMod(idx, items.size())
        : Math.min(Math.max(idx, 0), items.size() - 1);
    // Java 21: index = isCyclic() ? ... : Math.clamp(idx, 0, items.size() - 1);
    // Cancels a running animation and puts the drum back on the item
    scroller.setOffset(0);
    repaint();
  }

  /**
   * Moves the selection by the given number of items with an easing animation.
   * The index is updated immediately and only the painting catches up,
   * so the selected value is always the final one even mid-animation.
   */
  public void scroll(int delta) {
    int prev = index;
    int offset = scroller.getOffset(); // offset of an animation still in flight
    setSelectedIndex(index + delta);
    // Clamped at both ends when the drum is not cyclic, so the drum may move less
    int moved = isCyclic() ? delta : index - prev;
    // Shift the drum back by the distance it just jumped, then slide that offset to zero
    int max = ITEM_HEIGHT * MAX_ANIM_ROWS;
    int from = offset + moved * ITEM_HEIGHT;
    scroller.start(Math.min(Math.max(from, -max), max));
    // Java 21: scroller.start(Math.clamp(from, -max, max));
  }

  // The drum rotates endlessly only when it has more items than the visible rows
  private boolean isCyclic() {
    return items.size() > VISIBLE_ROWS;
  }

  // Returns the item index for the given offset from the selected item, or -1 if it is empty
  private int itemIndexAt(int offset) {
    int idx = index + offset;
    if (isCyclic()) {
      idx = Math.floorMod(idx, items.size());
    } else if (idx < 0 || idx >= items.size()) {
      idx = -1;
    }
    return idx;
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    int w = getWidth();
    int h = getHeight();
    int centerY = h / 2;
    paintSelectionBand(g2, w, centerY);
    // While the drum is offset, the rows that scrolled in from the edge must be
    // painted too, otherwise a gap appears at the leading edge
    int offset = scroller.getOffset();
    int rows = PAINT_ROWS + (Math.abs(offset) + ITEM_HEIGHT - 1) / ITEM_HEIGHT;
    for (int i = -rows; i <= rows; i++) {
      int idx = itemIndexAt(i);
      if (idx >= 0) {
        paintItem(g2, items.get(idx), w, centerY + i * ITEM_HEIGHT + offset, centerY);
      }
    }
    paintFadeGradient(g2, w, h, centerY);
    g2.dispose();
  }

  // Highlight band that indicates the selected item
  private void paintSelectionBand(Graphics2D g2, int w, int centerY) {
    int y = centerY - ITEM_HEIGHT / 2;
    Color sc = UIManager.getColor("List.selectionBackground");
    Color bc = sc == null ? getForeground() : sc;
    g2.setPaint(new Color(bc.getRed(), bc.getGreen(), bc.getBlue(), BAND_ALPHA));
    g2.fillRect(0, y, w, ITEM_HEIGHT);
    g2.setPaint(hasFocus() ? bc : getForeground());
    g2.drawLine(0, y, w, y);
    g2.drawLine(0, y + ITEM_HEIGHT, w, y + ITEM_HEIGHT);
  }

  // The farther from the center, the smaller and more transparent the item becomes
  private void paintItem(Graphics2D g2, String text, int w, int y, int centerY) {
    float d = Math.abs(y - centerY) / (float) ITEM_HEIGHT;
    float alpha = Math.max(0f, 1f - d * ALPHA_STEP);
    if (alpha > 0) {
      float scale = Math.max(MIN_SCALE, MAX_SCALE - d * SCALE_STEP);
      Font font = getFont();
      int style = d < .5f ? Font.BOLD : Font.PLAIN;
      g2.setFont(font.deriveFont(style, font.getSize2D() * scale));
      Color fg = getForeground();
      g2.setColor(new Color(
          fg.getRed(), fg.getGreen(), fg.getBlue(), (int) (alpha * MAX_ALPHA)));
      FontMetrics fm = g2.getFontMetrics();
      int tx = (w - fm.stringWidth(text)) / 2;
      int ty = y + (fm.getAscent() - fm.getDescent()) / 2;
      g2.drawString(text, tx, ty);
    }
  }

  // Overlay gradient that fades out the top and bottom of the drum
  private void paintFadeGradient(Graphics2D g2, int w, int h, int centerY) {
    Color bg = getBackground();
    Color tc = new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 0);
    int fade = Math.max(0, centerY - ITEM_HEIGHT);
    g2.setPaint(new GradientPaint(0f, 0f, bg, 0f, fade, tc));
    g2.fillRect(0, 0, w, fade);
    g2.setPaint(new GradientPaint(0f, h, bg, 0f, h - fade, tc));
    g2.fillRect(0, h - fade, w, fade);
  }

  private final class PickerAction extends AbstractAction {
    private final int delta;

    private PickerAction(int delta) {
      super();
      this.delta = delta;
    }

    @Override public void actionPerformed(ActionEvent e) {
      scroll(delta);
    }
  }

  private final class PickerHandler extends MouseInputAdapter
      implements FocusListener {
    @Override public void mousePressed(MouseEvent e) {
      pressedY = e.getY();
      dragging = false;
      // Grabbing the drum takes over from a running animation:
      // freeze it where it is instead of snapping to the item
      scroller.stop();
      requestFocusInWindow();
    }

    @Override public void mouseDragged(MouseEvent e) {
      int delta = e.getY() - pressedY;
      if (Math.abs(delta) > DRAG_THRESHOLD) {
        dragging = true;
      }
      // Dragging downwards brings the previous items to the center
      int steps = delta / ITEM_HEIGHT;
      if (steps != 0) {
        pressedY += steps * ITEM_HEIGHT;
        delta -= steps * ITEM_HEIGHT;
        setSelectedIndex(index - steps);
      }
      // Stop the drum at both ends if it is not cyclic
      boolean top = delta > 0 && itemIndexAt(-1) < 0;
      boolean bottom = delta < 0 && itemIndexAt(1) < 0;
      scroller.setOffset(top || bottom ? 0 : delta);
      repaint();
    }

    @Override public void mouseReleased(MouseEvent e) {
      // Clicking a neighbor item moves it to the center: the item under the cursor is
      // shifted by the drum offset, so subtract it to find the row actually clicked
      float dy = e.getY() - getHeight() / 2f - scroller.getOffset();
      int rows = dragging ? 0 : Math.round(dy / ITEM_HEIGHT);
      dragging = false;
      // Also eases the leftover drag offset back to the center when rows is zero
      scroll(rows);
    }

    @Override public void mouseWheelMoved(MouseWheelEvent e) {
      scroll(e.getWheelRotation());
    }

    @Override public void focusGained(FocusEvent e) {
      repaint();
    }

    @Override public void focusLost(FocusEvent e) {
      repaint();
    }
  }
}

/**
 * Slides a pixel offset back to zero with a cubic ease-out.
 * The drum is painted at this offset, so the picker can update its selected
 * index immediately and let the drawing catch up afterwards.
 */
class DrumRollScroller {
  private static final long DURATION = 180_000_000L; // 180ms in nanoseconds
  private static final int FRAME_DELAY = 15; // interval between animation frames (~66fps)

  private final JComponent view;
  private final Timer animator;
  private int offset; // pixel offset of the drum: zero while it rests on an item
  private int from; // offset the current animation starts from
  private long startTime;

  protected DrumRollScroller(JComponent view) {
    this.view = view;
    this.animator = new Timer(FRAME_DELAY, e -> update());
  }

  public int getOffset() {
    return offset;
  }

  // Places the drum by hand while dragging: setOffset(0) also cancels an animation
  public void setOffset(int px) {
    animator.stop();
    offset = px;
  }

  // Interrupts an animation without moving the drum
  public void stop() {
    animator.stop();
  }

  // Slides the offset from the given value back to zero, redirecting a running animation
  public void start(int fromOffset) {
    from = fromOffset;
    if (from != 0) {
      offset = from;
      // nanoTime is monotonic and fine grained: currentTimeMillis has a granularity
      // close to FRAME_DELAY on some platforms, which makes the easing look stepped
      startTime = System.nanoTime();
      animator.restart();
      view.repaint();
    }
  }

  // Called on every timer tick: eases the offset toward zero and stops at the end
  private void update() {
    long elapsed = System.nanoTime() - startTime;
    if (elapsed >= DURATION) {
      setOffset(0);
    } else {
      float progress = elapsed / (float) DURATION;
      // easeOut is applied to the remaining distance, so the offset reaches zero
      offset = Math.round(from * (1f - easeOut(progress)));
    }
    view.repaint();
  }

  // Cubic ease-out: starts fast and gently settles into the selection band
  private static float easeOut(float progress) {
    float remaining = 1f - progress;
    return 1f - remaining * remaining * remaining;
  }
}
