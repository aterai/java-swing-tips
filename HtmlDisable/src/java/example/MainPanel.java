// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JLabel label1 = new JLabel();
    label1.putClientProperty("html.disable", Boolean.TRUE);
    JButton button1 = new JButton();
    button1.putClientProperty("html.disable", Boolean.TRUE);

    label1.setText("<html><font color=red>Html l1</font></html>");
    button1.setText("<html><font color=red>Html b1</font></html>");
    label1.setToolTipText("<html>&lt;html&gt;&lt;font color=red&gt;Html&lt;/font&gt;");
    button1.setToolTipText("<html><font color=red>Html</font></html>");

    JLabel label2 = new JLabel();
    label2.setText("<html><font color=red>Html l2</font></html>");
    JButton button2 = new JButton();
    button2.setText("<html><font color=red>Html b2</font></html>");

    Box box = Box.createVerticalBox();
    box.add(label1);
    box.add(Box.createVerticalStrut(2));
    box.add(button1);
    box.add(Box.createVerticalStrut(20));
    box.add(label2);
    box.add(Box.createVerticalStrut(2));
    box.add(button2);
    add(box);
    setBorder(BorderFactory.createEmptyBorder(20, 5, 20, 5));
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
