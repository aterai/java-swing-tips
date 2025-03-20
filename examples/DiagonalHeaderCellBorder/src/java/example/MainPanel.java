// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.border.Border;
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
        TableCellRenderer hr = new VerticalTableHeaderRenderer();
        TableColumnModel cm = getColumnModel();
        cm.getColumn(0).setHeaderRenderer(new DiagonallySplitHeaderRenderer());
        cm.getColumn(0).setPreferredWidth(size * 5);
        for (int i = 1; i < cm.getColumnCount(); i++) {
          TableColumn tc = cm.getColumn(i);
          tc.setHeaderRenderer(hr);
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
        {"ccc", false, true, false, true}
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

class DiagonallySplitBorder extends MatteBorder {
  protected DiagonallySplitBorder(int top, int left, int bottom, int right, Color matteColor) {
    super(top, left, bottom, right, matteColor);
  }

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    super.paintBorder(c, g, x, y, width, height);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(getMatteColor());
    g2.drawLine(0, 0, c.getWidth() - 1, c.getHeight() - 1);
    g2.dispose();
  }
}

class DiagonallySplitHeaderRenderer implements TableCellRenderer {
  private final JPanel panel = new JPanel(new BorderLayout());
  private final JLabel trl = new JLabel("TOP-RIGHT", null, SwingConstants.RIGHT);
  private final JLabel bll = new JLabel("BOTTOM-LEFT", null, SwingConstants.LEFT);
  // private final LayerUI<Component> layerUI = new DiagonallySplitCellLayerUI();
  private final Border splitBorder = new DiagonallySplitBorder(0, 0, 1, 1, Color.GRAY);

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    trl.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 4));
    bll.setBorder(BorderFactory.createEmptyBorder(0, 4, 8, 0));
    panel.setOpaque(true);
    panel.setBackground(Color.WHITE);
    panel.setBorder(splitBorder);
    panel.add(trl, BorderLayout.NORTH);
    panel.add(bll, BorderLayout.SOUTH);
    return panel; // new JLayer<>(panel, layerUI);
  }
}

class VerticalTableHeaderRenderer implements TableCellRenderer {
  private final JPanel intermediate = new JPanel();
  private final JLabel label = new JLabel("", null, SwingConstants.LEADING);

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
    Component c = r.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);
    if (c instanceof JLabel) {
      JLabel l = (JLabel) c;
      label.setText(l.getText());
      label.setHorizontalTextPosition(SwingConstants.LEFT);
      label.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
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
    Graphics2D g2 = (Graphics2D) bi.getGraphics();
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
//       g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//       g2.setPaint(Color.GRAY);
//       g2.drawLine(0, 0, c.getWidth() - 1, c.getHeight() - 1);
//       g2.dispose();
//     }
//   }
// }
