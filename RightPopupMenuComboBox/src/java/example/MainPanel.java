// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsComboBoxUI;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import javax.accessibility.Accessible;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;

public final class MainPanel extends JPanel {
  private final JComboBox<String> combo00 = new JComboBox<>(makeModel());
  private final JComboBox<String> combo01 = new JComboBox<>(makeModel());

  public MainPanel() {
    super(new BorderLayout());
    initComboBox(combo01);
    int g = 5;
    JPanel p = new JPanel(new GridLayout(2, 2, g, g));
    p.add(combo00);
    p.add(new JLabel("<- default"));
    p.add(combo01);
    p.add(new JLabel("<- RightPopupMenuListener"));
    setBorder(BorderFactory.createEmptyBorder(g, g, g, g));
    add(p, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private void initComboBox(JComboBox<?> combo) {
    if (combo.getUI() instanceof WindowsComboBoxUI) {
      combo.setUI(new RightPopupWindowsComboBoxUI());
    } else {
      combo.setUI(new RightPopupBasicComboBoxUI());
    }
    combo.addPopupMenuListener(new RightPopupMenuListener());
  }

  private static ComboBoxModel<String> makeModel() {
    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    model.addElement("aaaa");
    model.addElement("aaaabbb");
    model.addElement("aaaabbbcc");
    model.addElement("asdfasdfas");
    model.addElement("bbb1");
    model.addElement("bbb12");
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

class RightPopupMenuListener implements PopupMenuListener {
  @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    EventQueue.invokeLater(() -> {
      JComboBox<?> combo = (JComboBox<?>) e.getSource();
      Accessible a = combo.getAccessibleContext().getAccessibleChild(0);
      // Or Accessible a = combo.getUI().getAccessibleChild(combo, 0);
      if (a instanceof JPopupMenu) {
        JPopupMenu pop = (JPopupMenu) a;
        Point p = new Point(combo.getSize().width, 0);
        SwingUtilities.convertPointToScreen(p, combo);
        pop.setLocation(p);
      }
    });
  }

  @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { /* not needed */ }

  @Override public void popupMenuCanceled(PopupMenuEvent e) { /* not needed */ }
}

class RightPopupWindowsComboBoxUI extends WindowsComboBoxUI {
  @Override protected JButton createArrowButton() {
    ImageIcon icon = new ImageIcon(getClass().getResource("14x14.png"));
    JButton button = new JButton(icon) {
      @Override public Dimension getPreferredSize() {
        return new Dimension(14, 14);
      }
    };
    button.setRolloverIcon(makeRolloverIcon(icon));
    button.setFocusPainted(false);
    button.setContentAreaFilled(false);
    return button;
  }

  private static ImageIcon makeRolloverIcon(ImageIcon srcIcon) {
    RescaleOp op = new RescaleOp(
        new float[] {1.2f, 1.2f, 1.2f, 1f},
        new float[] {0f, 0f, 0f, 0f}, null);
    BufferedImage img = new BufferedImage(srcIcon.getIconWidth(), srcIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics g = img.getGraphics();
    // g.drawImage(srcIcon.getImage(), 0, 0, null);
    srcIcon.paintIcon(null, g, 0, 0);
    g.dispose();
    return new ImageIcon(op.filter(img, null));
  }
}

class RightPopupBasicComboBoxUI extends BasicComboBoxUI {
  @Override protected JButton createArrowButton() {
    JButton button = super.createArrowButton();
    if (button instanceof BasicArrowButton) {
      ((BasicArrowButton) button).setDirection(SwingConstants.EAST);
    }
    return button;
  }
}
