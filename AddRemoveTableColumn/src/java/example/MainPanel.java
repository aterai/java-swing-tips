// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    UIManager.put("CheckBoxMenuItem.doNotCloseOnMouseClick", true);

    JTable table = new JTable(new DefaultTableModel(12, 8));
    table.getTableHeader().setComponentPopupMenu(new TableHeaderPopupMenu(table));

    // JPopupMenu pop = new TableHeaderPopupMenu(table);
    // JTableHeader header = table.getTableHeader();
    // header.setComponentPopupMenu(pop);
    // pop.addPopupMenuListener(new PopupMenuListener() {
    //   @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    //     cleanupHeader();
    //   }
    //
    //   @Override public void popupMenuCanceled(PopupMenuEvent e) {
    //     /* not needed */
    //   }
    //
    //   @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    //     cleanupHeader(); // Java 9 doNotCloseOnMouseClick ArrayIndexOutOfBoundsException
    //   }
    //
    //   private void cleanupHeader() {
    //     header.setDraggedColumn(null);
    //     // header.repaint();
    //     // table.repaint();
    //     repaint();
    //   }
    // });

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

class TableHeaderPopupMenu extends JPopupMenu {
  protected TableHeaderPopupMenu(JTable table) {
    super();
    TableColumnModel columnModel = table.getColumnModel();
    List<TableColumn> list = Collections.list(columnModel.getColumns());
    list.forEach(tableColumn -> {
      String name = Objects.toString(tableColumn.getHeaderValue());
      // System.out.format("%s - %s%n", name, tableColumn.getIdentifier());
      JCheckBoxMenuItem item = new JCheckBoxMenuItem(name, true);
      item.addItemListener(e -> {
        if (((AbstractButton) e.getItemSelectable()).isSelected()) {
          columnModel.addColumn(tableColumn);
        } else {
          columnModel.removeColumn(tableColumn);
        }
        updateMenuItems(columnModel);
      });
      add(item);
    });
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTableHeader) {
      JTableHeader header = (JTableHeader) c;
      header.setDraggedColumn(null);
      header.repaint();
      header.getTable().repaint();
      updateMenuItems(header.getColumnModel());
      super.show(c, x, y);
    }
  }

  private void updateMenuItems(TableColumnModel columnModel) {
    boolean isOnlyOneMenu = columnModel.getColumnCount() == 1;
    if (isOnlyOneMenu) {
      descendants(this).map(MenuElement::getComponent).forEach(mi ->
          mi.setEnabled(!(mi instanceof AbstractButton) || !((AbstractButton) mi).isSelected()));
    } else {
      descendants(this).forEach(me -> me.getComponent().setEnabled(true));
    }
  }

  private static Stream<MenuElement> descendants(MenuElement me) {
    return Stream.of(me.getSubElements())
      .flatMap(m -> Stream.concat(Stream.of(m), descendants(m)));
  }
}
