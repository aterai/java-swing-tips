// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    Box b1 = Box.createHorizontalBox();
    b1.add(Box.createHorizontalGlue());
    b1.add(new JButton("default"));
    b1.add(Box.createHorizontalStrut(5));
    b1.add(new JButton("a"));
    b1.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));

    Component b2 = createRightAlignBox2(Arrays.asList(new JButton("getPreferredSize"), new JButton("xxx")), 120, 5);
    Component b3 = createRightAlignBox3(Arrays.asList(new JButton("Spring+Box"), new JButton("Layout")), 100, 5);
    Component b4 = createRightAlignBox4(Arrays.asList(new JButton("SpringLayout"), new JButton("gap:2")), 120, 2);
    Component b5 = createRightAlignBox5(Arrays.asList(new JButton("GridLayout+Box"), new JButton("gap:2")), 2);
    Component b6 = createRightAlignBox6(Arrays.asList(new JButton("GridBugLayout"), new JButton("gap:2")), 120, 2);

    Box box = Box.createVerticalBox();
    Stream.of(b6, b5, b4, b3, b2, b1).forEach(c -> {
      box.add(new JSeparator());
      box.add(c);
    });
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component createRightAlignBox6(List<? extends Component> list, int width, int gap) {
    JPanel p = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(0, gap, 0, 0);
    list.forEach(b -> {
      c.ipadx = width - b.getPreferredSize().width;
      p.add(b, c);
    });
    p.setBorder(BorderFactory.createEmptyBorder(gap, gap, gap, gap));
    JPanel pp = new JPanel(new BorderLayout());
    pp.add(p, BorderLayout.EAST);
    return pp;
  }

  private static Component createRightAlignBox5(List<? extends Component> list, int gap) {
    JPanel p = new JPanel(new GridLayout(1, list.size(), gap, gap)) {
      @Override public Dimension getMaximumSize() {
        return super.getPreferredSize();
      }
    };
    list.forEach(p::add);
    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(p);
    box.setBorder(BorderFactory.createEmptyBorder(gap, gap, gap, gap));
    return box;
  }

  private static Component createRightAlignBox4(List<? extends Component> list, int width, int gap) {
    SpringLayout layout = new SpringLayout();
    JPanel p = new JPanel(layout) {
      @Override public Dimension getPreferredSize() {
        int maxHeight = list.stream()
            .map(b -> b.getPreferredSize().height)
            .reduce(0, Integer::max);
        return new Dimension(width * list.size() + gap + gap, maxHeight + gap + gap);
      }
    };
    Spring x = layout.getConstraint(SpringLayout.WIDTH, p);
    Spring y = Spring.constant(gap);
    Spring g = Spring.minus(Spring.constant(gap));
    Spring w = Spring.constant(width);
    for (Component b: list) {
      SpringLayout.Constraints constraints = layout.getConstraints(b);
      x = Spring.sum(x, g);
      constraints.setConstraint(SpringLayout.EAST, x);
      constraints.setY(y);
      constraints.setWidth(w);
      p.add(b);
      x = Spring.sum(x, Spring.minus(w));
    }
    return p;
  }

  private static Component createRightAlignBox3(List<? extends Component> list, int width, int gap) {
    SpringLayout layout = new SpringLayout();
    JPanel p = new JPanel(layout) {
      @Override public Dimension getPreferredSize() {
        int maxHeight = list.stream()
            .map(b -> b.getPreferredSize().height)
            .reduce(0, Integer::max);
        return new Dimension(width * list.size() + gap + gap, maxHeight + gap + gap);
      }
    };
    SpringLayout.Constraints cons = layout.getConstraints(p);
    // cons.setConstraint(SpringLayout.SOUTH, Spring.constant(p.getPreferredSize().height));
    cons.setConstraint(SpringLayout.EAST, Spring.constant((width + gap) * list.size()));

    Spring x = Spring.constant(0);
    Spring y = Spring.constant(gap);
    Spring g = Spring.constant(gap);
    Spring w = Spring.constant(width);
    for (Component b: list) {
      SpringLayout.Constraints constraints = layout.getConstraints(b);
      constraints.setX(x);
      constraints.setY(y);
      constraints.setWidth(w);
      p.add(b);
      x = Spring.sum(x, w);
      x = Spring.sum(x, g);
    }

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(p);
    return box;
  }

  private static Component createRightAlignBox2(List<? extends Component> list, int width, int gap) {
    JComponent box = new JPanel() {
      @Override public void updateUI() {
        list.forEach(b -> b.setPreferredSize(null));
        super.updateUI();
        EventQueue.invokeLater(() -> {
          int maxHeight = list.stream()
              .map(b -> b.getPreferredSize().height)
              .reduce(0, Integer::max);
          Dimension d = new Dimension(width, maxHeight);
          list.forEach(b -> b.setPreferredSize(d));
          revalidate();
        });
      }
    };
    box.setLayout(new BoxLayout(box, BoxLayout.X_AXIS));
    box.add(Box.createHorizontalGlue());
    list.forEach(b -> {
      box.add(b);
      box.add(Box.createHorizontalStrut(gap));
    });
    box.setBorder(BorderFactory.createEmptyBorder(gap, 0, gap, 0));
    return box;
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
