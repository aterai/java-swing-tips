// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final float[] DEFAULT_DASH = {1f};
  private final JComboBox<JoinStyle> joinCombo = new JComboBox<>(JoinStyle.values());
  private final JComboBox<EndCapStyle> endCapCombo = new JComboBox<>(EndCapStyle.values());
  private final JTextField field = new JTextField("10, 20");
  private final JLabel label = new JLabel();
  private final JButton button = new JButton("Change");

  private float[] getDashArray() {
    // String[] list = field.getText().split(","); // ErrorProne: StringSplitter
    String[] list = Stream.of(field.getText().split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .toArray(String[]::new);
    if (list.length == 0) {
      return DEFAULT_DASH;
    }
    float[] ary = new float[list.length];
    int i = 0;
    try {
      for (String s : list) {
        String ss = s.trim();
        if (!ss.isEmpty()) {
          ary[i++] = Float.parseFloat(ss);
        }
      }
    } catch (NumberFormatException ex) {
      EventQueue.invokeLater(() -> {
        Toolkit.getDefaultToolkit().beep();
        String msg = "Invalid input.\n" + ex.getMessage();
        JOptionPane.showMessageDialog(getRootPane(), msg, "Error", JOptionPane.ERROR_MESSAGE);
      });
      return DEFAULT_DASH;
    }
    return i == 0 ? DEFAULT_DASH : ary;
  }

  private MainPanel() {
    super(new BorderLayout());
    button.addActionListener(e -> {
      int ecs = endCapCombo.getItemAt(endCapCombo.getSelectedIndex()).getStyle();
      int js = joinCombo.getItemAt(joinCombo.getSelectedIndex()).getStyle();
      BasicStroke dashedStroke = new BasicStroke(5f, ecs, js, 5f, getDashArray(), 0f);
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
