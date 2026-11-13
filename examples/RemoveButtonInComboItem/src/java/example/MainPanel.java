// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.accessibility.Accessible;
import javax.swing.*;
import javax.swing.plaf.basic.ComboPopup;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JComboBox<String> c0 = createComboBox(true, false);
    JComboBox<String> c1 = createComboBox(false, false);
    JComboBox<String> c2 = createComboBox(true, true);
    JComboBox<String> c3 = createComboBox(false, true);

    JButton button = new JButton("add");
    button.addActionListener(e -> {
      String str = LocalDateTime.now(ZoneId.systemDefault()).toString();
      Stream.of(c0, c1, c2, c3).forEach(c -> {
        MutableComboBoxModel<String> m = (MutableComboBoxModel<String>) c.getModel();
        m.addElement(str);
      });
    });

    JPanel p = new JPanel(new GridLayout(2, 1));
    p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    p.add(createTitledPanel("setEditable(false)", Arrays.asList(c0, c1)));
    p.add(createTitledPanel("setEditable(true)", Arrays.asList(c2, c3)));

    add(p, BorderLayout.NORTH);
    add(button, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JComboBox<String> createComboBox(boolean isDefault, boolean isEditable) {
    ComboBoxModel<String> m = new DefaultComboBoxModel<>(new String[] {"aaa", "bbb", "ccc"});
    JComboBox<String> combo = isDefault ? new JComboBox<>(m) : new RemoveButtonComboBox<>(m);
    combo.setEditable(isEditable);
    return combo;
  }

  private static Component createTitledPanel(String title, List<? extends Component> list) {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(5, 5, 5, 5);
    c.weightx = 1d;
    c.gridx = GridBagConstraints.REMAINDER;
    list.forEach(cmp -> p.add(cmp, c));
    return p;
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
      Logger.getGlobal().severe(ex::getMessage);
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

class RemoveButtonComboBox<E> extends JComboBox<E> {
  private transient CellButtonsMouseListener listener;

  protected RemoveButtonComboBox(ComboBoxModel<E> model) {
    super(model);
  }

  @Override public void updateUI() {
    if (Objects.nonNull(listener)) {
      getList().ifPresent(list -> {
        list.removeMouseListener(listener);
        list.removeMouseMotionListener(listener);
      });
    }
    super.updateUI();
    setRenderer(new ButtonsRenderer<>(this));
    getList().ifPresent(list -> {
      listener = new CellButtonsMouseListener();
      list.addMouseListener(listener);
      list.addMouseMotionListener(listener);
    });
  }

  protected Optional<JList<?>> getList() {
    Accessible a = getAccessibleContext().getAccessibleChild(0);
    return a instanceof ComboPopup
        ? Optional.ofNullable(((ComboPopup) a).getList())
        : Optional.empty();
  }
}

class CellButtonsMouseListener extends MouseAdapter {
  @Override public void mouseMoved(MouseEvent e) {
    JList<?> list = (JList<?>) e.getComponent();
    Point pt = e.getPoint();
    int index = list.locationToIndex(pt);
    boolean isOnButton = index >= 0 && Objects.nonNull(getButton(list, pt, index));
    getButtonsRenderer(list).setRolloverIndex(isOnButton ? index : -1);
    list.repaint(); // repaint all cells
  }

  @Override public void mousePressed(MouseEvent e) {
    e.getComponent().repaint(); // repaint all cells
  }

  @Override public void mouseReleased(MouseEvent e) {
    JList<?> list = (JList<?>) e.getComponent();
    Point pt = e.getPoint();
    int index = list.locationToIndex(pt);
    if (index >= 0) {
      JButton button = getButton(list, pt, index);
      if (Objects.nonNull(button)) {
        button.doClick();
      }
    }
    getButtonsRenderer(list).setRolloverIndex(-1);
    list.repaint(); // repaint all cells
  }

  @Override public void mouseExited(MouseEvent e) {
    JList<?> list = (JList<?>) e.getComponent();
    getButtonsRenderer(list).setRolloverIndex(-1);
    // Repaint so that the rollover highlight of the delete button is cleared
    list.repaint();
  }

  private static ButtonsRenderer<?> getButtonsRenderer(JList<?> list) {
    return (ButtonsRenderer<?>) list.getCellRenderer();
  }

  private static <E> JButton getButton(JList<E> list, Point pt, int index) {
    E proto = list.getPrototypeCellValue();
    ListCellRenderer<? super E> renderer = list.getCellRenderer();
    // Calling the renderer also updates the target index of the delete button
    Component c = renderer.getListCellRendererComponent(list, proto, index, false, false);
    Rectangle r = list.getCellBounds(index, index);
    c.setBounds(r);
    // c.doLayout(); // may be needed for other layout managers (e.g. FlowLayout) // *1
    pt.translate(-r.x, -r.y);
    return Optional.ofNullable(SwingUtilities.getDeepestComponentAt(c, pt.x, pt.y))
        .filter(JButton.class::isInstance).map(JButton.class::cast).orElse(null);
  }
}

class ButtonsRenderer<E> implements ListCellRenderer<E> {
  private static final Color EVEN_COLOR = new Color(0xE6_FF_E6);
  private int targetIndex;
  private int rolloverIndex = -1;
  private final JPanel panel = new JPanel(new BorderLayout()) { // *1
    @Override public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      d.width = 0;
      return d;
    }
  };
  private final ListCellRenderer<? super E> renderer = new DefaultListCellRenderer();
  private final JButton deleteButton = new JButton("x") {
    @Override public Dimension getPreferredSize() {
      return new Dimension(16, 16);
    }

    @Override public void updateUI() {
      super.updateUI();
      setBorder(BorderFactory.createEmptyBorder());
      setFocusable(false);
      setRolloverEnabled(false);
      setContentAreaFilled(false);
    }
  };

  protected ButtonsRenderer(RemoveButtonComboBox<E> comboBox) {
    deleteButton.addActionListener(e -> {
      ComboBoxModel<E> m = comboBox.getModel();
      // Keep at least one item in the model
      boolean hasMultipleItems = m.getSize() > 1;
      if (hasMultipleItems && m instanceof MutableComboBoxModel) {
        ((MutableComboBoxModel<?>) m).removeElementAt(targetIndex);
        comboBox.setSelectedIndex(-1);
        comboBox.showPopup(); // reopen the popup to keep it visible
      }
    });
    panel.setOpaque(true);
    panel.add(deleteButton, BorderLayout.EAST);
  }

  public void setRolloverIndex(int index) {
    rolloverIndex = index;
  }

  @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
    Component c = renderer.getListCellRendererComponent(
        list, value, index, isSelected, cellHasFocus);
    if (index >= 0 && c instanceof JComponent) {
      JComponent label = (JComponent) c;
      label.setOpaque(false);
      this.targetIndex = index;
      updateDeleteButton(list, index == rolloverIndex);
      panel.setBackground(getPanelBackground(list, index, isSelected));
      panel.add(label);
      c = panel;
    }
    return c;
  }

  private static Color getPanelBackground(JList<?> list, int index, boolean isSelected) {
    Color bgc;
    if (isSelected) {
      bgc = list.getSelectionBackground();
    } else {
      bgc = index % 2 == 0 ? EVEN_COLOR : list.getBackground();
    }
    return bgc;
  }

  private void updateDeleteButton(JList<?> list, boolean isRollover) {
    boolean showDeleteButton = list.getModel().getSize() > 1;
    deleteButton.setVisible(showDeleteButton);
    if (showDeleteButton) {
      deleteButton.getModel().setRollover(isRollover);
      deleteButton.setForeground(isRollover ? Color.WHITE : list.getForeground());
    }
  }
}
