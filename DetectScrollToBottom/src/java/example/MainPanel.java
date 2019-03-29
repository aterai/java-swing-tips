// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 2));
    UIManager.put("ScrollBar.minimumThumbSize", new Dimension(32, 32));

    JTextArea textArea = new JTextArea();
    StringBuilder sb = new StringBuilder();
    IntStream.range(0, 2000).forEach(i -> sb.append(String.format("%04d%n", i)));
    textArea.setText(sb.toString());
    textArea.setCaretPosition(0);

    add(makePanel(textArea));
    add(makePanel(new JTable(100, 3)));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makePanel(Component c) {
    Component check = new JCheckBox("JCheckBox");
    check.setEnabled(false);
    JPanel p = new JPanel(new BorderLayout());
    JScrollPane scroll = new JScrollPane(c);
    scroll.getVerticalScrollBar().getModel().addChangeListener(e -> {
      BoundedRangeModel m = (BoundedRangeModel) e.getSource();
      int extent = m.getExtent();
      int maximum = m.getMaximum();
      int value = m.getValue();
      // https://stackoverflow.com/questions/12916192/how-to-know-if-a-jscrollbar-has-reached-the-bottom-of-the-jscrollpane
      // System.out.println("2. Value: " + (value + extent) + " Max: " + maximum);
      // https://ateraimemo.com/Swing/ScrollBarAsSlider.html
      if (value + extent >= maximum) {
        check.setEnabled(true);
      }
    });
    p.add(scroll);
    p.add(check, BorderLayout.SOUTH);
    return p;
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
