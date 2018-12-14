// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JPanel p = new JPanel();
    JButton b1 = new JButton("button");
    JButton b2 = new JButton();

    ImageIcon rss = new ImageIcon(getClass().getResource("feed-icon-14x14.png")); // http://feedicons.com/
    b2.setIcon(rss);
    b2.setRolloverIcon(makeRolloverIcon(rss));

    p.add(b1);
    p.add(b2);
    p.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

    List<JButton> list = Arrays.asList(b1, b2);

    JCheckBox focusPainted = new JCheckBox("setFocusPainted", true);
    focusPainted.addActionListener(e -> {
      boolean flg = ((JCheckBox) e.getSource()).isSelected();
      list.forEach(b -> b.setFocusPainted(flg));
      p.revalidate();
    });

    JCheckBox borderPainted = new JCheckBox("setBorderPainted", true);
    borderPainted.addActionListener(e -> {
      boolean flg = ((JCheckBox) e.getSource()).isSelected();
      list.forEach(b -> b.setBorderPainted(flg));
      p.revalidate();
    });

    JCheckBox contentAreaFilled = new JCheckBox("setContentAreaFilled", true);
    contentAreaFilled.addActionListener(e -> {
      boolean flg = ((JCheckBox) e.getSource()).isSelected();
      list.forEach(b -> b.setContentAreaFilled(flg));
      p.revalidate();
    });

    JCheckBox rolloverEnabled = new JCheckBox("setRolloverEnabled", true);
    rolloverEnabled.addActionListener(e -> {
      boolean flg = ((JCheckBox) e.getSource()).isSelected();
      list.forEach(b -> b.setRolloverEnabled(flg));
      p.revalidate();
    });

    Box box = Box.createVerticalBox();
    Stream.of(focusPainted, borderPainted, contentAreaFilled, rolloverEnabled).forEach(box::add);
    add(box, BorderLayout.NORTH);
    add(p);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static ImageIcon makeRolloverIcon(ImageIcon srcIcon) {
    RescaleOp op = new RescaleOp(
        new float[] {1.2f, 1.2f, 1.2f, 1f},
        new float[] {0f, 0f, 0f, 0f}, null);
    BufferedImage img = new BufferedImage(
        srcIcon.getIconWidth(), srcIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = img.createGraphics();
    // g2.drawImage(srcIcon.getImage(), 0, 0, null);
    srcIcon.paintIcon(null, g2, 0, 0);
    g2.dispose();
    return new ImageIcon(op.filter(img, null));
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
    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtil.createLookAndFeelMenu());

    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.setJMenuBar(mb);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

// @see https://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtil {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtil() { /* Singleton */ }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup lafRadioGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo lafInfo: UIManager.getInstalledLookAndFeels()) {
      menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lafRadioGroup));
    }
    return menu;
  }

  private static JRadioButtonMenuItem createLookAndFeelItem(String lafName, String lafClassName, ButtonGroup lafRadioGroup) {
    JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem(lafName, lafClassName.equals(lookAndFeel));
    lafItem.setActionCommand(lafClassName);
    lafItem.setHideActionText(true);
    lafItem.addActionListener(e -> {
      ButtonModel m = lafRadioGroup.getSelection();
      try {
        setLookAndFeel(m.getActionCommand());
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        ex.printStackTrace();
      }
    });
    lafRadioGroup.add(lafItem);
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
    for (Window window: Frame.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
