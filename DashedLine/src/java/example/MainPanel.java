// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.stream.Stream;
import javax.swing.*;

public class MainPanel extends JPanel {
  protected transient BasicStroke dashedStroke = makeStroke(1f, 1f, 5f, 1f);

  public MainPanel() {
    super(new BorderLayout());
    JTextField field = new JTextField("1f, 1f, 5f, 1f");

    JLabel label = new JLabel() {
      @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Insets i = getInsets();
        int w = getWidth();
        int h = getHeight() / 2;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setStroke(dashedStroke);
        g2.drawLine(i.left, h, w - i.right, h);
        g2.dispose();
      }
    };
    label.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

    JButton button = new JButton("Change");
    button.addActionListener(e -> {
      dashedStroke = makeStroke(getDashArray(tokenize(field.getText().trim())));
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

  private float[] getDashArray(String... strArray) {
    if (strArray.length == 0) {
      return new float[] {1f};
    }
    float[] dist = new float[strArray.length];
    int i = 0;
    try {
      for (String s: strArray) {
        String ss = s.trim();
        if (!ss.isEmpty()) {
          dist[i++] = Float.parseFloat(ss);
        }
      }
    } catch (NumberFormatException ex) {
      EventQueue.invokeLater(() -> {
        Toolkit.getDefaultToolkit().beep();
        String msg = "Invalid input.\n" + ex.getMessage();
        JOptionPane.showMessageDialog(getRootPane(), msg, "Error", JOptionPane.ERROR_MESSAGE);
      });
      return new float[] {1f};
    }
    return i == 0 ? new float[] {1f} : dist;
  }

  private static BasicStroke makeStroke(float... dist) {
    return new BasicStroke(5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dist, 0f);
  }

  private static String[] tokenize(String text) {
    // String[] strArray = text.split(","); // ErrorProne: StringSplitter
    return Stream.of(text.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .toArray(String[]::new);
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
