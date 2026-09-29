// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JDesktopPane desktop = new JDesktopPane();

    AtomicInteger counter = new AtomicInteger();
    JButton button = new JButton("add");
    button.addActionListener(e -> addInternalFrame(desktop, counter.getAndIncrement()));

    long threshold = button.getMultiClickThreshhold();
    SpinnerNumberModel model = new SpinnerNumberModel(threshold, 0L, 10_000L, 100L);
    model.addChangeListener(e -> button.setMultiClickThreshhold(model.getNumber().longValue()));

    JMenuBar mb = new JMenuBar();
    mb.add(new JLabel("MultiClickThreshhold: "));
    mb.add(new JSpinner(model));
    mb.add(Box.createHorizontalGlue());
    mb.add(button);
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    add(desktop);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void addInternalFrame(JDesktopPane desktop, int index) {
    JInternalFrame frame = new JInternalFrame("#" + index, true, true, true, true);
    frame.setBounds(index * 10, index * 10, 200, 100);
    desktop.add(frame);
    frame.setVisible(true);
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
      Logger.getGlobal().severe(ex::getMessage);
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
