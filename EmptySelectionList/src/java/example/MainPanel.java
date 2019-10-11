// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 0));
    JList<String> list1 = new JList<>(makeModel());
    list1.setEnabled(false);

    // System.out.println(UIManager.getBorder("List.focusCellHighlightBorder"));
    JList<String> list2 = new JList<>(makeModel());
    list2.setFocusable(false);
    list2.setSelectionModel(new DefaultListSelectionModel() {
      @Override public boolean isSelectedIndex(int index) {
        return false;
      }
    });

    JList<String> list3 = new JList<>(makeModel());
    ListCellRenderer<? super String> renderer = list3.getCellRenderer(); // new DefaultListCellRenderer();
    list3.setCellRenderer((list, value, index, isSelected, cellHasFocus) ->
        renderer.getListCellRendererComponent(list, value, index, false, false));

    add(new JScrollPane(list1));
    add(new JScrollPane(list2));
    add(new JScrollPane(list3));
    setPreferredSize(new Dimension(320, 240));
  }

  private static ListModel<String> makeModel() {
    DefaultListModel<String> model = new DefaultListModel<>();
    model.addElement("0000000000000");
    model.addElement("11111");
    model.addElement("222");
    model.addElement("333333333333");
    model.addElement("444444444");
    model.addElement("55555555555");
    model.addElement("666666");
    model.addElement("7777777777");
    model.addElement("88888888888888");
    model.addElement("99");
    return model;
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
