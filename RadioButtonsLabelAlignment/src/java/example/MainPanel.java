// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    p.setFocusTraversalPolicy(new ContainerOrderFocusTraversalPolicy());
    p.setFocusTraversalPolicyProvider(true);
    p.setFocusable(false);
    // p.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 1;
    gbc.insets.top = 5;
    ButtonGroup group = new ButtonGroup();
    List<JComponent> list = Arrays.asList(
        new JRadioButton("JRadioButton1"),
        new JRadioButton("JRadioButton2"),
        new JRadioButton("JRadioButton3"),
        new JLabel("JLabel1"),
        new JLabel("JLabel2"),
        new JCheckBox("JCheckBox1"),
        new JCheckBox("JCheckBox2"));
    int gap = 0;
    boolean leftToRightParent = p.getComponentOrientation().isLeftToRight();
    for (JComponent c : list) {
      // c.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
      gbc.insets.left = 0;
      gbc.insets.right = 0;
      if (c instanceof JRadioButton) {
        JRadioButton button = (JRadioButton) c;
        if (gap == 0) {
          gap = getIconSpace(button);
          button.setSelected(true);
        }
        group.add(button);
      } else if (c instanceof JLabel) {
        boolean leftToRight = c.getComponentOrientation().isLeftToRight();
        if (leftToRight && leftToRightParent) {
          gbc.insets.left = gap;
        } else if (leftToRight) {
          gbc.insets.right = gap;
        } else if (leftToRightParent) {
          gbc.insets.right = gap;
        } else {
          gbc.insets.left = gap;
        }
      }
      p.add(c, gbc);
    }
    gbc.gridx = 2;
    gbc.weightx = 1.0;
    gbc.insets.left = 5;
    gbc.insets.right = 5;
    list.forEach(c -> p.add(new JTextField(), gbc));
    add(p, BorderLayout.NORTH);
    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    setPreferredSize(new Dimension(320, 240));
  }

  private static int getIconSpace(JRadioButton button) {
    Icon icon = UIManager.getIcon("RadioButton.icon");
    int iconWidth = icon == null ? 0 : icon.getIconWidth();
    boolean leftToRight = button.getComponentOrientation().isLeftToRight();
    Insets i = button.getInsets();
    return (leftToRight ? i.left : i.right) + iconWidth + button.getIconTextGap();
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
