// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.TableColumnModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(0, 1));
    // https://stackoverflow.com/questions/16368343/jtable-resize-only-selected-column-when-container-size-changes
    // https://stackoverflow.com/questions/23201818/jtable-columns-doesnt-resize-probably-when-jframe-resize
    JTable resizeTable = new JTable(100, 3) {
      @Override public void doLayout() {
        if (getAutoResizeMode() == AUTO_RESIZE_LAST_COLUMN) {
          // Force the last column to absorb the width change even when the
          // JTableHeader itself is resized (e.g. via ancestor frame resizing).
          Optional.ofNullable(getTableHeader()).ifPresent(header -> {
            if (Objects.isNull(header.getResizingColumn())) {
              TableColumnModel cm = getColumnModel();
              header.setResizingColumn(cm.getColumn(cm.getColumnCount() - 1));
            }
          });
        }
        super.doLayout();
      }
    };
    add(createTitledPanel("Normal JTable.AUTO_RESIZE_LAST_COLUMN", new JTable(100, 3)));
    add(createTitledPanel("Resize only last column when JTable resized", resizeTable));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component createTitledPanel(String title, JTable table) {
    table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(new JScrollPane(table));
    return p;
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
