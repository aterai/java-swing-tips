// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsScrollBarUI;
import java.awt.*;
import java.util.logging.Logger;
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

    // JScrollPane s1 = new JScrollPane(new JTextArea(txt)) {
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

    JScrollPane s1 = new JScrollPane(new JTextArea("override\ngetMinimumThumbSize()\n" + txt));
    s1.setVerticalScrollBar(new VerticalScrollBar());

    JScrollPane s0 = new JScrollPane(new JTextArea("default\n\n" + txt));
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, s0, s1);
    split.setResizeWeight(.5);
    split.setDividerLocation(160);
    add(split);
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
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      Logger.getGlobal().severe(ex::getMessage);
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

class VerticalScrollBar extends JScrollBar {
  protected VerticalScrollBar() {
    super(VERTICAL);
  }

  @Override public void updateUI() {
    super.updateUI();
    if (getUI() instanceof WindowsScrollBarUI) {
      setUI(new WindowsScrollBarUI() {
        @Override protected Dimension getMinimumThumbSize() {
          return updateThumbSize(super.getMinimumThumbSize());
        }
      });
    } else {
      setUI(new MetalScrollBarUI() {
        @Override protected Dimension getMinimumThumbSize() {
          return updateThumbSize(super.getMinimumThumbSize());
        }
      });
    }
    putClientProperty("JScrollBar.fastWheelScrolling", Boolean.TRUE);
  }

  private Dimension updateThumbSize(Dimension d) {
    Container c = SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
    int height = d.height;
    if (c instanceof JScrollPane) {
      Rectangle r = SwingUtilities.calculateInnerArea((JScrollPane) c, null);
      height = Math.max(height, r.height / 12);
    }
    return new Dimension(d.width, height);
  }
}
