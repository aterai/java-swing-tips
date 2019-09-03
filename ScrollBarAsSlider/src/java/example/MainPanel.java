// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final int STEP = 5;
  private static final int EXTENT = 20;
  private static final int MIN = 0;
  private static final int MAX = EXTENT * 10; // 200
  private static final int VALUE = 50;

  private MainPanel() {
    super(new GridLayout(2, 1));
    JScrollBar scrollbar = new JScrollBar(Adjustable.HORIZONTAL, VALUE, EXTENT, MIN, MAX + EXTENT);
    SpinnerNumberModel model = new SpinnerNumberModel(VALUE, MIN, MAX, STEP);

    scrollbar.setUnitIncrement(STEP);
    scrollbar.getModel().addChangeListener(e -> model.setValue(((BoundedRangeModel) e.getSource()).getValue()));

    model.addChangeListener(e -> {
      SpinnerNumberModel source = (SpinnerNumberModel) e.getSource();
      scrollbar.setValue(source.getNumber().intValue());
    });

    add(makeTitledPanel("JSpinner", new JSpinner(model)));
    add(makeTitledPanel("JScrollBar", scrollbar));
    setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component cmp) {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.setBackground(Color.WHITE);
    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 1d;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(5, 5, 5, 5);
    p.add(cmp, c);
    return p;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
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
