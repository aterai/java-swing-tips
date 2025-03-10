// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.ComboPopup;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    UIManager.put("ComboBox.selectionBackground", Color.PINK);
    UIManager.put("ComboBox.selectionForeground", Color.CYAN);

    String[] model = {"111", "2222", "33333"};

    JComboBox<String> combo0 = new JComboBox<>(model);

    JComboBox<String> combo1 = new JComboBox<String>(model) {
      @Override public void updateUI() {
        super.updateUI();
        Object o = getAccessibleContext().getAccessibleChild(0);
        if (o instanceof ComboPopup) {
          JList<?> list = ((ComboPopup) o).getList();
          list.setSelectionForeground(Color.WHITE);
          list.setSelectionBackground(Color.ORANGE);
        }
      }
    };

    JComboBox<String> combo2 = new JComboBox<String>(model) {
      @Override public void updateUI() {
        setRenderer(null);
        super.updateUI();
        ListCellRenderer<? super String> renderer = getRenderer();
        setRenderer((list, value, index, isSelected, cellHasFocus) -> {
          Component c = renderer.getListCellRendererComponent(
              list, value, index, isSelected, cellHasFocus);
          if (isSelected) {
            c.setForeground(Color.WHITE);
            c.setBackground(Color.ORANGE);
          } else {
            c.setForeground(Color.BLACK);
            c.setBackground(Color.WHITE);
          }
          return c;
        });
      }
    };

    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    box.add(makeTitledPanel("UIManager.put(ComboBox.selection*ground, ...)", combo0));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("ComboPopup.getList().setSelection*ground(...)", combo1));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("ListCellRenderer", combo2));
    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
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
