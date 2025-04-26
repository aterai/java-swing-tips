// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel(new BorderLayout(5, 5));
    p.add(new JLabel("JLabel:"), BorderLayout.WEST);
    p.add(new JTextField("JTextField"));
    p.setBorder(BorderFactory.createTitledBorder("JLabel + JTextField"));

    JPanel panel = new JPanel(new BorderLayout(25, 25));
    panel.add(makePanel(), BorderLayout.NORTH);
    panel.add(p, BorderLayout.SOUTH);
    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(panel, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makePanel() {
    JPanel p = new JPanel(new BorderLayout(5, 5));
    p.add(new JComboBox<>(new String[] {"1111", "2222222", "3333333"}));

    String[] items = {"JComboBox 11111:", "JComboBox 222:", "JComboBox 33:"};
    JComboBox<String> comboBox = new JComboBox<String>(items) {
      @Override public void updateUI() {
        super.updateUI();
        UIManager.put("ComboBox.squareButton", Boolean.FALSE);
        UIManager.put("ComboBox.background", p.getBackground());
        setUI(new BasicComboBoxUI() {
          @Override protected JButton createArrowButton() {
            JButton button = new JButton(); // .createArrowButton();
            button.setBorder(BorderFactory.createEmptyBorder());
            button.setVisible(false);
            return button;
          }
        });
        ListCellRenderer<? super String> r = getRenderer();
        setRenderer((list, value, index, isSelected, cellHasFocus) -> {
          Component c = r.getListCellRendererComponent(
              list, value, index, isSelected, cellHasFocus);
          if (isSelected) {
            c.setForeground(list.getSelectionForeground());
            c.setBackground(list.getSelectionBackground());
          } else {
            c.setForeground(list.getForeground());
            c.setBackground(list.getBackground());
          }
          if (c instanceof JLabel) {
            ((JLabel) c).setHorizontalAlignment(SwingConstants.RIGHT);
          }
          return c;
        });
        setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        setOpaque(false);
        setFocusable(false);
      }
    };
    p.add(comboBox, BorderLayout.WEST);
    p.setBorder(BorderFactory.createTitledBorder("JComboBox + JComboBox"));
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
    // UIManager.put("ComboBox.selectionForeground", new ColorUIResource(Color.BLUE));
    // UIManager.put("ComboBox.selectionBackground", new ColorUIResource(Color.WHITE));
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
