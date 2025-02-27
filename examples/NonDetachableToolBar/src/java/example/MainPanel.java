// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsToolBarUI;
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicToolBarUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JCheckBox detachable = new JCheckBox("Floating(detachable)", false);

    JButton button = new JButton("button");
    button.setFocusable(false);

    JToolBar toolBar = new JToolBar() {
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
    toolBar.add(new JLabel("label"));
    toolBar.add(Box.createRigidArea(new Dimension(5, 5)));
    toolBar.add(button);
    toolBar.add(Box.createRigidArea(new Dimension(5, 5)));
    toolBar.add(new JComboBox<>(makeModel()));
    toolBar.add(Box.createGlue());

    JCheckBox movable = new JCheckBox("Floatable(movable)", true);
    movable.addActionListener(e -> {
      boolean b = ((JCheckBox) e.getSource()).isSelected();
      toolBar.setFloatable(b);
    });

    JPanel p = new JPanel();
    p.add(movable);
    p.add(detachable);
    add(toolBar, BorderLayout.NORTH);
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
