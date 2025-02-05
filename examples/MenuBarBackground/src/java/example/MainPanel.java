// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(createMenuBar()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JMenuBar createMenuBar() {
    JMenuBar mb = new JMenuBar() {
      private final transient Paint texture = makeCheckerTexture();
      @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(texture);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
      }
    };
    mb.setOpaque(false);
    String[] menuKeys = {"File", "Edit", "Help"};
    for (String key : menuKeys) {
      JMenu m = createMenu(key);
      // if (m != null)
      mb.add(m);
    }
    return mb;
  }

  private static JMenu createMenu(String key) {
    JMenu menu = new JMenu(key) {
      @Override protected void fireStateChanged() {
        ButtonModel m = getModel();
        if (m.isPressed() && m.isArmed()) {
          setOpaque(true);
        } else if (m.isSelected()) {
          setOpaque(true);
        } else {
          setOpaque(isRolloverEnabled() && m.isRollover());
        }
        super.fireStateChanged();
      }

      @Override public void updateUI() {
        super.updateUI();
        setOpaque(false); // Motif lnf
      }
    };
    // System.out.println(System.getProperty("os.name"));
    // System.out.println(System.getProperty("os.version"));
    // if (Objects.equals("Windows XP", System.getProperty("os.name"))) {
    //   menu.setBackground(new Color(0x0, true)); // XXX Windows XP lnf?
    // }
    menu.add("JMenuItem1");
    menu.add("JMenuItem2");
    menu.add("JMenuItem3");
    return menu;
  }

  public static TexturePaint makeCheckerTexture() {
    int cs = 6;
    int sz = cs * cs;
    BufferedImage img = new BufferedImage(sz, sz, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = img.createGraphics();
    g2.setPaint(new Color(0x32_C8_96_64, true));
    g2.fillRect(0, 0, sz, sz);
    for (int i = 0; i * cs < sz; i++) {
      for (int j = 0; j * cs < sz; j++) {
        if ((i + j) % 2 == 0) {
          g2.fillRect(i * cs, j * cs, cs, cs);
        }
      }
    }
    g2.dispose();
    return new TexturePaint(img, new Rectangle(sz, sz));
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
    // String key = "Menu.useMenuBarBackgroundForTopLevel";
    // System.out.println(key + ": " + UIManager.getBoolean(key));
    // TEST: UIManager.put(key, Boolean.FALSE);
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
