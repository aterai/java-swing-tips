// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.DefaultFormatter;
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

/**
 * Dropdown time picker.
 * hh:mm aa format (12 hours + AM/PM)
 */
class TimePickerField extends JPanel {
  private static final Pattern TIME_DELIMITER = Pattern.compile("[:\\s]+");
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

    MaskFormatter mask;
    try {
      mask = new MaskFormatter("##:## **");
      mask.setPlaceholderCharacter('_');
      mask.setCommitsOnValidEdit(false);
    } catch (ParseException ex) {
      Logger.getGlobal().severe(ex::getMessage);
    }

    timeField = new JFormattedTextField(createFormatter());
    timeField.setHorizontalAlignment(JTextField.LEFT);
    timeField.setFocusLostBehavior(JFormattedTextField.PERSIST);
    timeField.setText(getNowString());
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

  private void togglePopup() {
    if (popup.isVisible()) {
      popup.setVisible(false);
    } else {
      // Since the common synchronization is consolidated in the PopupMenuListener,
      // just show it at the specified position here
      popup.show(this, 0, getHeight());
    }
  }

  public void applyTime(String text) {
    timeField.setText(text);
  }

  private static JFormattedTextField.AbstractFormatter createFormatter() {
    DefaultFormatter formatter = new DefaultFormatter();
    formatter.setOverwriteMode(true);
    formatter.setAllowsInvalid(true);
    formatter.setCommitsOnValidEdit(false);
    return formatter;
  }

  public static String getNowString() {
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
    return LocalTime.now(ZoneId.systemDefault()).format(fmt).toUpperCase(Locale.ENGLISH);
  }

  // Parsing hours, minutes, and ampm from the text "hh:mm aa"
  // text example: "03:45 PM"
  public static int[] parseTime(String text) {
    // String[] parts = text.trim().split("[:\\s]+");
    String[] parts = TIME_DELIMITER.split(text.trim());
    int hour = Integer.parseInt(parts[0].trim());
    int min = Integer.parseInt(parts[1].trim());
    // ampm index: 0=AM,1=PM
    int ampmIndex = parts.length > 2 && "PM".equalsIgnoreCase(parts[2].trim()) ? 1 : 0;
    return new int[] {hour, min, ampmIndex};
  }

  // Get AM/PM locale display name
  public static String[] getAmPmStrings() {
    DateFormatSymbols dfs = DateFormatSymbols.getInstance();
    return dfs.getAmPmStrings();
  }
}

class TimePickerPopup extends JPopupMenu {
  private final TimePickerField owner;
  private final JList<String> hourList;
  private final JList<String> minList;
  private final JList<String> ampmList;
  private final List<String> hourModel;
  private final List<String> minModel;
  private transient PopupMenuListener handler;

  protected TimePickerPopup(TimePickerField owner) {
    super();
    this.owner = owner;
    hourModel = IntStream.rangeClosed(1, 12)
        .mapToObj(h -> String.format("%02d", h))
        .collect(Collectors.toList()); // Java 16: .toList();
    minModel = IntStream.range(0, 60)
        .mapToObj(m -> String.format("%02d", m))
        .collect(Collectors.toList()); // Java 16: .toList();

    hourList = createList(hourModel.toArray(new String[0]));
    minList = createList(minModel.toArray(new String[0]));
    ampmList = createList(TimePickerField.getAmPmStrings());

    JPanel listsPanel = new JPanel(new GridBagLayout());
    listsPanel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1d;
    c.weighty = 1d;
    c.insets = new Insets(0, 2, 0, 2);

    c.gridx = GridBagConstraints.RELATIVE;
    Locale loc = Locale.getDefault();
    String hourLabel = ChronoField.HOUR_OF_DAY.getDisplayName(loc);
    listsPanel.add(createColumn(hourLabel, hourList, true), c);

    String minLabel = ChronoField.MINUTE_OF_HOUR.getDisplayName(loc);
    listsPanel.add(createColumn(minLabel, minList, true), c);

    String ampmLabel = getAmpmLabel(loc);
    listsPanel.add(createColumn(ampmLabel, ampmList, false), c);

    JPanel root = new JPanel(new BorderLayout(0, 0));
    root.add(listsPanel, BorderLayout.CENTER);
    root.add(createFooter(), BorderLayout.SOUTH);

    setLayout(new BorderLayout());
    add(root);
  }

