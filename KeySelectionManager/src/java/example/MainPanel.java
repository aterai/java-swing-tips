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
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    box.add(makeTitledPanel("BasicComboBoxUI#DefaultKeySelectionManager", new JComboBox<>(makeModel())));
    box.add(Box.createVerticalStrut(5));

    // JComboBox<String> combo0 = new JComboBox<>(makeModel());
    // combo0.setEditable(true);

    JComboBox<String> combo1 = new JComboBox<String>(makeModel()) {
      @Override public boolean selectWithKeyChar(char keyChar) {
        // return super.selectWithKeyChar(keyChar);
        return false;
      }
    };
    box.add(makeTitledPanel("disable JComboBox#selectWithKeyChar(...)", combo1));
    box.add(Box.createVerticalStrut(5));

    JComboBox<String> combo2 = new JComboBox<>(makeModel());
    combo2.setKeySelectionManager(new JComboBox.KeySelectionManager() {
      // Java 10: @Override public int selectionForKey(char key, ComboBoxModel<?> model) {
      // Java 9:
      @SuppressWarnings("rawtypes")
      @Override public int selectionForKey(char key, ComboBoxModel model) {
        return -1;
      }
    });
    box.add(makeTitledPanel("disable KeySelectionManager#selectionForKey(...)", combo2));

    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
  }

  private static ComboBoxModel<String> makeModel() {
    String[] data = {
      "a", "ab", "abc", "b1", "b2", "b3"
    };
    return new DefaultComboBoxModel<>(data);
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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
