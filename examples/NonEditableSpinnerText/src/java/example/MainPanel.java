// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    SpinnerModel model = new SpinnerNumberModel(10, 0, 1000, 1);

    JSpinner disabledSpinner = new JSpinner(model);
    disabledSpinner.setEnabled(false);

    JSpinner readOnlySpinner = new JSpinner(model);
    JTextField field = ((JSpinner.DefaultEditor) readOnlySpinner.getEditor()).getTextField();
    field.setEditable(false);
    // A non-editable text field is painted with the inactive background color,
    // so restore the normal background to look the same as the editable one
    field.setBackground(UIManager.getColor("FormattedTextField.background"));

    Box box = Box.createVerticalBox();
    box.add(makeTitledPanel("Default", new JSpinner(model)));
    box.add(Box.createVerticalStrut(10));
    box.add(makeTitledPanel("spinner.setEnabled(false)", disabledSpinner));
    box.add(Box.createVerticalStrut(10));
    box.add(makeTitledPanel("field.setEditable(false)", readOnlySpinner));

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
