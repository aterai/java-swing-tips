// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.text.ParseException;
import javax.swing.*;
import javax.swing.text.MaskFormatter;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    MaskFormatter formatter = createFormatter("UUUUUUUUUU");
    // formatter.setAllowsInvalid(true);
    // formatter.setCommitsOnValidEdit(true);
    // formatter.setPlaceholder("_");
    // formatter.setPlaceholderCharacter('?');

    JFormattedTextField field1 = new JFormattedTextField(formatter);
    field1.setFocusLostBehavior(JFormattedTextField.REVERT);

    JFormattedTextField field2 = new JFormattedTextField(formatter);
    field2.setFocusLostBehavior(JFormattedTextField.COMMIT);

    JFormattedTextField field3 = new JFormattedTextField(formatter);
    field3.setFocusLostBehavior(JFormattedTextField.PERSIST);

    JCheckBox check = new JCheckBox("setCommitsOnValidEdit");
    check.addActionListener(e -> formatter.setCommitsOnValidEdit(check.isSelected()));

    Box box = Box.createVerticalBox();
    box.add(makeTitledPanel("COMMIT_OR_REVERT(default)", new JFormattedTextField(formatter)));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("REVERT", field1));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("COMMIT", field2));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("PERSIST", field3));
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(box, BorderLayout.NORTH);
    add(check, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  public static MaskFormatter createFormatter(String s) {
    MaskFormatter formatter = null;
    try {
      formatter = new MaskFormatter(s);
    } catch (ParseException ex) {
      // System.err.println("formatter is bad: " + ex.getMessage());
      UIManager.getLookAndFeel().provideErrorFeedback(null);
      ex.printStackTrace();
    }
    return formatter;
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
