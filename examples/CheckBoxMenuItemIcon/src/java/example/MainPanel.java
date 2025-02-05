// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JCheckBox check = new JCheckBox("JCheckBox#setIcon(...)");
    check.setIcon(new CheckIcon());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(createMenuBar()));
    add(check, BorderLayout.SOUTH);
    add(new JScrollPane(new JTextArea()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    JMenu menu = new JMenu("JMenu");
    menuBar.add(menu);

    menu.add(new JCheckBoxMenuItem("default"));
    Icon defIcon = UIManager.getIcon("CheckBoxMenuItem.checkIcon");
    UIManager.put("CheckBoxMenuItem.checkIcon", new CheckIcon());
    menu.add(new JCheckBoxMenuItem("checkIcon test"));
    UIManager.put("CheckBoxMenuItem.checkIcon", defIcon);

    JMenu menu2 = new JMenu("JMenu2");
    JCheckBoxMenuItem jcbmi = new JCheckBoxMenuItem("setIcon");
    jcbmi.setIcon(new CheckIcon());
    // jcbmi.setSelectedIcon(new CheckIcon());
    menu2.add(jcbmi);
    menuBar.add(menu);
    menuBar.add(menu2);
    return menuBar;
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

class CheckIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    if (c instanceof AbstractButton) {
      ButtonModel m = ((AbstractButton) c).getModel();
      Graphics2D g2 = (Graphics2D) g.create();
      g2.translate(x, y);
      g2.setPaint(m.isSelected() ? Color.ORANGE : Color.GRAY);
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.fillOval(1, 1, getIconWidth() - 2, getIconHeight() - 2);
      g2.dispose();
    }
  }

  @Override public int getIconWidth() {
    return 14;
  }

  @Override public int getIconHeight() {
    return 14;
  }
}
