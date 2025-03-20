// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.geom.Ellipse2D;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    List<? extends JButton> buttons = Arrays.asList(
        new RoundButton("005.png", "005d.png", "005g.png"),
        new RoundButton("003.png", "003d.png", "003g.png"),
        new RoundButton("001.png", "001d.png", "001g.png"),
        new RoundButton("002.png", "002d.png", "002g.png"),
        new RoundButton("004.png", "004d.png", "004g.png"));
    // TEST: buttons = makeButtonArray2(getClass()); // Set ButtonUI
    Box box = makeBox(buttons);
    add(box, BorderLayout.NORTH);

    JCheckBox check = new JCheckBox("ButtonBorder Color");
    check.addActionListener(e -> {
      Color bgc = ((JCheckBox) e.getSource()).isSelected() ? Color.WHITE : Color.BLACK;
      buttons.forEach(b -> b.setBackground(bgc));
      box.repaint();
    });
    JPanel p = new JPanel();
    p.add(check);

    JComboBox<? extends Enum<?>> alignmentsChoices = new JComboBox<>(ButtonAlignments.values());
    alignmentsChoices.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        ButtonAlignments ba = (ButtonAlignments) e.getItem();
        buttons.forEach(b -> b.setAlignmentY(ba.getAlignment()));
        box.revalidate();
      }
    });
    alignmentsChoices.setSelectedIndex(1);
    p.add(alignmentsChoices);
    p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    add(p, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Box makeBox(List<? extends JButton> buttons) {
    Box box = Box.createHorizontalBox();
    // JDK 5
    // new Box(BoxLayout.X_AXIS) {
    //   @Override protected void paintComponent(Graphics g) {
    //     if (Objects.nonNull(ui)) {
    //       super.paintComponent(g);
    //     } else if (isOpaque()) {
    //       g.setColor(getBackground());
    //       g.fillRect(0, 0, getWidth(), getHeight());
    //     }
    //   }
    // };
    box.setOpaque(true);
    box.setBackground(new Color(0x78_78_A0));
    box.add(Box.createHorizontalGlue());
    box.setBorder(BorderFactory.createEmptyBorder(60, 10, 60, 10));
    buttons.forEach(b -> {
      box.add(b);
      box.add(Box.createHorizontalStrut(5));
    });
    box.add(Box.createHorizontalGlue());
    return box;
  }

  // // TEST:
  // public static List<JButton> makeButtonArray2(Class clazz) {
  //   return Arrays.asList(
  //     new JButton(new ImageIcon(clazz.getResource("005.png"))) {{
  //       setPressedIcon(new ImageIcon(clazz.getResource("005d.png")));
  //       setRolloverIcon(new ImageIcon(clazz.getResource("005g.png")));
  //       setUI(new RoundImageButtonUI());
  //     }},
  //     new JButton(new ImageIcon(clazz.getResource("003.png"))) {{
  //       setPressedIcon(new ImageIcon(clazz.getResource("003d.png")));
  //       setRolloverIcon(new ImageIcon(clazz.getResource("003g.png")));
  //       setUI(new RoundImageButtonUI());
  //     }},
  //     new JButton(new ImageIcon(clazz.getResource("001.png"))) {{
  //       setPressedIcon(new ImageIcon(clazz.getResource("001d.png")));
  //       setRolloverIcon(new ImageIcon(clazz.getResource("001g.png")));
  //       setUI(new RoundImageButtonUI());
  //     }},
  //     new JButton(new ImageIcon(clazz.getResource("002.png"))) {{
  //       setPressedIcon(new ImageIcon(clazz.getResource("002d.png")));
  //       setRolloverIcon(new ImageIcon(clazz.getResource("002g.png")));
  //       setUI(new RoundImageButtonUI());
  //     }},
  //     new JButton(new ImageIcon(clazz.getResource("004.png"))) {{
  //       setPressedIcon(new ImageIcon(clazz.getResource("004d.png")));
  //       setRolloverIcon(new ImageIcon(clazz.getResource("004g.png")));
  //       setUI(new RoundImageButtonUI());
  //     }});
  // }

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

class RoundButton extends JButton {
  protected transient Shape shape;
  protected transient Shape base;
  // protected RoundButton() {
  //   super();
  // }
  //
  // protected RoundButton(Icon icon) {
  //   super(icon);
  // }
  //
  // protected RoundButton(String text) {
  //   super(text);
  // }
  //
  // protected RoundButton(Action a) {
  //   super(a);
  //   // setAction(a);
  // }
  //
  // protected RoundButton(String text, Icon icon) {
  //   super(text, icon);
  //   // setModel(new DefaultButtonModel());
  //   // init(text, icon);
  // }

  protected RoundButton(String i1, String i2, String i3) {
    super(makeIcon(i1));
    setPressedIcon(makeIcon(i2));
    setRolloverIcon(makeIcon(i3));
  }

  @Override public void updateUI() {
    super.updateUI();
    setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    setBackground(Color.BLACK);
    setContentAreaFilled(false);
    setFocusPainted(false);
    // setVerticalAlignment(SwingConstants.TOP);
    setAlignmentY(TOP_ALIGNMENT);
    initShape();
  }

  @Override public final void setPressedIcon(Icon pressedIcon) {
    super.setPressedIcon(pressedIcon);
  }

  @Override public final void setRolloverIcon(Icon rolloverIcon) {
    super.setRolloverIcon(rolloverIcon);
  }

  @Override public Dimension getPreferredSize() {
    Icon icon = getIcon();
    Insets i = getInsets();
    int iw = Math.max(icon.getIconWidth(), icon.getIconHeight());
    return new Dimension(iw + i.right + i.left, iw + i.top + i.bottom);
  }

  protected void initShape() {
    if (!getBounds().equals(base)) {
      Dimension s = getPreferredSize();
      base = getBounds();
      shape = new Ellipse2D.Double(0d, 0d, s.width - 1d, s.height - 1d);
    }
  }

  public static Icon makeIcon(String path) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return Optional.ofNullable(cl.getResource("example/" + path))
        .<Icon>map(ImageIcon::new)
        .orElseGet(() -> UIManager.getIcon("OptionPane.errorIcon"));
  }

  @Override protected void paintBorder(Graphics g) {
    initShape();
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(getBackground());
    // g2.setStroke(new BasicStroke(1f));
    g2.draw(shape);
    g2.dispose();
  }

  @Override public boolean contains(int x, int y) {
    initShape();
    // return shape != null && shape.contains(x, y);
    return Optional.ofNullable(shape)
        .map(s -> s.contains(x, y))
        .orElseGet(() -> super.contains(x, y));
  }
}

