// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String[] comboModel = {"Name 0", "Name 1", "Name 2"};
    JTable table = new JTable(makeModel(comboModel)) {
      private final Color evenColor = new Color(0xF0_F0_FA);
      @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
        Component c = super.prepareRenderer(tcr, row, column);
        if (isRowSelected(row)) {
          c.setForeground(getSelectionForeground());
          c.setBackground(getSelectionBackground());
        } else {
          c.setForeground(getForeground());
          c.setBackground(row % 2 == 0 ? evenColor : getBackground());
        }
        return c;
      }
    };

    TableColumn c0 = table.getColumnModel().getColumn(0);
    c0.setMinWidth(60);
    c0.setMaxWidth(60);
    c0.setResizable(false);

    UIManager.put("ComboBox.buttonDarkShadow", UIManager.getColor("TextField.foreground"));
    JComboBox<String> combo = makeComboBox(new DefaultComboBoxModel<>(comboModel));

    TableColumn c1 = table.getColumnModel().getColumn(1);
    c1.setCellRenderer(new ComboCellRenderer());
    c1.setCellEditor(new DefaultCellEditor(combo));
    // table.setDefaultEditor(JComboBox.class, new DefaultCellEditor(combo));

    table.setAutoCreateRowSorter(true);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel(String... comboModel) {
    String[] columnNames = {"Integer", "String", "Boolean"};
    Object[][] data = {
        {12, comboModel[0], true}, {5, comboModel[2], false},
        {92, comboModel[1], true}, {0, comboModel[0], false}
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
  }

  private static <E> JComboBox<E> makeComboBox(ComboBoxModel<E> model) {
    return new JComboBox<E>(model) {
      @Override public void updateUI() {
        super.updateUI();
        setBorder(BorderFactory.createEmptyBorder());
        setUI(new BasicComboBoxUI() {
          @Override protected JButton createArrowButton() {
            JButton button = super.createArrowButton();
            button.setContentAreaFilled(false);
            button.setBorder(BorderFactory.createEmptyBorder());
            return button;
          }
        });
        // JTextField editor = (JTextField) getEditor().getEditorComponent();
        // editor.setBorder(BorderFactory.createEmptyBorder());
        // editor.setOpaque(true);
        // editor.setEditable(false);
      }
    };
    // combo.setBorder(BorderFactory.createEmptyBorder());
    // ((JTextField) combo.getEditor().getEditorComponent()).setBorder(null);
    // ((JTextField) combo.getEditor().getEditorComponent()).setMargin(null);
    // combo.setBackground(Color.WHITE);
    // combo.setOpaque(true);
    // combo.setEditable(true);
    // return combo;
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

class ComboCellRenderer implements TableCellRenderer {
  protected static final Color EVEN_COLOR = new Color(0xF0_F0_FA);
  protected JButton button;
  protected final JComboBox<String> combo = new JComboBox<String>() {
    @Override public void updateUI() {
      super.updateUI();
      setBorder(BorderFactory.createEmptyBorder());
      setUI(new BasicComboBoxUI() {
        @Override protected JButton createArrowButton() {
          button = super.createArrowButton();
          button.setContentAreaFilled(false);
          button.setBorder(BorderFactory.createEmptyBorder());
          return button;
        }
      });
    }

    @Override public boolean isOpaque() {
      Object o = SwingUtilities.getAncestorOfClass(JTable.class, this);
      return o instanceof JTable ? opaqueCheck((JTable) o) : super.isOpaque();
    }

    private boolean opaqueCheck(JTable t) {
      Color bgc = getBackground();
      boolean colorMatch = bgc != null && bgc.equals(t.getBackground()) && t.isOpaque();
      return !colorMatch && super.isOpaque();
    }
  };

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    JTextField editor = (JTextField) combo.getEditor().getEditorComponent();
    editor.setBorder(BorderFactory.createEmptyBorder());
    editor.setOpaque(true);
    combo.removeAllItems();
    Optional.ofNullable(button).ifPresent(b -> {
      if (isSelected) {
        editor.setForeground(table.getSelectionForeground());
        editor.setBackground(table.getSelectionBackground());
        b.setBackground(table.getSelectionBackground());
      } else {
        editor.setForeground(table.getForeground());
        Color bg = row % 2 == 0 ? EVEN_COLOR : table.getBackground();
        editor.setBackground(bg);
        b.setBackground(bg);
      }
    });
    combo.addItem(Objects.toString(value, ""));
    return combo;
  }
}

// class ComboCellRenderer extends JComboBox<String> implements TableCellRenderer {
//   protected static final Color EVEN_COLOR = new Color(0xF0_F0_FA);
//   protected JButton button;
//   @Override public void updateUI() {
//     super.updateUI();
//     setBorder(BorderFactory.createEmptyBorder());
//     setUI(new BasicComboBoxUI() {
//       @Override protected JButton createArrowButton() {
//         button = super.createArrowButton();
//         button.setContentAreaFilled(false);
//         // button.setBackground(ComboCellRenderer.this.getBackground());
//         button.setBorder(BorderFactory.createEmptyBorder());
//         return button;
//       }
//     });
//   }
//
//   @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//     JTextField editor = (JTextField) getEditor().getEditorComponent();
//     editor.setBorder(BorderFactory.createEmptyBorder());
//     editor.setOpaque(true);
//     // editor.setEditable(false);
//     removeAllItems();
//     if (button != null) {
//       if (isSelected) {
//         editor.setForeground(table.getSelectionForeground());
//         editor.setBackground(table.getSelectionBackground());
//         button.setBackground(table.getSelectionBackground());
//       } else {
//         editor.setForeground(table.getForeground());
//         // setBackground(table.getBackground());
//         Color bg = row % 2 == 0 ? EVEN_COLOR : table.getBackground();
//         editor.setBackground(bg);
//         button.setBackground(bg);
//       }
//     }
//     addItem(Objects.toString(value, ""));
//     return this;
//   }
//
//   // Overridden for performance reasons. ---->
//   @Override public boolean isOpaque() {
//     Color back = getBackground();
//     Object o = SwingUtilities.getAncestorOfClass(JTable.class, this);
//     if (o instanceof JTable) {
//       JTable t = (JTable) o;
//       boolean colorMatch = back != null && back.equals(t.getBackground()) && t.isOpaque();
//       return !colorMatch && super.isOpaque();
//     } else {
//       return super.isOpaque();
//     }
//   }
//
//   @Override protected void firePropertyChange(String propertyName, Object ov, Object nv) {
//     // System.out.println(propertyName);
//     // if ((propertyName == "font" || propertyName == "foreground") && ov != nv) {
//     //   super.firePropertyChange(propertyName, ov, nv);
//     // }
//   }
//
//   // @Override public void firePropertyChange(String propertyName, boolean ov, boolean nv) {
//   //   /* Overridden for performance reasons. */
//   // }
//
//   @Override public void repaint(long tm, int x, int y, int width, int height) {
//     /* Overridden for performance reasons. */
//   }
//
//   @Override public void repaint(Rectangle r) {
//     /* Overridden for performance reasons. */
//   }
//
//   @Override public void repaint() {
//     /* Overridden for performance reasons. */
//   }
//
//   // @Override public void invalidate() {
//   //   /* Overridden for performance reasons. */
//   // }
//
//   // @Override public void validate() {
//   //   /* Overridden for performance reasons. */
//   // }
//
//   @Override public void revalidate() {
//     /* Overridden for performance reasons. */
//   }
//   // <---- Overridden for performance reasons.
// }
