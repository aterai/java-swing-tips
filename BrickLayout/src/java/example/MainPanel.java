// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final Dimension SIZE = new Dimension(6, 8);
  private static final int WIDTH = 2;

  private MainPanel() {
    super(new BorderLayout());
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Brick Layout"));
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    // c.gridy = GridBagConstraints.RELATIVE;
    for (int y = 0; y < SIZE.height; y++) {
      // // c.gridy = GridBagConstraints.RELATIVE; // c.gridy = y;
      // int d = y & 0b1; // = y % 2 == 0 ? 0 : 1;
      // if (d == 1) {
      //   c.gridwidth = 1;
      //   c.gridx = 0;
      //   panel.add(new JButton("s"), c);
      // }
      c.gridx = y & 0b1; // start x offset
      c.gridwidth = WIDTH;
      for (int x = 0; x < SIZE.width; x++) {
        panel.add(makeBrick(), c);
        c.gridx += WIDTH;
      }
      // if (d == 0) {
      //   c.gridwidth = 1;
      //   panel.add(new JButton("e"), c);
      // }
    }
    // GridBagLayout to create a board
    // https://community.oracle.com/thread/1357310
    // <guide-row>
    c.gridwidth = 1;
    // c.gridy = GridBagConstraints.REMAINDER;
    for (c.gridx = 0; c.gridx <= WIDTH * SIZE.width; c.gridx++) {
      panel.add(Box.createHorizontalStrut(24), c);
    }
    // </guide-row>

    add(panel);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeBrick() {
    return new JButton(" ");
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
    frame.setMinimumSize(new Dimension(300, 120));
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
