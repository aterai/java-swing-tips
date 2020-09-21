// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final int OFFSET = 30;
  private static final AtomicInteger OPEN_FRAME_COUNT = new AtomicInteger();
  private final JDesktopPane desktop = new JDesktopPane();

  private MainPanel() {
    super(new BorderLayout());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(createMenuBar()));
    // title, resizable, closable, maximizable, iconifiable
    JInternalFrame jif = new JInternalFrame("AlwaysOnTop", true, false, true, true);
    jif.setSize(180, 180);
    Integer layer = JLayeredPane.MODAL_LAYER + 1;
    int position = 0;
    desktop.add(jif, layer, position);
    jif.setVisible(true);
    // desktop.getDesktopManager().activateFrame(jif);
    add(desktop);
    setPreferredSize(new Dimension(320, 240));
  }

  private JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    JMenu menu = new JMenu("Document");
    menu.setMnemonic(KeyEvent.VK_D);
    menuBar.add(menu);

    JMenuItem menuItem = menu.add("New");
    menuItem.setMnemonic(KeyEvent.VK_N);
    menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.ALT_DOWN_MASK));
    menuItem.setActionCommand("new");
    menuItem.addActionListener(e -> {
      JInternalFrame f = makeInternalFrame();
      desktop.add(f);
      f.setVisible(true);
      // desktop.getDesktopManager().activateFrame(f);
    });
    menu.add(menuItem);

    menuItem = menu.add("Quit");
    menuItem.setMnemonic(KeyEvent.VK_Q);
    menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.ALT_DOWN_MASK));
    menuItem.setActionCommand("quit");
    menuItem.addActionListener(e ->
        Optional.ofNullable(SwingUtilities.getWindowAncestor(desktop)).ifPresent(Window::dispose));
    menu.add(menuItem);
    return menuBar;
  }

  private static JInternalFrame makeInternalFrame() {
    String title = String.format("Document #%s", OPEN_FRAME_COUNT.getAndIncrement());
    JInternalFrame f = new JInternalFrame(title, true, true, true, true);
    f.setSize(180, 100);
    f.setLocation(OFFSET * OPEN_FRAME_COUNT.intValue(), OFFSET * OPEN_FRAME_COUNT.intValue());
    return f;
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
