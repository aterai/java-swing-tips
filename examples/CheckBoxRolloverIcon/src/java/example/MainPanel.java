// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Box box = Box.createVerticalBox();
    box.add(new JCheckBox("Default"));
    box.add(Box.createVerticalStrut(5));

    JCheckBox check1 = new JCheckBox("JCheckBox.setRolloverIcon(...)") {
      @Override public void updateUI() {
        super.updateUI();
        // setIcon(new CheckBoxRolloverIcon());
        Icon icon = new CheckBoxRolloverIcon();
        setPressedIcon(icon);
        setSelectedIcon(icon);
        setRolloverIcon(icon);
      }
    };
    box.add(check1);
    box.add(Box.createVerticalStrut(5));

    JCheckBox check2 = new JCheckBox("UIManager CheckBox.icon") {
      @Override public void updateUI() {
        super.updateUI();
        setIcon(new CheckBoxIcon());
      }
    };
    box.add(check2);
    box.add(Box.createVerticalStrut(5));

    JCheckBox check3 = new JCheckBox("UIDefaults CheckBox[MouseOver].iconPainter") {
      @Override public void updateUI() {
        super.updateUI();
        UIDefaults d = UIManager.getLookAndFeelDefaults();
        Painter<JCheckBox> painter0 = getIconPainter(d, "Focused+Selected");
        Painter<JCheckBox> painter1 = getIconPainter(d, "MouseOver");
        Painter<JCheckBox> painter2 = (g, object, width, height) -> {
          painter1.paint(g, object, width, height);
          Graphics2D g2 = (Graphics2D) g.create();
          g2.setPaint(Color.WHITE);
          g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .2f));
          object.setSelected(true);
          painter0.paint(g2, object, width, height);
          object.setSelected(false);
          g2.dispose();
        };
        d.put("CheckBox[MouseOver].iconPainter", painter2);
        d.put("CheckBox[Focused+MouseOver].iconPainter", painter2);
        putClientProperty("Nimbus.Overrides", d);
        putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.TRUE);
      }
    };
    box.add(check3);
    box.add(Box.createVerticalStrut(5));

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    add(box, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  @SuppressWarnings("unchecked")
  private static Painter<JCheckBox> getIconPainter(UIDefaults d, String status) {
    return (Painter<JCheckBox>) d.get(String.format("CheckBox[%s].iconPainter", status));
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

class CheckBoxIcon implements Icon {
  private final Icon checkIcon = UIManager.getIcon("CheckBox.icon");

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    if (c instanceof AbstractButton) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.translate(x, y);
      AbstractButton b = (AbstractButton) c;
      ButtonModel model = b.getModel();
      if (!model.isSelected() && model.isRollover()) {
        checkIcon.paintIcon(c, g2, 0, 0);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .2f));
        b.setSelected(true);
        checkIcon.paintIcon(b, g2, 0, 0);
        b.setSelected(false);
      } else {
        checkIcon.paintIcon(c, g2, 0, 0);
      }
      g2.dispose();
    }
  }

  @Override public int getIconWidth() {
    return checkIcon.getIconWidth();
  }

  @Override public int getIconHeight() {
    return checkIcon.getIconHeight();
  }
}

class CheckBoxRolloverIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    if (c instanceof AbstractButton) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.translate(x, y);
      g2.setColor(new Color(255, 155, 155, 100));
      g2.fillRect(2, 2, getIconWidth() - 4, getIconHeight() - 4);
      g2.setColor(Color.RED);
      g2.drawLine(9, 3, 9, 3);
      g2.drawLine(8, 4, 9, 4);
      g2.drawLine(7, 5, 9, 5);
      g2.drawLine(6, 6, 8, 6);
      g2.drawLine(3, 7, 7, 7);
      g2.drawLine(4, 8, 6, 8);
      g2.drawLine(5, 9, 5, 9);
      g2.drawLine(3, 5, 3, 5);
      g2.drawLine(3, 6, 4, 6);
      g2.dispose();
    }
  }

  @Override public int getIconWidth() {
    return 18;
  }

  @Override public int getIconHeight() {
    return 18;
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
