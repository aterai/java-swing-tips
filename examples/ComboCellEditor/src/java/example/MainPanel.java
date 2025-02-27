// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    UIManager.put("ComboBox.buttonDarkShadow", UIManager.getColor("TextField.foreground"));
    String[] comboModel = {"Name 0", "Name 1", "Name 2"};
    JTable table = new JTable(makeModel(comboModel));
    TableColumn c0 = table.getColumnModel().getColumn(0);
    c0.setMinWidth(60);
    c0.setMaxWidth(60);
    c0.setResizable(false);

    TableColumn c1 = table.getColumnModel().getColumn(1);
    c1.setCellEditor(new DefaultCellEditor(makeCombo(new DefaultComboBoxModel<>(comboModel))));
    // table.setDefaultEditor(JComboBox.class, new DefaultCellEditor(combo));

    table.setAutoCreateRowSorter(true);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel(String... comboModel) {
    String[] columnNames = {"Integer", "String", "Boolean"};
    Object[][] data = {
        {12, comboModel[0], true}, {5, comboModel[2], false},
        {92, comboModel[1], true}, {3, comboModel[0], false}
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
  }

  private static <E> JComboBox<E> makeCombo(ComboBoxModel<E> model) {
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
