// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    SpinnerModel model = new SpinnerNumberModel(10, 0, 1000, 1);

    JSpinner spinner1 = new JSpinner(model);
    spinner1.setEnabled(false);

    // UIManager.put("FormattedTextField.inactiveBackground", Color.RED);
    JSpinner spinner2 = new JSpinner(model);
    JTextField field = ((JSpinner.DefaultEditor) spinner2.getEditor()).getTextField();
    field.setEditable(false);
    field.setBackground(UIManager.getColor("FormattedTextField.background"));

    Box box = Box.createVerticalBox();
    box.add(makeTitledPanel("Default", new JSpinner(model)));
    box.add(Box.createVerticalStrut(10));
    box.add(makeTitledPanel("spinner.setEnabled(false)", spinner1));
    box.add(Box.createVerticalStrut(10));
    box.add(makeTitledPanel("editor.setEditable(false)", spinner2));

    add(box, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
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
