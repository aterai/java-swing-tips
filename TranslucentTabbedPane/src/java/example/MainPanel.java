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
    tabs.addTab("Tab 3", new AlphaContainer(tab3panel));

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtil.createLookAndFeelMenu());
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

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    // try {
    //   UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    // } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
    //   ex.printStackTrace();
    // }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

// https://tips4java.wordpress.com/2009/05/31/backgrounds-with-transparency/
class AlphaContainer extends JPanel {
  private final JComponent component;

  protected AlphaContainer(JComponent component) {
    super(new BorderLayout());
    this.component = component;
    component.setOpaque(false);
    add(component);
  }

  @Override public boolean isOpaque() {
    return false;
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.setColor(component.getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());
  }
}

class MissingIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();

    int w = getIconWidth();
    int h = getIconHeight();
    int gap = w / 5;

    g2.setColor(Color.GRAY);
    g2.fillRect(x, y, w, h);

    g2.setColor(Color.RED);
    g2.setStroke(new BasicStroke(w / 8f));
    g2.drawLine(x + gap, y + gap, x + w - gap, y + h - gap);
    g2.drawLine(x + gap, y + h - gap, x + w - gap, y + gap);

    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 320;
  }

  @Override public int getIconHeight() {
    return 240;
  }
}

// @see https://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtil {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtil() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup lafGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo lafInfo : UIManager.getInstalledLookAndFeels()) {
      menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lafGroup));
    }
    return menu;
  }

  private static JMenuItem createLookAndFeelItem(String laf, String lafClass, ButtonGroup bg) {
    JMenuItem lafItem = new JRadioButtonMenuItem(laf, lafClass.equals(lookAndFeel));
    lafItem.setActionCommand(lafClass);
    lafItem.setHideActionText(true);
    lafItem.addActionListener(e -> {
      ButtonModel m = bg.getSelection();
      try {
        setLookAndFeel(m.getActionCommand());
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        UIManager.getLookAndFeel().provideErrorFeedback((Component) e.getSource());
      }
    });
    bg.add(lafItem);
    return lafItem;
  }

  private static void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
    String oldLookAndFeel = LookAndFeelUtil.lookAndFeel;
    if (!oldLookAndFeel.equals(lookAndFeel)) {
      UIManager.setLookAndFeel(lookAndFeel);
      LookAndFeelUtil.lookAndFeel = lookAndFeel;
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
