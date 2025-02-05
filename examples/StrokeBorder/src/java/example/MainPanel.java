// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final float[] DEFAULT_DASH = {1f};

  private MainPanel() {
    super(new BorderLayout());
    JComboBox<JoinStyle> joinCombo = new JComboBox<>(JoinStyle.values());
    JComboBox<EndCapStyle> endCapCombo = new JComboBox<>(EndCapStyle.values());
    JTextField field = new JTextField("10f, 20f");
    JLabel label = new JLabel();
    JButton button = new JButton("Change");
    button.addActionListener(e -> {
      int ecs = endCapCombo.getItemAt(endCapCombo.getSelectedIndex()).getStyle();
      int js = joinCombo.getItemAt(joinCombo.getSelectedIndex()).getStyle();
      String txt = field.getText();
      BasicStroke dashedStroke = new BasicStroke(5f, ecs, js, 5f, getDashArray(txt), 0f);
      label.setBorder(BorderFactory.createStrokeBorder(dashedStroke, Color.RED));
    });

    JPanel p = new JPanel(new BorderLayout(2, 2));
    p.add(field);
    p.add(button, BorderLayout.EAST);
    p.setBorder(BorderFactory.createTitledBorder("Comma Separated Values"));

    JPanel p1 = new JPanel(new GridLayout(2, 1));
    p1.add(endCapCombo);
    p1.add(joinCombo);

    p.add(p1, BorderLayout.NORTH);

    JPanel p2 = new JPanel(new BorderLayout());
    p2.add(label);
    p2.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    add(p, BorderLayout.NORTH);
    add(p2);
    setPreferredSize(new Dimension(320, 240));

    button.doClick();
    EventQueue.invokeLater(() -> getRootPane().setDefaultButton(button));
  }

  private float[] getDashArray(String txt) {
    List<Float> list = new ArrayList<>();
    try {
      Stream.of(txt.split(","))
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .map(Float::parseFloat)
          .forEach(list::add);
    } catch (NumberFormatException ex) {
      EventQueue.invokeLater(() -> {
        Toolkit.getDefaultToolkit().beep();
        Component c = getRootPane();
        String msg = "Invalid input.\n" + ex.getMessage();
        JOptionPane.showMessageDialog(c, msg, "Error", JOptionPane.ERROR_MESSAGE);
      });
      // list.clear();
    }
    return list.isEmpty() ? DEFAULT_DASH : toPrimitive(list);
  }

  public static float[] toPrimitive(List<Float> list) {
    float[] array = new float[list.size()];
    for (int i = 0; i < array.length; i++) {
      array[i] = list.get(i); // .floatValue();
    }
    return array;
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

enum JoinStyle {
  JOIN_BEVEL(BasicStroke.JOIN_BEVEL),
  JOIN_MITER(BasicStroke.JOIN_MITER),
  JOIN_ROUND(BasicStroke.JOIN_ROUND);
  private final int style;

  JoinStyle(int style) {
    this.style = style;
  }

  public int getStyle() {
    return style;
  }
}

enum EndCapStyle {
  CAP_BUTT(BasicStroke.CAP_BUTT),
  CAP_ROUND(BasicStroke.CAP_ROUND),
  CAP_SQUARE(BasicStroke.CAP_SQUARE);
  private final int style;

  EndCapStyle(int style) {
    this.style = style;
  }

  public int getStyle() {
    return style;
  }
}
