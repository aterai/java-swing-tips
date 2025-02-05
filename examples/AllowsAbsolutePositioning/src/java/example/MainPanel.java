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

    String txt1 = "middle mouse click in the track will set the position";
    String txt2 = "of the track to where the mouse is.";
    String help = String.join(" ", txt1, txt2);
    String txt = String.join("\n", Collections.nCopies(100, help));
    JTextArea textArea = new JTextArea("override TrackListener#mousePressed(...)\n" + txt);

    JScrollPane scroll = new JScrollPane(textArea);
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

class AbsolutePositioningWindowsScrollBarUI extends WindowsScrollBarUI {
  @Override protected TrackListener createTrackListener() {
    return new TrackListener() {
      @Override public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
          super.mousePressed(new MouseEvent(
              e.getComponent(), e.getID(), e.getWhen(),
              InputEvent.BUTTON2_DOWN_MASK ^ InputEvent.BUTTON2_MASK, // e.getModifiers(),
              // Java 9: InputEvent.BUTTON2_DOWN_MASK, // e.getModifiersEx(),
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
              // Java 9: InputEvent.BUTTON2_DOWN_MASK,
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
