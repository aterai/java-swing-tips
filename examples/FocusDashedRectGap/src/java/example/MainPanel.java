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
    super(new BorderLayout());
    JTextArea log = new JTextArea();
    log.setFont(log.getFont().deriveFont(10f));
    log.append(info("Button.dashedRectGapX"));
    log.append(info("Button.dashedRectGapY"));
    log.append(info("Button.dashedRectGapHeight"));
    log.append(info("Button.dashedRectGapWidth"));

    UIManager.put("Button.dashedRectGapX", 5);
    UIManager.put("Button.dashedRectGapY", 5);
    UIManager.put("Button.dashedRectGapHeight", 10);
    UIManager.put("Button.dashedRectGapWidth", 10);

    UIManager.put("Button.margin", new Insets(8, 8, 8, 8));
    UIManager.put("ToggleButton.margin", new Insets(8, 8, 8, 8));
    UIManager.put("RadioButton.margin", new Insets(8, 8, 8, 8));
    UIManager.put("CheckBox.margin", new Insets(8, 8, 8, 8));

    JPanel p = new JPanel();
    p.add(new JButton("JButton"));
    p.add(Box.createHorizontalStrut(32));
    p.add(new JToggleButton("JToggleButton"));
    p.add(Box.createHorizontalStrut(32));

    p.add(new JCheckBox("JCheckBox"));
    p.add(new JCheckBox("JCheckBox + BorderPainted") {
      @Override public void updateUI() {
        super.updateUI();
        setBorderPainted(true);
      }
    });
    p.add(new JCheckBox(PAD + "JCheckBox + td.padding"));

    p.add(new JRadioButton("JRadioButton"));
    p.add(new JRadioButton(PAD + "JRadioButton + td.padding"));

    add(p);
    add(new JScrollPane(log), BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static String info(String key) {
    return String.format("%s: %d%n", key, UIManager.getInt(key));
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
