// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    TableModel model = new DefaultTableModel(4, 3) {
      @Override public Class<?> getColumnClass(int column) {
        return Boolean.class;
      }
    };
    JTable table = new JTable(model) {
      private final Insets iconIns = new Insets(4, 4, 4, 4);
      private final transient Icon checkIcon = new CheckBoxIcon();

      @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        if (c instanceof JCheckBox) {
          int s = getRowHeight(row) - iconIns.top - iconIns.bottom;
          JCheckBox cb = (JCheckBox) c;
          cb.setIcon(new ScaledIcon(checkIcon, s, s));
          cb.setBorderPainted(false);
        }
        return c;
      }

      @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
        Component c = super.prepareEditor(editor, row, column);
        if (c instanceof JCheckBox) {
          int s = getRowHeight(row) - iconIns.top - iconIns.bottom;
          JCheckBox cb = (JCheckBox) c;
          cb.setIcon(new ScaledIcon(checkIcon, s, s));
          cb.setBackground(getSelectionBackground());
        }
        return c;
      }
    };
    table.setRowHeight(40);
    table.setSelectionBackground(Color.WHITE);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
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

class ScaledIcon implements Icon {
  private final Icon icon;
  private final int width;
  private final int height;

  protected ScaledIcon(Icon icon, int width, int height) {
    this.icon = icon;
    this.width = width;
    this.height = height;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.translate(x, y);
    double sx = width / (double) icon.getIconWidth();
    double sy = height / (double) icon.getIconHeight();
    g2.scale(sx, sy);
    icon.paintIcon(c, g2, 0, 0);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return width;
  }

  @Override public int getIconHeight() {
    return height;
  }
}

class CheckBoxIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    if (!(c instanceof AbstractButton)) {
      return;
    }
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.translate(x, y);
    g2.setPaint(Color.DARK_GRAY);
    float s = Math.min(getIconWidth(), getIconHeight()) * .05f;
    float w = getIconWidth() - s - s;
    float h = getIconHeight() - s - s;
    float gw = w / 8f;
    float gh = h / 8f;
    g2.setStroke(new BasicStroke(s));
    g2.draw(new Rectangle2D.Float(s, s, w, h));
    AbstractButton b = (AbstractButton) c;
    if (b.getModel().isSelected()) {
      g2.setStroke(new BasicStroke(3f * s));
      Path2D p = new Path2D.Float();
      p.moveTo(x + 2f * gw, y + .5f * h);
      p.lineTo(x + .4f * w, y + h - 2f * gh);
      p.lineTo(x + w - 2f * gw, y + 2f * gh);
      g2.draw(p);
    }
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 1000;
  }

  @Override public int getIconHeight() {
    return 1000;
  }
}
