// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder("JOptionPane"));
    for (MessageType type : MessageType.values()) {
      p.add(makeButton(p, type));
    }
    add(p);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JButton makeButton(JPanel p, MessageType type) {
    String msg = type.toString();
    JButton b = new JButton(msg);
    b.addActionListener(e -> showDialog(p.getRootPane(), msg, type.getMessageType()));
    return b;
  }

  private static void showDialog(Component c, String msg, int type) {
    JOptionPane.showMessageDialog(c, msg, msg, type);
  }

  @Override public void updateUI() {
    UIManager.put("OptionPane.buttonOrientation", null);
    super.updateUI();
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    JFrame.setDefaultLookAndFeelDecorated(true);
    JDialog.setDefaultLookAndFeelDecorated(true);
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

enum MessageType {
  PLAIN(JOptionPane.PLAIN_MESSAGE),
  ERROR(JOptionPane.ERROR_MESSAGE),
  INFORMATION(JOptionPane.INFORMATION_MESSAGE),
  WARNING(JOptionPane.WARNING_MESSAGE),
  QUESTION(JOptionPane.QUESTION_MESSAGE);
  private final int type;

  MessageType(int type) {
    this.type = type;
  }

  public int getMessageType() {
    return type;
  }
}
