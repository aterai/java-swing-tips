// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public final class MainPanel extends JPanel {
  private final BindingMapModel model = new BindingMapModel();
  private final JComponent[] components = {
      new JComboBox<>(),
      new JDesktopPane(),
      new JFormattedTextField(),
      // new JFileChooser(),
      new JInternalFrame(),
      new JLabel(),
      new JLayeredPane(),
      new JList<>(),
      new JMenuBar(),
      new JOptionPane(),
      new JPanel(),
      new JPopupMenu(),
      new JProgressBar(),
      new JRootPane(),
      new JScrollBar(),
      new JScrollPane(),
      new JSeparator(),
      new JSlider(),
      new JSpinner(),
      new JSplitPane(),
      new JTabbedPane(),
      new JTable(),
      new JTableHeader(),
      new JToolBar(),
      new JToolTip(),
      new JTree(),
      new JEditorPane(),
      new JTextArea(),
      new JTextField()
  };
  private final JComboBox<JComponent> compChoices = new JComboBox<>(components);

  private MainPanel() {
    super(new BorderLayout(5, 5));
    JTable table = new JTable(model);
    table.setAutoCreateRowSorter(true);
    DefaultListCellRenderer renderer = new DefaultListCellRenderer();
    compChoices.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
      Component c = renderer.getListCellRendererComponent(
          list, value, index, isSelected, cellHasFocus);
      if (c instanceof JLabel) {
        ((JLabel) c).setText(value.getClass().getName());
      }
      return c;
    });
    compChoices.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        model.setRowCount(0);
        JComponent c = compChoices.getItemAt(compChoices.getSelectedIndex());
        for (FocusType f : FocusType.values()) {
          loadBindingMap(f, c.getInputMap(f.getId()), c.getActionMap());
        }
      }
    });
    EventQueue.invokeLater(() -> compChoices.setSelectedIndex(compChoices.getItemCount() - 1));
    add(compChoices, BorderLayout.NORTH);
    add(new JScrollPane(table));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  // <!--
  // // original code:
  // // ftp://ftp.oreilly.de/pub/examples/english_examples/jswing2/code/goodies/Mapper.java
  // private Hashtable<Object, ArrayList<KeyStroke>> buildReverseMap(InputMap im) {
  //   Hashtable<Object, ArrayList<KeyStroke>> h = new Hashtable<>();
  //   if (Objects.isNull(im.allKeys())) {
  //     return h;
  //   }
  //   for (KeyStroke ks : im.allKeys()) {
  //     Object name = im.get(ks);
  //     if (h.containsKey(name)) {
  //       h.get(name).add(ks);
  //     } else {
  //       ArrayList<KeyStroke> keyList = new ArrayList<>();
  //       keyList.add(ks);
  //       h.put(name, keyList);
  //     }
  //   }
  //   return h;
  // }
  private void loadBindingMap(FocusType focusType, InputMap im, ActionMap am) {
    KeyStroke[] imKeys = im.allKeys();
    if (Objects.isNull(imKeys)) {
      return;
    }
    ActionMap tmpAm = new ActionMap();
    for (Object actionMapKey : am.allKeys()) {
      tmpAm.put(actionMapKey, am.get(actionMapKey));
    }
    for (KeyStroke ks : imKeys) {
      Object actionMapKey = im.get(ks);
      Action action = am.get(actionMapKey);
      String name = String.format("%s%s", action == null ? "____" : "", actionMapKey);
      model.addBinding(makeBinding(focusType, name, ks.toString()));
      tmpAm.remove(actionMapKey);
    }
    Object[] keys = tmpAm.allKeys();
    List<Object> list = Objects.nonNull(keys) ? Arrays.asList(keys) : Collections.emptyList();
    for (Object actionMapKey : list) {
      model.addBinding(makeBinding(focusType, actionMapKey.toString(), ""));
    }
  }
  // -->

  private Binding makeBinding(FocusType focusType, String actionName, String keyDescription) {
    return new Binding(focusType, actionName, keyDescription);
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

class BindingMapModel extends DefaultTableModel {
  private static final ColumnContext[] COLUMN_ARRAY = {
      new ColumnContext("Focus", String.class, false),
      new ColumnContext("ActionName", String.class, false),
      new ColumnContext("KeyDescription", String.class, false)
  };

  public void addBinding(Binding t) {
    Object[] obj = {t.getFocusTypeName(), t.getActionName(), t.getKeyDescription()};
    super.addRow(obj);
  }

  @Override public boolean isCellEditable(int row, int col) {
    return COLUMN_ARRAY[col].isEditable;
  }

  @Override public Class<?> getColumnClass(int column) {
    return COLUMN_ARRAY[column].columnClass;
  }

  @Override public int getColumnCount() {
    return COLUMN_ARRAY.length;
  }

  @Override public String getColumnName(int column) {
    return COLUMN_ARRAY[column].columnName;
  }

  private static class ColumnContext {
    public final String columnName;
    public final Class<?> columnClass;
    public final boolean isEditable;

    protected ColumnContext(String columnName, Class<?> columnClass, boolean isEditable) {
      this.columnName = columnName;
      this.columnClass = columnClass;
      this.isEditable = isEditable;
    }
  }
}

class Binding {
  private final FocusType focusType;
  private final String actionName;
  private final String keyDescription;

  protected Binding(FocusType focusType, String actionName, String keyDescription) {
    this.focusType = focusType;
    this.actionName = actionName;
    this.keyDescription = keyDescription;
  }

  public FocusType getFocusType() {
    return focusType;
  }

  public String getActionName() {
    return actionName;
  }

  public String getKeyDescription() {
    return keyDescription;
  }

  public String getFocusTypeName() {
    return getFocusType().name();
  }
}

@SuppressWarnings("PMD.LongVariable")
enum FocusType {
  WHEN_FOCUSED(JComponent.WHEN_FOCUSED),
  WHEN_IN_FOCUSED_WINDOW(JComponent.WHEN_IN_FOCUSED_WINDOW),
  WHEN_ANCESTOR_OF_FOCUSED_COMPONENT(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

  private final int id;

  FocusType(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }
}
