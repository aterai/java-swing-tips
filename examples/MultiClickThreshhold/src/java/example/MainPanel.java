// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JDesktopPane desktop = new JDesktopPane();

    AtomicInteger num = new AtomicInteger();
    JButton button = new JButton("add");
    button.addActionListener(e -> addInternalFrame(desktop, num.getAndIncrement()));

    long lv = button.getMultiClickThreshhold();
    SpinnerNumberModel m = new SpinnerNumberModel(lv, 0L, 10_000L, 100L);
    m.addChangeListener(e -> button.setMultiClickThreshhold(m.getNumber().longValue()));

    JMenuBar mb = new JMenuBar();
    mb.add(new JLabel("MultiClickThreshhold: "));
    mb.add(new JSpinner(m));
    mb.add(Box.createHorizontalGlue());
    mb.add(button);
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    add(desktop);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void addInternalFrame(JDesktopPane desktop, int idx) {
    String title = "#" + idx;
    JInternalFrame f = new JInternalFrame(title, true, true, true, true);
    desktop.add(f);
    f.setBounds(idx * 10, idx * 10, 200, 100);
    EventQueue.invokeLater(() -> f.setVisible(true));
    // desktop.getDesktopManager().activateFrame(f);
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      // System.out.println(UIManager.get("OptionPane.buttonClickThreshhold"));
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
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
