// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final int OFFSET = 30;
  private static final AtomicInteger OPEN_COUNTER = new AtomicInteger();

  private MainPanel() {
    super(new BorderLayout());
    JDesktopPane desktop = new JDesktopPane();
    // @see javax/swing/DefaultDesktopManager.java setupDragMode(...)
    // desktop.putClientProperty("JDesktopPane.dragMode", "faster");
    // desktop.putClientProperty("JDesktopPane.dragMode", "outline");
    desktop.add(createInternalFrame());

    JRadioButton r1 = new JRadioButton("LIVE_DRAG_MODE", true);
    r1.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        desktop.setDragMode(JDesktopPane.LIVE_DRAG_MODE);
      }
    });

    JRadioButton r2 = new JRadioButton("OUTLINE_DRAG_MODE");
    r2.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
      }
    });

    JPanel p = new JPanel();
    ButtonGroup bg = new ButtonGroup();
    Stream.of(r1, r2).forEach(r -> {
      bg.add(r);
      p.add(r);
    });

    JMenu menu = new JMenu("Window");
    menu.setMnemonic(KeyEvent.VK_W);

    JMenuItem menuItem = menu.add("New");
    menuItem.setMnemonic(KeyEvent.VK_N);
    menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.ALT_DOWN_MASK));
    menuItem.setActionCommand("new");
    menuItem.addActionListener(e -> desktop.add(createInternalFrame()));

    JMenuBar menuBar = new JMenuBar();
    menuBar.add(menu);
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(menuBar));

    add(desktop);
    add(p, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JInternalFrame createInternalFrame() {
    String title = String.format("Document #%s", OPEN_COUNTER.getAndIncrement());
    JInternalFrame f = new JInternalFrame(title, true, true, true, true);
    f.getContentPane().add(new JScrollPane(new JTree()));
    f.setSize(160, 100);
    f.setLocation(OFFSET * OPEN_COUNTER.intValue(), OFFSET * OPEN_COUNTER.intValue());
    EventQueue.invokeLater(() -> f.setVisible(true));
    return f;
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
