// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel()) {
      @Override public void updateUI() {
        super.updateUI();
        setAutoCreateRowSorter(true);
        setRowSelectionAllowed(true);
        setFillsViewportHeight(true);
        setShowVerticalLines(false);
        setShowHorizontalLines(false);
        setFocusable(false);
        setIntercellSpacing(new Dimension());
        TableColumn column = getColumnModel().getColumn(2);
        column.setCellRenderer(new AlignDecimalCellRenderer());
      }
    };
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"String", "Double", "ALIGN_DECIMAL"};
    Object[][] data = {
        {"aaa", 1.4142, 1.4142}, {"bbb", 98.765, 98.765},
        {"CCC", 1.73, 1.73}, {"DDD", 0d, 0d}
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

class AlignDecimalCellRenderer implements TableCellRenderer {
  private final JPanel panel = new JPanel(new BorderLayout());
  private final JTextPane textPane = new JTextPane() {
    @Override public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      d.width = 60;
      return d;
    }

    @Override public void updateUI() {
      super.updateUI();
      setOpaque(false);
      putClientProperty(HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
      EventQueue.invokeLater(() -> {
        // MutableAttributeSet attr = new SimpleAttributeSet();
        Style attr = getStyle(StyleContext.DEFAULT_STYLE);
        TabStop[] ts = {new TabStop(25f, TabStop.ALIGN_DECIMAL, TabStop.LEAD_NONE)};
        StyleConstants.setTabSet(attr, new TabSet(ts));
        setParagraphAttributes(attr, false);
      });
    }
  };

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    textPane.setFont(table.getFont());
    textPane.setText("\t" + Objects.toString(value, ""));
    if (isSelected) {
      textPane.setForeground(table.getSelectionForeground());
      panel.setBackground(table.getSelectionBackground());
    } else {
      textPane.setForeground(table.getForeground());
      panel.setBackground(table.getBackground());
    }
    panel.add(textPane, BorderLayout.EAST);
    return panel;
  }
}
