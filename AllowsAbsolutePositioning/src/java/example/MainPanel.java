// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsScrollBarUI;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.Collections;
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout(5, 5));

    // https://docs.oracle.com/javase/8/docs/api/javax/swing/plaf/synth/doc-files/componentProperties.html
    UIManager.put("ScrollBar.allowsAbsolutePositioning", Boolean.TRUE);

    String help = "middle mouse click in the track will set the position of the track to where the mouse is.";
    String txt = String.join("\n", Collections.nCopies(100, help));

    JScrollPane scroll = new JScrollPane(new JTextArea("override TrackListener#mousePressed(...)\n" + txt));
    scroll.setVerticalScrollBar(new JScrollBar(Adjustable.VERTICAL) {
      @Override public void updateUI() {
        super.updateUI();
        if (getUI() instanceof WindowsScrollBarUI) {
          setUI(new AbsolutePositioningWindowsScrollBarUI());
        } else {
          setUI(new AbsolutePositioningBasicScrollBarUI());
        }
        putClientProperty("JScrollBar.fastWheelScrolling", Boolean.TRUE);
      }
    });

    scroll.setHorizontalScrollBar(new JScrollBar(Adjustable.HORIZONTAL) {
      @Override public void updateUI() {
        super.updateUI();
        if (getUI() instanceof WindowsScrollBarUI) {
          setUI(new AbsolutePositioningWindowsScrollBarUI());
        } else {
          setUI(new AbsolutePositioningBasicScrollBarUI());
        }
        putClientProperty("JScrollBar.fastWheelScrolling", Boolean.TRUE);
      }
    });

    JPanel p = new JPanel(new GridLayout(1, 2));
    p.add(new JScrollPane(new JTextArea(txt)));
    p.add(scroll);

    add(new JLabel("ScrollBar.allowsAbsolutePositioning: true"), BorderLayout.NORTH);
    add(p);
    setPreferredSize(new Dimension(320, 240));
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

class AbsolutePositioningWindowsScrollBarUI extends WindowsScrollBarUI {
  @Override protected TrackListener createTrackListener() {
    return new TrackListener() {
      @Override public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
          super.mousePressed(new MouseEvent(
              e.getComponent(), e.getID(), e.getWhen(),
              InputEvent.BUTTON2_DOWN_MASK ^ InputEvent.BUTTON2_MASK, // e.getModifiers(),
              e.getX(), e.getY(),
              e.getXOnScreen(), e.getYOnScreen(),
              e.getClickCount(),
              e.isPopupTrigger(),
              MouseEvent.BUTTON2));
        } else {
          super.mousePressed(e);
        }
      }
    };
  }
}

class AbsolutePositioningBasicScrollBarUI extends BasicScrollBarUI {
  @Override protected TrackListener createTrackListener() {
    return new TrackListener() {
      @Override public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
          super.mousePressed(new MouseEvent(
              e.getComponent(), e.getID(), e.getWhen(),
              InputEvent.BUTTON2_DOWN_MASK ^ InputEvent.BUTTON2_MASK,
              e.getX(), e.getY(),
              e.getXOnScreen(), e.getYOnScreen(),
              e.getClickCount(),
              e.isPopupTrigger(),
              MouseEvent.BUTTON2));
        } else {
          super.mousePressed(e);
        }
      }
    };
  }
}
