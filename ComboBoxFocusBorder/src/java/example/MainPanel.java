// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    String[] model = {"aaaaaaa", "bbb", "c"};

    JComboBox<String> combo1 = new JComboBox<>(model);
    combo1.setFocusable(false);

    JComboBox<String> combo2 = new JComboBox<String>(model) {
      @Override public void updateUI() {
        setRenderer(null);
        super.updateUI();
        if (isWindowsLnF()) {
          setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
              JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
              if (index < 0) {
                l.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
              }
              return l;
            }
          });
        }
      }

      private boolean isWindowsLnF() {
        return getUI().getClass().getName().contains("WindowsComboBoxUI");
      }
    };

    JComboBox<String> combo3 = new JComboBox<String>(model) {
      @Override public void updateUI() {
        setRenderer(null);
        super.updateUI();
        if (isWindowsLnF()) {
          setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
              JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
              if (index < 0) {
                l.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
              }
              return l;
            }
          });
        }
      }

      @Override protected void paintBorder(Graphics g) {
        super.paintBorder(g);
        if (isFocusOwner() && !isPopupVisible() && isWindowsLnF()) {
          Graphics2D g2 = (Graphics2D) g.create();
          g2.setPaint(Color.DARK_GRAY);
          g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
          g2.dispose();
        }
      }

      private boolean isWindowsLnF() {
        return getUI().getClass().getName().contains("WindowsComboBoxUI");
      }
    };

    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    box.add(makeTitledPanel("default", new JComboBox<>(model)));
    box.add(Box.createVerticalStrut(10));
    box.add(makeTitledPanel("setFocusable(false)", combo1));
    box.add(Box.createVerticalStrut(10));
    box.add(makeTitledPanel("setRenderer(...)", combo2));
    box.add(Box.createVerticalStrut(10));
    box.add(makeTitledPanel("paintBorder(...)", combo3));

    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
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
