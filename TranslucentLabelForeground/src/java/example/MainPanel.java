// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JLabel label1 = new JLabel("ABC", SwingConstants.CENTER);
    label1.setForeground(new Color(0x64_FF_AA_AA, true));
    label1.setBackground(new Color(0x64_64_C8));
    label1.setFont(new Font(Font.MONOSPACED, Font.BOLD, 140));

    String color = "color:rgba(255,170,170,0.4);";
    String font = "font-family:monospace;font-weight:bold;font-size:140pt;";
    String style = font + color;
    JLabel label2 = new JLabel(String.format("<html><span style='%s'>ABC", style));
    label2.setBackground(new Color(0x64_64_C8));
    label2.setHorizontalAlignment(SwingConstants.CENTER);

    JPanel p = new JPanel(new GridLayout(2, 1)) {
      private transient Paint texture;
      @Override public void updateUI() {
        super.updateUI();
        texture = TextureUtils.createCheckerTexture(16, new Color(0xEE_32_32_32, true));
        setOpaque(false);
      }

      @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(texture);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
      }
    };
    p.add(label1);
    p.add(label2);

    JCheckBox check1 = new JCheckBox("setOpaque");
    check1.addActionListener(e -> {
      boolean b = ((JCheckBox) e.getSource()).isSelected();
      Stream.of(label1, label2).forEach(l -> l.setOpaque(b));
      p.repaint();
    });

    JCheckBox check2 = new JCheckBox("Background has Alpha");
    check2.addActionListener(e -> {
      boolean b = ((JCheckBox) e.getSource()).isSelected();
      Color bgc = new Color(0x64_64_64_C8, b);
      Stream.of(label1, label2).forEach(l -> l.setBackground(bgc));
      p.repaint();
    });

    Box box = Box.createHorizontalBox();
    box.add(check1);
    box.add(check2);

    add(p);
    add(box, BorderLayout.SOUTH);
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

final class TextureUtils {
  private TextureUtils() {
    /* HideUtilityClassConstructor */
  }

  public static TexturePaint createCheckerTexture(int cs, Color color) {
    int size = cs * cs;
    BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = img.createGraphics();
    g2.setPaint(color);
    g2.fillRect(0, 0, size, size);
    for (int i = 0; i * cs < size; i++) {
      for (int j = 0; j * cs < size; j++) {
        if ((i + j) % 2 == 0) {
          g2.fillRect(i * cs, j * cs, cs, cs);
        }
      }
    }
    g2.dispose();
    return new TexturePaint(img, new Rectangle(size, size));
  }
}
