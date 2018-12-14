// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private int num;

  private MainPanel() {
    super(new BorderLayout());

    JDesktopPane desktop = new JDesktopPane();

    JButton button = new JButton("add");
    button.addActionListener(e -> addInternalFrame(desktop));

    long lv = button.getMultiClickThreshhold();
    JSpinner spinner = new JSpinner(new SpinnerNumberModel(Long.valueOf(lv), Long.valueOf(0), Long.valueOf(10_000), Long.valueOf(100)));
    spinner.addChangeListener(e -> button.setMultiClickThreshhold((long) ((JSpinner) e.getSource()).getValue()));

    JMenuBar mb = new JMenuBar();
    mb.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    mb.add(new JLabel("MultiClickThreshhold: "));
    mb.add(spinner);
    mb.add(Box.createHorizontalGlue());
    mb.add(button);

    add(desktop);
    add(mb, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private void addInternalFrame(JDesktopPane desktop) {
    JInternalFrame f = createFrame("#" + num, num * 10, num * 10);
    desktop.add(f);
    desktop.getDesktopManager().activateFrame(f);
    num++;
  }

  private static JInternalFrame createFrame(String t, int x, int y) {
    JInternalFrame f = new JInternalFrame(t, true, true, true, true);
    f.setSize(200, 100);
    f.setLocation(x, y);
    f.setVisible(true);
    return f;
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
      System.out.println(UIManager.get("OptionPane.buttonClickThreshhold"));
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
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
