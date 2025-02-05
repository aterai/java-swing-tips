// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Point pt = new Point();
    Class<?> clz = getClass();
    Toolkit tk = getToolkit();
    List<Cursor> list = Stream.of("00", "01", "02")
        .map(s -> tk.createCustomCursor(tk.createImage(clz.getResource(s + ".png")), pt, s))
        .collect(Collectors.toList());

    Timer animator = new Timer(100, null);
    JButton button = new JButton("Start");
    button.setCursor(list.get(0));
    button.addActionListener(e -> {
      JButton b = (JButton) e.getSource();
      if (animator.isRunning()) {
        b.setText("Start");
        animator.stop();
      } else {
        b.setText("Stop");
        animator.start();
      }
    });
    button.addHierarchyListener(e -> {
      boolean b = (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
      if (b && !e.getComponent().isDisplayable()) {
        animator.stop();
      }
    });
    animator.addActionListener(new CursorActionListener(button, list));

    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));
    p.add(button);
    add(p);
    setBorder(BorderFactory.createTitledBorder("delay=100ms"));
    setPreferredSize(new Dimension(320, 240));
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
    // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class CursorActionListener implements ActionListener {
  private int counter;
  private final Component comp;
  private final List<Cursor> frames;

  protected CursorActionListener(Component comp, List<Cursor> frames) {
    this.comp = comp;
    this.frames = frames;
  }

  @Override public void actionPerformed(ActionEvent e) {
    comp.setCursor(frames.get(counter));
    counter = (counter + 1) % frames.size();
  }
}
