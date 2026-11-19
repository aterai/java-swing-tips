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

  @SuppressWarnings("ExecutableStatementCount")
  private static JPanel makeNumericKeypad() {
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(1, 1, 1, 1);
    c.fill = GridBagConstraints.BOTH;
    c.gridx = 0;
    c.gridy = 0;
    JPanel panel = new JPanel(new GridBagLayout());
    panel.add(makeButton("<html>Num<br>Lock"), c);
    c.gridx = GridBagConstraints.RELATIVE;
    panel.add(makeButton("/"), c);
    panel.add(makeButton("*"), c);
    panel.add(makeButton("-"), c);
    c.gridx = 0;
    c.gridy++;
    panel.add(makeButton("7", "Home"), c);
    c.gridx = GridBagConstraints.RELATIVE;
    panel.add(makeButton("8", "↑"), c);
    panel.add(makeButton("9", "PgUp"), c);
    c.gridheight = 2;
    panel.add(makeButton("+", "+"), c);
    c.gridx = 0;
    c.gridy++;
    c.gridheight = 1;
    panel.add(makeButton("4", "←"), c);
    c.gridx = GridBagConstraints.RELATIVE;
    panel.add(makeButton("5"), c);
    panel.add(makeButton("6", "→"), c);
    c.gridx = 0;
    c.gridy++;
    panel.add(makeButton("1", "End"), c);
    c.gridx = GridBagConstraints.RELATIVE;
    panel.add(makeButton("2", "↓"), c);
    panel.add(makeButton("3", "PgDn"), c);
    c.gridheight = 2;
    JButton enter = makeButton("Enter", "Enter");
    panel.add(enter, c);
    // The root pane is not available until the keypad is added to a window
    EventQueue.invokeLater(() -> {
      panel.getRootPane().setDefaultButton(enter);
      enter.requestFocusInWindow();
    });
    c.gridx = 0;
    c.gridy++;
    c.gridwidth = 2;
    c.gridheight = 1;
    panel.add(makeButton("0", "Insert"), c);
    c.gridx = GridBagConstraints.RELATIVE;
    c.gridwidth = 1;
    panel.add(makeButton(".", "Delete"), c);
    return panel;
  }

  private static JButton makeButton(String... labels) {
    String key = labels[0];
    String sub = labels.length > 1 ? labels[1] : " ";
    int gap = 2;
    Border border = BorderFactory.createEmptyBorder(gap, gap, gap, gap);
    JLabel keyLabel = new JLabel(key);
    keyLabel.setFont(keyLabel.getFont().deriveFont(12f));
    keyLabel.setBorder(border);
    JLabel subLabel = new JLabel(sub);
    subLabel.setFont(subLabel.getFont().deriveFont(9.5f));
    subLabel.setBorder(border);
    JButton button = new JButton() {
      @Override public void updateUI() {
        super.updateUI();
        setLayout(new BorderLayout());
        setMargin(border.getBorderInsets(this));
      }

      @Override public Dimension getPreferredSize() {
        int size = SIZE - gap * 2;
        return new Dimension(size, size);
      }
    };
    // button.addActionListener(e -> {
    //   if (key.length() == 1 && !Character.isHighSurrogate(key.charAt(0))) {
    //     System.out.println(KeyStroke.getKeyStroke(key.charAt(0)));
    //   }
    // });
    if (Objects.equals(key, sub)) {
      button.add(keyLabel);
    } else {
      button.add(keyLabel, BorderLayout.NORTH);
      button.add(subLabel, BorderLayout.SOUTH);
    }
    return button;
  }

  // private static Component makeButton(String... s) {
  //   String s1 = s.length > 1 ? s[1] : "&nbsp;";
  //   String title = String.format("<html>%s<br><span style='font-size:80%%'>%s", s[0], s1);
  //   int gap = 2;
  //   int size = SIZE - gap * 2;
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
