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
  private MainPanel() {
    super(new BorderLayout(5, 5));
    JPanel p = new JPanel();
    IntStream.range(0, 8)
        .mapToObj(i -> "JCheckBox" + i)
        .map(GroupCheckBox::new)
        .forEach(p::add);
    p.add(new GroupCheckComboBox<>(makeModel(), 3), BorderLayout.NORTH);
    add(p);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static ComboBoxModel<CheckItem> makeModel() {
    CheckItem[] m = {
        new CheckItem("One", false),
        new CheckItem("Tow", true),
        new CheckItem("Three", false),
        new CheckItem("Fore", false),
        new CheckItem("Five", false),
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

  @Override public void updateUI() {
    super.updateUI();
    setModel(new ToggleButtonModel() {
      private static final int GROUP_SIZE = 3;

      @Override public void setSelected(boolean selected) {
        if (selected) {
          if (getSelectedObjects().length == GROUP_SIZE) {
            UIManager.getLookAndFeel().provideErrorFeedback(GroupCheckBox.this);
          } else {
            super.setSelected(true);
          }
        } else {
          super.setSelected(false);
        }
      }

      @Override public Object[] getSelectedObjects() {
        Container parent = getParent();
        return Arrays.stream(parent.getComponents())
            .filter(AbstractButton.class::isInstance)
            .map(AbstractButton.class::cast)
            .filter(AbstractButton::isSelected)
            .toArray();
      }
    });
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
class GroupCheckComboBox<E extends CheckItem> extends JComboBox<E> {
  protected boolean keepOpen;
  private final JPanel panel = new JPanel(new BorderLayout());
  private final int groupSize;

  protected GroupCheckComboBox(ComboBoxModel<E> model, int groupSize) {
    super(model);
    this.groupSize = groupSize;
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
        String txt = Arrays.stream(getSelectedObjects())
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
      if (item.isSelected()) {
        item.setSelected(false);
      } else {
        if (getSelectedObjects().length == groupSize) {
          UIManager.getLookAndFeel().provideErrorFeedback(this);
        } else {
          item.setSelected(true);
        }
      }
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
}
