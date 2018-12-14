// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JTextArea log = new JTextArea();
    String useSystemExtensionHiding = "FileChooser.useSystemExtensionHiding";

    JButton button1 = new JButton("false");
    button1.addActionListener(e -> {
      UIManager.put(useSystemExtensionHiding, Boolean.FALSE);
      JFileChooser fileChooser = new JFileChooser();
      int retvalue = fileChooser.showOpenDialog(getRootPane());
      if (retvalue == JFileChooser.APPROVE_OPTION) {
        log.setText(fileChooser.getSelectedFile().getAbsolutePath());
      }
    });

    JButton button2 = new JButton("true");
    button2.addActionListener(e -> {
      UIManager.put(useSystemExtensionHiding, Boolean.TRUE);
      JFileChooser fileChooser = new JFileChooser();
      int retvalue = fileChooser.showOpenDialog(getRootPane());
      if (retvalue == JFileChooser.APPROVE_OPTION) {
        log.setText(fileChooser.getSelectedFile().getAbsolutePath());
      }
    });

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createTitledBorder(useSystemExtensionHiding));
    box.add(Box.createHorizontalGlue());
    box.add(button1);
    box.add(Box.createHorizontalStrut(5));
    box.add(button2);
    box.add(Box.createHorizontalGlue());

    JPanel p = new JPanel(new BorderLayout());
    p.add(box, BorderLayout.NORTH);
    p.add(new JScrollPane(log));

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtil.createLookAndFeelMenu());

    add(mb, BorderLayout.NORTH);
    add(p);
    setPreferredSize(new Dimension(320, 240));
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
      // UIManager.put("FileChooser.useSystemExtensionHiding", Boolean.FALSE);
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
