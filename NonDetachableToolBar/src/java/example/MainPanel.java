// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsToolBarUI;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicToolBarUI;

public class MainPanel extends JPanel {
  protected final JToolBar toolbar = new JToolBar("toolbar") {
    @Override public void updateUI() {
      super.updateUI();
      if (getUI() instanceof WindowsToolBarUI) {
        setUI(new WindowsToolBarUI() {
          @Override public void setFloating(boolean b, Point p) {
            if (detachable.isSelected()) {
              super.setFloating(b, p);
            } else {
              super.setFloating(false, p);
            }
          }
        });
      } else {
        setUI(new BasicToolBarUI() {
          @Override public void setFloating(boolean b, Point p) {
            if (detachable.isSelected()) {
              super.setFloating(b, p);
            } else {
              super.setFloating(false, p);
            }
          }
        });
      }
    }
  };
  protected final JCheckBox movable = new JCheckBox("Floatable(movable)", true);
  protected final JCheckBox detachable = new JCheckBox("Floating(detachable)", false);
  protected final JComboBox<String> combo = new JComboBox<>(makeModel());
  protected final JButton button = new JButton("button");

  public MainPanel() {
    super(new BorderLayout());

    button.setFocusable(false);
    toolbar.add(new JLabel("label"));
    toolbar.add(Box.createRigidArea(new Dimension(5, 5)));
    toolbar.add(button);
    toolbar.add(Box.createRigidArea(new Dimension(5, 5)));
    toolbar.add(combo);
    toolbar.add(Box.createGlue());

    movable.addActionListener(e -> toolbar.setFloatable(((JCheckBox) e.getSource()).isSelected()));

    JPanel p = new JPanel();
    p.add(movable);
    p.add(detachable);
    add(toolbar, BorderLayout.NORTH);
    add(p);
    setPreferredSize(new Dimension(320, 240));
  }

  private static ComboBoxModel<String> makeModel() {
    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    model.addElement("1111111");
    model.addElement("22222");
    model.addElement("3333333333333333");
    model.addElement("44444444444");
    return model;
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
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
