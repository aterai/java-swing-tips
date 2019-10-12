// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final Set<Integer> disableIndexSet = new HashSet<>();
  private final JTextField field = new JTextField("1, 2, 5");

  private MainPanel() {
    super(new BorderLayout(5, 5));
    JList<String> list = makeList(disableIndexSet);

    initDisableIndex(disableIndexSet);
    ActionMap am = list.getActionMap();
    am.put("selectNextRow", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        int index = list.getSelectedIndex();
        for (int i = index + 1; i < list.getModel().getSize(); i++) {
          if (!disableIndexSet.contains(i)) {
            list.setSelectedIndex(i);
            break;
          }
        }
      }
    });
    am.put("selectPreviousRow", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        int index = list.getSelectedIndex();
        for (int i = index - 1; i >= 0; i--) {
          if (!disableIndexSet.contains(i)) {
            list.setSelectedIndex(i);
            break;
          }
        }
      }
    });

    JButton button = new JButton("init");
    button.addActionListener(e -> {
      initDisableIndex(disableIndexSet);
      list.repaint();
    });

    Box box = Box.createHorizontalBox();
    box.add(new JLabel("Disabled Item Index:"));
    box.add(field);
    box.add(Box.createHorizontalStrut(2));
    box.add(button);

    add(new JScrollPane(list));
    add(box, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JList<String> makeList(Set<Integer> disableIndexSet) {
    DefaultListModel<String> model = new DefaultListModel<>();
    model.addElement("11111111111");
    model.addElement("222222222222222222");
    model.addElement("3333333333333");
    model.addElement("4444444444");
    model.addElement("5555555555555555");
    model.addElement("6666666666666");
    model.addElement("777777");

    return new JList<String>(model) {
      @Override public void updateUI() {
        setCellRenderer(null);
        super.updateUI();
        ListCellRenderer<? super String> renderer = getCellRenderer();
        setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
          Component c;
          if (disableIndexSet.contains(index)) {
            c = renderer.getListCellRendererComponent(list, value, index, false, false);
            c.setEnabled(false);
          } else {
            c = renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
          }
          return c;
        });
      }
    };
    // list.setSelectionModel(new DefaultListSelectionModel() {
    //   @Override public boolean isSelectedIndex(int index) {
    //     return !disableIndexSet.contains(index) && super.isSelectedIndex(index);
    //   }
    // });
    // return list;
  }

  protected void initDisableIndex(Set<Integer> set) {
    set.clear();
    try {
      set.addAll(Stream.of(field.getText().split(","))
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .map(Integer::valueOf)
          .collect(Collectors.toSet()));
    } catch (NumberFormatException ex) {
      Toolkit.getDefaultToolkit().beep();
      JOptionPane.showMessageDialog(field, "invalid value.\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
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
