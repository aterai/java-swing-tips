package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JComboBox<MyItem> combobox1 = makeComboBox();
    JComboBox<MyItem> combobox2 = makeComboBox();
    combobox2.setEditable(true);

    Box box1 = Box.createVerticalBox();
    box1.setBorder(BorderFactory.createTitledBorder("setEditable(false)"));
    box1.add(combobox1);

    Box box2 = Box.createVerticalBox();
    box2.setBorder(BorderFactory.createTitledBorder("setEditable(true)"));
    box2.add(combobox2);

    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(box1, BorderLayout.NORTH);
    add(box2, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JComboBox<MyItem> makeComboBox() {
    DefaultComboBoxModel<MyItem> model = new DefaultComboBoxModel<>();
    model.addElement(new MyItem("aaaa"));
    model.addElement(new MyItem("aaaabbb"));
    model.addElement(new MyItem("aaaabbbcc"));
    model.addElement(new MyItem("eeeeeeeee", true));
    model.addElement(new MyItem("bbb1"));
    model.addElement(new MyItem("bbb12"));

    JComboBox<MyItem> combo = new JComboBox<>(model);
    combo.setRenderer(new DefaultListCellRenderer() {
      @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        MyItem item = (MyItem) value;
        JLabel label = (JLabel) super.getListCellRendererComponent(list, item, index, isSelected, cellHasFocus);
        if (index != -1 && item.hasSeparator()) {
          label.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
        } else {
          label.setBorder(BorderFactory.createEmptyBorder());
        }
        return label;
      }
    });
    return combo;
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException
         | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class MyItem {
  private final String item;
  private final boolean flag;

  protected MyItem(String str) {
    this(str, false);
  }

  protected MyItem(String str, boolean flg) {
    item = str;
    flag = flg;
  }

  public boolean hasSeparator() {
    return flag;
  }

  @Override public String toString() {
    return item;
  }
}
