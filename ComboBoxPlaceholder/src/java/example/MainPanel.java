// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    String[][] arrays = {
      {"blue", "violet", "red", "yellow"},
      {"basketball", "soccer", "football", "hockey"},
      {"hot dogs", "pizza", "ravioli", "bananas"}
    };
    JComboBox<String> combo1 = new JComboBox<>(new String[] {"colors", "sports", "food"});
    JComboBox<String> combo2 = new JComboBox<>();

    combo1.setSelectedIndex(-1);
    combo1.setRenderer(new DefaultListCellRenderer() {
      @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        // XXX: String str = index < 0 ? "- Select category -" : value.toString();
        String str = Objects.toString(value, "- Select category -");
        super.getListCellRendererComponent(list, str, index, isSelected, cellHasFocus);
        return this;
      }
    });
    // combo1.addActionListener(e -> {
    //   int i = ((JComboBox<?>) e.getSource()).getSelectedIndex();
    //   if (i < 0) {
    //     combo2.setModel(new DefaultComboBoxModel<>());
    //   } else {
    //     int prev = combo2.getSelectedIndex();
    //     combo2.setModel(new DefaultComboBoxModel<>(arrays[i]));
    //     combo2.setSelectedIndex(Math.max(prev, 0));
    //   }
    // });
    combo1.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        int idx = ((JComboBox<?>) e.getItemSelectable()).getSelectedIndex();
        combo2.setModel(new DefaultComboBoxModel<>(arrays[idx]));
        combo2.setSelectedIndex(-1);
      }
    });

    combo2.setRenderer(new DefaultListCellRenderer() {
      @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        String str = Objects.toString(value, "- Select type -");
        super.getListCellRendererComponent(list, str, index, isSelected, cellHasFocus);
        return this;
      }
    });

    JPanel p = new JPanel(new GridLayout(4, 1, 5, 5));
    setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
    p.add(new JLabel("Category"));
    p.add(combo1);
    p.add(new JLabel("Type"));
    p.add(combo2);

    JButton button = new JButton("clear");
    button.addActionListener(e -> {
      combo1.setSelectedIndex(-1);
      combo2.setModel(new DefaultComboBoxModel<>());
    });

    add(p, BorderLayout.NORTH);
    add(button, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
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
