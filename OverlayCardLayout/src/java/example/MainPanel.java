// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String[] model = {"red", "green", "blue"};

    CardLayout cardLayout = new CardLayout();
    JPanel cards = new JPanel(cardLayout);
    cards.add(makePanel(Color.RED), model[0]);
    cards.add(makePanel(Color.GREEN), model[1]);
    cards.add(makePanel(Color.BLUE), model[2]);

    JComboBox<String> combo = new JComboBox<>(model);
    combo.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        cardLayout.show(cards, Objects.toString(e.getItem()));
      }
    });

    JPanel pp = new JPanel(new BorderLayout());
    pp.setOpaque(false);
    pp.setBorder(BorderFactory.createEmptyBorder(8, 24, 0, 24));
    pp.add(combo, BorderLayout.NORTH);

    JPanel p = new JPanel() {
      @Override public boolean isOptimizedDrawingEnabled() {
        return false;
      }
    };
    p.setLayout(new OverlayLayout(p));
    p.add(pp);
    p.add(cards);

    add(p);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makePanel(Color color) {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBackground(color);
    String title = String.format("JButton on the %s JPanel", color);
    p.add(new JButton(title));
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
