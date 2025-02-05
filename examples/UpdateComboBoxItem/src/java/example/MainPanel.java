// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.accessibility.Accessible;
import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.plaf.basic.ComboPopup;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    GridBagConstraints c = new GridBagConstraints();
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

    c.insets = new Insets(10, 5, 5, 0);
    c.gridheight = 1;
    c.gridwidth = 1;
    c.gridy = 0;

    c.gridx = 0;
    c.weightx = 0.0;
    c.anchor = GridBagConstraints.WEST;
    Stream.of(
        "setSelectedIndex(-1/idx):",
        "contentsChanged(...):",
        "repaint():",
        "(remove/insert)ItemAt(...):",
        "fireContentsChanged(...):"
    ).map(JLabel::new).forEach(l -> {
      p.add(l, c);
      c.gridy += 1;
    });

    c.gridy = 0;
    c.gridx = 1;
    c.weightx = 1.0;
    c.fill = GridBagConstraints.HORIZONTAL;

    CheckItem[] m = {
        new CheckItem("aaa", false),
        new CheckItem("bbb bbb", true),
        new CheckItem("111", false),
        new CheckItem("33333", true),
        new CheckItem("2222", true),
        new CheckItem("ccc ccc", false)
    };

    JComboBox<CheckItem> combo0 = new CheckedComboBox<>(new DefaultComboBoxModel<>(m));
    JComboBox<CheckItem> combo1 = new CheckedComboBox1<>(new DefaultComboBoxModel<>(m));
    JComboBox<CheckItem> combo2 = new CheckedComboBox2<>(new DefaultComboBoxModel<>(m));
    JComboBox<CheckItem> combo3 = new CheckedComboBox3<>(new DefaultComboBoxModel<>(m));
    JComboBox<CheckItem> combo4 = new CheckedComboBox4<>(new CheckableComboBoxModel<>(m));
    Stream.of(combo0, combo1, combo2, combo3, combo4).forEach(cmp -> {
      // cmp.setPrototypeDisplayValue(displayValue);
      p.add(cmp, c);
      c.gridy += 1;
    });

    add(p, BorderLayout.NORTH);
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

class CheckItem {
  private final String text;
  private boolean selected;

  protected CheckItem(String text, boolean selected) {
    this.text = text;
    this.selected = selected;
  }

  public boolean isSelected() {
    return selected;
  }

  public void setSelected(boolean isSelected) {
    selected = isSelected;
  }

  @Override public String toString() {
    return text;
  }
}

class CheckBoxCellRenderer<E extends CheckItem> implements ListCellRenderer<E> {
  private final JLabel label = new JLabel(" ");
  private final JCheckBox check = new JCheckBox(" ");

  @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
    Component c;
    if (index < 0) {
      String txt = getCheckedItemString(list.getModel());
      label.setText(txt.isEmpty() ? " " : txt);
      c = label;
    } else {
      check.setText(Objects.toString(value, ""));
      check.setSelected(value.isSelected());
      if (isSelected) {
        check.setBackground(list.getSelectionBackground());
        check.setForeground(list.getSelectionForeground());
      } else {
        check.setBackground(list.getBackground());
        check.setForeground(list.getForeground());
      }
      c = check;
    }
    return c;
  }

  private static <E extends CheckItem> String getCheckedItemString(ListModel<E> model) {
    return IntStream.range(0, model.getSize())
        .mapToObj(model::getElementAt)
        .filter(CheckItem::isSelected)
        .map(Objects::toString)
        .sorted()
        .collect(Collectors.joining(", "));
  }
}

class CheckedComboBox<E extends CheckItem> extends JComboBox<E> {
  private boolean keepOpen;
  private transient ActionListener listener;

  // protected CheckedComboBox() {
  //   super();
  // }

  protected CheckedComboBox(ComboBoxModel<E> model) {
    super(model);
  }

