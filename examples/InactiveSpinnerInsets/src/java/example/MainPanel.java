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
    // Color bgc2 = UIManager.getColor("FormattedTextField.inactiveBackground");
    // s2.setBorder(BorderFactory.createCompoundBorder(
    //   BorderFactory.createLineBorder(new Color(127, 157, 185)),
    //   BorderFactory.createLineBorder(bgc2, 2)));

    JTextArea info = new JTextArea();
    append(info, "TextField.shadow");
    append(info, "TextField.darkShadow");
    append(info, "TextField.light");
    append(info, "TextField.highlight");
    append(info, "Spinner.border");
    append(info, "Spinner.editorBorderPainted");

    JSpinner spinner2 = new JSpinner() {
      @Override public void updateUI() {
        super.updateUI();
        setBorder(BorderFactory.createEmptyBorder());
        Color borderColor = UIManager.getColor("TextField.shadow");
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) getEditor();
        editor.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, borderColor));
        JTextField field = editor.getTextField();
        field.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 0));
      }
    };
    spinner2.setEnabled(false);

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
    add(new JScrollPane(info));
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

  private static void append(JTextArea info, String key) {
    info.append(String.format("%s: %s%n", key, UIManager.get(key)));
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

class SimpleBorderSpinner extends JSpinner {
  private boolean isWindowsLnF;

  @Override public void updateUI() {
    super.updateUI();
    isWindowsLnF = getUI().getClass().getName().contains("WindowsSpinnerUI");
  }

  @Override protected void paintComponent(Graphics g) {
    if (isWindowsLnF) {
      Graphics2D g2 = (Graphics2D) g.create();
      String key = isEnabled() ? "background" : "inactiveBackground";
      g2.setPaint(UIManager.getColor("FormattedTextField." + key));
      g2.fillRect(0, 0, getWidth(), getHeight());
      g2.dispose();
    } else {
      super.paintComponent(g);
    }
  }

  @Override protected void paintChildren(Graphics g) {
    super.paintChildren(g);
    if (!isEnabled() && isWindowsLnF) {
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
