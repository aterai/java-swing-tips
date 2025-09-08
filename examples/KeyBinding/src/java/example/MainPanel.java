// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public final class MainPanel extends JPanel {
  private static final String[] COMPONENTS = {
      // "javax.swing.JApplet",
      "javax.swing.JButton",
      "javax.swing.JCheckBox",
      "javax.swing.JCheckBoxMenuItem",
      "javax.swing.JColorChooser",
      "javax.swing.JComboBox",
      // "javax.swing.JComponent",
      "javax.swing.JDesktopPane",
      "javax.swing.JDialog",
      "javax.swing.JEditorPane",
      "javax.swing.JFileChooser",
      "javax.swing.JFormattedTextField",
      "javax.swing.JFrame",
      "javax.swing.JInternalFrame",
      "javax.swing.JLabel",
      "javax.swing.JLayer",
      "javax.swing.JLayeredPane",
      "javax.swing.JList",
      "javax.swing.JMenu",
      "javax.swing.JMenuBar",
      "javax.swing.JMenuItem",
      "javax.swing.JOptionPane",
      "javax.swing.JPanel",
      "javax.swing.JPasswordField",
      "javax.swing.JPopupMenu",
      "javax.swing.JProgressBar",
      "javax.swing.JRadioButton",
      "javax.swing.JRadioButtonMenuItem",
      "javax.swing.JRootPane",
      "javax.swing.JScrollBar",
      "javax.swing.JScrollPane",
      "javax.swing.JSeparator",
      "javax.swing.JSlider",
      "javax.swing.JSpinner",
      "javax.swing.JSplitPane",
      "javax.swing.JTabbedPane",
      "javax.swing.JTable",
      "javax.swing.JTextArea",
      "javax.swing.JTextField",
      "javax.swing.JTextPane",
      "javax.swing.JToggleButton",
      "javax.swing.JToolBar",
      "javax.swing.JToolTip",
      "javax.swing.JTree",
      "javax.swing.JViewport",
      "javax.swing.JWindow",
      "javax.swing.table.JTableHeader",
  };
  private final BindingMapModel model = new BindingMapModel();
  private final JComboBox<String> compChoices = new JComboBox<>(COMPONENTS);

  private MainPanel() {
    super(new BorderLayout(5, 5));
    JTable table = new JTable(model);
    table.setAutoCreateRowSorter(true);
    compChoices.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        model.setRowCount(0);
        int idx = compChoices.getSelectedIndex();
        updateTableModel(compChoices.getItemAt(idx));
      }
    });
    EventQueue.invokeLater(() -> compChoices.setSelectedIndex(compChoices.getItemCount() - 1));
    add(compChoices, BorderLayout.NORTH);
    add(new JScrollPane(table));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private void updateTableModel(String name) {
    try {
      Class<?> clz = Class.forName(name);
      Constructor<?> constructor = clz.getConstructor();
      Object o = constructor.newInstance();
      if (o instanceof JComponent) {
        JComponent c = (JComponent) o;
        for (FocusType f : FocusType.values()) {
          loadBindingMap(f, c.getInputMap(f.getId()), c.getActionMap());
        }
      }
    } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException ex) {
      Logger.getGlobal().info(ex::getMessage);
    } catch (InvocationTargetException | IllegalAccessException ex) {
      Logger.getGlobal().severe(ex::getMessage);
    }
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

class BindingMapModel extends DefaultTableModel {
  private static final ColumnContext[] COLUMN_ARRAY = {
      new ColumnContext("ActionName", String.class, false),
      new ColumnContext("KeyDescription", String.class, false),
      new ColumnContext("Focus", String.class, false)
  };

  public void addBinding(Binding t) {
    Object[] obj = {t.getActionName(), t.getKeyDescription(), t.getFocusTypeName()};
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
