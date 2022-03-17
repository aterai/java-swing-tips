// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.accessibility.Accessible;
import javax.swing.*;
import javax.swing.plaf.basic.ComboPopup;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel(new GridLayout(0, 1));
    p.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
    p.add(new JLabel("Default:"));
    p.add(new JComboBox<>(makeModel()));
    p.add(Box.createVerticalStrut(20));
    p.add(new JLabel("CheckedComboBox:"));
    p.add(new CheckedComboBox<>(makeModel()));
    // p.add(new CheckedComboBox<>(new CheckableComboBoxModel<>(m)));
    p.add(Box.createVerticalStrut(20));
    p.add(new JLabel("CheckedComboBox(Windows):"));
    p.add(new WindowsCheckedComboBox<>(makeModel()));
    add(p, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static ComboBoxModel<CheckableItem> makeModel() {
    CheckableItem[] m = {
        new CheckableItem("aaa", false),
        new CheckableItem("bb", true),
        new CheckableItem("111", false),
        new CheckableItem("33333", true),
        new CheckableItem("2222", true),
        new CheckableItem("c", false)
    };
    return new DefaultComboBoxModel<>(m);
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
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

// class CheckableComboBoxModel<E> extends DefaultComboBoxModel<E> {
//   protected CheckableComboBoxModel(E[] items) {
//     super(items);
//   }
//
//   public void fireContentsChanged(int index) {
//     super.fireContentsChanged(this, index, index);
//   }
// }

class CheckableItem {
  private final String text;
  private boolean selected;

  protected CheckableItem(String text, boolean selected) {
    this.text = text;
    this.selected = selected;
  }

  public boolean isSelected() {
    return selected;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  @Override public String toString() {
    return text;
  }
}

// class CheckBoxCellRenderer<E extends CheckableItem> implements ListCellRenderer<E> {
//   private final JLabel label = new JLabel(" ");
//   private final JCheckBox check = new JCheckBox(" ");
//
//   @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
//     if (index < 0) {
//       String txt = getCheckedItemString(list.getModel());
//       label.setText(txt.isEmpty() ? " " : txt);
//       return label;
//     } else {
//       check.setText(Objects.toString(value, ""));
//       check.setSelected(value.isSelected());
//       if (isSelected) {
//         check.setBackground(list.getSelectionBackground());
//         check.setForeground(list.getSelectionForeground());
//       } else {
//         check.setBackground(list.getBackground());
//         check.setForeground(list.getForeground());
//       }
//       return check;
//     }
//   }
//
//   private static <E extends CheckableItem> String getCheckedItemString(ListModel<E> model) {
//     return IntStream.range(0, model.getSize())
//         .mapToObj(model::getElementAt)
//         .filter(CheckableItem::isSelected)
//         .map(Objects::toString)
//         .sorted()
//         .collect(Collectors.joining(", "));
//     // List<String> sl = new ArrayList<>();
//     // for (int i = 0; i < model.getSize(); i++) {
//     //   CheckableItem v = model.getElementAt(i);
//     //   if (v.isSelected()) {
//     //     sl.add(v.toString());
//     //   }
//     // }
//     // if (sl.isEmpty()) {
//     //   // When returning the empty string, the height of JComboBox may become 0 in some cases.
//     //   return " ";
//     // } else {
//     //   return sl.stream().sorted().collect(Collectors.joining(", "));
//     // }
//   }
// }

class CheckedComboBox<E extends CheckableItem> extends JComboBox<E> {
  protected boolean keepOpen;
  private final JPanel panel = new JPanel(new BorderLayout());

  //  protected CheckedComboBox() {
  //    super();
  //  }

  protected CheckedComboBox(ComboBoxModel<E> model) {
    super(model);
  }

  // protected CheckedComboBox(E[] m) {
  //   super(m);
  // }

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
            keepOpen = true;
            updateItem(list.locationToIndex(e.getPoint()));
          }
        }
      });
    }

    DefaultListCellRenderer renderer = new DefaultListCellRenderer();
    JCheckBox check = new JCheckBox();
    check.setOpaque(false);
    setRenderer((list, value, index, isSelected, cellHasFocus) -> {
      panel.removeAll();
      Component c = renderer.getListCellRendererComponent(
          list, value, index, isSelected, cellHasFocus);
      if (index < 0) {
        String txt = getCheckedItemString(list.getModel());
        JLabel l = (JLabel) c;
        l.setText(txt.isEmpty() ? " " : txt);
        l.setOpaque(false);
        l.setForeground(list.getForeground());
        panel.setOpaque(false);
      } else {
        check.setSelected(value.isSelected());
        panel.add(check, BorderLayout.WEST);
        panel.setOpaque(true);
        panel.setBackground(c.getBackground());
      }
      panel.add(c);
      return panel;
    });
    initActionMap();
  }

  protected void initActionMap() {
    KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0);
    getInputMap(JComponent.WHEN_FOCUSED).put(ks, "checkbox-select");
    getActionMap().put("checkbox-select", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        Accessible a = getAccessibleContext().getAccessibleChild(0);
        if (a instanceof ComboPopup) {
          updateItem(((ComboPopup) a).getList().getSelectedIndex());
        }
      }
    });
  }

  protected void updateItem(int index) {
    if (isPopupVisible() && index >= 0) {
      E item = getItemAt(index);
      item.setSelected(!item.isSelected());
      // item.selected ^= true;
      // ComboBoxModel m = getModel();
      // if (m instanceof CheckableComboBoxModel) {
      //   ((CheckableComboBoxModel) m).fireContentsChanged(index);
      // }
      // removeItemAt(index);
      // insertItemAt(item, index);
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

  protected static <E extends CheckableItem> String getCheckedItemString(ListModel<E> model) {
    return IntStream.range(0, model.getSize())
        .mapToObj(model::getElementAt)
        .filter(CheckableItem::isSelected)
        .map(Objects::toString)
        .sorted()
        .collect(Collectors.joining(", "));
  }
}

class WindowsCheckedComboBox<E extends CheckableItem> extends CheckedComboBox<E> {
  private transient ActionListener listener;

  protected WindowsCheckedComboBox(ComboBoxModel<E> model) {
    super(model);
  }

  @Override public void updateUI() {
    setRenderer(null);
    removeActionListener(listener);
    super.updateUI();
    listener = e -> {
      if ((e.getModifiers() & AWTEvent.MOUSE_EVENT_MASK) != 0) {
        keepOpen = true;
        updateItem(getSelectedIndex());
      }
    };
    addActionListener(listener);

    JLabel label = new JLabel(" ");
    JCheckBox check = new JCheckBox(" ");
    setRenderer((list, value, index, isSelected, cellHasFocus) -> {
      if (index < 0) {
        String txt = getCheckedItemString(list.getModel());
        label.setText(txt.isEmpty() ? " " : txt);
        return label;
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
        return check;
      }
    });
    initActionMap();
  }
}
