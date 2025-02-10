// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    DefaultListModel<String> model = new DefaultListModel<>();
    model.addElement("11\n1");
    model.addElement("222222222222222\n222222222222222");
    model.addElement("3333333333333333333\n33333333333333333333\n33333333333333333");
    model.addElement("444");

    add(new JScrollPane(new JList<String>(model) {
      private transient MouseInputListener handler;
      @Override public void updateUI() {
        removeMouseListener(handler);
        removeMouseMotionListener(handler);
        super.updateUI();
        setFixedCellHeight(-1);
        handler = new CellButtonsMouseListener<>(this);
        addMouseListener(handler);
        addMouseMotionListener(handler);
        setCellRenderer(new ButtonsRenderer<>(model));
      }
    }));
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

class CellButtonsMouseListener<E> extends MouseInputAdapter {
  private int prevIndex = -1;
  // private JButton prevButton;
  private final Rectangle prevButtonRect = new Rectangle();
  private final JList<E> list;

  protected CellButtonsMouseListener(JList<E> list) {
    super();
    this.list = list;
  }

  @Override public void mouseMoved(MouseEvent e) {
    // JList<?> list = (JList<?>) e.getComponent();
    Point pt = e.getPoint();
    int index = list.locationToIndex(pt);
    if (!list.getCellBounds(index, index).contains(pt)) {
      if (prevIndex >= 0) {
        rectRepaint(list, list.getCellBounds(prevIndex, prevIndex));
      }
      prevButtonRect.setSize(0, 0);
      return;
    }
    ListCellRenderer<? super E> lcr = list.getCellRenderer();
    if (index >= 0 && lcr instanceof ButtonsRenderer) {
      ButtonsRenderer<?> renderer = (ButtonsRenderer<?>) lcr;
      JButton button = getButton(list, pt, index);
      renderer.button = button;
      if (Objects.nonNull(button)) {
        button.getModel().setRollover(true);
        repaintCell(renderer, button.getBounds(), index);
      } else {
        repaintPrevButton(renderer, index);
      }
    }
    prevIndex = index;
  }

  private void repaintCell(ButtonsRenderer<?> renderer, Rectangle btnRect, int index) {
    renderer.rolloverIndex = index;
    if (!prevButtonRect.equals(btnRect)) {
      rectRepaint(list, list.getCellBounds(prevIndex, index));
    }
    prevButtonRect.setBounds(btnRect);
  }

  private void repaintPrevButton(ButtonsRenderer<?> renderer, int index) {
    renderer.rolloverIndex = -1;
    if (prevIndex == index) {
      if (!prevButtonRect.isEmpty()) {
        rectRepaint(list, list.getCellBounds(prevIndex, prevIndex));
      }
    } else {
      rectRepaint(list, list.getCellBounds(index, index));
    }
    prevIndex = -1;
    prevButtonRect.setSize(0, 0);
  }

  @Override public void mousePressed(MouseEvent e) {
    // JList<?> list = (JList<?>) e.getComponent();
    Point pt = e.getPoint();
    int index = list.locationToIndex(pt);
    if (index >= 0) {
      JButton button = getButton(list, pt, index);
      ListCellRenderer<? super E> renderer = list.getCellRenderer();
      if (Objects.nonNull(button) && renderer instanceof ButtonsRenderer) {
        ButtonsRenderer<?> r = (ButtonsRenderer<?>) renderer;
        r.pressedIndex = index;
        r.button = button;
        rectRepaint(list, list.getCellBounds(index, index));
      }
    }
  }

  @Override public void mouseReleased(MouseEvent e) {
    // JList<?> list = (JList<?>) e.getComponent();
    Point pt = e.getPoint();
    int index = list.locationToIndex(pt);
    if (index >= 0) {
      JButton button = getButton(list, pt, index);
      ListCellRenderer<? super E> renderer = list.getCellRenderer();
      if (Objects.nonNull(button) && renderer instanceof ButtonsRenderer) {
        ButtonsRenderer<?> r = (ButtonsRenderer<?>) renderer;
        r.pressedIndex = -1;
        // r.button = null;
        button.doClick();
        rectRepaint(list, list.getCellBounds(index, index));
      }
    }
  }

  private static void rectRepaint(JComponent c, Rectangle rect) {
    Optional.ofNullable(rect).ifPresent(c::repaint);
  }

  private static <E> JButton getButton(JList<E> list, Point pt, int index) {
    E proto = list.getPrototypeCellValue();
    ListCellRenderer<? super E> cr = list.getCellRenderer();
    Component c = cr.getListCellRendererComponent(list, proto, index, false, false);
    Rectangle r = list.getCellBounds(index, index);
    c.setBounds(r);
    // c.doLayout(); // may be needed for other layout managers (e.g. FlowLayout) // *1
    pt.translate(-r.x, -r.y);
    // Component b = SwingUtilities.getDeepestComponentAt(c, pt.x, pt.y);
    // if (b instanceof JButton) {
    //   return (JButton) b;
    // } else {
    //   return null;
    // }
    return Optional.ofNullable(SwingUtilities.getDeepestComponentAt(c, pt.x, pt.y))
        .filter(JButton.class::isInstance).map(JButton.class::cast).orElse(null);
  }
}

class ButtonsRenderer<E> implements ListCellRenderer<E> {
  private static final Color EVEN_COLOR = new Color(0xE6_FF_E6);
  protected int pressedIndex = -1;
  protected int rolloverIndex = -1;
  protected JButton button;
  private final JTextArea textArea = new JTextArea();
  private final JButton deleteButton = new JButton("delete");
  private final JButton copyButton = new JButton("copy");
  private final List<JButton> buttons = Arrays.asList(deleteButton, copyButton);
  private final JPanel renderer = new JPanel(new BorderLayout()) { // *1
    @Override public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      d.width = 0; // VerticalScrollBar as needed
      return d;
    }
  };
  private int targetIndex;

  protected ButtonsRenderer(DefaultListModel<E> model) {
    renderer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
    renderer.setOpaque(true);
    textArea.setLineWrap(true);
    textArea.setOpaque(false);
    renderer.add(textArea);

    deleteButton.addActionListener(e -> {
      boolean oneOrMore = model.getSize() > 1;
      if (oneOrMore) {
        model.remove(targetIndex);
      }
    });
    copyButton.addActionListener(e -> model.add(targetIndex, model.get(targetIndex)));

    Box box = Box.createHorizontalBox();
    buttons.forEach(b -> {
      b.setFocusable(false);
      b.setRolloverEnabled(false);
      box.add(b);
      box.add(Box.createHorizontalStrut(5));
    });
    renderer.add(box, BorderLayout.EAST);
  }

  @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
    textArea.setText(Objects.toString(value, ""));
    this.targetIndex = index;
    if (isSelected) {
      renderer.setBackground(list.getSelectionBackground());
      textArea.setForeground(list.getSelectionForeground());
    } else {
      renderer.setBackground(index % 2 == 0 ? EVEN_COLOR : list.getBackground());
      textArea.setForeground(list.getForeground());
    }
    buttons.forEach(ButtonsRenderer::resetButtonStatus);
    if (Objects.nonNull(button)) {
      if (index == pressedIndex) {
        button.getModel().setSelected(true);
        button.getModel().setArmed(true);
        button.getModel().setPressed(true);
      } else if (index == rolloverIndex) {
        button.getModel().setRollover(true);
      }
    }
    return renderer;
  }

  private static void resetButtonStatus(AbstractButton button) {
    ButtonModel model = button.getModel();
    model.setRollover(false);
    model.setArmed(false);
    model.setPressed(false);
    model.setSelected(false);
  }
}
