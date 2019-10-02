// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    UIManager.put("SplitPane.oneTouchButtonSize", 32);
    UIManager.put("SplitPane.oneTouchButtonOffset", 50);
    UIManager.put("SplitPane.centerOneTouchButtons", true);

    UIManager.put("SplitPaneDivider.border", BorderFactory.createLineBorder(Color.RED, 10));
    UIManager.put("SplitPaneDivider.draggingColor", new Color(0x64_FF_64_64, true));

    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT) {
      @Override public void updateUI() {
        super.updateUI();
        BasicSplitPaneDivider divider = ((BasicSplitPaneUI) getUI()).getDivider();
        // divider.setBorder(BorderFactory.createMatteBorder(20, 0, 5, 0, Color.RED)); // bug?
        // divider.setBorder(BorderFactory.createLineBorder(Color.RED, 10));
        divider.setBackground(Color.ORANGE);
        for (Component c: divider.getComponents()) {
          if (c instanceof JButton) {
            JButton b = (JButton) c;
            // @see BasicSplitPaneDivider#createLeftOneTouchButton()
            // @see BasicSplitPaneDivider#createRightOneTouchButton()
            // public void paint(Graphics g) {
            //  ... then draw the arrow.
            //  g.setColor(Color.black);
            //  g.fillPolygon(xs, ys, 3);
            //  ...
            // b.setOpaque(false);
            // b.setForeground(Color.BLUE);
            b.setBackground(Color.ORANGE);
          }
        }
      }
    };
    splitPane.setTopComponent(new JScrollPane(new JTable(8, 3)));
    splitPane.setBottomComponent(new JScrollPane(new JTree()));
    splitPane.setOneTouchExpandable(true);
    splitPane.setDividerSize(32);

    BasicSplitPaneDivider divider = ((BasicSplitPaneUI) splitPane.getUI()).getDivider();
    divider.setBackground(Color.ORANGE);
    for (Component c: divider.getComponents()) {
      if (c instanceof JButton) {
        JButton b = (JButton) c;
        b.setBackground(Color.ORANGE);
      }
    }

    add(splitPane);
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
