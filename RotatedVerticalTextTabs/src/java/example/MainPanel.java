// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabs1 = new JTabbedPane(SwingConstants.LEFT);
    JTabbedPane tabs2 = new JTabbedPane(SwingConstants.RIGHT);
    Stream.of("computer", "directory", "file").forEach(title -> {
      Icon icon = UIManager.getIcon(String.format("FileView.%sIcon", title));
      JLabel c1 = new JLabel(title, icon, SwingConstants.LEADING);
      tabs1.addTab(null, makeVerticalTabIcon(title, icon, false), c1);
      JLabel c2 = new JLabel(title, icon, SwingConstants.CENTER);
      tabs2.addTab(null, makeVerticalTabIcon(title, icon, true), c2);
    });
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabs1, tabs2);
    split.setResizeWeight(.5);
    add(split);
    setPreferredSize(new Dimension(320, 240));
  }

  private Icon makeVerticalTabIcon(String title, Icon icon, boolean clockwise) {
    JLabel label = new JLabel(title, icon, SwingConstants.LEADING);
    label.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
    Dimension d = label.getPreferredSize();
    int w = d.height;
    int h = d.width;
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = (Graphics2D) bi.getGraphics();
    AffineTransform at = clockwise ? AffineTransform.getTranslateInstance(w, 0)
                                   : AffineTransform.getTranslateInstance(0, h);
    at.quadrantRotate(clockwise ? 1 : -1);
    g2.setTransform(at);
    SwingUtilities.paintComponent(g2, label, this, 0, 0, d.width, d.height);
    g2.dispose();
    return new ImageIcon(bi);
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