  @Override public void updateUI() {
    removePopupMenuListener(handler);
    super.updateUI();
    // Add a listener to monitor events right before the popup becomes visible
    handler = new TimePickerPopupListener();
    addPopupMenuListener(handler);
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

  // Fallback: Synthesize from DateFormatSymbols in "AM/PM" format
  private static String getAmpmLabel(Locale loc) {
    String ampmRaw = ChronoField.AMPM_OF_DAY.getDisplayName(loc);
    String ampmLabel;
    boolean b1 = !Objects.equals(ampmRaw, "AmPmOfDay");
    boolean b2 = !Objects.equals(ampmRaw, "AMPM_OF_DAY");
    if (ampmRaw != null && !ampmRaw.isEmpty() && b1 && b2) {
      ampmLabel = ampmRaw;
    } else {
      String[] ap2 = DateFormatSymbols.getInstance(loc).getAmPmStrings();
      ampmLabel = ap2[0] + "/" + ap2[1];
    }
    return ampmLabel;
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    d.width = 220;
    return d;
  }

  @Override public final void setLayout(LayoutManager mgr) {
    super.setLayout(mgr);
  }

  @Override public final Component add(Component comp) {
    return super.add(comp);
  }

  private JList<String> createList(String... model) {
    JList<String> list = new JList<>(model);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.setFixedCellHeight(20);
    list.setFocusable(true);
    list.setCellRenderer(new DefaultListCellRenderer() {
      @Override public Component getListCellRendererComponent(JList<?> l, Object val, int idx, boolean sel, boolean focus) {
        super.getListCellRendererComponent(l, val, idx, sel, focus);
        setHorizontalAlignment(CENTER);
        setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
        return this;
      }
    });
    return list;
  }

  private JPanel createColumn(String label, JList<String> list, boolean alwaysScroll) {
    JLabel lbl = new JLabel(label, SwingConstants.CENTER);
    lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
    Component sp;
    if (alwaysScroll) {
      sp = new TranslucentScrollPane(list);
    } else {
      sp = new JScrollPane(list) {
        @Override public void updateUI() {
          super.updateUI();
          setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        }
      };
    }
    JPanel col = new JPanel(new BorderLayout(0, 1));
    col.setOpaque(false);
    col.add(lbl, BorderLayout.NORTH);
    col.add(sp);
    return col;
  }

  private JPanel createFooter() {
    JButton resetBtn = new JButton("Now");
    resetBtn.addActionListener(e -> synchronizeFromField(TimePickerField.getNowString()));
    JButton okBtn = new JButton("OK");
    okBtn.addActionListener(e -> applyAndClose());
    JPanel footer = new JPanel(new FlowLayout(FlowLayout.TRAILING, 6, 1));
    footer.add(resetBtn);
    footer.add(okBtn);
    return footer;
  }

  public void synchronizeFromField(String text) {
    int[] t = TimePickerField.parseTime(text);
    int hour = t[0]; // 1..12
    int min = t[1]; // 0..59
    int ampm = t[2]; // 0=AM, 1=PM
    // hour: 01..12（index 0..11）
    int hourIndex = Math.min(Math.max(hour - 1, 0), 11);
    // Java 21: int hourIndex = Math.clamp(hour - 1, 0, 11);
    hourList.setSelectedIndex(hourIndex);
    minList.setSelectedIndex(min);
    ampmList.setSelectedIndex(ampm);
    EventQueue.invokeLater(() -> {
      scrollToSelected(hourList);
      scrollToSelected(minList);
      scrollToSelected(ampmList);
    });
  }

  private void scrollToSelected(JList<?> list) {
    int idx = list.getSelectedIndex();
    if (idx >= 0) {
      // Center selected item as much as possible
      Rectangle cell = list.getCellBounds(idx, idx);
      if (cell != null) {
        Rectangle vis = list.getVisibleRect();
        vis.y = Math.max(0, cell.y + cell.height / 2 - vis.height / 2);
        list.scrollRectToVisible(vis);
      }
    }
  }

  private void applyAndClose() {
    int hourIndex = hourList.getSelectedIndex();
    int minuteIndex = minList.getSelectedIndex();
    int ampmIndex = ampmList.getSelectedIndex();
    String hour = hourIndex >= 0 ? hourModel.get(hourIndex) : "12";
    String min = minuteIndex >= 0 ? minModel.get(minuteIndex) : "00";
    String[] ampmStrings = TimePickerField.getAmPmStrings();
    String ampm = ampmIndex == 1 ? ampmStrings[1] : ampmStrings[0];
    owner.applyTime(hour + ":" + min + " " + ampm.toUpperCase(Locale.ENGLISH));
    setVisible(false);
  }

  private final class TimePickerPopupListener implements PopupMenuListener {
    @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
      // Always synchronize with the field time before showing
      synchronizeFromField(owner.getTimeText());

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

class TranslucentScrollPane extends JScrollPane {
  protected TranslucentScrollPane(Component view) {
    super(view);
  }

  @Override public boolean isOptimizedDrawingEnabled() {
    return false; // JScrollBar is overlap
  }

  @Override public void updateUI() {
    super.updateUI();
    EventQueue.invokeLater(() -> {
      getVerticalScrollBar().setUI(new TranslucentScrollBarUI());
      setComponentZOrder(getVerticalScrollBar(), 0);
      setComponentZOrder(getViewport(), 1);
      getVerticalScrollBar().setOpaque(false);
      // Dimension d = new Dimension(TranslucentScrollBarUI.MAX_WIDTH, 0);
      // getVerticalScrollBar().setPreferredSize(d);
    });
    setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
    setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
    setLayout(new TranslucentScrollPaneLayout());
  }
}

class TranslucentScrollPaneLayout extends ScrollPaneLayout {
  @Override public void layoutContainer(Container parent) {
    if (parent instanceof JScrollPane) {
      JScrollPane scrollPane = (JScrollPane) parent;
      Rectangle availR = SwingUtilities.calculateInnerArea(scrollPane, null);
      if (Objects.nonNull(viewport)) {
        viewport.setBounds(availR);
      }
      if (Objects.nonNull(vsb)) {
        int w = TranslucentScrollBarUI.BAR_WIDTH;
        vsb.setLocation(availR.x + availR.width - w, availR.y);
        vsb.setSize(w, availR.height);
      }
    }
  }
}

class InvisibleButton extends JButton {
  private static final Dimension ZERO_SIZE = new Dimension();

  @Override public Dimension getPreferredSize() {
    return ZERO_SIZE;
  }
}

class TranslucentScrollBarUI extends BasicScrollBarUI {
  public static final int BAR_WIDTH = 8;
  private static final Color DEFAULT_COLOR = new Color(0x64_64_B4_FF, true);
  private static final Color DRAGGING_COLOR = new Color(0x64_64_B4_C8, true);
  private static final Color ROLLOVER_COLOR = new Color(0x64_64_B4_DC, true);

  @Override protected JButton createDecreaseButton(int orientation) {
    return new InvisibleButton();
  }

  @Override protected JButton createIncreaseButton(int orientation) {
    return new InvisibleButton();
  }

  @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
    // BasicScrollBarUI overrides
    // g.fillRect(r.x, r.y, r.width - 1, r.height - 1);
  }

  @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
    if (c != null && c.isEnabled() && r.width <= r.height) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(
          RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      Color color = getThumbColor();
      if (Objects.equals(DEFAULT_COLOR, color)) {
        int dw = r.width - c.getPreferredSize().width;
        r.x += dw;
        r.width -= dw;
      }
      g2.setPaint(color);
      g2.fillRect(r.x, r.y, r.width - 2, r.height - 1);
      g2.dispose();
    }
  }

  private Color getThumbColor() {
    Color color;
    if (isDragging) {
      color = DRAGGING_COLOR;
    } else if (isThumbRollover()) {
      color = ROLLOVER_COLOR;
    } else {
      color = DEFAULT_COLOR;
    }
    return color;
  }
}
