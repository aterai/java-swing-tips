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
    JToolBar toolBar = new JToolBar("ToolBar");
    toolBar.add(new JCheckBox("JCheckBox"));
    toolBar.add(new JTextField(10));

    EventQueue.invokeLater(() -> {
      Container w = getTopLevelAncestor();
      if (w instanceof Window) {
        // Setting a specific location for a floating JToolBar
        // https://stackoverflow.com/questions/41701664/setting-a-specific-location-for-a-floating-jtoolBar
        Point pt = w.getLocation();
        BasicToolBarUI ui = (BasicToolBarUI) toolBar.getUI();
        ui.setFloatingLocation(pt.x + 120, pt.y + 160);
        ui.setFloating(true, null);
      }
    });

    // // TEST: Here is another approach
    // EventQueue.invokeLater(() -> {
    //   Window w = (Window) getTopLevelAncestor();
    //   Point pt = w.getLocation();
    //   ((BasicToolBarUI) toolBar.getUI()).setFloating(true, null);
    //   Container c = toolBar.getTopLevelAncestor();
    //   if (c instanceof Window) {
    //     c.setLocation(pt.x + 120, pt.y + 160);
    //   }
    // });

    add(toolBar, BorderLayout.NORTH);
    add(Box.createHorizontalStrut(0), BorderLayout.WEST);
    add(Box.createHorizontalStrut(0), BorderLayout.EAST);
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
