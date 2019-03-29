// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Optional;
import java.util.stream.Stream;
import javax.swing.*;

public class MainPanel extends JPanel {
  private final JTextField field = new JTextField("1f, 1f, 5f, 1f");
  protected transient BasicStroke dashedStroke;

  protected final float[] getDashArray() {
    // String[] slist = field.getText().split(","); // ErrorProne: StringSplitter
    String[] slist = Stream.of(field.getText().split(","))
      .map(String::trim)
      .filter(s -> !s.isEmpty())
      .toArray(String[]::new);
    if (slist.length == 0) {
      return new float[] {1f};
    }
    float[] list = new float[slist.length];
    int i = 0;
    try {
      for (String s: slist) {
        String ss = s.trim();
        if (!ss.isEmpty()) {
          list[i++] = Float.parseFloat(ss);
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
    return i == 0 ? new float[] {1f} : list;
  }

  public MainPanel() {
    super(new BorderLayout());
    JLabel label = new JLabel() {
      @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        dashedStroke = Optional.ofNullable(dashedStroke)
          .orElseGet(() -> new BasicStroke(5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, getDashArray(), 0f));
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
      dashedStroke = null;
      label.repaint();
    });

    JPanel p = new JPanel(new BorderLayout(5, 5));
    p.add(field);
    p.add(button, BorderLayout.EAST);
    p.setBorder(BorderFactory.createTitledBorder("Comma Separated Values"));

    add(p, BorderLayout.NORTH);
    add(label);
    setPreferredSize(new Dimension(320, 240));
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
