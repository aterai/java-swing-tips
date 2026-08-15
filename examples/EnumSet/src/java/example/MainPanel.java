// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    JTable table = new JTable(model) {
      @Override public void updateUI() {
        super.updateUI();
        TableColumn c = getColumnModel().getColumn(1);
        c.setCellRenderer(new CheckBoxesRenderer());
        c.setCellEditor(new CheckBoxesEditor());
      }
    };
    table.putClientProperty("terminateEditOnFocusLost", true);

    // if (System.getProperty("java.version").startsWith("1.6.0")) {
    //   // 1.6.0_xx bug? column header click -> edit cancel?
    //   table.getTableHeader().addMouseListener(new MouseAdapter() {
    //     @Override public void mousePressed(MouseEvent e) {
    //       if (table.isEditing()) {
    //         table.getCellEditor().stopCellEditing();
    //       }
    //     }
    //   });
    // }

    @SuppressWarnings("PMD.UseConcurrentHashMap")
    Map<Permission, Integer> bitFlags = new EnumMap<>(Permission.class);
    bitFlags.put(Permission.READ, 1 << 2);
    bitFlags.put(Permission.WRITE, 1 << 1);
    bitFlags.put(Permission.EXECUTE, 1);

    JLabel label = new JLabel();
    JButton button = new JButton("ls -l (chmod)");
    button.addActionListener(e -> label.setText(createPermissionsText(model, bitFlags)));

    JPanel p = new JPanel(new BorderLayout());
    p.add(label);
    p.add(button, BorderLayout.EAST);
    add(new JScrollPane(table));
    add(p, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static String createPermissionsText(
      TableModel model, Map<Permission, Integer> bitFlags) {
    StringBuilder octalBuf = new StringBuilder(3);
    StringBuilder rwxBuf = new StringBuilder(9);
    for (int i = 0; i < model.getRowCount(); i++) {
      Set<?> v = (Set<?>) model.getValueAt(i, 1);
      int bits = 0;
      if (v.contains(Permission.READ)) {
        bits |= bitFlags.get(Permission.READ);
        rwxBuf.append('r');
      } else {
        rwxBuf.append('-');
      }
      if (v.contains(Permission.WRITE)) {
        bits |= bitFlags.get(Permission.WRITE);
        rwxBuf.append('w');
      } else {
        rwxBuf.append('-');
      }
      if (v.contains(Permission.EXECUTE)) {
        bits |= bitFlags.get(Permission.EXECUTE);
        rwxBuf.append('x');
      } else {
        rwxBuf.append('-');
      }
      octalBuf.append(bits);
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

enum Permission {
  EXECUTE, WRITE, READ
}

class CheckBoxesPanel extends JPanel {
  private static final Color TRANSPARENT = new Color(0x0, true);
  private static final String[] TITLES = {"r", "w", "x"};
  private final List<JCheckBox> checkBoxes = Stream.of(TITLES).map(s -> {
    JCheckBox b = new JCheckBox(s);
    b.setOpaque(false);
    b.setFocusable(false);
    b.setRolloverEnabled(false);
    b.setBackground(TRANSPARENT);
    return b;
  }).collect(Collectors.toList());

  @Override public void updateUI() {
    super.updateUI();
    setOpaque(false);
    setBackground(TRANSPARENT);
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    EventQueue.invokeLater(this::initCheckBoxes);
  }

  protected String[] getModeTitles() {
    return Arrays.copyOf(TITLES, TITLES.length);
  }

  private void initCheckBoxes() {
    removeAll();
    for (JCheckBox b : checkBoxes) {
      add(b);
      add(Box.createHorizontalStrut(5));
    }
  }

  protected void updateCheckBoxes(Object v) {
    initCheckBoxes();
    Set<?> f = v instanceof Set ? (Set<?>) v : EnumSet.noneOf(Permission.class);
    checkBoxes.get(0).setSelected(f.contains(Permission.READ));
    checkBoxes.get(1).setSelected(f.contains(Permission.WRITE));
    checkBoxes.get(2).setSelected(f.contains(Permission.EXECUTE));
  }

  protected void doClickCheckBox(String text) {
    checkBoxes.stream()
        .filter(b -> b.getText().equals(text))
        .findFirst()
        .ifPresent(JCheckBox::doClick);
  }

  protected Set<Permission> getPermissions() {
    Set<Permission> f = EnumSet.noneOf(Permission.class);
    if (checkBoxes.get(0).isSelected()) {
      f.add(Permission.READ);
    }
    if (checkBoxes.get(1).isSelected()) {
      f.add(Permission.WRITE);
    }
    if (checkBoxes.get(2).isSelected()) {
      f.add(Permission.EXECUTE);
    }
    return f;
  }
}

class CheckBoxesRenderer implements TableCellRenderer {
  private final CheckBoxesPanel panel = new CheckBoxesPanel();

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    panel.updateCheckBoxes(value);
    return panel;
  }
}

class CheckBoxesEditor extends AbstractCellEditor implements TableCellEditor {
  private final CheckBoxesPanel panel = new CheckBoxesPanel();

  protected CheckBoxesEditor() {
    super();
    String[] titles = panel.getModeTitles();
    ActionMap am = panel.getActionMap();
    Stream.of(titles).forEach(t -> am.put(t, new AbstractAction(t) {
      @Override public void actionPerformed(ActionEvent e) {
        panel.doClickCheckBox(t);
        fireEditingStopped();
      }
    }));
    InputMap im = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), titles[0]);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), titles[1]);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), titles[2]);
  }

  @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    panel.updateCheckBoxes(value);
    return panel;
  }

  @Override public Object getCellEditorValue() {
    return panel.getPermissions();
  }
}
