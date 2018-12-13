// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final JSpinner spinner = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1));
  private final JSpinner spinner0 = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1));
  private final JSpinner spinner1 = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1));

  public MainPanel() {
    super(new BorderLayout());

    // UIManager.put("FormattedTextField.inactiveBackground", Color.RED);
    JTextField field = ((JSpinner.NumberEditor) spinner.getEditor()).getTextField();
    field.setEditable(false);
    field.setBackground(UIManager.getColor("FormattedTextField.background"));

    spinner1.setEnabled(false);

    Box box = Box.createVerticalBox();
    box.add(makeTitledPanel("Default", spinner0));
    box.add(makeTitledPanel("spinner.setEnabled(false)", spinner1));
    box.add(makeTitledPanel("field.setEnabled(false)", spinner));

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
