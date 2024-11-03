// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    String title1 = "<html><h3>Button title 001</h3>";
    String help1 = "1234567890<br>123456789012345<br>1234567890";
    JToggleButton b1 = new JToggleButton(title1 + help1);
    String title2 = "<html><h3>Button title 002</h3>";
    String help2 = "123456789090";
    JToggleButton b2 = new JToggleButton(title2 + help2);
    ButtonGroup bg = new ButtonGroup();
    bg.add(b1);
    bg.add(b2);
    JPanel p = new JPanel(new GridLayout(1, 2, 5, 5));
    String help = "RadioCard with JToggleButton and JRadioButton";
    p.setBorder(BorderFactory.createTitledBorder(help));
    p.add(makeRadioIconLayer(b1));
    p.add(makeRadioIconLayer(b2));
    add(p);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeRadioIconLayer(AbstractButton button) {
    JRadioButton radio = new JRadioButton();
    radio.setOpaque(false);
    Dimension d = radio.getPreferredSize();
    Insets i = button.getInsets();
    button.setMargin(new Insets(i.top, d.width, i.bottom, i.right));
    button.setPreferredSize(button.getPreferredSize()); // avoid button size shrinking
    button.setVerticalAlignment(SwingConstants.TOP);
    LayerUI<AbstractButton> layer = new LayerUI<AbstractButton>() {
      @Override public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        if (c instanceof JLayer) {
          AbstractButton b = (AbstractButton) ((JLayer<?>) c).getView();
          Graphics2D g2 = (Graphics2D) g.create();
          radio.setSelected(b.isSelected());
          int x = i.left - d.width + 8;
          int y = i.top + 8;
          SwingUtilities.paintComponent(g2, radio, b, x, y, d.width, d.height);
          g2.dispose();
        }
      }
    };
    return new JLayer<>(button, layer);
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
