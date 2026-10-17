// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  // java - Laying out a keyboard in Swing - Stack Overflow
  // https://stackoverflow.com/questions/24622279/laying-out-a-keyboard-in-swing
  private static final String[][] KEYS = {
      {"`", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "-", "=", "BS"},
      {"Tab", "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "[", "]", "\\", ""},
      {"Ctrl", "A", "S", "D", "F", "G", "H", "J", "K", "L", ";", "'", "Enter", ""},
      {"Shift", "Z", "X", "C", "V", "B", "N", "M", ",", ".", "/", "", "↑"},
      {"Fn", "Alt", "                           ", "Alt", "←", "↓", "→"},
  };

  private MainPanel() {
    super(new BorderLayout(2, 2));
    JPanel box = new JPanel(new FlowLayout(FlowLayout.CENTER));
    box.add(createKeyboardPanel());

    add(box, BorderLayout.NORTH);
    add(new JScrollPane(new JTextArea()));
    setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    setPreferredSize(new Dimension(320, 240));
  }

  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  private static Component createKeyboardPanel() {
    JPanel keyboard = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;

    int columns = 0;
    for (int row = 0; row < KEYS.length; row++) {
      c.gridx = 0;
      c.gridy = row;
      for (String key : KEYS[row]) {
        c.gridwidth = getGridWidth(key);
        if (key.isEmpty()) {
          keyboard.add(Box.createHorizontalStrut(KeyButton.SIZE), c);
        } else {
          keyboard.add(new KeyButton(key, key.length() <= 2), c);
        }
        c.gridx += c.gridwidth;
      }
      columns = Math.max(columns, c.gridx);
    }

    // Guide row: zero-height struts that fix the width of every grid column
    c.gridy = KEYS.length;
    c.gridwidth = 1;
    for (int i = 0; i < columns; i++) {
      c.gridx = i;
      keyboard.add(Box.createHorizontalStrut(KeyButton.SIZE), c);
    }
    return keyboard;
  }

  @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
  private static int getGridWidth(String key) {
    int len = key.length();
    int width;
    if (len > 10) { // space bar
      width = 14;
    } else if (len > 4) { // Shift, Enter
      width = 4;
    } else if (len > 1) { // Tab, Ctrl, Alt, BS, Fn
      width = 3;
    } else if (len == 1) { // default keys
      width = 2;
    } else { // empty string: invisible spacer
      width = 1;
    }
    return width;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      Logger.getGlobal().severe(ex::getMessage);
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

class KeyButton extends JButton {
  public static final int SIZE = 10;
  private final boolean square;

  protected KeyButton(String text, boolean square) {
    super(text);
    this.square = square;
  }

  @Override public void updateUI() {
    // The size variant must be set before the UI delegate is installed,
    // otherwise it is not applied until the next updateUI() call
    putClientProperty("JComponent.sizeVariant", "mini");
    super.updateUI();
    setFocusable(false);
    setBorder(BorderFactory.createEmptyBorder());
  }

  @Override public Dimension getPreferredSize() {
    return square ? new Dimension(SIZE * 2, SIZE * 2) : super.getPreferredSize();
  }
}
