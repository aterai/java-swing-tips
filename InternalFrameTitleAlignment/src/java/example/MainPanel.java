// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicInternalFrameUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JDesktopPane desktop = new JDesktopPane();
    addFrame(desktop, 0);
    addFrame(desktop, 1);
    add(desktop);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void addFrame(JDesktopPane desktop, int idx) {
    String titleAlignment = idx == 0 ? "CENTER" : "LEADING";
    JInternalFrame frame = new JInternalFrame("title: " + titleAlignment, true, true, true, true);

    BasicInternalFrameUI ui = (BasicInternalFrameUI) frame.getUI();
    JComponent titleBar = (JComponent) ui.getNorthPane();
    UIDefaults d = new UIDefaults();
    d.put("InternalFrame:InternalFrameTitlePane.titleAlignment", titleAlignment);
    titleBar.putClientProperty("Nimbus.Overrides", d);

    frame.add(makePanel());
    frame.setSize(240, 100);
    frame.setVisible(true);
    frame.setLocation(10 + 60 * idx, 10 + 120 * idx);
    desktop.add(frame);
    desktop.getDesktopManager().activateFrame(frame);
  }

  private static Component makePanel() {
    JPanel p = new JPanel();
    p.add(new JLabel("label"));
    p.add(new JButton("button"));
    return p;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
      UIManager.getLookAndFeelDefaults().put("InternalFrame:InternalFrameTitlePane.titleAlignment", "LEADING");
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
