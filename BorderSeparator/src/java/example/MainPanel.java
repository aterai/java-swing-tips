// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    DefaultComboBoxModel<ListItem> model = new DefaultComboBoxModel<>();
    model.addElement(new ListItem("1111"));
    model.addElement(new ListItem("1111222"));
    model.addElement(new ListItem("111122233"));
    model.addElement(new ListItem("444444", true));
    model.addElement(new ListItem("555"));
    model.addElement(new ListItem("6666666"));

    JComboBox<ListItem> combo1 = makeComboBox(model);
    JComboBox<ListItem> combo2 = makeComboBox(model);
    combo2.setEditable(true);

    Box box1 = Box.createVerticalBox();
    box1.setBorder(BorderFactory.createTitledBorder("setEditable(false)"));
    box1.add(combo1);

    Box box2 = Box.createVerticalBox();
    box2.setBorder(BorderFactory.createTitledBorder("setEditable(true)"));
    box2.add(combo2);

    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(box1, BorderLayout.NORTH);
    add(box2, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JComboBox<ListItem> makeComboBox(ComboBoxModel<ListItem> model) {
    return new JComboBox<ListItem>(model) {
      @Override public void updateUI() {
        setRenderer(null);
        super.updateUI();
        ListCellRenderer<? super ListItem> renderer = getRenderer();
        setRenderer((list, value, index, isSelected, cellHasFocus) -> {
          Component c = renderer.getListCellRendererComponent(
              list, value, index, isSelected, cellHasFocus);
          if (c instanceof JComponent) {
            JComponent jc = (JComponent) c;
            if (index != -1 && value.hasSeparator()) {
              jc.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
            } else {
              jc.setBorder(BorderFactory.createEmptyBorder());
            }
          }
          return c;
        });
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

class ListItem {
  private final String item;
  private final boolean flag;

  protected ListItem(String str) {
    this(str, false);
  }

  protected ListItem(String str, boolean flg) {
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
