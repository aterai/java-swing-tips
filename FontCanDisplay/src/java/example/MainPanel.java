// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    int code = 0x1F512;
    JLabel label = new JLabel(new String(Character.toChars(code)));
    label.setFont(label.getFont().deriveFont(24f));
    label.setHorizontalAlignment(SwingConstants.CENTER);
    label.setVerticalAlignment(SwingConstants.CENTER);

    String[] columnNames = {"family", "name", "postscript name", "canDisplay", "isEmpty"};
    DefaultTableModel model = new DefaultTableModel(null, columnNames) {
      @Override public boolean isCellEditable(int row, int column) {
        return false;
      }

      @Override public Class<?> getColumnClass(int column) {
        return column > 2 ? Boolean.class : String.class;
      }
    };
    JTable table = new JTable(model);
    Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
    Stream.of(fonts)
        .map(f -> {
          String txt = new String(Character.toChars(code));
          FontRenderContext frc = getFontMetrics(f).getFontRenderContext();
          return new Object[] {
              f.getFamily(),
              f.getName(),
              f.getPSName(),
              f.canDisplay(code),
              f.createGlyphVector(frc, txt).getVisualBounds().isEmpty()
          };
        })
        .forEach(model::addRow);
    table.getSelectionModel().addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting() && table.getSelectedRowCount() == 1) {
        label.setFont(fonts[table.getSelectedRow()].deriveFont(24f));
      }
    });

    add(label, BorderLayout.NORTH);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
