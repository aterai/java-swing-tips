// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    JLabel lbl1 = new JLabel("11111111") {
      @Override public Dimension getPreferredSize() {
        return new Dimension(1200, 600);
      }
    };
    JLabel lbl2 = new JLabel("22222222") {
      @Override public Dimension getPreferredSize() {
        return new Dimension(600, 1200);
      }
    };

    JScrollPane sp1 = new JScrollPane(lbl1);
    JScrollPane sp2 = new JScrollPane(lbl2);

    ChangeListener cl = new ChangeListener() {
      private final AtomicBoolean adjusting = new AtomicBoolean();
      @Override public void stateChanged(ChangeEvent e) {
        JViewport src = null;
        JViewport tgt = null;
        if (Objects.equals(e.getSource(), sp1.getViewport())) {
          src = sp1.getViewport();
          tgt = sp2.getViewport();
        } else if (Objects.equals(e.getSource(), sp2.getViewport())) {
          src = sp2.getViewport();
          tgt = sp1.getViewport();
        }
        if (adjusting.get() || Objects.isNull(tgt) || Objects.isNull(src)) {
          return;
        }
        adjusting.set(true);
        Dimension dim1 = src.getViewSize();
        Dimension siz1 = src.getSize();
        Point pnt1 = src.getViewPosition();
        Dimension dim2 = tgt.getViewSize();
        Dimension siz2 = tgt.getSize();
        // Point pnt2 = tgt.getViewPosition();
        double dy = pnt1.getY() / (dim1.height - siz1.height) * (dim2.height - siz2.height);
        pnt1.y = (int) dy;
        double dx = pnt1.getX() / (dim1.width - siz1.width) * (dim2.width - siz2.width);
        pnt1.x = (int) dx;
        tgt.setViewPosition(pnt1);
        adjusting.set(false);
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
