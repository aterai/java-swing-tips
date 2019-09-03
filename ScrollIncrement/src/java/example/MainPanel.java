// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final String LF = "\n";

  private MainPanel() {
    super(new BorderLayout());
    StringBuilder buf = new StringBuilder();
    IntStream.range(0, 100).forEach(i -> buf.append(i + LF));

    JScrollPane scrollPane = new JScrollPane(new JTextArea(buf.toString()));
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    JScrollBar vsb = scrollPane.getVerticalScrollBar();

    SpinnerNumberModel model = new SpinnerNumberModel(vsb.getUnitIncrement(1), 1, 100_000, 1);
    model.addChangeListener(e -> vsb.setUnitIncrement(model.getNumber().intValue()));

    JSpinner spinner = new JSpinner(model);
    spinner.setEditor(new JSpinner.NumberEditor(spinner, "#####0"));
    Box box = Box.createHorizontalBox();
    box.add(new JLabel("Unit Increment:"));
    box.add(Box.createHorizontalStrut(2));
    box.add(spinner);
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(box, BorderLayout.NORTH);
    add(scrollPane);
    setPreferredSize(new Dimension(320, 240));
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
