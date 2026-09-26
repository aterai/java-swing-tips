// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.synth.SynthUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new HeaderCheckBoxTable(createModel());
    table.setFillsViewportHeight(true);
    add(new JScrollPane(table));

    JMenuBar menuBar = new JMenuBar();
    menuBar.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(menuBar));

    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel createModel() {
    Object[] columnNames = {Status.INDETERMINATE, "Integer", "String"};
    Object[][] data = {
        {true, 1, "BBB"}, {false, 12, "AAA"}, {true, 2, "DDD"}, {false, 5, "CCC"},
        {true, 3, "EEE"}, {false, 6, "GGG"}, {true, 4, "FFF"}, {false, 7, "HHH"},
    };
    return new DefaultTableModel(data, columnNames) {
      private final Class<?>[] columnClasses = {Boolean.class, Integer.class, String.class};

      @Override public Class<?> getColumnClass(int column) {
        // getValueAt(0, column) throws an exception if the model has no rows
        return columnClasses[column];
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

class HeaderCheckBoxTable extends JTable {
  private static final int CHECKBOX_COLUMN = 0;
  private transient HeaderCheckBoxHandler handler;

  protected HeaderCheckBoxTable(TableModel model) {
    super(model);
  }

  @Override public void updateUI() {
    // Changing to Nimbus LAF and back doesn't reset look and feel of JTable completely
    // https://bugs.openjdk.org/browse/JDK-6788475
    // Set a temporary ColorUIResource to avoid this issue
    setSelectionForeground(new ColorUIResource(Color.RED));
    setSelectionBackground(new ColorUIResource(Color.RED));
    getTableHeader().removeMouseListener(handler);
    TableModel m = getModel();
    if (Objects.nonNull(m)) {
      m.removeTableModelListener(handler);
    }
    super.updateUI();

    m = getModel();
    for (int i = 0; i < m.getColumnCount(); i++) {
      TableCellRenderer r = getDefaultRenderer(m.getColumnClass(i));
      if (r instanceof Component) {
        SwingUtilities.updateComponentTreeUI((Component) r);
      }
    }
    int vci = convertColumnIndexToView(CHECKBOX_COLUMN);
    getColumnModel().getColumn(vci).setHeaderRenderer(new HeaderRenderer());

    handler = new HeaderCheckBoxHandler(this, CHECKBOX_COLUMN);
    handler.updateHeaderState();
    m.addTableModelListener(handler);
    getTableHeader().addMouseListener(handler);
  }

  @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
    Component c = super.prepareEditor(editor, row, column);
    if (c instanceof JCheckBox) {
      JCheckBox b = (JCheckBox) c;
      b.setBackground(getSelectionBackground());
      b.setBorderPainted(true);
    }
    return c;
  }
}

class HeaderRenderer implements TableCellRenderer {
  private final JCheckBox check = new JCheckBox();
  private final JLabel label = new JLabel("Check All");
  private final Icon icon = new ComponentIcon(label);

  protected HeaderRenderer() {
    check.setOpaque(false);
    label.setOpaque(false);
    label.setIcon(new ComponentIcon(check));
    if (isSynth()) {
      check.setText(" ");
    }
  }

  private boolean isSynth() {
    return check.getUI() instanceof SynthUI;
  }

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Status status = value instanceof Status ? (Status) value : Status.INDETERMINATE;
    status.configureHeaderCheckBox(check);
    TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
    Component c = r.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);
    if (c instanceof JLabel) {
      JLabel l = (JLabel) c;
      l.setOpaque(false);
      if (isSynth()) {
        check.setPreferredSize(l.getPreferredSize());
      }
      l.setIcon(icon);
      l.setText(null);
    }
    return c;
  }
}

class HeaderCheckBoxHandler extends MouseAdapter implements TableModelListener {
  private final JTable table;
  private final int targetColumnIndex;

  protected HeaderCheckBoxHandler(JTable table, int index) {
    super();
    this.table = table;
    this.targetColumnIndex = index;
  }

