// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  // java - Laying out a keyboard in Swing - Stack Overflow
  // https://stackoverflow.com/questions/24622279/laying-out-a-keyboard-in-swing
  private static final String[][] KEYS = {
    {"`", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "-", "=", "BS"},
    {"Tab", "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "[", "]", "\\", ""},
    {"Ctrl", "A", "S", "D", "F", "G", "H", "J", "K", "L", ";", "'", "Enter", ""},
    {"Shift", "Z", "X", "C", "V", "B", "N", "M", ",", ".", "/", "", "↑"},
    {"Fn", "Alt", "                           ", "Alt", "←", "↓", "→"}
  };

  private MainPanel() {
    super(new BorderLayout(2, 2));
    Component keyboard = makeKeyboardPanel();
    EventQueue.invokeLater(() -> SwingUtilities.updateComponentTreeUI(keyboard));

    JPanel box = new JPanel(new FlowLayout(FlowLayout.CENTER));
    box.add(keyboard);

    add(box, BorderLayout.NORTH);
    add(new JScrollPane(new JTextArea()));
    setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeKeyboardPanel() {
    JPanel keyboard = new JPanel(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;
    // c.weightx = 1d;
    // c.weighty = 1d;
    // c.gridheight = 1;
    // c.gridx = 0;
    c.gridy = 50;
    for (int i = 0; i < KEYS[0].length * 2; i++) {
      c.gridx = i;
      keyboard.add(Box.createHorizontalStrut(KeyButton.SIZE));
    }

    for (int row = 0; row < KEYS.length; row++) {
      c.gridx = 0;
      c.gridy = row;
      for (int col = 0; col < KEYS[row].length; col++) {
        String key = KEYS[row][col];
        int len = key.length();
        c.gridwidth = len > 10 ? 14
                    : len > 4  ? 4
                    : len > 1  ? 3
                    : len == 1 ? 2 : 1;
        if (key.isEmpty()) {
          keyboard.add(Box.createHorizontalStrut(KeyButton.SIZE), c);
        } else {
          keyboard.add(createKeyButton(key, len <= 2), c);
        }
        c.gridx += c.gridwidth;
      }
    }
    return keyboard;
  }

  private static AbstractButton createKeyButton(String key, boolean square) {
    return new KeyButton(key, square);
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
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class KeyButton extends JButton {
  public static final int SIZE = 10;
  private final boolean square;

  protected KeyButton(String str) {
    this(str, true);
  }

  protected KeyButton(String str, boolean square) {
    super(str);
    this.square = square;
    // Font font = getFont();
    // setFont(font.deriveFont(6f));
    setFocusable(false);
    putClientProperty("JComponent.sizeVariant", "mini");
    setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
  }

  @Override public Dimension getPreferredSize() {
    if (square) {
      return new Dimension(SIZE * 2, SIZE * 2);
    } else {
      return super.getPreferredSize();
    }
  }
}
