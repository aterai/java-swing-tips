// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.EventObject;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));

    JTable table = makeTable();

    ActionMap am = table.getActionMap();
    Action sncc = am.get("selectNextColumnCell");

    Action action = new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        if (!table.isEditing() || !isEditorFocusCycle(table.getEditorComponent())) {
          // System.out.println("Exit editor");
          sncc.actionPerformed(e);
        }
      }
    };
    am.put("selectNextColumnCell2", action);

    InputMap im = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "selectNextColumnCell2");

    add(new JScrollPane(makeTable()));
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  protected boolean isEditorFocusCycle(Component editor) {
    Component child = CheckBoxesEditor.getEditorFocusCycleAfter(editor);
    if (child != null) {
      child.requestFocus();
      return true;
    }
    return false;
  }

  private static JTable makeTable() {
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
    return new JTable(model) {
      @Override public void updateUI() {
        super.updateUI();
        // putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        setSelectionForeground(Color.BLACK);
        setSelectionBackground(new Color(0xDC_DC_FF));
        TableColumn c = getColumnModel().getColumn(1);
        c.setCellRenderer(new CheckBoxesRenderer());
        c.setCellEditor(new CheckBoxesEditor());
      }
    };
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
  protected final String[] titles = {"r", "w", "x"};
  protected final List<JCheckBox> buttons = new ArrayList<>(titles.length);
  protected Border focusBorder; // = UIManager.getBorder("Table.focusCellHighlightBorder");
  protected Border noFocusBorder;

  @Override public void updateUI() {
    super.updateUI();
    setOpaque(false);
    setFocusTraversalPolicyProvider(true);
    setFocusCycleRoot(true);
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    focusBorder = UIManager.getBorder("Table.focusCellHighlightBorder");
    Border b = UIManager.getBorder("Table.noFocusBorder");
    if (b == null) { // Nimbus???
      Insets i = focusBorder.getBorderInsets(this);
      b = BorderFactory.createEmptyBorder(i.top, i.left, i.bottom, i.right);
    }
    noFocusBorder = b;
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
    JCheckBox b = new JCheckBox(" " + title + " ");
    b.setOpaque(false);
    // b.setFocusPainted(false);
    // b.setFocusable(false);
    // b.setRolloverEnabled(false);
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
  private static final Color BGC = new Color(0x0, true);
  private final CheckBoxesPanel renderer = new CheckBoxesPanel();

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if (isSelected) {
      renderer.setOpaque(true);
      renderer.setBackground(table.getSelectionBackground());
    } else {
      renderer.setOpaque(true);
      renderer.setBackground(BGC);
    }
    renderer.setBorder(hasFocus ? renderer.focusBorder : renderer.noFocusBorder);
    renderer.updateButtons(value);
    return renderer;
  }
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
              .filter(b -> b.getText().trim().equals(t))
              .findFirst()
              .ifPresent(JCheckBox::doClick);
          // fireEditingStopped();
        }
      });
    });

    // TEST:
    // renderer.setFocusTraversalPolicy(new LayoutFocusTraversalPolicy() {
    //   @Override protected boolean accept(Component c) {
    //     // return !Objects.equals(c, textarea) && super.accept(c);
    //     return super.accept(c);
    //   }
    //   @Override public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
    //     // int i = order.indexOf(aComponent);
    //     System.out.println("getComponentAfter getComponentAfter getComponentAfter");
    //     return super.getComponentAfter(focusCycleRoot, aComponent);
    //   }
    //   @Override public Component getDefaultComponent(Container container) {
    //     // return button;
    //     return renderer.buttons.get(0); // getRootPane().getDefaultButton();
    //   }
    // });

    InputMap im = renderer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), renderer.titles[0]);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), renderer.titles[1]);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), renderer.titles[2]);
  }

  @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    renderer.setOpaque(true);
    renderer.setBackground(table.getSelectionBackground());
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

  @Override public boolean isCellEditable(EventObject e) {
    EventQueue.invokeLater(() -> {
      Component child = getEditorFocusCycleAfter((Component) e.getSource());
      if (child != null) {
        child.requestFocus();
      }
    });
    return super.isCellEditable(e);
  }

  public static Component getEditorFocusCycleAfter(Component editor) {
    Component fo = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
    if (fo == null || !(editor instanceof Container)) {
      return null;
    }
    Container root = (Container) editor;
    if (!root.isFocusCycleRoot()) {
      root = root.getFocusCycleRootAncestor();
    }
    if (root == null) {
      return null;
    }
    // System.out.println("FocusCycleRoot: " + root.getClass().getName());
    FocusTraversalPolicy ftp = root.getFocusTraversalPolicy();
    Component child = ftp.getComponentAfter(root, fo);
    if (child != null && SwingUtilities.isDescendingFrom(child, editor)) {
      // System.out.println("requestFocus: " + child.getClass().getName());
      // child.requestFocus();
      return child;
    }
    return null;
  }
}
