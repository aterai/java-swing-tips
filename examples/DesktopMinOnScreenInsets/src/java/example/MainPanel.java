// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;
import javax.swing.plaf.basic.BasicInternalFrameUI;

public final class MainPanel extends JPanel {
  private static final int OFFSET = 30;
  private static final AtomicInteger OPEN_COUNTER = new AtomicInteger();

  private MainPanel() {
    super(new BorderLayout());
    // System.out.println(UIManager.get("Desktop.minOnScreenInsets"));
    JInternalFrame f = createInternalFrame();
    Dimension d = ((BasicInternalFrameUI) f.getUI()).getNorthPane().getPreferredSize();
    UIManager.put("Desktop.minOnScreenInsets", new Insets(d.height, 16, 3, 16));
    UIManager.put("Desktop.background", Color.LIGHT_GRAY);

    JDesktopPane desktop = new JDesktopPane();
    desktop.add(f);

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
