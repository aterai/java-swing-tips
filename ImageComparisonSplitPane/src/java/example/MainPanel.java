// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JCheckBox check = new JCheckBox("setXORMode(Color.BLUE)", true);
    check.addActionListener(e -> repaint());

    JSplitPane split = new JSplitPane();
    split.setContinuousLayout(true);
    split.setResizeWeight(.5);

    ImageIcon icon = new ImageIcon(getClass().getResource("test.png"));

    Component beforeCanvas = new JComponent() {
      @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(icon.getImage(), 0, 0, icon.getIconWidth(), icon.getIconHeight(), this);
      }
    };
    split.setLeftComponent(beforeCanvas);

    Component afterCanvas = new JComponent() {
      @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        int iw = icon.getIconWidth();
        int ih = icon.getIconHeight();
        if (check.isSelected()) {
          g2.setColor(getBackground());
          g2.setXORMode(Color.BLUE);
        } else {
          g2.setPaintMode();
        }
        Point pt = getLocation();
        g2.translate(-pt.x + split.getInsets().left, 0);
        g2.drawImage(icon.getImage(), 0, 0, iw, ih, this);
        g2.dispose();
      }
    };
    split.setRightComponent(afterCanvas);

    add(split);
    add(check, BorderLayout.SOUTH);
    setOpaque(false);
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
    frame.setResizable(false);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
