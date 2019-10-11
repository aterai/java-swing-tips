// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.metal.MetalTabbedPaneUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(0, 1, 5, 5));

    JTabbedPane tabbedPane1 = new JTabbedPane() {
      @Override public void updateUI() {
        super.updateUI();
        if (getUI() instanceof WindowsTabbedPaneUI) {
          setUI(new WindowsTabbedPaneUI() {
            @Override protected boolean shouldPadTabRun(int tabPlacement, int run) {
              return false;
            }
          });
        } else {
          setUI(new MetalTabbedPaneUI() {
            @Override protected boolean shouldPadTabRun(int tabPlacement, int run) {
              // BasicTabbedPaneUI: return runCount > 1;
              // MetalTabbedPaneUI: return runCount > 1 && run < runCount - 1;
              return false;
            }
          });
        }
      }
    };

    JTabbedPane tabbedPane2 = new JTabbedPane() {
      @Override public void updateUI() {
        super.updateUI();
        if (getUI() instanceof WindowsTabbedPaneUI) {
          setUI(new WindowsTabbedPaneUI() {
            @Override protected boolean shouldPadTabRun(int tabPlacement, int run) {
              return true;
            }
          });
        } else {
          setUI(new MetalTabbedPaneUI() {
            @Override protected boolean shouldPadTabRun(int tabPlacement, int run) {
              // BasicTabbedPaneUI: return runCount > 1;
              // MetalTabbedPaneUI: return runCount > 1 && run < runCount - 1;
              return true;
            }
          });
        }
      }
    };

    add(makeTabbedPane("default", new JTabbedPane()));
    add(makeTabbedPane("shouldPadTabRun: false", tabbedPane1));
    add(makeTabbedPane("shouldPadTabRun: true", tabbedPane2));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTabbedPane makeTabbedPane(String title, JTabbedPane tabbedPane) {
    tabbedPane.addTab("1111111111111111111111111111", new ColorIcon(Color.RED), new JLabel(title));
    tabbedPane.addTab("2", new ColorIcon(Color.GREEN), new JLabel());
    tabbedPane.addTab("333333333333333333333333333333333", new ColorIcon(Color.BLUE), new JLabel());
    tabbedPane.addTab("444444444444", new ColorIcon(Color.ORANGE), new JLabel());
    return tabbedPane;
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
    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtil.createLookAndFeelMenu());

    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.setJMenuBar(mb);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class ColorIcon implements Icon {
  private final Color color;

  protected ColorIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(color);
    g2.fillRect(0, 0, getIconWidth(), getIconHeight());
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 12;
  }

  @Override public int getIconHeight() {
    return 12;
  }
}

// @see https://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtil {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtil() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup lafGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo lafInfo: UIManager.getInstalledLookAndFeels()) {
      menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lafGroup));
    }
    return menu;
  }

  private static JMenuItem createLookAndFeelItem(String lafName, String lafClassName, ButtonGroup lafGroup) {
    JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem(lafName, lafClassName.equals(lookAndFeel));
    lafItem.setActionCommand(lafClassName);
    lafItem.setHideActionText(true);
    lafItem.addActionListener(e -> {
      ButtonModel m = lafGroup.getSelection();
      try {
        setLookAndFeel(m.getActionCommand());
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        ex.printStackTrace();
        Toolkit.getDefaultToolkit().beep();
      }
    });
    lafGroup.add(lafItem);
    return lafItem;
  }

  private static void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
    String oldLookAndFeel = LookAndFeelUtil.lookAndFeel;
    if (!oldLookAndFeel.equals(lookAndFeel)) {
      UIManager.setLookAndFeel(lookAndFeel);
      LookAndFeelUtil.lookAndFeel = lookAndFeel;
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window: Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
