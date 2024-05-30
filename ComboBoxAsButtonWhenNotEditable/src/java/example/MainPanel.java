// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    // UIManager.put("ComboBox.buttonWhenNotEditable", false);
    JComboBox<String> combo = makeComboBox(false);
    UIDefaults d = UIManager.getLookAndFeelDefaults();
    d.put("ComboBox.buttonWhenNotEditable", false);
    combo.putClientProperty("Nimbus.Overrides", d);
    combo.putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.TRUE);

    Box box = Box.createVerticalBox();
    box.add(makeTitledPanel("Default(editable):", makeComboBox(true)));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("Default:", makeComboBox(false)));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("ComboBox.buttonWhenNotEditable: FALSE", combo));
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
  }

  private static JComboBox<String> makeComboBox(boolean editable) {
    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    model.addElement("11111");
    model.addElement("22222222");
    model.addElement("33333333333");
    JComboBox<String> combo = new JComboBox<>(model);
    combo.setEditable(editable);
    return combo;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
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
