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
  private transient BasicStroke dashedStroke = makeStroke(1f, 1f, 5f, 1f);

  private MainPanel() {
    super(new BorderLayout());
    JLabel label = new JLabel() {
      @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Insets i = getInsets();
        int w = getWidth();
        int h = getHeight() / 2;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setStroke(getDashedStroke());
        g2.drawLine(i.left, h, w - i.right, h);
        g2.dispose();
      }
    };
    label.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

    JTextField field = new JTextField("1f, 1f, 5f, 1f");
    JButton button = new JButton("Change");
    button.addActionListener(e -> {
      dashedStroke = makeStroke(getDashArray(field.getText()));
      label.repaint();
    });
    EventQueue.invokeLater(() -> getRootPane().setDefaultButton(button));

    JPanel p = new JPanel(new BorderLayout(5, 5));
    p.setBorder(BorderFactory.createTitledBorder("Comma Separated Values"));
    p.add(field);
    p.add(button, BorderLayout.EAST);

    add(p, BorderLayout.NORTH);
    add(label);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  public BasicStroke getDashedStroke() {
    return dashedStroke;
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

  private static BasicStroke makeStroke(float... dist) {
    return new BasicStroke(5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dist, 0f);
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
