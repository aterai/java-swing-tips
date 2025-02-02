// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    ComboBoxModel<ListItem> model = makeModel();
    JComboBox<ListItem> combo1 = makeComboBox(model);
    JComboBox<ListItem> combo2 = makeComboBox(model);
    combo2.setEditable(true);
    JPanel p = new JPanel(new GridLayout(2, 1, 25, 25));
    p.add(makeTitledPanel("setEditable(false)", combo1));
    p.add(makeTitledPanel("setEditable(true)", combo2));
    add(p, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static ComboBoxModel<ListItem> makeModel() {
    DefaultComboBoxModel<ListItem> model = new DefaultComboBoxModel<>();
    model.addElement(new ListItem("1111"));
    model.addElement(new ListItem("1111222"));
    model.addElement(new ListItem("111122233"));
    model.addElement(new ListItem("444444", true));
    model.addElement(new ListItem("555"));
    model.addElement(new ListItem("6666666"));
    return model;
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
            ((JComponent) c).setBorder(value.getSeparatorBorder(index));
          }
          return c;
        });
      }
    };
  }

  private static Component makeTitledPanel(String title, Component c) {
    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createTitledBorder(title));
    box.add(c);
    return box;
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

  public Border getSeparatorBorder(int index) {
    Border b;
    if (index != -1 && hasSeparator()) {
      b = BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY);
    } else {
      b = BorderFactory.createEmptyBorder();
    }
    return b;
  }

  @Override public String toString() {
    return item;
  }
}
