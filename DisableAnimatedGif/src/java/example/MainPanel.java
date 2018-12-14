// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    ImageIcon icon = new ImageIcon(getClass().getResource("duke.running.gif"));

    JLabel label1 = new JLabel(icon);
    label1.setEnabled(false);
    label1.setBorder(BorderFactory.createTitledBorder("Default"));

    JLabel label2 = new JLabel(icon) {
      @Override public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
        int info = infoflags;
        if (!isEnabled()) {
          info &= ~FRAMEBITS;
        }
        return super.imageUpdate(img, info, x, y, w, h);
      }
    };
    label2.setEnabled(false);
    label2.setBorder(BorderFactory.createTitledBorder("Override imageUpdate(...)"));

    JLabel label3 = new JLabel(icon);
    label3.setEnabled(false);
    label3.setBorder(BorderFactory.createTitledBorder("setDisabledIcon"));
    ImageIcon i = new ImageIcon(getClass().getResource("duke.running_frame_0001.gif"));
    label3.setDisabledIcon(makeDisabledIcon(i));

    JCheckBox check = new JCheckBox("setEnabled");
    check.addActionListener(e -> {
      JCheckBox c = (JCheckBox) e.getSource();
      label1.setEnabled(c.isSelected());
      label2.setEnabled(c.isSelected());
      label3.setEnabled(c.isSelected());
    });
    JPanel p = new JPanel(new GridLayout(2, 2));
    p.add(label1);
    p.add(label2);
    p.add(label3);
    add(check, BorderLayout.NORTH);
    add(p);
    // setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Icon makeDisabledIcon(ImageIcon icon) {
    Image img = icon.getImage();
    // BufferedImage source = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
    // Graphics g = source.createGraphics();
    // g.drawImage(img, 0, 0, null);
    // g.dispose();
    // ColorConvertOp colorConvert = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
    // BufferedImage destination = colorConvert.filter(source, null);
    // return new ImageIcon(destination);
    return new ImageIcon(GrayFilter.createDisabledImage(img));
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    // frame.setResizable(false);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
