// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    TableModel model = createModel();
    JLabel label = new JLabel();
    JButton button = new JButton("ls -l (chmod)");
    button.addActionListener(e -> label.setText(createPermissionsText(model)));

    JTable table = new JTable(model) {
      @Override public void updateUI() {
        super.updateUI();
        TableColumn c = getColumnModel().getColumn(1);
        c.setCellRenderer(new CheckBoxesRenderer());
        c.setCellEditor(new CheckBoxesEditor());
        putClientProperty("terminateEditOnFocusLost", true);
      }
    };
    JPanel p = new JPanel(new BorderLayout());
    p.add(label);
    p.add(button, BorderLayout.EAST);
    add(new JScrollPane(table));
    add(p, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  // e.g. " 740 -rwxr-----"
  private static String createPermissionsText(TableModel model) {
    StringBuilder octalBuf = new StringBuilder(3);
    StringBuilder rwxBuf = new StringBuilder(9);
    for (int i = 0; i < model.getRowCount(); i++) {
      Set<?> permissions = (Set<?>) model.getValueAt(i, 1);
      int mode = 0;
      for (Permission perm : Permission.values()) {
        boolean granted = permissions.contains(perm);
        mode |= granted ? perm.getMode() : 0;
        rwxBuf.append(granted ? perm.getSymbol() : '-');
      }
      octalBuf.append(mode);
    }
    return String.format(" %s -%s", octalBuf, rwxBuf);
  }

  private static TableModel createModel() {
    String[] columnNames = {"user", "rwx"};
    Object[][] data = {
        {"owner", EnumSet.allOf(Permission.class)},
        {"group", EnumSet.of(Permission.READ)},
        {"other", EnumSet.noneOf(Permission.class)},
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

// Declared in "rwx" display order
enum Permission {
  READ('r', 1 << 2), WRITE('w', 1 << 1), EXECUTE('x', 1);

  private final char symbol;
  private final int mode;

  Permission(char symbol, int mode) {
    this.symbol = symbol;
    this.mode = mode;
  }

  public char getSymbol() {
    return symbol;
  }

  public int getMode() {
    return mode;
  }
}

class CheckBoxesPanel extends JPanel {
  private static final Color TRANSPARENT = new Color(0x0, true);
  private final Map<Permission, JCheckBox> checkBoxes = Stream
      .of(Permission.values())
      .collect(Collectors.toMap(
          Function.identity(),
          CheckBoxesPanel::createCheckBox,
          (a, b) -> a,
          () -> new EnumMap<>(Permission.class)));

  @Override public void updateUI() {
    super.updateUI();
    setOpaque(false);
    setBackground(TRANSPARENT);
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
  }

  private static JCheckBox createCheckBox(Permission perm) {
    JCheckBox b = new JCheckBox(String.valueOf(perm.getSymbol()));
    b.setOpaque(false);
    b.setFocusable(false);
    b.setRolloverEnabled(false);
    b.setBackground(TRANSPARENT);
    return b;
  }

  // Re-add the check boxes on every update to avoid ghost images on Windows Aero
  private void initCheckBoxes() {
    removeAll();
    checkBoxes.values().forEach(b -> {
      add(b);
      add(Box.createHorizontalStrut(5));
    });
  }

  protected void updateCheckBoxes(Object value) {
    initCheckBoxes();
    Set<?> permissions = value instanceof Set
        ? (Set<?>) value
        : Collections.emptySet();
    checkBoxes.forEach((perm, b) -> b.setSelected(permissions.contains(perm)));
  }

  protected void toggleCheckBox(Permission perm) {
    checkBoxes.get(perm).doClick();
  }

  protected Set<Permission> getPermissions() {
    return checkBoxes.entrySet().stream()
        .filter(e -> e.getValue().isSelected())
        .map(Map.Entry::getKey)
        .collect(Collectors.toCollection(() -> EnumSet.noneOf(Permission.class)));
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
    Stream.of(Permission.values()).forEach(perm -> {
      String key = perm.name();
      am.put(key, createToggleAction(perm));
      // 'r' -> KeyEvent.VK_R, 'w' -> KeyEvent.VK_W, 'x' -> KeyEvent.VK_X
      int keyCode = KeyEvent.getExtendedKeyCodeForChar(perm.getSymbol());
      im.put(KeyStroke.getKeyStroke(keyCode, 0), key);
    });
  }

  private Action createToggleAction(Permission perm) {
    return new AbstractAction(perm.name()) {
      @Override public void actionPerformed(ActionEvent e) {
        editor.toggleCheckBox(perm);
        fireEditingStopped();
      }
    };
  }

  @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    editor.updateCheckBoxes(value);
    return editor;
  }

  @Override public Object getCellEditorValue() {
    return editor.getPermissions();
  }
}
