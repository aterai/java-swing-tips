// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel();
    p.add(new JTextField(16));
    p.add(new JButton("button"));
    p.add(new JTextField(16));
    p.add(new JButton("button"));
    add(p);
    add(new JLabel("JFrame:mouseClicked -> clearGlobalFocusOwner"), BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  private static JMenuBar makeMenuBar() {
    JMenuBar menubar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    fileMenu.add("dummy");
    menubar.add(fileMenu);
    return menubar;
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setJMenuBar(makeMenuBar());
    frame.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        System.out.println("clicked");
        KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
      }
    });
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
