// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    int code = 0x1F512;
    JLabel label = new JLabel(String.valueOf(Character.toChars(code)));
    label.setFont(label.getFont().deriveFont(24f));
    label.setHorizontalAlignment(SwingConstants.CENTER);
    label.setVerticalAlignment(SwingConstants.CENTER);

    Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
    JTable table = new JTable(makeModel(fonts, code));
    table.getSelectionModel().addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting() && table.getSelectedRowCount() == 1) {
        label.setFont(fonts[table.getSelectedRow()].deriveFont(24f));
      }
    });

    add(label, BorderLayout.NORTH);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private TableModel makeModel(Font[] fonts, int code) {
    String[] columnNames = {"family", "name", "postscript name", "canDisplay", "isEmpty"};
    Object[][] data = Stream.of(fonts)
        .map(f -> {
          String txt = String.valueOf(Character.toChars(code));
          FontRenderContext frc = getFontMetrics(f).getFontRenderContext();
          return new Object[] {
              f.getFamily(),
              f.getName(),
              f.getPSName(),
              f.canDisplay(code),
              f.createGlyphVector(frc, txt).getVisualBounds().isEmpty()
          };
        })
        .toArray(Object[][]::new);
    return new DefaultTableModel(data, columnNames) {
      @Override public boolean isCellEditable(int row, int column) {
        return false;
      }

      @Override public Class<?> getColumnClass(int column) {
        return column > 2 ? Boolean.class : String.class;
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
