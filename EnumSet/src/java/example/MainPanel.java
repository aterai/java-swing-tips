// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
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

    String[] columnNames = {"user", "rwx"};
    Object[][] data = {
      {"owner", EnumSet.allOf(Permissions.class)},
      {"group", EnumSet.of(Permissions.READ)},
      {"other", EnumSet.noneOf(Permissions.class)}
    };
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
    JTable table = new JTable(model) {
      @Override public void updateUI() {
        super.updateUI();
        TableColumn c = getColumnModel().getColumn(1);
        c.setCellRenderer(new CheckBoxesRenderer());
        c.setCellEditor(new CheckBoxesEditor());
      }
    };
    table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

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

    EnumMap<Permissions, Integer> map = new EnumMap<>(Permissions.class);
    map.put(Permissions.READ, 1 << 2);
    map.put(Permissions.WRITE, 1 << 1);
    map.put(Permissions.EXECUTE, 1 << 0);

    JLabel label = new JLabel();
    JButton button = new JButton("ls -l (chmod)");
    button.addActionListener(e -> {
      StringBuilder nbuf = new StringBuilder(3);
      StringBuilder buf = new StringBuilder(9);
      for (int i = 0; i < model.getRowCount(); i++) {
        Set<?> v = (Set<?>) model.getValueAt(i, 1);
        int flg = 0;
        if (v.contains(Permissions.READ)) {
          flg |= map.get(Permissions.READ);
          buf.append('r');
        } else {
          buf.append('-');
        }
        if (v.contains(Permissions.WRITE)) {
          flg |= map.get(Permissions.WRITE);
          buf.append('w');
        } else {
          buf.append('-');
        }
        if (v.contains(Permissions.EXECUTE)) {
          flg |= map.get(Permissions.EXECUTE);
          buf.append('x');
        } else {
          buf.append('-');
        }
        nbuf.append(flg);
      }
      label.setText(String.format(" %s -%s", nbuf, buf));
    });

    JPanel p = new JPanel(new BorderLayout());
    p.add(label);
    p.add(button, BorderLayout.EAST);
    add(new JScrollPane(table));
    add(p, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

enum Permissions { EXECUTE, WRITE, READ }

class CheckBoxesPanel extends JPanel {
  private static final Color BGC = new Color(0x0, true);
  protected final String[] titles = {"r", "w", "x"};
  protected final List<JCheckBox> buttons = new ArrayList<>(titles.length);

  @Override public void updateUI() {
    super.updateUI();
    setOpaque(false);
    setBackground(BGC);
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    EventQueue.invokeLater(this::initButtons);
  }

  private void initButtons() {
    removeAll();
    buttons.clear();
    for (String t: titles) {
      JCheckBox b = makeCheckBox(t);
      buttons.add(b);
      add(b);
      add(Box.createHorizontalStrut(5));
    }
  }

  private static JCheckBox makeCheckBox(String title) {
    JCheckBox b = new JCheckBox(title);
    b.setOpaque(false);
    b.setFocusable(false);
    b.setRolloverEnabled(false);
    b.setBackground(BGC);
    return b;
  }

  protected void updateButtons(Object v) {
    initButtons();
    Set<?> f = v instanceof Set ? (Set<?>) v : EnumSet.noneOf(Permissions.class);
    buttons.get(0).setSelected(f.contains(Permissions.READ));
    buttons.get(1).setSelected(f.contains(Permissions.WRITE));
    buttons.get(2).setSelected(f.contains(Permissions.EXECUTE));
  }
}

class CheckBoxesRenderer implements TableCellRenderer {
  private final CheckBoxesPanel renderer = new CheckBoxesPanel();

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    renderer.updateButtons(value);
    return renderer;
  }
  // public static class UIResource extends CheckBoxesRenderer implements javax.swing.plaf.UIResource {}
}

class CheckBoxesEditor extends AbstractCellEditor implements TableCellEditor {
  protected final CheckBoxesPanel renderer = new CheckBoxesPanel();

  protected CheckBoxesEditor() {
    super();
    ActionMap am = renderer.getActionMap();
    Stream.of(renderer.titles).forEach(t -> {
      am.put(t, new AbstractAction(t) {
        @Override public void actionPerformed(ActionEvent e) {
          renderer.buttons.stream()
            .filter(b -> b.getText().equals(t))
            .findFirst()
            .ifPresent(JCheckBox::doClick);
          fireEditingStopped();
        }
      });
    });
    InputMap im = renderer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), renderer.titles[0]);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), renderer.titles[1]);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), renderer.titles[2]);
  }

  @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    renderer.updateButtons(value);
    return renderer;
  }

  @Override public Object getCellEditorValue() {
    Set<Permissions> f = EnumSet.noneOf(Permissions.class);
    if (renderer.buttons.get(0).isSelected()) {
      f.add(Permissions.READ);
    }
    if (renderer.buttons.get(1).isSelected()) {
      f.add(Permissions.WRITE);
    }
    if (renderer.buttons.get(2).isSelected()) {
      f.add(Permissions.EXECUTE);
    }
    return f;
  }
}
