// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Box box = Box.createVerticalBox();
    box.add(makeComboBox(makeModel()));
    box.setBorder(BorderFactory.createTitledBorder("ComboBoxSeparator"));
    add(box, BorderLayout.NORTH);
    add(new JScrollPane(new JTextArea("JTextArea")));
    setPreferredSize(new Dimension(320, 240));
  }

  private static ComboBoxModel<Object> makeModel() {
    DefaultComboBoxModel<Object> model = new DefaultComboBoxModel<Object>() {
      @Override public void setSelectedItem(Object anObject) {
        if (!(anObject instanceof JSeparator)) {
          super.setSelectedItem(anObject);
        }
      }
    };
    model.addElement("0000");
    model.addElement("0000111");
    model.addElement("000011122");
    model.addElement("00001112233333");
    model.addElement(new JSeparator());
    model.addElement("bbb1");
    model.addElement("bbb12");
    model.addElement("bbb33333");
    model.addElement(new JSeparator());
    model.addElement("11111");
    model.addElement("2222222");
    return model;
  }

  private static <E> JComboBox<E> makeComboBox(ComboBoxModel<E> model) {
    JComboBox<E> combo = new JComboBox<E>(model) {
      @Override public void updateUI() {
        setRenderer(null);
        super.updateUI();
        ListCellRenderer<? super E> renderer = getRenderer();
        setRenderer((list, value, index, isSelected, cellHasFocus) -> {
          if (value instanceof JSeparator) {
            return (Component) value;
          } else {
            return renderer.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
          }
        });
      }
    };
    ActionMap am = combo.getActionMap();
    String selectPrevKey = "selectPrevious3";
    am.put(selectPrevKey, new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        JComboBox<?> cb = (JComboBox<?>) e.getSource();
        int index = cb.getSelectedIndex();
        if (index == 0) {
          return;
        }
        Object o = cb.getItemAt(index - 1);
        if (o instanceof JSeparator) {
          cb.setSelectedIndex(index - 2);
        } else {
          cb.setSelectedIndex(index - 1);
        }
      }
    });
    String selectNextKey = "selectNext3";
    am.put(selectNextKey, new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        JComboBox<?> cb = (JComboBox<?>) e.getSource();
        int index = cb.getSelectedIndex();
        if (index == cb.getItemCount() - 1) {
          return;
        }
        Object o = cb.getItemAt(index + 1);
        if (o instanceof JSeparator) {
          cb.setSelectedIndex(index + 2);
        } else {
          cb.setSelectedIndex(index + 1);
        }
      }
    });

    InputMap im = combo.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), selectPrevKey);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0), selectPrevKey);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), selectNextKey);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0), selectNextKey);

    return combo;
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
