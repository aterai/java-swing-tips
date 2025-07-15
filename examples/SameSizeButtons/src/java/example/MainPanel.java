// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private final JCheckBox borderCheck = new JCheckBox("OptionPane.buttonAreaBorder");

  private MainPanel() {
    super(new BorderLayout());
    JOptionPane optionPane = new JOptionPane(
        "message", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION);

    JButton button1 = new JButton("default");
    button1.addActionListener(e -> showDefaultDialog(optionPane));

    JButton button2 = new JButton("sameSizeButtons");
    button2.addActionListener(e -> showSameSizeButtonsDialog(optionPane));

    JPanel p1 = new JPanel();
    p1.add(button1);
    p1.add(button2);

    JPanel p2 = new JPanel();
    p2.add(borderCheck);

    add(p1, BorderLayout.NORTH);
    add(p2, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private void showDefaultDialog(JOptionPane optionPane) {
    UIManager.getLookAndFeelDefaults().put("OptionPane.sameSizeButtons", false);
    // JOptionPane.showConfirmDialog(getRootPane(), "message");
    // JOptionPane pane1 = new JOptionPane(..., QUESTION_MESSAGE, YES_NO_CANCEL_OPTION);
    UIDefaults d = new UIDefaults();
    d.put("OptionPane.sameSizeButtons", false);
    optionPane.putClientProperty("Nimbus.Overrides", d);
    optionPane.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
    SwingUtilities.updateComponentTreeUI(optionPane);
    optionPane.createDialog(getRootPane(), "title").setVisible(true);
  }

  private void showSameSizeButtonsDialog(JOptionPane optionPane) {
    // UIManager.getLookAndFeelDefaults().put("OptionPane.sameSizeButtons", true);
    // UIManager.put("OptionPane.buttonAreaBorder", BorderFactory.createLineBorder(...));
    // JOptionPane.showConfirmDialog(getRootPane(), "message");
    UIDefaults d = new UIDefaults();
    Border b = borderCheck.isSelected()
        ? BorderFactory.createLineBorder(Color.RED, 10)
        : BorderFactory.createEmptyBorder();
    d.put("OptionPane.buttonAreaBorder", b);
    d.put("OptionPane.sameSizeButtons", true);
    optionPane.putClientProperty("Nimbus.Overrides", d);
    optionPane.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
    SwingUtilities.updateComponentTreeUI(optionPane);
    optionPane.createDialog(getRootPane(), "title").setVisible(true);
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
      // UIManager.getLookAndFeelDefaults().put("OptionPane.sameSizeButtons", true);
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
