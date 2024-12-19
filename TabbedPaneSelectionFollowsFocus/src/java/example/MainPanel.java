// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.plaf.metal.MetalTabbedPaneUI;

public final class MainPanel extends JPanel {
  private int focusIndex = -1;

  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabs = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT) {
      @Override public void updateUI() {
        super.updateUI();
        if (getUI() instanceof MetalTabbedPaneUI) {
          setUI(new MetalTabbedPaneUI() {
            @Override protected void navigateSelectedTab(int direction) {
              super.navigateSelectedTab(direction);
              focusIndex = getFocusIndex();
            }
          });
        }
      }
    };
    tabs.addChangeListener(e -> {
      focusIndex = tabs.getSelectedIndex();
      tabs.repaint();
    });
    String help1 = "SPACE: selectTabWithFocus";
    String help2 = "LEFT: navigateLeft";
    String help3 = "RIGHT: navigateRight";
    JTextArea textArea = new JTextArea(String.join("\n", help1, help2, help3));
    textArea.setEditable(false);
    tabs.addTab("help", new JScrollPane(textArea));
    IntStream.range(0, 10).forEach(i -> tabs.addTab("title" + i, new JLabel("JLabel" + i)));

    InputMap im = tabs.getInputMap(WHEN_FOCUSED);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "selectTabWithFocus");

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    mb.add(makeCheckBox(tabs));
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    add(new JLayer<>(tabs, new LayerUI<JTabbedPane>() {
      @Override public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        if (c instanceof JLayer) {
          JLayer<?> layer = (JLayer<?>) c;
          JTabbedPane tabbedPane = (JTabbedPane) layer.getView();
          if (focusIndex >= 0 && focusIndex != tabbedPane.getSelectedIndex()) {
            Rectangle r = tabbedPane.getBoundsAt(focusIndex);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
            g2.setPaint(Color.RED);
            g2.fill(r);
            g2.dispose();
          }
        }
      }
    }));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JCheckBox makeCheckBox(JTabbedPane tabs) {
    String key = "TabbedPane.selectionFollowsFocus";
    JCheckBox check = new JCheckBox(key, UIManager.getBoolean(key)) {
      @Override public void updateUI() {
        super.updateUI();
        boolean b = UIManager.getLookAndFeelDefaults().getBoolean(key);
        setSelected(b);
        UIManager.put(key, b);
      }
    };
    check.setFocusable(false);
    check.addActionListener(e -> {
      boolean b = ((JCheckBox) e.getSource()).isSelected();
      UIManager.put(key, b);
      SwingUtilities.updateComponentTreeUI(tabs);
    });
    return check;
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
