// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    // RadioButton.disabledText
    // ComboBox.disabledForeground
    // ComboBox.disabledBackground
    // Label.disabledForeground
    // CheckBoxMenuItem.disabledForeground
    // CheckBox.disabledText
    // Label.disabledShadow
    // ToggleButton.disabledText
    // RadioButtonMenuItem.disabledForeground
    // Button.disabledToolBarBorderBackground
    // Menu.disabledForeground
    // MenuItem.disabledForeground
    // Button.disabledText

    UIManager.put("CheckBox.disabledText", Color.RED);
    UIManager.put("ComboBox.disabledForeground", Color.GREEN);
    UIManager.put("Button.disabledText", Color.YELLOW);
    UIManager.put("Label.disabledForeground", Color.ORANGE);

    JCheckBox cbx1 = new JCheckBox("default", true);
    JCheckBox cbx2 = new JCheckBox("<html>html <span style='color:red'>tag", true);
    JLabel label = new JLabel("label disabledForeground");
    JButton button = new JButton("button disabledText");

    String[] model1 = {"disabledForeground", "bb"};
    JComboBox<String> cmb1 = new JComboBox<>(model1);

    String[] model2 = {"<html>html</html>", "renderer"};
    JComboBox<String> cmb2 = new JComboBox<String>(model2) {
      @Override public void updateUI() {
        setRenderer(null);
        super.updateUI();
        ListCellRenderer<?super String> r = getRenderer();
        setRenderer((list, value, index, isSelected, cellHasFocus) -> {
          Component c = r.getListCellRendererComponent(
              list, value, index, isSelected, cellHasFocus);
          if (index < 0 && !isEnabled() && c instanceof JLabel) {
            JLabel l = (JLabel) c;
            l.setText("<html><font color='red'>" + l.getText());
            l.setOpaque(false);
            // l.setForeground(Color.RED);
          }
          return c;
        });
        // setEditable(true);
      }
    };

    String[] model3 = {"setEditable(true)", "setDisabledTextColor"};
    JComboBox<String> cmb3 = new JComboBox<>(model3);
    cmb3.setEditable(true);
    JTextField editor = (JTextField) cmb3.getEditor().getEditorComponent();
    editor.setDisabledTextColor(Color.PINK);

    List<JComponent> list = Arrays.asList(cbx1, cbx2, cmb1, cmb2, cmb3, label, button);

    JCheckBox cbx = new JCheckBox("setEnabled");
    cbx.addActionListener(e -> {
      boolean flg = ((JCheckBox) e.getSource()).isSelected();
      list.forEach(c -> c.setEnabled(flg));
    });

    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 5));
    list.forEach(c -> {
      c.setEnabled(false);
      c.setAlignmentX(LEFT_ALIGNMENT);
      int h = c.getPreferredSize().height;
      c.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
      box.add(c);
      box.add(Box.createVerticalStrut(5));
    });
    box.add(Box.createVerticalGlue());
    add(cbx, BorderLayout.NORTH);
    add(box);
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
    frame.setMinimumSize(new Dimension(256, 100));
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
