package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

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

        add(new JScrollPane(makeList(m, false)));
        add(new JScrollPane(makeList(m, true)));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JList<String> makeList(ListModel<String> model, boolean hasTextAreaRenderer) {
        return new JList<String>(model) {
            @Override public void updateUI() {
                setCellRenderer(null);
                super.updateUI();
                if (hasTextAreaRenderer) {
                    setCellRenderer(new TextAreaRenderer<>());
                    if (getFixedCellHeight() != -1) {
                        System.out.println(getFixedCellHeight());
                        setFixedCellHeight(-1);
                    }
                }
            }
        };
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
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

class TextAreaRenderer<E> extends JTextArea implements ListCellRenderer<E> {
    // private Border focusBorder; // = new DotBorder(new Color(~list1.getSelectionBackground().getRGB()), 2);
    // private static final Border NOMAL_BORDER = BorderFactory.createEmptyBorder(2, 2, 2, 2);
    private static final Color EVEN_COLOR = new Color(230, 255, 230);
    private Border noFocusBorder;
    private Border focusBorder;
    @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
        // setLineWrap(true);
        setText(Objects.toString(value, ""));
        if (isSelected) {
            setBackground(new Color(list.getSelectionBackground().getRGB())); // Nimbus
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(index % 2 == 0 ? EVEN_COLOR : list.getBackground());
            setForeground(list.getForeground());
        }
        if (cellHasFocus) {
            setBorder(focusBorder);
        } else {
            setBorder(noFocusBorder);
        }
        return this;
    }
    @Override public void updateUI() {
        super.updateUI();
        focusBorder = UIManager.getBorder("List.focusCellHighlightBorder");
        noFocusBorder = UIManager.getBorder("List.noFocusBorder");
        if (Objects.isNull(noFocusBorder) && Objects.nonNull(focusBorder)) {
            Insets i = focusBorder.getBorderInsets(this);
            noFocusBorder = BorderFactory.createEmptyBorder(i.top, i.left, i.bottom, i.right);
        }
    }
}

// class DotBorder extends LineBorder {
//     protected DotBorder(Color color, int thickness) {
//         super(color, thickness);
//     }
//     @Override public boolean isBorderOpaque() {
//         return true;
//     }
//     @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
//         Graphics2D g2 = (Graphics2D) g.create();
//         g2.translate(x, y);
//         g2.setPaint(getLineColor());
//         BasicGraphicsUtils.drawDashedRect(g2, 0, 0, w, h);
//         g2.dispose();
//     }
// }

// class DotBorder extends EmptyBorder {
//     private static final BasicStroke dashed = new BasicStroke(
//         1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
//         new float[]{1f}, 0f);
//     protected DotBorder(Insets borderInsets) {
//         super(borderInsets);
//     }
//     protected DotBorder(int top, int left, int bottom, int right) {
//         super(top, left, bottom, right);
//     }
//     @Override public boolean isBorderOpaque() {
//         return true;
//     }
//     @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
//         Graphics2D g2 = (Graphics2D) g.create();
//         g2.setPaint(c.getForeground());
//         g2.setStroke(dashed);
//         g2.translate(x, y);
//         g2.drawRect(0, 0, w - 1, h - 1);
//         g2.dispose();
//     }
//     // @Override public Insets getBorderInsets()
//     // @Override public Insets getBorderInsets(Component c)
//     // @Override public Insets getBorderInsets(Component c, Insets insets)
// }