// class RoundImageButtonUI extends BasicButtonUI {
//   protected Shape shape, base;
//   @Override protected void installDefaults(AbstractButton b) {
//     super.installDefaults(b);
//     clearTextShiftOffset();
//     defaultTextShiftOffset = 0;
//     Icon icon = b.getIcon();
//     if (Objects.isNull(icon)) {
//       return;
//     }
//     b.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
//     b.setContentAreaFilled(false);
//     b.setFocusPainted(false);
//     b.setOpaque(false);
//     b.setBackground(Color.BLACK);
//     // b.setVerticalAlignment(SwingConstants.TOP);
//     b.setAlignmentY(Component.TOP_ALIGNMENT);
//     initShape(b);
//   }
//
//   @Override protected void installListeners(AbstractButton b) {
//     BasicButtonListener listener = new BasicButtonListener(b) {
//       @Override public void mousePressed(MouseEvent e) {
//         AbstractButton b = (AbstractButton) e.getSource();
//         initShape(b);
//         if (shape.contains(e.getPoint())) {
//           super.mousePressed(e);
//         }
//       }
//
//       @Override public void mouseEntered(MouseEvent e) {
//         if (shape.contains(e.getPoint())) {
//           super.mouseEntered(e);
//         }
//       }
//
//       @Override public void mouseMoved(MouseEvent e) {
//         if (shape.contains(e.getPoint())) {
//           super.mouseEntered(e);
//         } else {
//           super.mouseExited(e);
//         }
//       }
//     };
//     if (Objects.nonNull(listener)) {
//       b.addMouseListener(listener);
//       b.addMouseMotionListener(listener);
//       b.addFocusListener(listener);
//       b.addPropertyChangeListener(listener);
//       b.addChangeListener(listener);
//     }
//   }
//
//   @Override public void paint(Graphics g, JComponent c) {
//     super.paint(g, c);
//     Graphics2D g2 = (Graphics2D) g.create();
//     initShape(c);
//     // Border
//     g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//     g2.setPaint(c.getBackground());
//     // g2.setStroke(new BasicStroke(1f));
//     g2.draw(shape);
//     g2.dispose();
//   }
//
//   @Override public Dimension getPreferredSize(JComponent c) {
//     JButton b = (JButton) c;
//     Icon icon = b.getIcon();
//     Insets i = b.getInsets();
//     int iw = Math.max(icon.getIconWidth(), icon.getIconHeight());
//     return new Dimension(iw + i.right + i.left, iw + i.top + i.bottom);
//   }
//
//   private void initShape(JComponent c) {
//     if (!c.getBounds().equals(base)) {
//       Dimension s = c.getPreferredSize();
//       base = c.getBounds();
//       shape = new Ellipse2D.Double(0d, 0d, s.width - 1d, s.height - 1d);
//     }
//   }
// }

enum ButtonAlignments {
  TOP("Top Alignment", Component.TOP_ALIGNMENT),
  CENTER("Center Alignment", Component.CENTER_ALIGNMENT),
  BOTTOM("Bottom Alignment", Component.BOTTOM_ALIGNMENT);
  private final String description;
  private final float alignment;

  ButtonAlignments(String description, float alignment) {
    this.description = description;
    this.alignment = alignment;
  }

  public float getAlignment() {
    return alignment;
  }

  @Override public String toString() {
    return description;
  }
}
