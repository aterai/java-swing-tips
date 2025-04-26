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
    super(new GridLayout(2, 1, 5, 5));
    UIManager.put("TabbedPane.tabRunOverlay", 0);
    UIManager.put("TabbedPane.selectedLabelShift", 0);
    UIManager.put("TabbedPane.labelShift", 0);
    // TabbedPane.selectedTabPadInsets : InsetsUIResource[top=2,left=2,bottom=2,right=1]
    UIManager.put("TabbedPane.selectedTabPadInsets", new Insets(0, 0, 0, 0));

    JTabbedPane tabbedPane = new JTabbedPane() {
      @Override public void updateUI() {
        super.updateUI();
        if (getUI() instanceof WindowsTabbedPaneUI) {
          setUI(new WindowsTabbedPaneUI() {
            @Override protected boolean shouldRotateTabRuns(int tabPlacement) {
              return false;
            }
          });
        } else {
          setUI(new MetalTabbedPaneUI() {
            @Override protected boolean shouldRotateTabRuns(int tabPlacement) {
              return false;
            }

            // This method with two arguments is not used at all elsewhere and may be a bug.
            @Override protected boolean shouldRotateTabRuns(int tabPlacement, int selectedRun) {
              return false;
            }
          });
        }
      }
    };

    add(makeTabbedPane(new JTabbedPane()));
    add(makeTabbedPane(tabbedPane));

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    setPreferredSize(new Dimension(320, 240));
  }

  private static JTabbedPane makeTabbedPane(JTabbedPane tabbedPane) {
    tabbedPane.addTab("111111111111111111111111", new ColorIcon(Color.RED), new JLabel());
    tabbedPane.addTab("2", new ColorIcon(Color.GREEN), new JLabel());
    tabbedPane.addTab("3333333333333333333333333333", new ColorIcon(Color.BLUE), new JLabel());
    tabbedPane.addTab("444444444444", new ColorIcon(Color.ORANGE), new JLabel());
    return tabbedPane;
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

// @see SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = makeButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton makeButton(UIManager.LookAndFeelInfo info) {
    boolean selected = info.getClassName().equals(lookAndFeel);
    return new JRadioButtonMenuItem(info.getName(), selected);
  }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        ex.printStackTrace();
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
