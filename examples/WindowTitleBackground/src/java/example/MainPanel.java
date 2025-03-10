// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  public static final int KEY_COL_IDX = 0;
  public static final int COLOR_COL_IDX = 1;

  private MainPanel() {
    super(new BorderLayout());
    add(new JScrollPane(makeTable(makeModel())));
    setPreferredSize(new Dimension(320, 240));
  }

  private JTable makeTable(TableModel tableModel) {
    return new JTable(tableModel) {
      private transient TableModelListener listener;

      @Override public void updateUI() {
        getModel().removeTableModelListener(listener);
        super.updateUI();
        setDefaultRenderer(Color.class, new ColorRenderer());
        setDefaultEditor(Color.class, new ColorEditor());
        listener = e -> {
          if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == COLOR_COL_IDX) {
            TableModel model = (TableModel) e.getSource();
            int row = e.getFirstRow();
            String key = Objects.toString(model.getValueAt(row, KEY_COL_IDX));
            Color color = (Color) model.getValueAt(row, COLOR_COL_IDX);
            UIManager.put(key, new ColorUIResource(color));
            EventQueue.invokeLater(() -> {
              Container c = getTopLevelAncestor();
              Optional.ofNullable(c).ifPresent(SwingUtilities::updateComponentTreeUI);
            });
          }
        };
        getModel().addTableModelListener(listener);
      }
    };
  }

  private static TableModel makeModel() {
    String[] columnNames = {"Key", "Color"};
    Object[][] data = {
        {"activeCaption", UIManager.getColor("activeCaption")},
        {"activeCaptionBorder", UIManager.getColor("activeCaptionBorder")},
        {"activeCaptionText", UIManager.getColor("activeCaptionText")},
        {"control", UIManager.getColor("control")},
        {"controlDkShadow", UIManager.getColor("controlDkShadow")},
        {"controlHighlight", UIManager.getColor("controlHighlight")},
        {"controlLtHighlight", UIManager.getColor("controlLtHighlight")},
        {"controlShadow", UIManager.getColor("controlShadow")},
        {"controlText", UIManager.getColor("controlText")},
        {"desktop", UIManager.getColor("desktop")},
        {"inactiveCaption", UIManager.getColor("inactiveCaption")},
        {"inactiveCaptionBorder", UIManager.getColor("inactiveCaptionBorder")},
        {"inactiveCaptionText", UIManager.getColor("inactiveCaptionText")},
        {"info", UIManager.getColor("info")},
        {"infoText", UIManager.getColor("infoText")},
        {"menu", UIManager.getColor("menu")},
        {"menuPressedItemB", UIManager.getColor("menuPressedItemB")},
        {"menuPressedItemF", UIManager.getColor("menuPressedItemF")},
        {"menuText", UIManager.getColor("menuText")},
        {"scrollbar", UIManager.getColor("scrollbar")},
        {"text", UIManager.getColor("text")},
        {"textHighlight", UIManager.getColor("textHighlight")},
        {"textHighlightText", UIManager.getColor("textHighlightText")},
        {"textInactiveText", UIManager.getColor("textInactiveText")},
        {"textText", UIManager.getColor("textText")},
        {"window", UIManager.getColor("window")},
        {"windowBorder", UIManager.getColor("windowBorder")},
        {"windowText", UIManager.getColor("windowText")}
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public boolean isCellEditable(int row, int column) {
        return column == COLOR_COL_IDX;
      }

      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    JFrame.setDefaultLookAndFeelDecorated(true);
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class ColorRenderer extends DefaultTableCellRenderer {
  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = super.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);
    if (value instanceof Color && c instanceof JLabel) {
      Color color = (Color) value;
      JLabel l = (JLabel) c;
      l.setIcon(new ColorIcon(color));
      l.setText(String.format("(%d, %d, %d)", color.getRed(), color.getGreen(), color.getBlue()));
    }
    return c;
  }
}

// https://docs.oracle.com/javase/tutorial/uiswing/examples/components/TableDialogEditDemoProject/src/components/ColorEditor.java
class ColorEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
  protected static final String EDIT = "edit";
  private final JButton button = new JButton();
  private final JColorChooser colorChooser;
  private final JDialog dialog;
  private Color currentColor;

  protected ColorEditor() {
    super();
    // Set up the editor (from the table's point of view),
    // which is a button.
    // This button brings up the color chooser dialog,
    // which is the editor from the user's point of view.
    button.setActionCommand(EDIT);
    button.addActionListener(this);
    // button.setBorderPainted(false);
    button.setContentAreaFilled(false);
    button.setFocusPainted(false);
    button.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    button.setOpaque(false);
    button.setHorizontalAlignment(SwingConstants.LEFT);
    button.setHorizontalTextPosition(SwingConstants.RIGHT);

    // Set up the dialog that the button brings up.
    colorChooser = new JColorChooser();
    dialog = JColorChooser.createDialog(button, "Pick a Color", true, colorChooser, this, null);
  }

  /**
   * Handles events from the editor button and from
   * the dialog's OK button.
   */
  @Override public void actionPerformed(ActionEvent e) {
    if (EDIT.equals(e.getActionCommand())) {
      // The user has clicked the cell, so
      // bring up the dialog.
      button.setBackground(currentColor);
      button.setIcon(new ColorIcon(currentColor));
      colorChooser.setColor(currentColor);
      dialog.setVisible(true);

      // Make the renderer reappear.
      fireEditingStopped();
    } else { // User pressed dialog's "OK" button.
      currentColor = colorChooser.getColor();
    }
  }

  // Implement the one CellEditor method that AbstractCellEditor doesn't.
  @Override public Object getCellEditorValue() {
    return currentColor;
  }

  // Implement the one method defined by TableCellEditor.
  @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    currentColor = (Color) value;
    button.setIcon(new ColorIcon(currentColor));
    int r = currentColor.getRed();
    int g = currentColor.getGreen();
    int b = currentColor.getBlue();
    button.setText(String.format("(%d, %d, %d)", r, g, b));
    return button;
  }
}

class ColorIcon implements Icon {
  private final Color color;

  protected ColorIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(color);
    g2.fillRect(0, 0, getIconWidth(), getIconHeight());
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 10;
  }

  @Override public int getIconHeight() {
    return 10;
  }
}
