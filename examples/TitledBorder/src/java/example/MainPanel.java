// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout(5, 5));
    TitledBorder border = BorderFactory.createTitledBorder("TitledBorder test");
    JPanel panel = new JPanel();
    panel.setBorder(border);

    JComboBox<VerticalOrientation> combo1 = new JComboBox<>(VerticalOrientation.values());
    combo1.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        border.setTitlePosition(((VerticalOrientation) e.getItem()).getMode());
        panel.repaint();
      }
    });

    JComboBox<Justification> combo2 = new JComboBox<>(Justification.values());
    combo2.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        border.setTitleJustification(((Justification) e.getItem()).getMode());
        panel.repaint();
      }
    });

    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.insets = new Insets(5, 5, 5, 5);
    c.anchor = GridBagConstraints.LINE_END;
    JPanel p2 = new JPanel(new GridBagLayout());
    p2.add(new JLabel("TitlePosition:"), c);
    p2.add(new JLabel("TitleJustification:"), c);

    c.gridx = 1;
    c.weightx = 1d;
    c.fill = GridBagConstraints.HORIZONTAL;
    p2.add(combo1, c);
    p2.add(combo2, c);

    add(p2, BorderLayout.NORTH);
    add(panel);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
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

enum VerticalOrientation {
  DEFAULT_POSITION(TitledBorder.DEFAULT_POSITION, "Default Position"),
  ABOVE_TOP(TitledBorder.ABOVE_TOP, "Above Top"),
  TOP(TitledBorder.TOP, "Top"),
  BELOW_TOP(TitledBorder.BELOW_TOP, "Below Top"),
  ABOVE_BOTTOM(TitledBorder.ABOVE_BOTTOM, "Above Bottom"),
  BOTTOM(TitledBorder.BOTTOM, "Bottom"),
  BELOW_BOTTOM(TitledBorder.BELOW_BOTTOM, "Below Bottom");
  private final int mode;
  private final String description;

  VerticalOrientation(int mode, String description) {
    this.mode = mode;
    this.description = description;
  }

  public int getMode() {
    return mode;
  }

  @Override public String toString() {
    return description;
  }
}

@SuppressWarnings("PMD.LongVariable")
enum Justification {
  DEFAULT_JUSTIFICATION(TitledBorder.DEFAULT_JUSTIFICATION, "Default Justification"),
  LEFT(TitledBorder.LEFT, "Left"),
  CENTER(TitledBorder.CENTER, "Center"),
  RIGHT(TitledBorder.RIGHT, "Right"),
  LEADING(TitledBorder.LEADING, "Leading"),
  TRAILING(TitledBorder.TRAILING, "Trailing");
  private final int mode;
  private final String description;

  Justification(int mode, String description) {
    this.mode = mode;
    this.description = description;
  }

  public int getMode() {
    return mode;
  }

  @Override public String toString() {
    return description;
  }
}
