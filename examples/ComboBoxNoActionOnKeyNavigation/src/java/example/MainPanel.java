// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String key1 = "ComboBox.noActionOnKeyNavigation";
    JCheckBox check1 = new JCheckBox(key1, UIManager.getBoolean(key1));
    check1.addActionListener(e -> {
      JCheckBox c1 = (JCheckBox) e.getSource();
      UIManager.put(key1, c1.isSelected());
    });

    String key2 = "ComboBox.isEnterSelectablePopup";
    JCheckBox check2 = new JCheckBox(key2, UIManager.getBoolean(key2));
    check2.addActionListener(e -> {
      JCheckBox c2 = (JCheckBox) e.getSource();
      UIManager.put(key2, c2.isSelected());
    });

    JComboBox<String> combo1 = new JComboBox<>(makeModel());
    combo1.setEditable(false);

    JComboBox<String> combo2 = new JComboBox<>(makeModel());
    combo2.setEditable(true);

    Box box1 = Box.createVerticalBox();
    box1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    box1.add(check1);
    box1.add(Box.createVerticalStrut(5));
    box1.add(check2);

    Box box2 = Box.createVerticalBox();
    box2.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    box2.add(combo1);
    box2.add(Box.createVerticalStrut(10));
    box2.add(combo2);

    JPanel p = new JPanel(new GridLayout(2, 1));
    p.add(box1);
    p.add(box2);
    add(p, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static ComboBoxModel<String> makeModel() {
    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    model.addElement("00000");
    model.addElement("11111");
    model.addElement("22222");
    model.addElement("33333");
    model.addElement("44444");
    model.addElement("55555");
    model.addElement("66666");
    model.addElement("77777");
    model.addElement("88888");
    model.addElement("99999");
    return model;
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
