// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseWheelListener;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder("JComboBox"));
    GridBagConstraints c = new GridBagConstraints();

    c.gridx = 0;
    c.insets = new Insets(5, 5, 5, 0);
    c.anchor = GridBagConstraints.LINE_END;
    p.add(new JLabel("Wheel:"), c);
    p.add(new JLabel("Normal:"), c);

    c.gridx = 1;
    c.weightx = 1d;
    c.fill = GridBagConstraints.HORIZONTAL;
    p.add(makeComboBox(), c);
    p.add(new JComboBox<>(makeModel()), c);

    JTextArea textArea = new JTextArea();
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(textArea));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JComboBox<String> makeComboBox() {
    return new JComboBox<String>(makeModel()) {
      private transient MouseWheelListener handler;
      @Override public void updateUI() {
        removeMouseWheelListener(handler);
        super.updateUI();
        handler = e -> {
          Component c = e.getComponent();
          if (c.hasFocus() && c instanceof JComboBox) {
            JComboBox<?> combo = (JComboBox<?>) c;
            int idx = combo.getSelectedIndex() + e.getWheelRotation();
            if (idx >= 0 && idx < combo.getItemCount()) {
              combo.setSelectedIndex(idx);
            }
          }
        };
        addMouseWheelListener(handler);
      }
    };
  }

  private static ComboBoxModel<String> makeModel() {
    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    model.addElement("111111");
    model.addElement("22222222");
    model.addElement("3333333333");
    model.addElement("444444444444");
    model.addElement("5555555");
    model.addElement("66666666666");
    model.addElement("77777777");
    model.addElement("88888888888");
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
