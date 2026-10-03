// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    int size = 32;
    JTable table = new JTable(makeModel()) {
      @Override public void updateUI() {
        super.updateUI();
        setRowHeight(size);
        setAutoResizeMode(AUTO_RESIZE_OFF);
        TableCellRenderer verticalRenderer = new VerticalTableHeaderRenderer();
        TableColumnModel cm = getColumnModel();
        TableColumn firstColumn = cm.getColumn(0);
        firstColumn.setHeaderRenderer(new DiagonallySplitHeaderRenderer());
        firstColumn.setPreferredWidth(size * 5);
        for (int i = 1; i < cm.getColumnCount(); i++) {
          TableColumn tc = cm.getColumn(i);
          tc.setHeaderRenderer(verticalRenderer);
          tc.setPreferredWidth(size);
        }
      }
    };
    JScrollPane scroll = new JScrollPane(table);
    scroll.setColumnHeader(new JViewport() {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = size * 2;
        return d;
      }
    });
    add(scroll);
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"", "Boolean1", "Boolean2", "Boolean3", "Boolean4"};
    Object[][] data = {
        {"aaa", true, true, false, true},
        {"bbb", false, false, false, true},
        {"ccc", false, true, false, true},
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

class DiagonallySplitBorder extends MatteBorder {
  protected DiagonallySplitBorder(
      int top, int left, int bottom, int right, Color matteColor) {
    super(top, left, bottom, right, matteColor);
  }

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    super.paintBorder(c, g, x, y, width, height);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(getMatteColor());
    g2.drawLine(0, 0, width - 1, height - 1);
    g2.dispose();
  }
}

class DiagonallySplitHeaderRenderer implements TableCellRenderer {
  private final JPanel panel = new JPanel(new BorderLayout());
  // private final LayerUI<Component> layerUI = new DiagonallySplitCellLayerUI();

  protected DiagonallySplitHeaderRenderer() {
    JLabel columnHeader = new JLabel("TOP-RIGHT", null, SwingConstants.RIGHT);
    columnHeader.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 4));
    JLabel rowHeader = new JLabel("BOTTOM-LEFT", null, SwingConstants.LEFT);
    rowHeader.setBorder(BorderFactory.createEmptyBorder(0, 4, 8, 0));
    panel.setOpaque(true);
    panel.setBackground(Color.WHITE);
    panel.setBorder(new DiagonallySplitBorder(0, 0, 1, 1, Color.GRAY));
    panel.add(columnHeader, BorderLayout.NORTH);
    panel.add(rowHeader, BorderLayout.SOUTH);
  }

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    return panel; // new JLayer<>(panel, layerUI);
  }
}

class VerticalTableHeaderRenderer implements TableCellRenderer {
  private final JPanel intermediate = new JPanel();
  private final JLabel label = new JLabel("", null, SwingConstants.LEADING);

  protected VerticalTableHeaderRenderer() {
    label.setHorizontalTextPosition(SwingConstants.LEFT);
    label.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
  }

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
    Component c = r.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);
    if (c instanceof JLabel) {
      JLabel l = (JLabel) c;
      label.setText(l.getText());
      label.setFont(l.getFont());
      label.setForeground(l.getForeground());
      l.setHorizontalAlignment(SwingConstants.CENTER);
      l.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.GRAY));
      l.setIcon(makeVerticalHeaderIcon(label));
      l.setText(null);
    }
    return c;
  }

  private Icon makeVerticalHeaderIcon(Component c) {
    Dimension d = c.getPreferredSize();
    int w = d.height;
    int h = d.width;
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    AffineTransform at = AffineTransform.getTranslateInstance(0, h);
    at.quadrantRotate(-1);
    g2.setTransform(at);
    SwingUtilities.paintComponent(g2, c, intermediate, 0, 0, h, w);
    g2.dispose();
    return new ImageIcon(bi);
  }
}

// class DiagonallySplitCellLayerUI extends LayerUI<Component> {
//   @Override public void paint(Graphics g, JComponent c) {
//     super.paint(g, c);
//     if (c instanceof JLayer) {
//       Graphics2D g2 = (Graphics2D) g.create();
//       g2.setRenderingHint(
//           RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//       g2.setPaint(Color.GRAY);
//       g2.drawLine(0, 0, c.getWidth() - 1, c.getHeight() - 1);
//       g2.dispose();
//     }
//   }
// }
