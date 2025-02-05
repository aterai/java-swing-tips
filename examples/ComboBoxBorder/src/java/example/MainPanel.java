// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    UIManager.put("ComboBox.foreground", Color.WHITE);
    UIManager.put("ComboBox.background", Color.BLACK);
    UIManager.put("ComboBox.selectionForeground", Color.CYAN);
    UIManager.put("ComboBox.selectionBackground", Color.BLACK);

    UIManager.put("ComboBox.buttonDarkShadow", Color.BLACK);
    UIManager.put("ComboBox.buttonBackground", Color.WHITE);
    UIManager.put("ComboBox.buttonHighlight", Color.WHITE);
    UIManager.put("ComboBox.buttonShadow", Color.WHITE);

    UIManager.put("ComboBox.border", BorderFactory.createLineBorder(Color.WHITE));
    UIManager.put("ComboBox.editorBorder", BorderFactory.createLineBorder(Color.GREEN));

    UIManager.put("TitledBorder.titleColor", Color.WHITE);
    UIManager.put("TitledBorder.border", BorderFactory.createEmptyBorder());

    JComboBox<String> combo0 = new JComboBox<>(makeComboBoxModel());
    initBorder(combo0.getAccessibleContext().getAccessibleChild(0));

    JComboBox<String> combo1 = new JComboBox<String>(makeComboBoxModel()) {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new BasicComboBoxUI());
        initBorder(getAccessibleContext().getAccessibleChild(0));
      }
    };

    JComboBox<String> combo2 = new JComboBox<String>(makeComboBoxModel()) {
      private transient MouseAdapter handler;

      @Override public void updateUI() {
        removeMouseListener(handler);
        super.updateUI();
        setUI(new BasicComboBoxUI() {
          @Override protected JButton createArrowButton() {
            JButton b = new JButton(new ArrowIcon()); // .createArrowButton();
            b.setBackground(Color.BLACK);
            b.setContentAreaFilled(false);
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createEmptyBorder());
            return b;
          }
        });
        handler = new RolloverListener();
        addMouseListener(handler);
        initBorder(getAccessibleContext().getAccessibleChild(0));
      }
    };

    Box box = Box.createVerticalBox();
    box.add(makeTitledPanel("MetalComboBoxUI:", combo0));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("BasicComboBoxUI:", combo1));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("BasicComboBoxUI#createArrowButton():", combo2));
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    add(box, BorderLayout.NORTH);
    setOpaque(true);
    setBackground(Color.BLACK);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    p.setOpaque(true);
    p.setBackground(Color.BLACK);
    return p;
  }

  private static void initBorder(Object o) {
    if (o instanceof JComponent) {
      ((JComponent) o).setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.WHITE));
    }
  }

  private static ComboBoxModel<String> makeComboBoxModel() {
    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    model.addElement("1234");
    model.addElement("5555555555555555555555");
    model.addElement("6789000000000");
    return model;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class ArrowIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(Color.WHITE);
    int shift = 0;
    if (c instanceof AbstractButton) {
      ButtonModel m = ((AbstractButton) c).getModel();
      if (m.isPressed()) {
        shift = 1;
      } else {
        if (m.isRollover()) {
          g2.setPaint(Color.WHITE);
        } else {
          g2.setPaint(Color.BLACK);
        }
      }
    }
    g2.translate(x, y + shift);
    g2.drawLine(2, 3, 6, 3);
    g2.drawLine(3, 4, 5, 4);
    g2.drawLine(4, 5, 4, 5);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 9;
  }

  @Override public int getIconHeight() {
    return 9;
  }
}

class RolloverListener extends MouseAdapter {
  private ButtonModel getButtonModel(MouseEvent e) {
    JComboBox<?> cb = (JComboBox<?>) e.getComponent();
    JButton b = (JButton) cb.getComponent(0);
    return b.getModel();
  }

  @Override public void mouseEntered(MouseEvent e) {
    getButtonModel(e).setRollover(true);
  }

  @Override public void mouseExited(MouseEvent e) {
    getButtonModel(e).setRollover(false);
  }

  @Override public void mousePressed(MouseEvent e) {
    getButtonModel(e).setPressed(true);
  }

  @Override public void mouseReleased(MouseEvent e) {
    getButtonModel(e).setPressed(false);
  }
}
