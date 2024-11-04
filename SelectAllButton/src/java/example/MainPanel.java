// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel());

    Action selectAllAction = new AbstractAction("selectAll") {
      @Override public void actionPerformed(ActionEvent e) {
        e.setSource(table);
        table.getActionMap().get("selectAll").actionPerformed(e);
      }
    };
    Action copyAction = new AbstractAction("copy") {
      @Override public void actionPerformed(ActionEvent e) {
        e.setSource(table);
        table.getActionMap().get("copy").actionPerformed(e);
      }
    };

    JPanel p = new JPanel();
    p.add(new JButton(selectAllAction));
    p.add(new JButton(copyAction));

    JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    sp.setTopComponent(new JScrollPane(table));
    sp.setBottomComponent(new JScrollPane(new JTextArea()));
    sp.setResizeWeight(.5);

    JMenuBar menuBar = new JMenuBar();
    JMenu menu = new JMenu("Edit");
    menu.setMnemonic(KeyEvent.VK_E);
    menuBar.add(menu);
    menu.add(selectAllAction);
    menu.add(copyAction);

    add(p, BorderLayout.NORTH);
    add(sp);
    setPreferredSize(new Dimension(320, 240));
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(menuBar));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false}, {"CCC", 92, true}, {"DDD", 0, false}
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
