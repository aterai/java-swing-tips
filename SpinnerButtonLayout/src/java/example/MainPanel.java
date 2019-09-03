// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.swing.*;
import javax.swing.plaf.basic.BasicSpinnerUI;

public final class MainPanel extends JPanel {
  private final JSpinner spinner0 = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1));
  private final JSpinner spinner1 = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1));
  private final JSpinner spinner2 = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1)) {
    @Override public void updateUI() {
      super.updateUI();
      setUI(new BasicSpinnerUI() {
        @Override protected LayoutManager createLayout() {
          return new SpinnerLayout();
        }
      });
    }
  };
  private final JSpinner spinner3 = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1)) {
    @Override public void setLayout(LayoutManager mgr) {
      super.setLayout(new SpinnerLayout());
    }
  };

  public MainPanel() {
    super(new BorderLayout());
    spinner1.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

    Box box = Box.createVerticalBox();
    box.add(makeTitledPanel("Default", spinner0));
    box.add(makeTitledPanel("RIGHT_TO_LEFT", spinner1));
    box.add(makeTitledPanel("L(Prev), R(Next)", spinner2));
    box.add(makeTitledPanel("L(Prev), R(Next)", spinner3));

    add(box, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
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

class SpinnerLayout extends BorderLayout {
  private final Map<Object, Object> layoutMap;

  protected SpinnerLayout() {
    super();
    layoutMap = new HashMap<>();
    layoutMap.put("Editor", "Center");
    layoutMap.put("Next", "East");
    layoutMap.put("Previous", "West");
  }

  @Override public void addLayoutComponent(Component comp, Object constraints) {
    Object cons = Optional.ofNullable(layoutMap.get(constraints)).orElse(constraints);
    super.addLayoutComponent(comp, cons);
  }
}
