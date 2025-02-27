// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final int STEP = 5;
  private static final int EXT = 20;
  private static final int MIN = 0;
  private static final int MAX = EXT * 10; // 200
  private static final int VALUE = 50;

  private MainPanel() {
    super(new GridLayout(2, 1));
    JScrollBar bar = new JScrollBar(Adjustable.HORIZONTAL, VALUE, EXT, MIN, MAX + EXT);
    SpinnerNumberModel model = new SpinnerNumberModel(VALUE, MIN, MAX, STEP);
    bar.setUnitIncrement(STEP);
    bar.getModel().addChangeListener(e -> {
      int v = ((BoundedRangeModel) e.getSource()).getValue();
      model.setValue(v);
    });
    model.addChangeListener(e -> {
      int v = ((SpinnerNumberModel) e.getSource()).getNumber().intValue();
      bar.setValue(v);
    });

    add(makeTitledPanel("JSpinner", new JSpinner(model)));
    add(makeTitledPanel("JScrollBar", bar));
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
