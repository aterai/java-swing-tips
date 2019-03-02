// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Locale;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Box box = Box.createVerticalBox();
    box.add(makeSystemColorPanel(SystemColor.desktop, "desktop"));
    box.add(makeSystemColorPanel(SystemColor.activeCaption, "activeCaption"));
    box.add(makeSystemColorPanel(SystemColor.inactiveCaption, "inactiveCaption"));
    box.add(makeSystemColorPanel(SystemColor.activeCaptionText, "activeCaptionText"));
    box.add(makeSystemColorPanel(SystemColor.inactiveCaptionText, "inactiveCaptionText"));
    box.add(makeSystemColorPanel(SystemColor.activeCaptionBorder, "activeCaptionBorder"));
    box.add(makeSystemColorPanel(SystemColor.inactiveCaptionBorder, "inactiveCaptionBorder"));
    box.add(makeSystemColorPanel(SystemColor.window, "window"));
    box.add(makeSystemColorPanel(SystemColor.windowText, "windowText"));
    box.add(makeSystemColorPanel(SystemColor.menu, "menu"));
    box.add(makeSystemColorPanel(SystemColor.menuText, "menuText"));
    box.add(makeSystemColorPanel(SystemColor.text, "text"));
    box.add(makeSystemColorPanel(SystemColor.textHighlight, "textHighlight"));
    box.add(makeSystemColorPanel(SystemColor.textText, "textText"));
    box.add(makeSystemColorPanel(SystemColor.textHighlightText, "textHighlightText"));
    box.add(makeSystemColorPanel(SystemColor.control, "control"));
    box.add(makeSystemColorPanel(SystemColor.controlLtHighlight, "controlLtHighlight"));
    box.add(makeSystemColorPanel(SystemColor.controlHighlight, "controlHighlight"));
    box.add(makeSystemColorPanel(SystemColor.controlShadow, "controlShadow"));
    box.add(makeSystemColorPanel(SystemColor.controlDkShadow, "controlDkShadow"));
    box.add(makeSystemColorPanel(SystemColor.controlText, "controlText"));
    // box.add(makeSystemColorPanel(SystemColor.inactiveCaptionControlText, "inactiveControlText"));
    box.add(makeSystemColorPanel(SystemColor.control, "control"));
    box.add(makeSystemColorPanel(SystemColor.scrollbar, "scrollbar"));
    box.add(makeSystemColorPanel(SystemColor.info, "info"));
    box.add(makeSystemColorPanel(SystemColor.infoText, "infoText"));
    // box.add(Box.createVerticalStrut(10));
    // box.add(makeSystemColorPanel(new Color(0x00_4E_98), "test"));

    add(new JScrollPane(box));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeSystemColorPanel(Color color, String text) {
    String hex = Integer.toHexString(color.getRGB()).toUpperCase(Locale.ENGLISH);
    JTextField field = new JTextField(text + ": 0x" + hex);
    field.setEditable(false);

    JPanel p = new JPanel(new BorderLayout());
    p.add(new JLabel(new ColorIcon(color)), BorderLayout.EAST);
    p.add(field);
    return p;
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
    g2.translate(x, y);
    g2.setPaint(color);
    g2.fillRect(1, 1, getIconWidth() - 2, getIconHeight() - 2);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 16;
  }

  @Override public int getIconHeight() {
    return 16;
  }
}
