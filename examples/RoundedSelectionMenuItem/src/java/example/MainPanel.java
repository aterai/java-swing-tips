// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsMenuItemUI;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.basic.BasicMenuItemUI;
import javax.swing.plaf.synth.SynthMenuItemUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    // UIManager.put("MenuItem.border", BorderFactory.createEmptyBorder(2, 2, 2, 2));
    UIManager.put("MenuItem.borderPainted", Boolean.FALSE);

    JMenu sub = makeMenu();
    sub.setMnemonic('M');
    KeyStroke ks1 = KeyStroke.getKeyStroke(KeyEvent.VK_1, 0);
    KeyStroke ks2 = KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_DOWN_MASK);
    KeyStroke ks3 = KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.SHIFT_DOWN_MASK);
    sub.add("MenuItem1").setAccelerator(ks1);
    sub.add("MenuItem2").setAccelerator(ks2);
    sub.add("MenuItem3").setAccelerator(ks3);

    JMenu menu = LookAndFeelUtils.createLookAndFeelMenu();
    menu.setMnemonic('L');
    menu.add(sub);

    JMenuBar mb = new JMenuBar();
    mb.add(menu);
    mb.add(sub);
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    JPopupMenu popup = makePopupMenu();
    popup.add("MenuItem4").setAccelerator(ks1);
    popup.add("MenuItem5").setAccelerator(ks2);
    popup.add("MenuItem6").setAccelerator(ks3);

    JTree tree = new JTree();
    tree.setComponentPopupMenu(popup);

    add(new JScrollPane(tree));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JPopupMenu makePopupMenu() {
    return new JPopupMenu() {
      @Override public JMenuItem add(String s) {
        return add(new JMenuItem(s) {
          @Override public void updateUI() {
            super.updateUI();
            ButtonUI ui = getUI();
            if (ui instanceof WindowsMenuItemUI) {
              setUI(new WindowsRoundMenuItemUI());
            } else if (!(ui instanceof SynthMenuItemUI)) {
              setUI(new BasicRoundMenuItemUI());
            }
          }
        });
      }
    };
  }

  private static JMenu makeMenu() {
    return new JMenu("JMenu(M)") {
      @Override public JMenuItem add(String s) {
        return add(new JMenuItem(s) {
          @Override public void updateUI() {
            super.updateUI();
            ButtonUI ui = getUI();
            if (ui instanceof WindowsMenuItemUI) {
              setUI(new WindowsRoundedMenuItemUI());
            } else if (!(ui instanceof SynthMenuItemUI)) {
              setUI(new BasicRoundMenuItemUI());
            }
          }
        });
      }
    };
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.put("PopupMenuUI", "example.RoundedPopupMenuUI");
      // UIManager.put("MenuItemUI", "example.WindowsRoundedMenuItemUI");
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
        Logger.getGlobal().severe(ex::getMessage);
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

class BasicRoundMenuItemUI extends BasicMenuItemUI {
  @Override protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
    ButtonModel model = menuItem.getModel();
    Color oldColor = g.getColor();
    int menuWidth = menuItem.getWidth();
    int menuHeight = menuItem.getHeight();
    if (menuItem.isOpaque()) {
      if (model.isArmed() || menuItem instanceof JMenu && model.isSelected()) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // g2.clearRect(0, 0, menuWidth, menuHeight);
        g2.setPaint(menuItem.getBackground());
        g2.fillRect(0, 0, menuWidth, menuHeight);
        g2.setColor(bgColor);
        g2.fillRoundRect(2, 2, menuWidth - 4, menuHeight - 4, 8, 8);
        g2.dispose();
      } else {
        g.setColor(menuItem.getBackground());
        g.fillRect(0, 0, menuWidth, menuHeight);
      }
      g.setColor(oldColor);
    } else if (model.isArmed() || menuItem instanceof JMenu && model.isSelected()) {
      g.setColor(bgColor);
      ((Graphics2D) g).setRenderingHint(
          RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.fillRoundRect(0, 0, menuWidth, menuHeight, 8, 8);
      g.setColor(oldColor);
    }
  }
}

class WindowsRoundMenuItemUI extends WindowsMenuItemUI {
  private BufferedImage buffer;

  @Override protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
    ButtonModel model = menuItem.getModel();
    if (model.isArmed() || menuItem instanceof JMenu && model.isSelected()) {
      int width = menuItem.getWidth();
      int height = menuItem.getHeight();
      if (buffer == null || buffer.getWidth() != width || buffer.getHeight() != height) {
        buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
      }
      Graphics2D g2 = buffer.createGraphics();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      // g2.setComposite(AlphaComposite.Clear);
      // g2.fillRect(0, 0, width, height);
      // g2.setComposite(AlphaComposite.Src);
      g2.fillRoundRect(0, 0, width, height, 8, 8);
      g2.setComposite(AlphaComposite.SrcAtop);
      super.paintBackground(g2, menuItem, bgColor);
      g2.dispose();
      g.drawImage(buffer, 0, 0, menuItem);
    } else {
      super.paintBackground(g, menuItem, bgColor);
    }
  }
}

class WindowsRoundedMenuItemUI extends WindowsMenuItemUI {
  private VolatileImage buffer;

  @Override protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
    ButtonModel model = menuItem.getModel();
    if (model.isArmed() || menuItem instanceof JMenu && model.isSelected()) {
      paintSelectedBackground(g, menuItem, bgColor);
    } else {
      super.paintBackground(g, menuItem, bgColor);
    }
  }

  @SuppressWarnings("PMD.CyclomaticComplexity")
  private void paintSelectedBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
    int width = menuItem.getWidth();
    int height = menuItem.getHeight();
    GraphicsConfiguration config = ((Graphics2D) g).getDeviceConfiguration();
    // VolatileImage buffer = config.createCompatibleVolatileImage(
    //     width, height, Transparency.TRANSLUCENT);
    do {
      int status = VolatileImage.IMAGE_INCOMPATIBLE;
      if (buffer != null) {
        status = buffer.validate(config);
      }
      if (status == VolatileImage.IMAGE_INCOMPATIBLE || status == VolatileImage.IMAGE_RESTORED) {
        if (buffer == null
            || buffer.getWidth() != width || buffer.getHeight() != height
            || status == VolatileImage.IMAGE_INCOMPATIBLE) {
          if (buffer != null) {
            buffer.flush();
          }
          buffer = config.createCompatibleVolatileImage(width, height, Transparency.TRANSLUCENT);
        }
        Graphics2D g2 = buffer.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, width, height);
        g2.setPaintMode(); // g2.setComposite(AlphaComposite.Src);
        g2.setPaint(Color.WHITE);
        g2.fillRoundRect(0, 0, width, height, 8, 8);
        g2.setComposite(AlphaComposite.SrcAtop);
        super.paintBackground(g2, menuItem, bgColor);
        g2.dispose();
      }
    } while (buffer.contentsLost());
    g.drawImage(buffer, 0, 0, menuItem);
  }
}
