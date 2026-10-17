// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.accessibility.Accessible;
import javax.swing.*;
import javax.swing.plaf.basic.ComboPopup;

public final class MainPanel extends JPanel {
  private static final int SELECTION_LIMIT = 3;

  private MainPanel() {
    super(new BorderLayout(5, 5));
    JPanel p = new JPanel();
    IntStream.range(0, 8)
        .mapToObj(i -> new GroupCheckBox("JCheckBox" + i, SELECTION_LIMIT))
        .forEach(p::add);
    p.add(new LimitedCheckComboBox<>(createModel(), SELECTION_LIMIT));
    add(p);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static ComboBoxModel<CheckedItem> createModel() {
    CheckedItem[] m = {
        new CheckedItem("One", false),
        new CheckedItem("Two", true),
        new CheckedItem("Three", false),
        new CheckedItem("Four", false),
        new CheckedItem("Five", false),
    };
    return new DefaultComboBoxModel<>(m);
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

final class GroupCheckBox extends JCheckBox {
  /* default */ GroupCheckBox(String title, int maxSelectionCount) {
    super(title);
    // Replace the model in the constructor rather than in updateUI(),
    // so that the selection state is not lost when the Look&Feel is changed.
    setModel(new ButtonGroupModel(maxSelectionCount));
  }

  private final class ButtonGroupModel extends ToggleButtonModel {
    private final int maxSelectionCount;

    private ButtonGroupModel(int maxSelectionCount) {
      super();
      this.maxSelectionCount = maxSelectionCount;
    }

    @Override public void setSelected(boolean selected) {
      // Reject only a new selection that would exceed the limit
      // (re-selecting an already selected check box is not an error).
      if (selected && !isSelected() && getSelectedObjects().length >= maxSelectionCount) {
        UIManager.getLookAndFeel().provideErrorFeedback(GroupCheckBox.this);
      } else {
        super.setSelected(selected);
      }
    }

    // Returns the selected buttons in the same parent container (the group).
    @Override public Object[] getSelectedObjects() {
      Container parent = getParent();
      // Before being added to a container, the group consists of this check box only
      Component[] buttons = parent == null
          ? new Component[] {GroupCheckBox.this}
          : parent.getComponents();
      return Arrays.stream(buttons)
          .filter(AbstractButton.class::isInstance)
          .map(AbstractButton.class::cast)
          .filter(AbstractButton::isSelected)
          .toArray();
    }
  }
}

class CheckedItem {
  private final String label;
  private boolean selected;

  protected CheckedItem(String label, boolean selected) {
    this.label = label;
    this.selected = selected;
  }

  public boolean isSelected() {
    return selected;
  }

  public void setSelected(boolean isSelected) {
    selected = isSelected;
  }

  @Override public String toString() {
    return label;
  }
}

// https://ateraimemo.com/Swing/CheckedComboBox.html
// https://github.com/aterai/java-swing-tips/blob/master/CheckedComboBox/src/java/example/MainPanel.java
class LimitedCheckComboBox<E extends CheckedItem> extends JComboBox<E> {
  private final JPanel rendererPanel = new JPanel(new BorderLayout());
  private final int maxSelectionCount;
  private boolean keepPopupVisible;

  protected LimitedCheckComboBox(ComboBoxModel<E> model, int maxSelectionCount) {
    super(model);
    this.maxSelectionCount = maxSelectionCount;
  }

  @Override public Dimension getPreferredSize() {
    return new Dimension(200, 20);
  }

  @Override public void updateUI() {
    setRenderer(null);
    super.updateUI();
    Accessible a = getAccessibleContext().getAccessibleChild(0);
    if (a instanceof ComboPopup) {
      ((ComboPopup) a).getList().addMouseListener(new MouseAdapter() {
        @Override public void mousePressed(MouseEvent e) {
          JList<?> list = (JList<?>) e.getComponent();
          if (SwingUtilities.isLeftMouseButton(e)) {
            keepPopupVisible = true;
            toggleItemSelection(list.locationToIndex(e.getPoint()));
          }
        }
      });
    }

    DefaultListCellRenderer labelRenderer = new DefaultListCellRenderer();
    JCheckBox check = new JCheckBox();
    check.setOpaque(false);
    setRenderer((list, value, index, isSelected, cellHasFocus) -> {
      Component c = labelRenderer.getListCellRendererComponent(
          list, value, index, isSelected, cellHasFocus);
      configureRendererPanel(value, index < 0, c, check, list.getForeground());
      return rendererPanel;
    });
    initActionMap();
  }

  private void configureRendererPanel(
      E value, boolean isDisplayArea, Component label, JCheckBox check, Color foreground) {
    rendererPanel.removeAll();
    if (isDisplayArea && label instanceof JLabel) {
      // The combo box display area shows the comma-separated selected items
      JLabel l = (JLabel) label;
      l.setText(createSelectedText(getSelectedObjects()));
      l.setOpaque(false);
      l.setForeground(foreground);
      rendererPanel.setOpaque(false);
    } else {
      check.setSelected(value.isSelected());
      rendererPanel.add(check, BorderLayout.WEST);
      rendererPanel.setOpaque(true);
      rendererPanel.setBackground(label.getBackground());
    }
    rendererPanel.add(label);
  }

  public static String createSelectedText(Object... selectedObjects) {
    String txt = Arrays.stream(selectedObjects)
        .map(Objects::toString)
        .collect(Collectors.joining(", "));
    return txt.isEmpty() ? " " : txt;
  }

  protected void initActionMap() {
    KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);
    getInputMap(WHEN_FOCUSED).put(ks, "checkbox-select");
    getActionMap().put("checkbox-select", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        Accessible a = getAccessibleContext().getAccessibleChild(0);
        if (a instanceof ComboPopup) {
          toggleItemSelection(((ComboPopup) a).getList().getSelectedIndex());
        }
      }
    });
  }

  protected void toggleItemSelection(int itemIndex) {
    if (isPopupVisible() && itemIndex >= 0) {
      toggleSelection(getItemAt(itemIndex));
    }
  }

  private void toggleSelection(E item) {
    if (item.isSelected()) {
      item.setSelected(false);
    } else if (isSelectionLimitReached()) {
      notifySelectionLimit();
    } else {
      item.setSelected(true);
    }

    // Force the JComboBox to refresh its displayed text
    // when the same item is toggled repeatedly.
    setSelectedIndex(-1);
    setSelectedItem(item);
  }

  private boolean isSelectionLimitReached() {
    return getSelectedObjects().length >= maxSelectionCount;
  }

  private void notifySelectionLimit() {
    UIManager.getLookAndFeel().provideErrorFeedback(this);
  }

  @Override public void setPopupVisible(boolean v) {
    if (keepPopupVisible) {
      keepPopupVisible = false;
    } else {
      super.setPopupVisible(v);
    }
  }

  @Override public Object[] getSelectedObjects() {
    return IntStream.range(0, getItemCount())
        .mapToObj(this::getItemAt)
        .filter(CheckedItem::isSelected)
        .toArray();
  }
}
