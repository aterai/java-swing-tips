// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JComboBox<String> combo = new JComboBox<>(makeModel());
    Action up = new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        JComboBox<?> c = (JComboBox<?>) e.getSource();
        int i = c.getSelectedIndex();
        // c.setSelectedIndex(i == 0 ? c.getItemCount() - 1 : i - 1);
        int size = c.getItemCount();
        c.setSelectedIndex((i - 1 + size) % size);
      }
    };
    Action down = new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        JComboBox<?> c = (JComboBox<?>) e.getSource();
        int i = c.getSelectedIndex();
        // c.setSelectedIndex(i == c.getItemCount() - 1 ? 0 : i + 1);
        int size = c.getItemCount();
        c.setSelectedIndex((i + 1) % size);
      }
    };
    ActionMap am = combo.getActionMap();
    am.put("loopUp", up);
    am.put("loopDown", down);

    InputMap im = combo.getInputMap();
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "loopUp");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "loopDown");

    Box box = Box.createVerticalBox();
    box.add(makeTitledPanel("default:", new JComboBox<>(makeModel())));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("loop:", combo));
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
  }

  private static ComboBoxModel<String> makeModel() {
    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    IntStream.range(0, 10).forEach(i -> model.addElement("item: " + i));
    return model;
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
