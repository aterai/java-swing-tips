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

public class MainPanel extends JPanel {
  protected final Set<Integer> disableIndexSet = new HashSet<>();
  protected final JTextField field = new JTextField("1, 2, 5");
  protected final JList<String> list = makeList(disableIndexSet);

  public MainPanel() {
    super(new BorderLayout(5, 5));

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
    model.addElement("aaaaaaaaaaaa");
    model.addElement("bbbbbbbbbbbbbbbbbb");
    model.addElement("ccccccccccc");
    model.addElement("dddddddddddd");
    model.addElement("eeeeeeeeeeeeeeeeeee");
    model.addElement("fffffffffffffffffffffff");
    model.addElement("ggggggggg");

    JList<String> list = new JList<>(model);
    list.setCellRenderer(new DefaultListCellRenderer() {
      @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component c;
        if (disableIndexSet.contains(index)) {
          c = super.getListCellRendererComponent(list, value, index, false, false);
          c.setEnabled(false);
        } else {
          c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
        return c;
      }
    });

    // list.setSelectionModel(new DefaultListSelectionModel() {
    //   @Override public boolean isSelectedIndex(int index) {
    //     return !disableIndexSet.contains(index) && super.isSelectedIndex(index);
    //   }
    // });

    return list;
  }

  protected final void initDisableIndex(Set<Integer> set) {
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
