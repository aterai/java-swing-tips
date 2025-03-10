// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String[] model = {"11111", "222", "3"};

    JComboBox<String> combo1 = new JComboBox<>(model);
    combo1.setFocusable(false);

    JComboBox<String> combo2 = new FocusComboBox<>(model);

    JComboBox<String> combo3 = new FocusComboBox<String>(model) {
      @Override protected void paintBorder(Graphics g) {
        super.paintBorder(g);
        if (isFocusOwner() && !isPopupVisible() && isWindowsLnF()) {
          Graphics2D g2 = (Graphics2D) g.create();
          g2.setPaint(Color.DARK_GRAY);
          g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
          g2.dispose();
        }
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

class FocusComboBox<E> extends JComboBox<E> {
  protected FocusComboBox(ComboBoxModel<E> model) {
    super(model);
  }

  @SafeVarargs
  protected FocusComboBox(E... model) {
    super(model);
  }

  @Override public void updateUI() {
    setRenderer(null);
    super.updateUI();
    if (isWindowsLnF()) {
      ListCellRenderer<? super E> renderer = getRenderer();
      setRenderer((list, value, index, isSelected, cellHasFocus) -> {
        Component c = renderer.getListCellRendererComponent(
            list, value, index, isSelected, cellHasFocus);
        if (index < 0 && c instanceof JComponent) {
          ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        }
        return c;
      });
    }
  }

  protected boolean isWindowsLnF() {
    return getUI().getClass().getName().contains("WindowsComboBoxUI");
  }
}
