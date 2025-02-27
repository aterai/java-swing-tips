// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JComboBox<String> combo01 = new AlternateRowColorComboBox<>(makeModel());
    // // MetalLookAndFeel
    // combo01.setUI(new MetalComboBoxUI() {
    //   @Override public PropertyChangeListener createPropertyChangeListener() {
    //     return new MetalPropertyChangeListener() {
    //       @Override public void propertyChange(PropertyChangeEvent e) {
    //         String propertyName = e.getPropertyName();
    //         if (propertyName == "background") {
    //           Color color = (Color) e.getNewValue();
    //           // arrowButton.setBackground(color);
    //           listBox.setBackground(color);
    //         } else {
    //           super.propertyChange(e);
    //         }
    //       }
    //     };
    //   }
    // });

    JComboBox<String> combo02 = new AlternateRowColorComboBox<>(makeModel());
    combo02.setEditable(true);

    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    box.add(makeTitledPanel("setEditable(false)", combo01));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("setEditable(true)", combo02));

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
    model.addElement("1111");
    model.addElement("1111222");
    model.addElement("111122233");
    model.addElement("1234123512351234");
    model.addElement("bbb1");
    model.addElement("bbb12");
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

class AlternateRowColorComboBox<E> extends JComboBox<E> {
  private static final Color EVEN_BGC = new Color(0xE1_FF_E1);
  private static final Color ODD_BGC = Color.WHITE;
  private transient ItemListener itemColorListener;

  // protected AlternateRowColorComboBox() {
  //   super();
  // }

  protected AlternateRowColorComboBox(ComboBoxModel<E> model) {
    super(model);
  }

  // protected AlternateRowColorComboBox(E[] items) {
  //   super(items);
  // }

  @Override public void setEditable(boolean flag) {
    super.setEditable(flag);
    Component field = getEditor().getEditorComponent();
    if (flag && field instanceof JComponent) {
      ((JComponent) field).setOpaque(true);
      field.setBackground(getAlternateRowColor(getSelectedIndex()));
    }
  }

  @Override public void updateUI() {
    removeItemListener(itemColorListener);
    super.updateUI();
    ListCellRenderer<? super E> r = getRenderer();
    setRenderer((list, value, index, isSelected, cellHasFocus) -> {
      Component c = r.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (index >= 0 && c instanceof JComponent) {
        ((JComponent) c).setOpaque(true);
      }
      if (!isSelected) {
        c.setBackground(getAlternateRowColor(index));
      }
      return c;
    });
    itemColorListener = e -> {
      if (e.getStateChange() != ItemEvent.SELECTED) {
        return;
      }
      JComboBox<?> cb = (JComboBox<?>) e.getItemSelectable();
      Color rc = getAlternateRowColor(cb.getSelectedIndex());
      if (cb.isEditable()) {
        cb.getEditor().getEditorComponent().setBackground(rc);
      } else {
        cb.setBackground(rc);
      }
    };
    addItemListener(itemColorListener);
    EventQueue.invokeLater(() -> {
      Component c = getEditor().getEditorComponent();
      c.setBackground(getAlternateRowColor(getSelectedIndex()));
      if (c instanceof JComponent) {
        ((JComponent) c).setOpaque(true);
      }
    });
  }

  protected static Color getAlternateRowColor(int index) {
    return index % 2 == 0 ? EVEN_BGC : ODD_BGC;
  }
}
