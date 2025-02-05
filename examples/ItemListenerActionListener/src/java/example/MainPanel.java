// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea textArea = new JTextArea();

    JPanel p = new JPanel(new GridLayout(0, 1));
    p.add(makeComboBox(textArea));
    p.add(makeCheckBoxes(textArea));
    p.add(makeRadioButtons(textArea));

    add(p, BorderLayout.NORTH);
    add(new JScrollPane(textArea));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeComboBox(JTextArea textArea) {
    JComboBox<DayOfWeek> combo = new JComboBox<>(DayOfWeek.values());
    combo.addItemListener(e -> {
      ItemSelectable c = e.getItemSelectable();
      DayOfWeek dow = (DayOfWeek) e.getItem();
      boolean b = e.getStateChange() == ItemEvent.SELECTED;
      print(textArea, e, c.getClass(), b, dow);
    });
    combo.addActionListener(e -> {
      Object c = e.getSource();
      DayOfWeek dow = combo.getItemAt(combo.getSelectedIndex());
      boolean b = Objects.equals("comboBoxChanged", e.getActionCommand());
      print(textArea, e, c.getClass(), b, dow);
    });

    JButton button0 = new JButton("0");
    button0.addActionListener(e -> combo.setSelectedIndex(0));

    JButton button1 = new JButton("clear(-1)");
    button1.addActionListener(e -> combo.setSelectedIndex(-1));

    JPanel p = new JPanel();
    Stream.of(Box.createVerticalStrut(40), combo, button0, button1).forEach(p::add);
    return p;
  }

  private static Component makeCheckBoxes(JTextArea textArea) {
    ItemListener il = e -> {
      // AbstractButton c = (AbstractButton) e.getItem();
      AbstractButton c = (AbstractButton) e.getItemSelectable();
      boolean b = e.getStateChange() == ItemEvent.SELECTED;
      String ac = c.getActionCommand();
      print(textArea, e, c.getClass(), b, ac);
      // if (e.getStateChange() == ItemEvent.SELECTED) {
      //   for (Object o : e.getItemSelectable().getSelectedObjects()) {
      //     System.out.println(Objects.toString(o));
      //   }
      // }
    };
    ActionListener al = e -> {
      AbstractButton c = (AbstractButton) e.getSource();
      print(textArea, e, c.getClass(), c.isSelected(), e.getActionCommand());
    };
    List<AbstractButton> list = new ArrayList<>(7);
    Box p = Box.createHorizontalBox();
    Stream.of(DayOfWeek.values()).map(Objects::toString).map(JCheckBox::new).forEach(check -> {
      list.add(check);
      check.addItemListener(il);
      check.addActionListener(al);
      p.add(check);
    });

    JButton button0 = new JButton("clear");
    button0.addActionListener(e -> list.forEach(c -> c.setSelected(false)));

    JButton button1 = new JButton("all");
    button1.addActionListener(e -> list.forEach(c -> c.setSelected(true)));

    p.add(button0);
    p.add(button1);
    return new JScrollPane(p);
  }

  private static Component makeRadioButtons(JTextArea textArea) {
    ButtonGroup bg = new ButtonGroup();
    ItemListener il = e -> {
      AbstractButton c = (AbstractButton) e.getItemSelectable();
      String ac = Optional.ofNullable(bg.getSelection())
          .map(ButtonModel::getActionCommand)
          .orElse("NULL");
      print(textArea, e, c.getClass(), e.getStateChange() == ItemEvent.SELECTED, ac);
    };
    ActionListener al = e -> {
      AbstractButton c = (AbstractButton) e.getSource();
      print(textArea, e, c.getClass(), c.isSelected(), e.getActionCommand());
    };
    Box p = Box.createHorizontalBox();
    Stream.of(DayOfWeek.values()).map(Objects::toString).map(JRadioButton::new).forEach(r -> {
      r.addItemListener(il);
      r.addActionListener(al);
      p.add(r);
      bg.add(r);
    });

    JButton button0 = new JButton("clear");
    button0.addActionListener(e -> bg.clearSelection());

    JButton button1 = new JButton("MONDAY");
    button1.addActionListener(e -> Collections.list(bg.getElements()).stream()
        .findFirst().ifPresent(b -> b.setSelected(!b.isSelected())));

    p.add(button0);
    p.add(button1);
    return new JScrollPane(p);
  }

  private static void print(JTextArea log, AWTEvent e, Class<?> clz, boolean selected, Object o) {
    String l = e.getClass().getSimpleName();
    String s = selected ? "SELECTED" : "DESELECTED";
    log.append(String.format("%-14s %s %-10s %s%n", l, clz.getSimpleName(), s, o));
    log.setCaretPosition(log.getDocument().getLength());
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
      ex.printStackTrace();
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
