// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    // toolBar1.putClientProperty("JToolBar.isRollover", Boolean.FALSE);
    // toolBar2.putClientProperty("JToolBar.isRollover", Boolean.FALSE);
    // Icon icon1 = makeIcon("toolbarButtonGraphics/general/Copy24.gif");
    // Icon icon2 = makeIcon("toolbarButtonGraphics/general/Cut24.gif");
    // Icon icon3 = makeIcon("toolbarButtonGraphics/general/Help24.gif");
    Icon icon1 = new ColorIcon(Color.RED);
    Icon icon2 = new ColorIcon(Color.GREEN);
    Icon icon3 = new ColorIcon(Color.BLUE);

    JToolBar toolBar1 = new JToolBar("ToolBarButton");
    toolBar1.add(new JButton(icon1));
    toolBar1.add(new JButton(icon2));
    toolBar1.add(Box.createGlue());
    toolBar1.add(new JButton(icon3));

    JToolBar toolBar2 = new JToolBar("JButton");
    toolBar2.add(createToolBarButton(icon1));
    toolBar2.add(createToolBarButton(icon2));
    toolBar2.add(Box.createGlue());
    toolBar2.add(createToolBarButton(icon3));

    add(toolBar1, BorderLayout.NORTH);
    add(new JScrollPane(new JTextArea()));
    add(toolBar2, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JButton createToolBarButton(Icon icon) {
    JButton b = new JButton(icon);
    b.setRequestFocusEnabled(false);
    return b;
  }

  // private static Icon makeIcon(String path) {
  //   ClassLoader cl = Thread.currentThread().getContextClassLoader();
  //   return Optional.ofNullable(cl.getResource(path)).map(url -> {
  //     try (InputStream s = url.openStream()) {
  //       return new ImageIcon(ImageIO.read(s));
  //     } catch (IOException ex) {
  //       return makeMissingIcon();
  //     }
  //   }).orElseGet(MainPanel::makeMissingIcon);
  // }

  // private static Icon makeMissingIcon() {
  //   Icon missingIcon = UIManager.getIcon("html.missingImage");
  //   int iw = missingIcon.getIconWidth();
  //   int ih = missingIcon.getIconHeight();
  //   BufferedImage bi = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
  //   Graphics2D g2 = bi.createGraphics();
  //   missingIcon.paintIcon(null, g2, (24 - iw) / 2, (24 - ih) / 2);
  //   g2.dispose();
  //   return new ImageIcon(bi);
  // }

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

class ColorIcon implements Icon {
  private final Color color;

  protected ColorIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.translate(x, y);
    g2.setPaint(color);
    g2.fillOval(1, 1, getIconWidth() - 2, getIconHeight() - 2);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 24;
  }

  @Override public int getIconHeight() {
    return 24;
  }
}

// class ToolBarButton extends JButton {
//   protected ToolBarButton(ImageIcon icon) {
//     super(icon);
//     setContentAreaFilled(false);
//     setFocusPainted(false);
//     addMouseListener(new MouseAdapter() {
//       @Override public void mouseEntered(MouseEvent e) {
//         setContentAreaFilled(true);
//       }
//
//       @Override public void mouseExited(MouseEvent e) {
//         setContentAreaFilled(false);
//       }
//     });
//   }
// }
