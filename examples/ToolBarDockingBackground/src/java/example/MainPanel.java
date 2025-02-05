// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicToolBarUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    UIManager.put("ToolBar.dockingBackground", new Color(0x64_FF_00_00, true));
    UIManager.put("ToolBar.floatingBackground", new Color(0x64_00_00_FF, true));
    UIManager.put("ToolBar.dockingForeground", Color.BLUE);
    UIManager.put("ToolBar.floatingForeground", Color.RED);

    JCheckBox check = new JCheckBox("translucent", true);
    JToolBar toolBar = new JToolBar();
    toolBar.add(check);
    toolBar.add(Box.createRigidArea(new Dimension(5, 5)));
    toolBar.add(new JButton("Button"));
    toolBar.add(Box.createRigidArea(new Dimension(5, 5)));
    toolBar.add(new JRadioButton("RadioButton"));
    toolBar.add(Box.createGlue());

    check.addActionListener(e -> {
      // if (((JCheckBox) e.getSource()).isSelected()) {
      //   UIManager.put(dockingBackground, new Color(0xAA_FF_00_00, true));
      //   UIManager.put(floatingBackground, new Color(0xAA_00_00_FF, true));
      // } else {
      //   UIManager.put(dockingBackground, Color.RED);
      //   UIManager.put(floatingBackground, Color.BLUE);
      // }
      // toolBar.updateUI();
      BasicToolBarUI ui = (BasicToolBarUI) toolBar.getUI();
      if (((JCheckBox) e.getSource()).isSelected()) {
        ui.setDockingColor(new Color(0x64_FF_00_00, true));
        ui.setFloatingColor(new Color(0x64_00_00_FF, true));
      } else {
        ui.setDockingColor(Color.RED);
        ui.setFloatingColor(Color.BLUE);
      }
    });

    add(toolBar, BorderLayout.NORTH);
    add(new JScrollPane(new JTree()));
    setPreferredSize(new Dimension(320, 240));
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
