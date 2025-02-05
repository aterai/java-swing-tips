// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JDesktopPane desktop = new JDesktopPane();
    desktop.setDesktopManager(new DefaultDesktopManager() {
      @Override public void iconifyFrame(JInternalFrame f) {
        Rectangle r = this.getBoundsForIconOf(f);
        r.width = f.getDesktopIcon().getPreferredSize().width;
        f.getDesktopIcon().setBounds(r);
        super.iconifyFrame(f);
      }
    });
    desktop.add(createFrame("looooooooooooong title #", 1));
    desktop.add(createFrame("#", 0));

    AtomicInteger idx = new AtomicInteger(2);
    JButton button = new JButton("add");
    button.addActionListener(e -> desktop.add(createFrame("#", idx.getAndIncrement())));

    add(desktop);
    add(button, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JInternalFrame createFrame(String t, int i) {
    JInternalFrame f = new JInternalFrame(t + i, true, true, true, true);
    f.setDesktopIcon(createDesktopIcon(f));
    f.setSize(200, 100);
    f.setLocation(5 + 40 * i, 5 + 50 * i);
    EventQueue.invokeLater(() -> f.setVisible(true));
    return f;
  }

  private static JInternalFrame.JDesktopIcon createDesktopIcon(JInternalFrame f) {
    return new JInternalFrame.JDesktopIcon(f) {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        JInternalFrame frame = getInternalFrame();
        String title = frame.getTitle();
        Font font = getFont();
        if (Objects.nonNull(font)) {
          // testWidth();
          // @see javax/swing/plaf/basic/BasicInternalFrameTitlePane.java
          // Handler#minimumLayoutSize(Container)
          // Calculate width.
          int buttonsW = 22;
          if (frame.isClosable()) {
            buttonsW += 19;
          }
          if (frame.isMaximizable()) {
            buttonsW += 19;
          }
          if (frame.isIconifiable()) {
            buttonsW += 19;
          }
          // buttonsW = Math.max(buttonsW, buttonsW2);

          FontMetrics fm = getFontMetrics(font);
          int titleW = SwingUtilities.computeStringWidth(fm, title);
          Insets i = getInsets();
          // 2: Magic number of gap between icons
          d.width = buttonsW + i.left + i.right + titleW + 2 + 2 + 2;
          // 27: Magic number for NimbusLookAndFeel
          d.height = Math.min(27, d.height);
          // System.out.println("BasicInternalFrameTitlePane: " + d.width);
        }
        return d;
      }

      // private void testWidth() {
      //   Dimension dim = getLayout().minimumLayoutSize(this);
      //   System.out.println("minimumLayoutSize: " + dim.width);
      //   int buttonsW = SwingUtils.descendants(this)
      //       .filter(AbstractButton.class::isInstance)
      //       .mapToInt(c -> c.getPreferredSize().width)
      //       .sum();
      //   System.out.println("Total width of all buttons: " + buttonsW);
      // }
    };
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
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

// final class SwingUtils {
//   private SwingUtils() {
//     /* Singleton */
//   }
//
//   public static Stream<Component> descendants(Container parent) {
//     return Stream.of(parent.getComponents())
//         .filter(Container.class::isInstance).map(Container.class::cast)
//         .flatMap(c -> Stream.concat(Stream.of(c), descendants(c)));
//   }
// }
