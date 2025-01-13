// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.accessibility.Accessible;
import javax.swing.*;
import javax.swing.plaf.basic.ComboPopup;

public final class MainPanel extends JPanel {
  private final JTextArea log = new JTextArea();

  private MainPanel() {
    super(new BorderLayout());
    ItemListener listener = e -> {
      Object item = e.getItem();
      ItemSelectable selectable = e.getItemSelectable();
      Object[] objects = selectable.getSelectedObjects();
      if (e.getStateChange() == ItemEvent.SELECTED) {
        log.append("Item: " + item + "\n");
        log.append("ItemSelectable: " + selectable.getClass().getName() + "\n");
      }
      log.append("SelectedObjects:");
      if (objects != null) {
        Arrays.stream(objects).forEach(o -> {
          String str = Objects.toString(o);
          if (o instanceof AbstractButton) {
            str = ((AbstractButton) o).getText();
          }
          log.append(" " + str);
        });
      }
      log.append("\n----\n");
    };

    JPanel p1 = new JPanel();
    ButtonGroup group = new ButtonGroup();
    Stream.of("JRadioButton1", "JRadioButton2")
        .map(JRadioButton::new)
        .forEach(b -> {
          b.addItemListener(listener);
          group.add(b);
          p1.add(b);
        });

    JPanel p2 = new JPanel();
    Stream.of("JCheckBox1", "JCheckBox2", "JCheckBox3")
        .map(GroupCheckBox::new)
        .forEach(b -> {
          b.addItemListener(listener);
          p2.add(b);
        });

    String[] model = {"One", "Tow", "Three"};
    JComboBox<String> combo1 = new JComboBox<>(model);
    combo1.addItemListener(listener);

    CheckedComboBox<CheckItem> combo2 = new CheckedComboBox<>(makeModel());
    combo2.addItemListener(listener);

    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    box.add(p1);
    box.add(p2);
    box.add(combo1);
    box.add(Box.createVerticalStrut(2));
    box.add(combo2);
    add(box, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));
  }

  private static ComboBoxModel<CheckItem> makeModel() {
    CheckItem[] m = {
        new CheckItem("One", false),
        new CheckItem("Tow", true),
        new CheckItem("Three", false),
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

class GroupCheckBox extends JCheckBox {
  protected GroupCheckBox(String title) {
    super(title);
  }

  @Override public Object[] getSelectedObjects() {
    Container parent = getParent();
    return Arrays.stream(parent.getComponents())
        .filter(AbstractButton.class::isInstance)
        .map(AbstractButton.class::cast)
        .filter(AbstractButton::isSelected)
        .toArray();
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

// https://ateraimemo.com/Swing/CheckedComboBox.html
// https://github.com/aterai/java-swing-tips/blob/master/CheckedComboBox/src/java/example/MainPanel.java
class CheckedComboBox<E extends CheckItem> extends JComboBox<E> {
  protected boolean keepOpen;
  private final JPanel panel = new JPanel(new BorderLayout());

  protected CheckedComboBox(ComboBoxModel<E> model) {
    super(model);
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
        // String txt = getCheckedItemString(list.getModel());
        String txt = Arrays.stream(getSelectedObjects())
            // .filter(CheckBoxItem.class::isInstance)
            // .map(CheckBoxItem.class::cast)
            // .filter(CheckBoxItem::isSelected)
            .map(Objects::toString)
            .sorted()
            .collect(Collectors.joining(", "));
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
    getInputMap(WHEN_FOCUSED).put(ks, "checkbox-select");
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

  @Override public Object[] getSelectedObjects() {
    return IntStream.range(0, getItemCount())
        .mapToObj(getModel()::getElementAt)
        .filter(CheckItem::isSelected)
        .toArray();
  }

  // protected static <E extends CheckBoxItem> String getCheckedItemString(ListModel<E> model) {
  //   return IntStream.range(0, model.getSize())
  //       .mapToObj(model::getElementAt)
  //       .filter(CheckBoxItem::isSelected)
  //       .map(Objects::toString)
  //       .sorted()
  //       .collect(Collectors.joining(", "));
  // }
}
