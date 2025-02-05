// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final SpinnerNumberModel model = new SpinnerNumberModel(9, 0, 100, 1);

  private MainPanel() {
    super(new BorderLayout(5, 5));
    JPanel p1 = new CenterLinePanel();
    p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
    p1.add(Box.createHorizontalGlue());
    p1.add(makeLabel());
    p1.add(Box.createHorizontalGlue());

    JPanel p2 = new CenterLinePanel();
    p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
    p2.add(Box.createVerticalGlue());
    p2.add(makeLabel());
    p2.add(Box.createVerticalGlue());

    JPanel panel = new JPanel(new GridLayout(1, 2, 5, 5));
    List<Component> list = Arrays.asList(p1, p2);
    list.forEach(c -> {
      c.setBackground(Color.WHITE);
      panel.add(c);
    });

    model.addChangeListener(e -> list.forEach(Component::revalidate));

    JPanel np = new JPanel(new GridLayout(1, 2));
    np.add(new JLabel("BoxLayout.X_AXIS", SwingConstants.CENTER));
    np.add(new JLabel("BoxLayout.Y_AXIS", SwingConstants.CENTER));

    JPanel sp = new JPanel(new BorderLayout());
    sp.add(new JLabel("MinimumSize: "), BorderLayout.WEST);
    sp.add(new JSpinner(model));

    add(np, BorderLayout.NORTH);
    add(panel);
    add(sp, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private JLabel makeLabel() {
    JLabel l = new JLabel("abc") {
      @Override public Dimension getPreferredSize() {
        return new Dimension(50, 50);
      }

      @Override public Dimension getMinimumSize() {
        return Optional.ofNullable(super.getMinimumSize()).map(d -> {
          int i = model.getNumber().intValue();
          d.setSize(i, i);
          return d;
        }).orElse(null);
      }
    };
    l.setOpaque(true);
    l.setBackground(Color.ORANGE);
    l.setFont(l.getFont().deriveFont(Font.PLAIN));
    l.setAlignmentX(CENTER_ALIGNMENT);
    l.setAlignmentY(CENTER_ALIGNMENT);
    l.setVerticalAlignment(SwingConstants.CENTER);
    l.setVerticalTextPosition(SwingConstants.CENTER);
    l.setHorizontalAlignment(SwingConstants.CENTER);
    l.setHorizontalTextPosition(SwingConstants.CENTER);
    return l;
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

class CenterLinePanel extends JPanel {
  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
    g.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight());
  }
}
