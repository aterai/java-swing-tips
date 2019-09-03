// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JProgressBar progressBar = new JProgressBar();
    progressBar.setIndeterminate(true);
    JPanel p = new JPanel(new GridBagLayout());
    p.add(progressBar);

    int ctv = UIManager.getInt("ProgressBar.cycleTime");
    SpinnerNumberModel cycleTime = new SpinnerNumberModel(ctv, 1_000, 10_000, 100);

    int riv = UIManager.getInt("ProgressBar.repaintInterval");
    SpinnerNumberModel repaintInterval = new SpinnerNumberModel(riv, 10, 100, 10);

    JButton button = new JButton("UIManager.put");
    button.addActionListener(e -> {
      progressBar.setIndeterminate(false);
      UIManager.put("ProgressBar.repaintInterval", repaintInterval.getNumber().intValue());
      UIManager.put("ProgressBar.cycleTime", cycleTime.getNumber().intValue());
      progressBar.setIndeterminate(true);
    });

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(button);

    JPanel sp = new JPanel(new GridLayout(3, 2, 5, 5));
    sp.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    sp.add(new JLabel("ProgressBar.cycleTime:", SwingConstants.RIGHT));
    sp.add(new JSpinner(cycleTime));
    sp.add(new JLabel("ProgressBar.repaintInterval:", SwingConstants.RIGHT));
    sp.add(new JSpinner(repaintInterval));
    sp.add(Box.createHorizontalStrut(5));
    sp.add(box);

    add(sp, BorderLayout.NORTH);
    add(p);
    // add(box, BorderLayout.SOUTH);
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
