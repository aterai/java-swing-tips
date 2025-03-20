// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
// import javax.swing.plaf.basic.BasicComboBoxUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String[] items = {"JComboBox 11111:", "JComboBox 222:", "JComboBox 33:"};
    Box p = Box.createVerticalBox();
    p.add(makeTitledPanel("setPreferredSize", makeComboBox1(items)));
    p.add(Box.createVerticalStrut(5));
    p.add(makeTitledPanel("getListCellRendererComponent", makeComboBox2(items)));
    p.add(Box.createVerticalStrut(5));
    p.add(makeTitledPanel("html", makeComboBox3(items)));
    p.add(Box.createVerticalStrut(5));
    p.add(makeTitledPanel("icon", makeComboBox4(items)));
    p.add(Box.createVerticalStrut(5));
    // p.add(makeTitledPanel("BasicComboBoxUI", makeComboBox5(items)));
    add(p, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JComboBox<String> makeComboBox1(String... items) {
    return new JComboBox<String>(items) {
      @Override public void updateUI() {
        super.updateUI();
        ListCellRenderer<? super String> r = getRenderer();
        if (r instanceof Component) {
          ((Component) r).setPreferredSize(new Dimension(0, 32));
        }
      }
    };
  }

  private JComboBox<String> makeComboBox2(String... items) {
    JComboBox<String> combo = new JComboBox<>(items);
    combo.setRenderer(new DefaultListCellRenderer() {
      private int cellHeight;
      @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        // Dimension d = super.getPreferredSize();
        // cellHeight = index < 0 ? d.height : 32;
        cellHeight = Optional.ofNullable(super.getPreferredSize())
            .filter(d -> index < 0).map(d -> d.height).orElse(32);
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }

      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = cellHeight;
        return d;
      }
    });
    return combo;
  }

  private static JComboBox<String> makeComboBox3(String... items) {
    return new JComboBox<String>(items) {
      @Override public void updateUI() {
        setRenderer(null);
        super.updateUI();
        ListCellRenderer<? super String> r = getRenderer();
        setRenderer((list, value, index, isSelected, cellHasFocus) -> {
          String title = Objects.toString(value, "");
          if (index >= 0) {
            title = String.format("<html><table><td height='32'>%s", value);
          }
          return r.getListCellRendererComponent(list, title, index, isSelected, cellHasFocus);
        });
      }
    };
  }

  private static JComboBox<String> makeComboBox4(String... items) {
    return new JComboBox<String>(items) {
      @Override public void updateUI() {
        setRenderer(null);
        super.updateUI();
        ListCellRenderer<? super String> r = getRenderer();
        setRenderer((list, value, index, isSelected, cellHasFocus) -> {
          Component c = r.getListCellRendererComponent(
              list, value, index, isSelected, cellHasFocus);
          if (c instanceof JLabel) {
            ((JLabel) c).setIcon(index >= 0 ? new H32Icon() : null);
          }
          return c;
        });
      }
    };
  }

  // private static JComboBox<String> makeComboBox5() {
  //   JComboBox<String> combo = new JComboBox<>(items);
  //   combo.setUI(new BasicComboBoxUI() {
  //     @Override protected ComboPopup createPopup() {
  //       return new BasicComboPopup(combo) {
  //         @Override protected void configureList() {
  //           super.configureList();
  //           list.setFixedCellHeight(60);
  //         }
  //       };
  //     }
  //   });
  //   return combo;
  // }

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

class H32Icon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    /* Empty icon */
  }

  @Override public int getIconWidth() {
    return 0;
  }

  @Override public int getIconHeight() {
    return 32;
  }
}