  @Override public void tableChanged(TableModelEvent e) {
    int col = e.getColumn();
    boolean targetChanged = col == targetColumnIndex || col == TableModelEvent.ALL_COLUMNS;
    if (targetChanged && e.getFirstRow() != TableModelEvent.HEADER_ROW) {
      updateHeaderState();
    }
  }

  /* default */ void updateHeaderState() {
    int vci = table.convertColumnIndexToView(targetColumnIndex);
    if (vci >= 0) {
      setHeaderStatus(vci, resolveHeaderState(table.getModel()));
    }
  }

  private void setHeaderStatus(int vci, Status status) {
    TableColumn column = table.getColumnModel().getColumn(vci);
    if (column.getHeaderValue() != status) {
      column.setHeaderValue(status);
      JTableHeader h = table.getTableHeader();
      h.repaint(h.getHeaderRect(vci));
    }
  }

  private Status resolveHeaderState(TableModel model) {
    List<Boolean> values = IntStream.range(0, model.getRowCount())
        .mapToObj(i -> Objects.equals(model.getValueAt(i, targetColumnIndex), true))
        .distinct()
        .limit(2)
        .collect(Collectors.toList()); // Java 16: .toList();
    Status status;
    if (values.isEmpty()) {
      status = Status.DESELECTED;
    } else {
      boolean isUniform = values.size() == 1;
      if (isUniform) {
        boolean isSelected = values.get(0); // Java 21: values.getFirst();
        status = isSelected ? Status.SELECTED : Status.DESELECTED;
      } else {
        status = Status.INDETERMINATE;
      }
    }
    return status;
  }

  @Override public void mouseClicked(MouseEvent e) {
    JTableHeader header = (JTableHeader) e.getComponent();
    TableModel model = table.getModel();
    int vci = header.columnAtPoint(e.getPoint());
    int mci = table.convertColumnIndexToModel(vci);
    if (header.isEnabled() && mci == targetColumnIndex && model.getRowCount() > 0) {
      TableColumn column = table.getColumnModel().getColumn(vci);
      boolean selected = column.getHeaderValue() == Status.DESELECTED;
      setAllValues(model, selected);
      setHeaderStatus(vci, selected ? Status.SELECTED : Status.DESELECTED);
    }
  }

  private void setAllValues(TableModel model, boolean selected) {
    // Suppress the header state check for each row while updating all rows
    model.removeTableModelListener(this);
    try {
      for (int i = 0; i < model.getRowCount(); i++) {
        model.setValueAt(selected, i, targetColumnIndex);
      }
    } finally {
      model.addTableModelListener(this);
    }
  }
}

class ComponentIcon implements Icon {
  private final Component cmp;

  protected ComponentIcon(Component cmp) {
    this.cmp = cmp;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Container parent = c.getParent();
    int iconWidth = getIconWidth();
    int iconHeight = getIconHeight();
    SwingUtilities.paintComponent(g, cmp, parent, x, y, iconWidth, iconHeight);
  }

  @Override public int getIconWidth() {
    return cmp.getPreferredSize().width;
  }

  @Override public int getIconHeight() {
    return cmp.getPreferredSize().height;
  }
}

enum Status {
  SELECTED(true, true),
  DESELECTED(false, true),
  INDETERMINATE(true, false);

  private final boolean selected;
  private final boolean enabled;

  Status(boolean selected, boolean enabled) {
    this.selected = selected;
    this.enabled = enabled;
  }

  /* default */ void configureHeaderCheckBox(JCheckBox check) {
    check.setSelected(selected);
    check.setEnabled(enabled);
  }
}

// @see SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = createButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton createButton(UIManager.LookAndFeelInfo info) {
    boolean selected = info.getClassName().equals(lookAndFeel);
    return new JRadioButtonMenuItem(info.getName(), selected);
  }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        Logger.getGlobal().severe(ex::getMessage);
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
