// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  // private static final String PAD = "<html><table><td height='32'>";
  // private static final String PAD = "<html><table cellpadding='0'>";
  private static final String PAD = "<html><table><td style='padding:1'>";

  private MainPanel() {
    super();
    System.out.println(UIManager.getInt("Button.dashedRectGapX"));
    System.out.println(UIManager.getInt("Button.dashedRectGapY"));
    System.out.println(UIManager.getInt("Button.dashedRectGapHeight"));
    System.out.println(UIManager.getInt("Button.dashedRectGapWidth"));

    UIManager.put("Button.dashedRectGapX", 5);
    UIManager.put("Button.dashedRectGapY", 5);
    UIManager.put("Button.dashedRectGapHeight", 10);
    UIManager.put("Button.dashedRectGapWidth", 10);

    UIManager.put("Button.margin", new Insets(8, 8, 8, 8));
    UIManager.put("ToggleButton.margin", new Insets(8, 8, 8, 8));
    UIManager.put("RadioButton.margin", new Insets(8, 8, 8, 8));
    UIManager.put("CheckBox.margin", new Insets(8, 8, 8, 8));

    add(new JButton("JButton"));
    add(Box.createHorizontalStrut(32));
    add(new JToggleButton("JToggleButton"));
    add(Box.createHorizontalStrut(32));

    add(new JCheckBox("JCheckBox"));
    add(new JCheckBox("JCheckBox") {
      @Override public void updateUI() {
        super.updateUI();
        setBorderPainted(true);
      }
    });
    add(new JCheckBox(PAD + "JCheckBox"));

    add(new JRadioButton("JRadioButton"));
    add(new JRadioButton(PAD + "JRadioButton"));

    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
