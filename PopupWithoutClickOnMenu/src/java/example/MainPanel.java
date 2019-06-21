// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JCheckBox check = new JCheckBox("JMenu: hover(show popup automatically) on cursor", true);
    JMenuBar bar = makeMenuBar();
    addListenerToJMenu(bar, new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        if (check.isSelected()) {
          ((AbstractButton) e.getComponent()).doClick();
        }
      }

      @Override public void mouseEntered(MouseEvent e) {
        if (check.isSelected()) {
          ((AbstractButton) e.getComponent()).doClick();
        }
      }
    });

    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(bar));

    add(check, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JMenuBar makeMenuBar() {
    JMenuBar bar = new JMenuBar();

    JMenu menu = bar.add(new JMenu("File"));
    menu.add("Open");
    menu.add("Save");
    menu.add("Exit");

    menu = bar.add(new JMenu("Edit"));
    menu.add("Undo");
    menu.add("Redo");
    menu.addSeparator();
    menu.add("Cut");
    menu.add("Copy");
    menu.add("Paste");
    menu.add("Delete");

    menu = bar.add(new JMenu("Test"));
    menu.add("JMenuItem1");
    menu.add("JMenuItem2");
    JMenu sub = new JMenu("JMenu");
    sub.add("JMenuItem4");
    sub.add("JMenuItem5");
    menu.add(sub);
    menu.add("JMenuItem3");

    return bar;
  }

  private static void addListenerToJMenu(JMenuBar menuBar, MouseListener l) {
    // for (Component menu: menuBar.getComponents()) {
    for (MenuElement menu: menuBar.getSubElements()) {
      if (menu instanceof JMenu) {
        ((JMenu) menu).addMouseListener(l);
      }
    }
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
