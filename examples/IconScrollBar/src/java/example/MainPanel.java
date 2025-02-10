// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsScrollBarUI;
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JLabel label = new JLabel("JLabel") {
      @Override public Dimension getPreferredSize() {
        return new Dimension(1000, 1000);
      }
    };
    JScrollBar scrollBar = new JScrollBar(Adjustable.VERTICAL) {
      @Override public void updateUI() {
        super.updateUI();
        if (getUI() instanceof WindowsScrollBarUI) {
          setUI(new WindowsIconScrollBarUI());
        } else {
          setUI(new BasicIconScrollBarUI());
        }
      }
    };
    JScrollPane scrollPane = new JScrollPane(label);
    scrollPane.setVerticalScrollBar(scrollBar);
    add(scrollPane);
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

class WindowsIconScrollBarUI extends WindowsScrollBarUI {
  @Override protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
    super.paintThumb(g, c, thumbBounds);
    JScrollBar sb = (JScrollBar) c;
    if (!sb.isEnabled() || thumbBounds.width > thumbBounds.height) {
      return;
    }

    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Color oc;
    Color ic;
    if (isDragging) {
      oc = SystemColor.activeCaption.darker();
      ic = SystemColor.inactiveCaptionText.darker();
    } else if (isThumbRollover()) {
      oc = SystemColor.activeCaption.brighter();
      ic = SystemColor.inactiveCaptionText.brighter();
    } else {
      oc = SystemColor.activeCaption;
      ic = SystemColor.inactiveCaptionText;
    }
    paintCircle(g2, thumbBounds, 6, oc);
    paintCircle(g2, thumbBounds, 10, ic);
    g2.dispose();
  }

  private void paintCircle(Graphics2D g2, Rectangle thumbBounds, int w, Color color) {
    g2.setPaint(color);
    int ww = thumbBounds.width - w;
    g2.fillOval(thumbBounds.x + w / 2, thumbBounds.y + (thumbBounds.height - ww) / 2, ww, ww);
  }
}

class BasicIconScrollBarUI extends BasicScrollBarUI {
  @Override protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
    super.paintThumb(g, c, thumbBounds);
    JScrollBar sb = (JScrollBar) c;
    if (!sb.isEnabled() || thumbBounds.width > thumbBounds.height) {
      return;
    }

    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Color oc;
    Color ic;
    if (isDragging) {
      oc = SystemColor.activeCaption.darker();
      ic = SystemColor.inactiveCaptionText.darker();
    } else if (isThumbRollover()) {
      oc = SystemColor.activeCaption.brighter();
      ic = SystemColor.inactiveCaptionText.brighter();
    } else {
      oc = SystemColor.activeCaption;
      ic = SystemColor.inactiveCaptionText;
    }
    paintCircle(g2, thumbBounds, 6, oc);
    paintCircle(g2, thumbBounds, 10, ic);
    g2.dispose();
  }

  private void paintCircle(Graphics2D g2, Rectangle thumbBounds, int w, Color color) {
    g2.setPaint(color);
    int ww = thumbBounds.width - w;
    g2.fillOval(thumbBounds.x + w / 2, thumbBounds.y + (thumbBounds.height - ww) / 2, ww, ww);
  }
}
