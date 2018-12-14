// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsSpinnerUI;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout(5, 5));
    // UIManager.put("FormattedTextField.inactiveBackground", Color.RED);
    JSpinner spinner0 = new JSpinner();
    spinner0.setEnabled(false);

    JSpinner spinner1 = new JSpinner();
    spinner1.setEnabled(false);
    JSpinner.DefaultEditor editor1 = (JSpinner.DefaultEditor) spinner1.getEditor();
    editor1.setOpaque(false);
    JTextField field1 = editor1.getTextField();
    field1.setOpaque(false);

    // JSpinner s2 = new JSpinner();
    // s2.setBorder(BorderFactory.createCompoundBorder(
    //   BorderFactory.createLineBorder(new Color(127, 157, 185)),
    //   BorderFactory.createLineBorder(UIManager.getColor("FormattedTextField.inactiveBackground"), 2)));

    JSpinner spinner2 = new JSpinner();
    spinner2.setEnabled(false);
    spinner2.setBorder(BorderFactory.createEmptyBorder());
    JSpinner.DefaultEditor editor2 = (JSpinner.DefaultEditor) spinner2.getEditor();
    editor2.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, new Color(127, 157, 185)));
    JTextField field2 = editor2.getTextField();
    field2.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 0));

    JSpinner spinner3 = new SimpleBorderSpinner();
    spinner3.setEnabled(false);

    Box box = Box.createVerticalBox();
    addTestSpinner(box, spinner0, "Default");
    addTestSpinner(box, spinner1, "setOpaque(false)");
    addTestSpinner(box, spinner2, "setBorder(...)");
    addTestSpinner(box, spinner3, "paintComponent, paintChildren");

    List<Component> list = Arrays.asList(spinner0, spinner1, spinner2, spinner3);
    JCheckBox check = new JCheckBox("setEnabled");
    check.addActionListener(e -> {
      boolean flg = ((JCheckBox) e.getSource()).isSelected();
      list.forEach(s -> s.setEnabled(flg));
    });

    setBorder(BorderFactory.createEmptyBorder(2, 20, 2, 20));
    add(box, BorderLayout.NORTH);
    add(check, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void addTestSpinner(Box box, JSpinner spinner, String title) {
    JPanel p = new JPanel(new BorderLayout());
    p.add(spinner);
    p.setBorder(BorderFactory.createTitledBorder(title));
    box.add(p);
    box.add(Box.createVerticalStrut(2));
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
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class SimpleBorderSpinner extends JSpinner {
  @Override protected void paintComponent(Graphics g) {
    if (getUI() instanceof WindowsSpinnerUI) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setPaint(isEnabled() ? UIManager.getColor("FormattedTextField.background")
                  : UIManager.getColor("FormattedTextField.inactiveBackground"));
      g2.fillRect(0, 0, getWidth(), getHeight());
      g2.dispose();
    }
  }

  @Override protected void paintChildren(Graphics g) {
    super.paintChildren(g);
    if (!isEnabled() && getUI() instanceof WindowsSpinnerUI) {
      Rectangle r = getComponent(0).getBounds();
      r.add(getComponent(1).getBounds());
      r.width--;
      r.height--;
      // r.grow(-1, -1);

      Graphics2D g2 = (Graphics2D) g.create();
      g2.setPaint(UIManager.getColor("FormattedTextField.inactiveBackground"));
      g2.draw(r);
      g2.dispose();
    }
  }
}
