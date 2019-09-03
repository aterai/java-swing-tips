// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.table.TableColumnModel;

public final class MainPanel extends JPanel {
  private final JTable table = new JTable(100, 3) {
    // https://stackoverflow.com/questions/16368343/jtable-resize-only-selected-column-when-container-size-changes
    // https://stackoverflow.com/questions/23201818/jtable-columns-doesnt-resize-probably-when-jframe-resize
    @Override public void doLayout() {
      Optional.ofNullable(getTableHeader()).ifPresent(header -> {
        if (Objects.isNull(header.getResizingColumn()) && getAutoResizeMode() == JTable.AUTO_RESIZE_LAST_COLUMN) {
          TableColumnModel tcm = getColumnModel();
          header.setResizingColumn(tcm.getColumn(tcm.getColumnCount() - 1));
        }
      });
      super.doLayout();
    }
  };

  private MainPanel() {
    super(new GridLayout(0, 1));
    // table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    add(makeTitledPanel("Normal JTable.AUTO_RESIZE_LAST_COLUMN", new JTable(100, 3)));
    add(makeTitledPanel("Resize only last column when JTable resized", table));

    // // TEST:
    // JTable table1 = new JTable(100, 3) {
    //   private transient ComponentListener resizeHandler;
    //   @Override public void updateUI() {
    //     removeComponentListener(resizeHandler);
    //     super.updateUI();
    //     resizeHandler = new ComponentAdapter() {
    //       @Override public void componentResized(ComponentEvent e) {
    //         Optional.ofNullable(getTableHeader()).ifPresent(header -> {
    //           if (header.getResizingColumn() != null && getAutoResizeMode() == JTable.AUTO_RESIZE_LAST_COLUMN) {
    //             TableColumnModel tcm = getColumnModel();
    //             header.setResizingColumn(tcm.getColumn(tcm.getColumnCount() - 1));
    //           }
    //         });
    //       }
    //     };
    //     addComponentListener(resizeHandler);
    //   }
    // };
    // add(makeTitledPanel("JTable#addComponentListener(...)", table1));
    //
    // JTable table2 = new JTable(100, 3);
    // table2.getTableHeader().addComponentListener(new ComponentAdapter() {
    //   @Override public void componentResized(ComponentEvent e) {
    //     Optional.ofNullable(table2.getTableHeader()).ifPresent(header -> {
    //       if (header.getResizingColumn() == null && table2.getAutoResizeMode() == JTable.AUTO_RESIZE_LAST_COLUMN) {
    //         TableColumnModel tcm = table2.getColumnModel();
    //         header.setResizingColumn(tcm.getColumn(tcm.getColumnCount() - 1));
    //       }
    //     });
    //   }
    // });
    // add(makeTitledPanel("JTableHeader#addComponentListener(...)", table2));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, JTable table) {
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
