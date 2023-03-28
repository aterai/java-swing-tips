// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    JButton button1 = new JButton("JOptionPane.showMessageDialog");
    button1.addActionListener(e -> {
      Component c = ((JComponent) e.getSource()).getRootPane();
      JOptionPane.showMessageDialog(c, "showMessageDialog");
    });

    JButton button2 = new JButton("Default");
    button2.addActionListener(e -> {
      Frame frame = JOptionPane.getFrameForComponent(getRootPane());
      JDialog dialog = new JDialog(frame, "title", true);
      Action act = new AbstractAction("OK") {
        @Override public void actionPerformed(ActionEvent e) {
          dialog.dispose();
        }
      };
      dialog.getContentPane().add(makePanel(act));
      dialog.pack();
      dialog.setResizable(false);
      dialog.setLocationRelativeTo(getRootPane());
      dialog.setVisible(true);
    });

    JButton button3 = new JButton("close JDialog with ESC key");
    button3.addActionListener(e -> {
      Frame frame = JOptionPane.getFrameForComponent(getRootPane());
      JDialog dialog = new JDialog(frame, "title", true);
      Action act = new AbstractAction("OK") {
        @Override public void actionPerformed(ActionEvent e) {
          dialog.dispose();
        }
      };
      JRootPane rp = dialog.getRootPane();
      String cmd = "close-it";
      rp.getActionMap().put(cmd, act);
      KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
      rp.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(esc, cmd);
      dialog.getContentPane().add(makePanel(act));
      dialog.pack();
      dialog.setResizable(false);
      dialog.setLocationRelativeTo(getRootPane());
      dialog.setVisible(true);
    });

    JPanel p1 = new JPanel();
    p1.setBorder(BorderFactory.createTitledBorder("JOptionPane"));
    p1.add(button1);

    JPanel p2 = new JPanel();
    p2.setBorder(BorderFactory.createTitledBorder("JDialog"));
    p2.add(button2);
    p2.add(button3);

    add(p1);
    add(p2);
    setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    setPreferredSize(new Dimension(320, 240));
  }

  public static Component makePanel(Action act) {
    JPanel p = new JPanel(new GridBagLayout()) {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = Math.max(240, d.width);
        return d;
      }
    };
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(5, 10, 5, 10);
    c.anchor = GridBagConstraints.LINE_START;
    p.add(new JLabel(new ColorIcon(Color.RED)), c);

    c.insets = new Insets(5, 0, 5, 0);
    // p.add(new JLabel("<html>Message<br>11111<br>222222222<br>333333333"), c);
    p.add(new JLabel("Message"), c);

    c.gridwidth = 2;
    c.gridy = 1;
    c.weightx = 1d;
    c.anchor = GridBagConstraints.CENTER;
    c.fill = GridBagConstraints.NONE;
    p.add(new JButton(act), c);

    return p;
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

class ColorIcon implements Icon {
  private final Color color;

  protected ColorIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(color);
    g2.fillOval(0, 0, getIconWidth(), getIconHeight());
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 32;
  }

  @Override public int getIconHeight() {
    return 32;
  }
}
