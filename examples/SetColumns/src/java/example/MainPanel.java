// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextField field = new JTextField(20);
    field.setToolTipText("setColumns(20)");

    JPasswordField passwd = new JPasswordField(20);
    passwd.setToolTipText("setColumns(20)");

    JSpinner spinner = new JSpinner();
    spinner.setToolTipText("setColumns(20)");
    ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setColumns(20);

    JComboBox<String> combo1 = new JComboBox<>();
    combo1.setEditable(true);
    combo1.setToolTipText("setEditable(true), setColumns(20)");
    ((JTextField) combo1.getEditor().getEditorComponent()).setColumns(20);

    JComboBox<String> combo2 = new JComboBox<>();
    combo2.setToolTipText("setEditable(true), default");
    combo2.setEditable(true);

    JComboBox<String> combo3 = new JComboBox<>();
    combo3.setToolTipText("setEditable(false), default");

    Component[] list = {field, passwd, spinner, combo1, combo2, combo3};
    add(makePanel(list));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JPanel makePanel(Component... c) {
    SpringLayout layout = new SpringLayout();
    JPanel p = new JPanel(layout);
    layout.putConstraint(SpringLayout.WEST, c[0], 10, SpringLayout.WEST, p);
    layout.putConstraint(SpringLayout.WEST, c[1], 10, SpringLayout.WEST, p);
    layout.putConstraint(SpringLayout.WEST, c[2], 10, SpringLayout.WEST, p);
    layout.putConstraint(SpringLayout.WEST, c[3], 10, SpringLayout.WEST, p);
    layout.putConstraint(SpringLayout.WEST, c[4], 10, SpringLayout.WEST, p);
    layout.putConstraint(SpringLayout.WEST, c[5], 10, SpringLayout.WEST, p);
    layout.putConstraint(SpringLayout.NORTH, c[0], 10, SpringLayout.NORTH, p);
    layout.putConstraint(SpringLayout.NORTH, c[1], 10, SpringLayout.SOUTH, c[0]);
    layout.putConstraint(SpringLayout.NORTH, c[2], 10, SpringLayout.SOUTH, c[1]);
    layout.putConstraint(SpringLayout.NORTH, c[3], 10, SpringLayout.SOUTH, c[2]);
    layout.putConstraint(SpringLayout.NORTH, c[4], 10, SpringLayout.SOUTH, c[3]);
    layout.putConstraint(SpringLayout.NORTH, c[5], 10, SpringLayout.SOUTH, c[4]);
    Arrays.asList(c).forEach(p::add);
    return p;
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
