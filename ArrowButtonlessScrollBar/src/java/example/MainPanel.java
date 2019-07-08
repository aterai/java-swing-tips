// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Collections;
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 0));

    UIManager.put("ScrollBar.width", 10);
    UIManager.put("ScrollBar.thumbHeight", 20); // GTKLookAndFeel, SynthLookAndFeel, NimbusLookAndFeel
    UIManager.put("ScrollBar.minimumThumbSize", new Dimension(30, 30));
    UIManager.put("ScrollBar.incrementButtonGap", 0);
    UIManager.put("ScrollBar.decrementButtonGap", 0);

    // UIManager.put("ScrollBar.squareButtons", Boolean.TRUE);
    // UIManager.put("ArrowButton.size", 8);

    Color thumbColor = new Color(0xCD_CD_CD);
    UIManager.put("ScrollBar.thumb", thumbColor);
    // UIManager.put("ScrollBar.thumbShadow", thumbColor);
    // UIManager.put("ScrollBar.thumbDarkShadow", thumbColor);
    // UIManager.put("ScrollBar.thumbHighlight", thumbColor);

    Color trackColor = new Color(0xF0_F0_F0);
    UIManager.put("ScrollBar.track", trackColor);

    String txt = String.join("\n", Collections.nCopies(100, "aaaaaaaaaaaaaaaaaaa"));

    // JScrollPane scrollPane = new JScrollPane(new JTextArea(txt));
    // scrollPane.setVerticalScrollBar(new JScrollBar(Adjustable.VERTICAL) {
    //   @Override public void updateUI() {
    //     super.updateUI();
    //     setUI(new ArrowButtonlessScrollBarUI());
    //     putClientProperty("JScrollBar.fastWheelScrolling", Boolean.TRUE);
    //   }
    //
    //   @Override public Dimension getPreferredSize() {
    //     Dimension d = super.getPreferredSize();
    //     d.width = 10;
    //     return d;
    //   }
    // });
    //
    // scrollPane.setHorizontalScrollBar(new JScrollBar(Adjustable.HORIZONTAL) {
    //   @Override public void updateUI() {
    //     super.updateUI();
    //     setUI(new ArrowButtonlessScrollBarUI());
    //     putClientProperty("JScrollBar.fastWheelScrolling", Boolean.TRUE);
    //   }
    // });
    // add(scrollPane);

    add(new JScrollPane(new JTextArea(txt)));
    add(new JScrollPane(new JTextArea(txt)) {
      @Override public void updateUI() {
        super.updateUI();
        getVerticalScrollBar().setUI(new ArrowButtonlessScrollBarUI());
        getHorizontalScrollBar().setUI(new ArrowButtonlessScrollBarUI());
      }
    });
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

class ZeroSizeButton extends JButton {
  private static final Dimension ZERO_SIZE = new Dimension();

  @Override public Dimension getPreferredSize() {
    return ZERO_SIZE;
  }
}

class ArrowButtonlessScrollBarUI extends BasicScrollBarUI {
  @Override protected JButton createDecreaseButton(int orientation) {
    return new ZeroSizeButton();
  }

  @Override protected JButton createIncreaseButton(int orientation) {
    return new ZeroSizeButton();
  }

  // @Override protected Dimension getMinimumThumbSize() {
  //   // return new Dimension(20, 20);
  //   return UIManager.getDimension("ScrollBar.minimumThumbSize");
  // }

  @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(trackColor);
    g2.fill(r);
    g2.dispose();
  }

  @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
    JScrollBar sb = (JScrollBar) c;
    if (!sb.isEnabled()) {
      return;
    }
    BoundedRangeModel m = sb.getModel();
    if (m.getMaximum() - m.getMinimum() - m.getExtent() > 0) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      Color color;
      if (isDragging) {
        color = thumbDarkShadowColor;
      } else if (isThumbRollover()) {
        color = thumbLightShadowColor;
      } else {
        color = thumbColor;
      }
      g2.setPaint(color);
      g2.fillRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2);
      g2.dispose();
    }
  }
}
