// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsScrollBarUI;
import java.awt.*;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.plaf.metal.MetalScrollBarUI;

public final class MainPanel extends JPanel {
  private static final String LF = "\n";

  private MainPanel() {
    super(new BorderLayout());
    UIManager.put("ScrollBar.minimumThumbSize", new Dimension(32, 32)); // BasicScrollBarUI

    StringBuilder buf = new StringBuilder();
    IntStream.range(0, 1000).forEach(i -> buf.append(i).append(LF));
    String txt = buf.toString();

    // JScrollPane scroll = new JScrollPane(new JTextArea(txt)) {
    //   @Override public void updateUI() {
    //     super.updateUI();
    //     JScrollPane c = this;
    //     EventQueue.invokeLater(() -> {
    //       if (getUI() instanceof WindowsScrollPaneUI) {
    //         getVerticalScrollBar().setUI(new WindowsScrollBarUI() {
    //           @Override protected Dimension getMinimumThumbSize() {
    //             Dimension d = super.getMinimumThumbSize();
    //             Rectangle r = SwingUtilities.calculateInnerArea(c, null);
    //             return new Dimension(d.width, Math.max(d.height, r.height / 12));
    //           }
    //         });
    //       } else {
    //         getVerticalScrollBar().setUI(new MetalScrollBarUI() {
    //           @Override protected Dimension getMinimumThumbSize() {
    //             Dimension d = super.getMinimumThumbSize();
    //             Rectangle r = SwingUtilities.calculateInnerArea(c, null);
    //             return new Dimension(d.width, Math.max(d.height, r.height / 12));
    //           }
    //         });
    //       }
    //     });
    //   }
    // };

    JScrollPane scroll = new JScrollPane(new JTextArea("override\ngetMinimumThumbSize()\n" + txt));
    scroll.setVerticalScrollBar(new JScrollBar(Adjustable.VERTICAL) {
      @Override public void updateUI() {
        super.updateUI();
        if (getUI() instanceof WindowsScrollBarUI) {
          setUI(new WindowsScrollBarUI() {
            @Override protected Dimension getMinimumThumbSize() {
              Dimension d = super.getMinimumThumbSize();
              Rectangle r = SwingUtilities.calculateInnerArea(scroll, null);
              return new Dimension(d.width, Math.max(d.height, r.height / 12));
            }
          });
        } else {
          setUI(new MetalScrollBarUI() {
            @Override protected Dimension getMinimumThumbSize() {
              Dimension d = super.getMinimumThumbSize();
              Rectangle r = SwingUtilities.calculateInnerArea(scroll, null);
              d.height = Math.max(d.height, r.height / 12);
              return d;
            }
          });
        }
        putClientProperty("JScrollBar.fastWheelScrolling", Boolean.TRUE);
      }
    });

    JSplitPane sp = new JSplitPane();
    sp.setLeftComponent(new JScrollPane(new JTextArea("default\n\n" + txt)));
    sp.setRightComponent(scroll);
    sp.setResizeWeight(.5);
    add(sp);
  }

  @Override public Dimension getPreferredSize() {
    return new Dimension(320, 240);
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
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
