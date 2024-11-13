// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel()) {
      private static final int LIST_ICON_COLUMN = 1;

      @Override public String getToolTipText(MouseEvent e) {
        String txt = super.getToolTipText(e);
        Point pt = e.getPoint();
        int row = rowAtPoint(pt);
        int col = columnAtPoint(pt);
        if (row >= 0 && col >= 0) {
          TableCellRenderer tcr = getCellRenderer(row, col);
          Component c = prepareRenderer(tcr, row, col);
          int mci = convertColumnIndexToModel(col);
          if (mci == LIST_ICON_COLUMN && c instanceof JPanel) {
            txt = getToolTipText(e, c);
          }
        }
        return txt;
      }

      private String getToolTipText(MouseEvent e, Component c) {
        Point pt = e.getPoint();
        int row = rowAtPoint(pt);
        int col = columnAtPoint(pt);
        Rectangle r = getCellRect(row, col, true);
        c.setBounds(r);
        // https://stackoverflow.com/questions/10854831/tool-tip-in-jpanel-in-jtable-not-working
        c.doLayout();
        pt.translate(-r.x, -r.y);
        return Optional.ofNullable(SwingUtilities.getDeepestComponentAt(c, pt.x, pt.y))
            .filter(JLabel.class::isInstance)
            .map(JLabel.class::cast)
            .map(l -> ((ImageIcon) l.getIcon()).getDescription())
            .orElseGet(() -> super.getToolTipText(e));
      }

      @Override public void updateUI() {
        // [JDK-6788475]
        // Changing to Nimbus LAF and back doesn't reset look and feel of JTable completely
        // https://bugs.openjdk.org/browse/JDK-6788475
        setSelectionForeground(new ColorUIResource(Color.RED));
        setSelectionBackground(new ColorUIResource(Color.RED));
        super.updateUI();
        getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer());
        getColumnModel().getColumn(LIST_ICON_COLUMN).setCellRenderer(new ListIconRenderer());
        setRowHeight(40);
      }
    };
    // table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setFillsViewportHeight(true);
    table.setAutoCreateRowSorter(true);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"String", "List<Icon>"};
    Icon informationIcon = getOptionPaneIcon("OptionPane.informationIcon");
    Icon errorIcon = getOptionPaneIcon("OptionPane.errorIcon");
    Icon questionIcon = getOptionPaneIcon("OptionPane.questionIcon");
    Icon warningIcon = getOptionPaneIcon("OptionPane.warningIcon");
    Object[][] data = {
        {"aa", Arrays.asList(informationIcon, errorIcon)},
        {"bb", Arrays.asList(errorIcon, informationIcon, warningIcon, questionIcon)},
        {"cc", Arrays.asList(questionIcon, errorIcon, warningIcon)},
        {"dd", Collections.singletonList(informationIcon)},
        {"ee", Arrays.asList(warningIcon, questionIcon)}
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
  }

  public static Icon getOptionPaneIcon(String key) {
    ImageIcon icon = (ImageIcon) UIManager.getIcon(key);
    icon.setDescription(key);
    return icon;
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

class ListIconRenderer implements TableCellRenderer {
  private final JPanel renderer = new JPanel(new FlowLayout(FlowLayout.LEFT));

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    renderer.removeAll();
    if (isSelected) {
      renderer.setOpaque(true);
      renderer.setBackground(table.getSelectionBackground());
    } else {
      renderer.setOpaque(false);
      // renderer.setBackground(table.getBackground());
    }
    if (value instanceof List<?>) {
      ((List<?>) value).stream()
          .filter(Icon.class::isInstance)
          .map(Icon.class::cast)
          .map(ListIconRenderer::makeLabel)
          .forEach(renderer::add);
    }
    return renderer;
  }

  private static Component makeLabel(Icon icon) {
    JLabel label = new JLabel(icon);
    label.setToolTipText(icon.toString());
    return label;
  }
}
