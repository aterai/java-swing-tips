// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final transient Image bgImage;

  private MainPanel() {
    super(new BorderLayout());
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    bgImage = Optional.ofNullable(cl.getResource("example/test.png")).map(url -> {
      try (InputStream s = url.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(MainPanel::makeMissingImage);

    Color bgc = new Color(110, 110, 0, 100);
    Color fgc = new Color(255, 255, 0, 100);

    UIManager.put("TabbedPane.shadow", fgc);
    UIManager.put("TabbedPane.darkShadow", fgc);
    UIManager.put("TabbedPane.light", fgc);
    UIManager.put("TabbedPane.highlight", fgc);
    UIManager.put("TabbedPane.tabAreaBackground", fgc);
    UIManager.put("TabbedPane.unselectedBackground", fgc);
    UIManager.put("TabbedPane.background", bgc);
    UIManager.put("TabbedPane.foreground", Color.WHITE);
    UIManager.put("TabbedPane.focus", fgc);
    UIManager.put("TabbedPane.contentAreaColor", fgc);
    UIManager.put("TabbedPane.selected", fgc);
    UIManager.put("TabbedPane.selectHighlight", fgc);
    // UIManager.put("TabbedPane.borderHighlightColor", fgc); // Do not work
    // Maybe "TabbedPane.borderHightlightColor" is a typo,
    // but this is defined in MetalTabbedPaneUI
    UIManager.put("TabbedPane.borderHightlightColor", fgc);

    JPanel tab1panel = new JPanel();
    tab1panel.setBackground(new Color(0, 220, 220, 50));

    JPanel tab2panel = new JPanel();
    tab2panel.setBackground(new Color(220, 0, 0, 50));

    JPanel tab3panel = new JPanel();
    tab3panel.setBackground(new Color(0, 0, 220, 50));

    JCheckBox cb = new JCheckBox("setOpaque(false)");
    cb.setOpaque(false);
    cb.setForeground(Color.WHITE);
    tab3panel.add(cb);
    tab3panel.add(new JCheckBox("setOpaque(true)"));

    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab("Tab 1", tab1panel);
    tabs.addTab("Tab 2", tab2panel);
    tabs.addTab("Tab 3", makeAlphaContainer(tab3panel));

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    add(tabs);
    setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    setPreferredSize(new Dimension(320, 240));
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
  }

  private static Image makeMissingImage() {
    Icon missingIcon = new MissingIcon();
    int w = missingIcon.getIconWidth();
    int h = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, 0, 0);
    g2.dispose();
    return bi;
  }

  private static Container makeAlphaContainer(JComponent component) {
    Container c = new JPanel(new BorderLayout()) {
      @Override public boolean isOpaque() {
        return false;
      }

      @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(component.getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
      }
    };
    component.setOpaque(false);
    c.add(component);
    return c;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    // try {
    //   UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    // } catch (UnsupportedLookAndFeelException ignored) {
    //   Toolkit.getDefaultToolkit().beep();
    // } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
    //   ex.printStackTrace();
    //   return;
    // }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

// // https://tips4java.wordpress.com/2009/05/31/backgrounds-with-transparency/
// class AlphaContainer extends JPanel {
//   private final JComponent component;
//
//   protected AlphaContainer(JComponent component) {
//     super(new BorderLayout());
//     this.component = component;
//     component.setOpaque(false);
//     add(component);
//   }
//
//   @Override public boolean isOpaque() {
//     return false;
//   }
//
//   @Override protected void paintComponent(Graphics g) {
//     super.paintComponent(g);
//     g.setColor(component.getBackground());
//     g.fillRect(0, 0, getWidth(), getHeight());
//   }
// }

class MissingIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    int w = getIconWidth();
    int h = getIconHeight();
    int gap = w / 5;
    g2.setColor(Color.GRAY);
    g2.translate(x, y);
    g2.fillRect(0, 0, w, h);
    g2.setColor(Color.RED);
    g2.setStroke(new BasicStroke(w / 8f));
    g2.drawLine(gap, gap, w - gap, h - gap);
    g2.drawLine(gap, h - gap, w - gap, gap);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 320;
  }

  @Override public int getIconHeight() {
    return 240;
  }
}

// @see SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = makeButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton makeButton(UIManager.LookAndFeelInfo info) {
    boolean selected = info.getClassName().equals(lookAndFeel);
    return new JRadioButtonMenuItem(info.getName(), selected);
  }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        ex.printStackTrace();
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
