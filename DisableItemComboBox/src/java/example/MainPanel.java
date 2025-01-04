// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextField field = new JTextField("1, 2, 5");

    DisableItemComboBox<String> combo = new DisableItemComboBox<>(makeModel());
    combo.setDisableIndexSet(getDisableIndexFromTextField(field));

    JButton button = new JButton("init");
    button.addActionListener(e -> combo.setDisableIndexSet(getDisableIndexFromTextField(field)));

    Box box = Box.createHorizontalBox();
    box.add(new JLabel("Disabled Item Index:"));
    box.add(field);
    box.add(Box.createHorizontalStrut(2));
    box.add(button);
    add(box, BorderLayout.SOUTH);
    add(combo, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    setPreferredSize(new Dimension(320, 240));
  }

  private static ComboBoxModel<String> makeModel() {
    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    model.addElement("0000000000000");
    model.addElement("111111");
    model.addElement("222222222222");
    model.addElement("33");
    model.addElement("4444444444444444");
    model.addElement("555555555555555555555555");
    model.addElement("6666666666");
    return model;
  }

  private static Set<Integer> getDisableIndexFromTextField(JTextField field) {
    return Stream.of(getPara(field))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(Integer::valueOf)
        .collect(Collectors.toSet());
  }

  private static String[] getPara(JTextField field) {
    String[] list;
    try {
      list = field.getText().split(",");
    } catch (NumberFormatException ex) {
      UIManager.getLookAndFeel().provideErrorFeedback(field);
      String msg = "invalid value.\n" + ex.getMessage();
      JOptionPane.showMessageDialog(field, msg, "Error", JOptionPane.ERROR_MESSAGE);
      list = new String[0];
    }
    return list;
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

class DisableItemComboBox<E> extends JComboBox<E> {
  private final Set<Integer> disableIndexSet = new HashSet<>();
  private boolean isDisableIndex;
  private final Action up = new AbstractAction() {
    @Override public void actionPerformed(ActionEvent e) {
      int si = getSelectedIndex();
      for (int i = si - 1; i >= 0; i--) {
        if (isEnabledIndex(i)) {
          setSelectedIndex(i);
          break;
        }
      }
    }
  };
  private final Action down = new AbstractAction() {
    @Override public void actionPerformed(ActionEvent e) {
      int si = getSelectedIndex();
      for (int i = si + 1; i < getItemCount(); i++) {
        if (isEnabledIndex(i)) {
          setSelectedIndex(i);
          break;
        }
      }
    }
  };

  // protected DisableItemComboBox() {
  //   super();
  // }

  protected DisableItemComboBox(ComboBoxModel<E> model) {
    super(model);
  }

  // protected DisableItemComboBox(E[] items) {
  //   super(items);
  // }

  @Override public void updateUI() {
    setRenderer(null);
    super.updateUI();
    ListCellRenderer<? super E> renderer = getRenderer();
    setRenderer((list, value, index, isSelected, cellHasFocus) -> {
      Component c;
      if (isEnabledIndex(index)) {
        c = renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        c.setEnabled(true);
      } else {
        c = renderer.getListCellRendererComponent(list, value, index, false, false);
        c.setEnabled(false);
      }
      return c;
    });
    EventQueue.invokeLater(() -> {
      String selectPrev = "selectPrevious3";
      String selectNext = "selectNext3";
      ActionMap am = getActionMap();
      am.put(selectPrev, up);
      am.put(selectNext, down);
      InputMap im = getInputMap();
      im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), selectPrev);
      im.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0), selectPrev);
      im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), selectNext);
      im.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0), selectNext);
    });
  }

  @Override public void setPopupVisible(boolean v) {
    if (!v && isDisableIndex) {
      isDisableIndex = false;
    } else {
      super.setPopupVisible(v);
    }
  }

  @Override public void setSelectedIndex(int index) {
    if (isEnabledIndex(index)) {
      // isDisableIndex = false;
      super.setSelectedIndex(index);
    } else {
      isDisableIndex = true;
    }
  }

  public boolean isEnabledIndex(int idx) {
    return !disableIndexSet.contains(idx);
  }

  public void setDisableIndexSet(Set<Integer> set) {
    disableIndexSet.clear();
    disableIndexSet.addAll(set);
  }
}
