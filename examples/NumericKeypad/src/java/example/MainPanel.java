// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private static final int SIZE = 46;

  private MainPanel() {
    super();
    JPanel panel = makeNumericKeypad();
    panel.setBorder(BorderFactory.createLineBorder(Color.WHITE));
    add(panel);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JPanel makeNumericKeypad() {
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(1, 1, 1, 1);
    c.fill = GridBagConstraints.BOTH;
    c.gridx = 0;
    c.gridy = 0;
    JPanel p = new JPanel(new GridBagLayout());
    p.add(makeButton("<html>Num<br>Lock"), c);
    c.gridx = GridBagConstraints.RELATIVE;
    p.add(makeButton("/"), c);
    p.add(makeButton("*"), c);
    p.add(makeButton("-"), c);
    c.gridx = 0;
    c.gridy++;
    p.add(makeButton("7", "Home"), c);
    c.gridx = GridBagConstraints.RELATIVE;
    p.add(makeButton("8", "↑"), c);
    p.add(makeButton("9", "PgUp"), c);
    c.gridheight = 2;
    p.add(makeButton("+", "+"), c);
    c.gridx = 0;
    c.gridy++;
    c.gridheight = 1;
    p.add(makeButton("4", "←"), c);
    c.gridx = GridBagConstraints.RELATIVE;
    p.add(makeButton("5"), c);
    p.add(makeButton("6", "→"), c);
    c.gridx = 0;
    c.gridy++;
    p.add(makeButton("1", "End"), c);
    c.gridx = GridBagConstraints.RELATIVE;
    p.add(makeButton("2", "↓"), c);
    p.add(makeButton("3", "PgDn"), c);
    c.gridheight = 2;
    JButton enter = makeButton("Enter", "Enter");
    p.add(enter, c);
    EventQueue.invokeLater(() -> {
      p.getRootPane().setDefaultButton(enter);
      enter.requestFocusInWindow();
    });
    c.gridx = 0;
    c.gridy++;
    c.gridwidth = 2;
    c.gridheight = 1;
    p.add(makeButton("0", "Insert"), c);
    c.gridx = GridBagConstraints.RELATIVE;
    c.gridwidth = 1;
    p.add(makeButton(".", "Delete"), c);
    return p;
  }

  private static JButton makeButton(String... s) {
    String key = s[0];
    String sub = s.length > 1 ? s[1] : " ";
    int gap = 2;
    Border border = BorderFactory.createEmptyBorder(gap, gap, gap, gap);
    JLabel l1 = new JLabel(key);
    l1.setFont(l1.getFont().deriveFont(12f));
    l1.setBorder(border);
    JLabel l2 = new JLabel(sub);
    l2.setFont(l2.getFont().deriveFont(9.5f));
    l2.setBorder(border);
    JButton button = new JButton() {
      @Override public void updateUI() {
        super.updateUI();
        setLayout(new BorderLayout());
        setMargin(border.getBorderInsets(this));
      }

      @Override public Dimension getPreferredSize() {
        int sz = SIZE - gap * 2;
        return new Dimension(sz, sz);
      }
    };
    // button.addActionListener(e -> {
    //   if (key.length() == 1 && !Character.isHighSurrogate(key.charAt(0))) {
    //     System.out.println(KeyStroke.getKeyStroke(key.charAt(0)));
    //   }
    // });
    if (Objects.equals(key, sub)) {
      button.add(l1);
    } else {
      button.add(l1, BorderLayout.NORTH);
      button.add(l2, BorderLayout.SOUTH);
    }
    return button;
  }

  // private static Component makeButton(String... s) {
  //   String s1 = s.length > 1 ? s[1] : "&nbsp;";
  //   String title = String.format("<html>%s<br><span style='font-size:80%%'>%s", s[0], s1);
  //   int gap = 2;
  //   int sz = SIZE - gap * 2;
  //   JLabel label = new JLabel(title, null, SwingConstants.LEFT);
  //   label.setPreferredSize(new Dimension(sz, sz));
  //   label.setBorder(BorderFactory.createEmptyBorder(gap, gap, gap, gap));
  //   JButton b = new JButton(new ComponentIcon(label));
  //   b.setHorizontalAlignment(SwingConstants.LEFT);
  //   b.setMargin(new Insets(gap, gap, gap, gap));
  //   return b;
  // }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      Logger.getGlobal().severe(ex::getMessage);
      return;
    }
    JFrame frame = new JFrame("@title@");
    frame.setMinimumSize(new Dimension(300, 120));
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

// class ComponentIcon implements Icon {
//   private final Component cmp;
//
//   protected ComponentIcon(Component cmp) {
//     this.cmp = cmp;
//   }
//
//   @Override public void paintIcon(Component c, Graphics g, int x, int y) {
//     int w = getIconWidth();
//     int h = getIconHeight();
//     SwingUtilities.paintComponent(g, cmp, c.getParent(), x, y, w, h);
//   }
//
//   @Override public int getIconWidth() {
//     return cmp.getPreferredSize().width;
//   }
//
//   @Override public int getIconHeight() {
//     return cmp.getPreferredSize().height;
//   }
// }
