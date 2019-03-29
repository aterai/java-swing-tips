// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    JLabel lbl1 = new JLabel("aaaaaaaaaa") {
      @Override public Dimension getPreferredSize() {
        return new Dimension(1200, 600);
      }
    };
    JLabel lbl2 = new JLabel("bbbbbbbbbb") {
      @Override public Dimension getPreferredSize() {
        return new Dimension(600, 1200);
      }
    };

    JScrollPane sp1 = new JScrollPane(lbl1);
    JScrollPane sp2 = new JScrollPane(lbl2);

    ChangeListener cl = new ChangeListener() {
      private boolean adjflg;
      @Override public void stateChanged(ChangeEvent e) {
        JViewport src = null;
        JViewport tgt = null;
        if (e.getSource() == sp1.getViewport()) {
          src = sp1.getViewport();
          tgt = sp2.getViewport();
        } else if (e.getSource() == sp2.getViewport()) {
          src = sp2.getViewport();
          tgt = sp1.getViewport();
        }
        if (adjflg || Objects.isNull(tgt) || Objects.isNull(src)) {
          return;
        }
        adjflg = true;
        Dimension dim1 = src.getViewSize();
        Dimension siz1 = src.getSize();
        Point pnt1 = src.getViewPosition();
        Dimension dim2 = tgt.getViewSize();
        Dimension siz2 = tgt.getSize();
        // Point pnt2 = tgt.getViewPosition();
        double d;
        d = pnt1.getY() / (dim1.getHeight() - siz1.getHeight()) * (dim2.getHeight() - siz2.getHeight());
        pnt1.y = (int) d;
        d = pnt1.getX() / (dim1.getWidth() - siz1.getWidth()) * (dim2.getWidth() - siz2.getWidth());
        pnt1.x = (int) d;
        tgt.setViewPosition(pnt1);
        adjflg = false;
      }
    };
    sp1.getViewport().addChangeListener(cl);
    sp2.getViewport().addChangeListener(cl);

    // // Test:
    // lbl1.setPreferredSize(new Dimension(1200, 600));
    // lbl2.setPreferredSize(new Dimension(1200, 600));
    // Dimension d1 = lbl1.getPreferredSize();
    // Dimension d2 = lbl2.getPreferredSize();
    // if (d1.width == d2.width) {
    //   BoundedRangeModel m = sp1.getHorizontalScrollBar().getModel();
    //   sp2.getHorizontalScrollBar().setModel(m);
    // }
    // if (d1.height == d2.height) {
    //   BoundedRangeModel m = sp1.getVerticalScrollBar().getModel();
    //   sp2.getVerticalScrollBar().setModel(m);
    // }
    add(sp1);
    add(sp2);
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