  // // @SuppressWarnings("PMD.UseVarargs")
  // protected CheckedComboBox(E[] m) {
  //   super(m);
  // }

  // @SafeVarargs protected CheckedComboBox(E... m) {
  //   super(m);
  // }

  @Override public Dimension getPreferredSize() {
    return new Dimension(200, 20);
  }

  @Override public void updateUI() {
    setRenderer(null);
    removeActionListener(listener);
    super.updateUI();
    listener = e -> {
      if ((e.getModifiers() & AWTEvent.MOUSE_EVENT_MASK) != 0) {
        updateItem(getSelectedIndex());
        keepOpen = true;
      }
    };
    setRenderer(new CheckBoxCellRenderer<>());
    addActionListener(listener);
    String selectCmd = "checkbox-select";
    getActionMap().put(selectCmd, new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        Accessible a = getAccessibleContext().getAccessibleChild(0);
        if (a instanceof ComboPopup) {
          updateItem(((ComboPopup) a).getList().getSelectedIndex());
        }
      }
    });
    KeyStroke spaceKey = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);
    getInputMap(WHEN_FOCUSED).put(spaceKey, selectCmd);
  }

  protected void updateItem(int index) {
    if (isPopupVisible()) {
      E item = getItemAt(index);
      item.setSelected(!item.isSelected());
      setSelectedIndex(-1);
      setSelectedItem(item);
    }
  }

  @Override public void setPopupVisible(boolean v) {
    if (keepOpen) {
      keepOpen = false;
    } else {
      super.setPopupVisible(v);
    }
  }
}

class CheckedComboBox1<E extends CheckItem> extends CheckedComboBox<E> {
  protected CheckedComboBox1(ComboBoxModel<E> model) {
    super(model);
  }

  @Override protected void updateItem(int index) {
    if (isPopupVisible()) {
      CheckItem item = getItemAt(index);
      item.setSelected(!item.isSelected());
      contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, index, index));
    }
  }
}

class CheckedComboBox2<E extends CheckItem> extends CheckedComboBox<E> {
  protected CheckedComboBox2(ComboBoxModel<E> model) {
    super(model);
  }

  @Override protected void updateItem(int index) {
    if (isPopupVisible()) {
      CheckItem item = getItemAt(index);
      item.setSelected(!item.isSelected());
      repaint();
      Accessible a = getAccessibleContext().getAccessibleChild(0);
      if (a instanceof ComboPopup) {
        ((ComboPopup) a).getList().repaint();
      }
    }
  }
}

class CheckedComboBox3<E extends CheckItem> extends CheckedComboBox<E> {
  protected CheckedComboBox3(ComboBoxModel<E> model) {
    super(model);
  }

  @Override protected void updateItem(int index) {
    if (isPopupVisible()) {
      E item = getItemAt(index);
      item.setSelected(!item.isSelected());
      removeItemAt(index);
      insertItemAt(item, index);
      setSelectedItem(item);
    }
  }
}

class CheckableComboBoxModel<E> extends DefaultComboBoxModel<E> {
  // @SuppressWarnings("PMD.UseVarargs")
  // protected CheckableComboBoxModel(E[] items) {
  //   super(items);
  // }

  @SafeVarargs protected CheckableComboBoxModel(E... items) {
    super(items);
  }

  public void fireContentsChanged(int index) {
    super.fireContentsChanged(this, index, index);
  }
}

class CheckedComboBox4<E extends CheckItem> extends CheckedComboBox<E> {
  protected CheckedComboBox4(ComboBoxModel<E> model) {
    super(model);
  }

  @Override protected void updateItem(int index) {
    if (isPopupVisible()) {
      CheckItem item = getItemAt(index);
      item.setSelected(!item.isSelected());
      ComboBoxModel<E> m = getModel();
      if (m instanceof CheckableComboBoxModel) {
        ((CheckableComboBoxModel<E>) m).fireContentsChanged(index);
      }
    }
  }
}
