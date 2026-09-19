// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel()) {
      @Override public void updateUI() {
        super.updateUI();
        TableColumn c = getColumnModel().getColumn(1);
        c.setCellRenderer(new CheckBoxesRenderer());
        c.setCellEditor(new CheckBoxesEditor());
        putClientProperty("terminateEditOnFocusLost", true);
      }
    };
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"user", "rwx"};
    Object[][] data = {
        {"owner", 7}, {"group", 6}, {"other", 5},
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
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

class CheckBoxesPanel extends JPanel {
  // Permission symbols in "rwx" display order; bit: r -> 4, w -> 2, x -> 1
  public static final List<String> SYMBOLS = List.of("r", "w", "x");
  private static final Color TRANSPARENT = new Color(0x0, true);
  private final List<JCheckBox> checkBoxes = SYMBOLS.stream()
      .map(CheckBoxesPanel::createCheckBox)
      .collect(Collectors.toList());

  @Override public void updateUI() {
    super.updateUI();
    setOpaque(false);
    setBackground(TRANSPARENT);
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
  }

  private static JCheckBox createCheckBox(String symbol) {
    JCheckBox b = new JCheckBox(symbol);
    b.setOpaque(false);
    b.setFocusable(false);
    b.setRolloverEnabled(false);
    b.setBackground(TRANSPARENT);
    return b;
  }

  // Convert an index in SYMBOLS to its chmod bit: 0 -> 4, 1 -> 2, 2 -> 1
  private static int getBit(int index) {
    return 1 << (SYMBOLS.size() - 1 - index);
  }

  // Re-add the check boxes on every update to avoid ghost images on Windows Aero
  private void initCheckBoxes() {
    removeAll();
    checkBoxes.forEach(b -> {
      add(b);
      add(Box.createHorizontalStrut(5));
    });
  }

  protected void updateCheckBoxes(Object value) {
    initCheckBoxes();
    int mode = value instanceof Integer ? (int) value : 0;
    for (int i = 0; i < checkBoxes.size(); i++) {
      checkBoxes.get(i).setSelected((mode & getBit(i)) != 0);
    }
  }

  protected void toggleCheckBox(int index) {
    checkBoxes.get(index).doClick();
  }

  protected int getMode() {
    return IntStream.range(0, checkBoxes.size())
        .filter(i -> checkBoxes.get(i).isSelected())
        .map(CheckBoxesPanel::getBit)
        .reduce(0, (a, b) -> a | b);
  }
}

class CheckBoxesRenderer implements TableCellRenderer {
  private final CheckBoxesPanel renderer = new CheckBoxesPanel();

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    renderer.updateCheckBoxes(value);
    return renderer;
  }
}

class CheckBoxesEditor extends AbstractCellEditor implements TableCellEditor {
  private final CheckBoxesPanel editor = new CheckBoxesPanel();

  protected CheckBoxesEditor() {
    super();
    ActionMap am = editor.getActionMap();
    InputMap im = editor.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    List<String> symbols = CheckBoxesPanel.SYMBOLS;
    for (int i = 0; i < symbols.size(); i++) {
      String symbol = symbols.get(i);
      am.put(symbol, createToggleAction(i));
      // "r" -> KeyEvent.VK_R, "w" -> KeyEvent.VK_W, "x" -> KeyEvent.VK_X
      int keyCode = KeyEvent.getExtendedKeyCodeForChar(symbol.charAt(0));
      im.put(KeyStroke.getKeyStroke(keyCode, 0), symbol);
    }
  }

  private Action createToggleAction(int index) {
    return new AbstractAction(CheckBoxesPanel.SYMBOLS.get(index)) {
      @Override public void actionPerformed(ActionEvent e) {
        editor.toggleCheckBox(index);
        fireEditingStopped();
      }
    };
  }

  @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    editor.updateCheckBoxes(value);
    return editor;
  }

  @Override public Object getCellEditorValue() {
    return editor.getMode();
  }
}
