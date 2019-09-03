// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicToolBarUI;

public final class MainPanel extends JPanel {
  public static final int ICON_SIZE = 32;

  private MainPanel() {
    super(new BorderLayout());
    add(new JScrollPane(new JTree()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JToolBar makeVerticalToolBar() {
    JToolBar toolbar = new JToolBar(SwingConstants.VERTICAL);
    // toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.PAGE_AXIS));

    // TEST:
    // JToolBar toolbar = new JToolBar(orientation) {
    //   @Override public Dimension getPreferredSize() {
    //     if (((BasicToolBarUI) getUI()).isFloating()) {
    //       setLayout(new GridLayout(0, 3));
    //       return new Dimension(ICON_SIZE * 3, ICON_SIZE * 2);
    //     } else {
    //       setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    //       return super.getPreferredSize();
    //     }
    //   }
    //   @Override public Dimension getMinimumSize() {
    //     return getPreferredSize();
    //   }
    //   @Override public Dimension getMaximumSize() {
    //     return getPreferredSize();
    //   }
    // };

    JPanel panel = new JPanel() {
      @Override public Dimension getPreferredSize() {
        if (((BasicToolBarUI) toolbar.getUI()).isFloating()) {
          setLayout(new GridLayout(0, 3));
          return new Dimension(ICON_SIZE * 3, ICON_SIZE * 2);
        } else {
          setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
          return super.getPreferredSize();
        }
      }

      @Override public Dimension getMinimumSize() {
        return getPreferredSize();
      }

      @Override public Dimension getMaximumSize() {
        return getPreferredSize();
      }
    };
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.add(new ColorPanel(Color.RED));
    panel.add(new ColorPanel(Color.GREEN));
    panel.add(new ColorPanel(Color.BLUE));
    panel.add(new ColorPanel(Color.ORANGE));
    panel.add(new ColorPanel(Color.CYAN));

    toolbar.add(panel);
    toolbar.add(Box.createGlue());

    // TEST: AncestorListener
    // toolbar.addAncestorListener(new AncestorListener() {
    //   @Override public void ancestorAdded(AncestorEvent e) {
    //     Container pp = e.getAncestorParent();
    //     if (pp instanceof JFrame) {
    //       System.out.println(((JFrame) pp).getTitle());
    //     }
    //     Container p = e.getAncestor();
    //     if (p instanceof JDialog) {
    //       System.out.println("  GridLayout");
    //       panel.setLayout(new GridLayout(0, 3));
    //       ((JDialog) p).pack();
    //       // ((JDialog) p).setSize(ICON_SIZE * 3, ICON_SIZE * 2);
    //     } else {
    //       System.out.println("  BoxLayout");
    //       panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    //     }
    //   }
    //   @Override public void ancestorMoved(AncestorEvent e) {}
    //   @Override public void ancestorRemoved(AncestorEvent e) {}
    // });
    return toolbar;
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
    frame.getContentPane().add(Box.createHorizontalStrut(10), BorderLayout.NORTH);
    frame.getContentPane().add(Box.createHorizontalStrut(10), BorderLayout.SOUTH);
    frame.getContentPane().add(makeVerticalToolBar(), BorderLayout.EAST);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class ColorPanel extends JPanel {
  protected ColorPanel(Color color) {
    super();
    setBackground(color);
  }

  @Override public Dimension getPreferredSize() {
    return new Dimension(MainPanel.ICON_SIZE, MainPanel.ICON_SIZE);
  }

  @Override public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  @Override public Dimension getMaximumSize() {
    return getPreferredSize();
  }
}
