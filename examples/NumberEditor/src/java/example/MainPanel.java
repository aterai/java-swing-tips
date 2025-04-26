// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    JSpinner spinner1 = new JSpinner(new SpinnerNumberModel(0d, 0d, 1d, .01));
    JSpinner.NumberEditor editor1 = new JSpinner.NumberEditor(spinner1, "0%");
    // editor1.getTextField().setEditable(false);
    spinner1.setEditor(editor1);

    JSpinner spinner2 = new JSpinner(new SpinnerNumberModel(0d, 0d, 1d, .01));
    JSpinner.NumberEditor editor2 = new JSpinner.NumberEditor(spinner2, "0%");
    editor2.getTextField().setEditable(false);
    editor2.getTextField().setBackground(UIManager.getColor("FormattedTextField.background"));
    spinner2.setEditor(editor2);

    add(makeTitledPanel("JSpinner", spinner1));
    add(makeTitledPanel("getTextField().setEditable(false)", spinner2));
    setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component cmp) {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 1d;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(5, 5, 5, 5);
    p.add(cmp, c);
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
