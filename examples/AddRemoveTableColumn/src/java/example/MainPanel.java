// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Collections;
import java.util.Objects;
import java.util.logging.Logger;
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

final class TableHeaderPopupMenu extends JPopupMenu {
  /* default */ TableHeaderPopupMenu(JTable table) {
    super();
    TableColumnModel columnModel = table.getColumnModel();
    for (TableColumn tableColumn : Collections.list(columnModel.getColumns())) {
      add(makeCheckBoxMenuItem(tableColumn, columnModel));
    }
  }

  private JMenuItem makeCheckBoxMenuItem(TableColumn column, TableColumnModel model) {
    String name = Objects.toString(column.getHeaderValue());
    // System.out.format("%s - %s%n", name, column.getIdentifier());
    JMenuItem item = new JCheckBoxMenuItem(name, true);
    item.addItemListener(e -> {
      if (((AbstractButton) e.getItemSelectable()).isSelected()) {
        model.addColumn(column);
      } else {
        model.removeColumn(column);
      }
      updateMenuItems(model.getColumnCount() == 1);
    });
    return item;
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTableHeader) {
      JTableHeader header = (JTableHeader) c;
      header.setDraggedColumn(null);
      header.repaint();
      header.getTable().repaint();
      updateMenuItems(header.getColumnModel().getColumnCount() == 1);
      super.show(c, x, y);
    }
  }

  private void updateMenuItems(boolean isOnlyOneMenu) {
    if (isOnlyOneMenu) {
      descendants(this)
          .map(MenuElement::getComponent)
          .filter(AbstractButton.class::isInstance)
          .map(AbstractButton.class::cast)
          .forEach(b -> b.setEnabled(!b.isSelected()));
    } else {
      descendants(this)
          .map(MenuElement::getComponent)
          .forEach(c -> c.setEnabled(true));
    }
  }

  private static Stream<MenuElement> descendants(MenuElement me) {
    return Stream.of(me.getSubElements())
        .flatMap(m -> Stream.concat(Stream.of(m), descendants(m)));
  }
}
