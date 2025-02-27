// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import javax.swing.plaf.metal.MetalLookAndFeel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JInternalFrame f0 = new JInternalFrame("metal(default)", true, true, true, true);
    // TEST: f0.setUI(new BasicInternalFrameUI(f0));
    f0.setSize(240, 100);
    f0.setLocation(20, 10);

    JInternalFrame f1 = new JInternalFrame("basic", true, true, true, true) {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new BasicInternalFrameUI(this) {
          @Override protected JComponent createNorthPane(JInternalFrame w) {
            return new BumpsFreeInternalFrameTitlePane(w);
          }
        });
      }
    };
    f1.setSize(240, 100);
    f1.setLocation(40, 120);

    JDesktopPane desktop = new JDesktopPane();
    desktop.add(f0);
    desktop.add(f1);
    EventQueue.invokeLater(() -> {
      JInternalFrame[] allFrames = desktop.getAllFrames();
      Arrays.asList(allFrames).forEach(f -> f.setVisible(true));
    });

    add(desktop);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class BumpsFreeInternalFrameTitlePane extends BasicInternalFrameTitlePane {
  protected BumpsFreeInternalFrameTitlePane(JInternalFrame w) {
    super(w);
  }

  @Override protected void paintTitleBackground(Graphics g) {
    super.paintTitleBackground(g);
    boolean isSelected = frame.isSelected();
    int width = getWidth();
    int height = getHeight();
    Color shadow = isSelected
        ? MetalLookAndFeel.getPrimaryControlDarkShadow()
        : MetalLookAndFeel.getControlDarkShadow();
    g.setColor(shadow);
    g.drawLine(0, height - 1, width, height - 1);
    g.drawLine(0, 0, 0, 0);
    g.drawLine(width - 1, 0, width - 1, 0);
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    d.height = 24;
    return d;
  }

  @Override public void createButtons() {
    super.createButtons();
    Arrays.asList(closeButton, maxButton, iconButton).forEach(b -> {
      b.setContentAreaFilled(false);
      b.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
    });
  }
}
