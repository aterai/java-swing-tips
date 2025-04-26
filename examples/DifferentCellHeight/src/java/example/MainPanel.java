// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 0));
    DefaultListModel<String> m = new DefaultListModel<>();
    m.addElement("111");
    m.addElement("111\n222222");
    m.addElement("111\n222222\n333333333");
    m.addElement("111\n222222\n333333333\n444444444444");

    JList<String> list = new JList<String>(m) {
      @Override public void updateUI() {
        setCellRenderer(null);
        super.updateUI();
        setCellRenderer(new TextAreaRenderer<>());
        if (getFixedCellHeight() != -1) {
          setFixedCellHeight(-1);
        }
      }
    };

    add(new JScrollPane(new JList<>(m)));
    add(new JScrollPane(list));
    setPreferredSize(new Dimension(320, 240));
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

class TextAreaRenderer<E> implements ListCellRenderer<E> {
  // private Border focusBorder = new DotBorder(new Color(~selectionBgc.getRGB()), 2);
  // private static final Border NORMAL_BORDER = BorderFactory.createEmptyBorder(2, 2, 2, 2);
  private static final Color EVEN_COLOR = new Color(0xE6_FF_E6);
  private Border noFocusBorder;
  private Border focusBorder;
  private final JTextArea renderer = new JTextArea() {
    @Override public void updateUI() {
      super.updateUI();
      focusBorder = UIManager.getBorder("List.focusCellHighlightBorder");
      noFocusBorder = UIManager.getBorder("List.noFocusBorder");
      if (Objects.isNull(noFocusBorder) && Objects.nonNull(focusBorder)) {
        Insets i = focusBorder.getBorderInsets(this);
        noFocusBorder = BorderFactory.createEmptyBorder(i.top, i.left, i.bottom, i.right);
      }
    }
  };

  @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
    // setLineWrap(true);
    renderer.setText(Objects.toString(value, ""));
    if (isSelected) {
      renderer.setBackground(new Color(list.getSelectionBackground().getRGB())); // Nimbus
      renderer.setForeground(list.getSelectionForeground());
    } else {
      renderer.setBackground(index % 2 == 0 ? EVEN_COLOR : list.getBackground());
      renderer.setForeground(list.getForeground());
    }
    if (cellHasFocus) {
      renderer.setBorder(focusBorder);
    } else {
      renderer.setBorder(noFocusBorder);
    }
    return renderer;
  }
}

// class DotBorder extends LineBorder {
//   protected DotBorder(Color color, int thickness) {
//     super(color, thickness);
//   }
//
//   @Override public boolean isBorderOpaque() {
//     return true;
//   }
//
//   @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
//     Graphics2D g2 = (Graphics2D) g.create();
//     g2.translate(x, y);
//     g2.setPaint(getLineColor());
//     BasicGraphicsUtils.drawDashedRect(g2, 0, 0, w, h);
//     g2.dispose();
//   }
// }

// class DotBorder extends EmptyBorder {
//   private static final BasicStroke dashed = new BasicStroke(
//     1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
//     new float[]{1f}, 0f);
//
//   protected DotBorder(Insets borderInsets) {
//     super(borderInsets);
//   }
//
//   protected DotBorder(int top, int left, int bottom, int right) {
//     super(top, left, bottom, right);
//   }
//
//   @Override public boolean isBorderOpaque() {
//     return true;
//   }
//
//   @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
//     Graphics2D g2 = (Graphics2D) g.create();
//     g2.setPaint(c.getForeground());
//     g2.setStroke(dashed);
//     g2.translate(x, y);
//     g2.drawRect(0, 0, w - 1, h - 1);
//     g2.dispose();
//   }
// }
