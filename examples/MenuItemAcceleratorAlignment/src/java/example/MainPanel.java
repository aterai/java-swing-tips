// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(createMenuBar()));
    add(new JScrollPane(new JTextArea()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JMenuBar createMenuBar() {
    // Default: the accelerator text alignment depends on the LookAndFeel
    JMenu defaultMenu = new JMenu("Default");
    defaultMenu.setMnemonic(KeyEvent.VK_D);

    // RightAlign: the accelerator text is always right-aligned
    JMenu rightAlignedMenu = new JMenu("RightAlign");
    rightAlignedMenu.setMnemonic(KeyEvent.VK_R);

    addMenuItems(defaultMenu, rightAlignedMenu, "MenuItem", KeyEvent.VK_N, "alt N");
    addMenuItems(defaultMenu, rightAlignedMenu, "aaa", KeyEvent.VK_1, "alt ESCAPE");
    addMenuItems(defaultMenu, rightAlignedMenu, "12345", KeyEvent.VK_2, "alt ctrl SPACE");
    addMenuItems(defaultMenu, rightAlignedMenu, "c", KeyEvent.VK_3, "alt ctrl shift BACK_SPACE");

    JMenuBar menuBar = new JMenuBar();
    menuBar.add(defaultMenu);
    menuBar.add(rightAlignedMenu);
    menuBar.add(LookAndFeelUtils.createLookAndFeelMenu());
    return menuBar;
  }

  // Add menu items that share the same accelerator to both menus for comparison
  private static void addMenuItems(
      JMenu defaultMenu, JMenu rightAlignedMenu, String text, int mnemonic, String keyStroke) {
    KeyStroke accelerator = KeyStroke.getKeyStroke(keyStroke);

    JMenuItem defaultItem = new JMenuItem(text);
    defaultItem.setMnemonic(mnemonic);
    defaultItem.setAccelerator(accelerator);
    defaultMenu.add(defaultItem);

    JMenuItem rightAlignedItem = new RightAlignedAcceleratorItem(text, accelerator);
    rightAlignedItem.setMnemonic(mnemonic);
    rightAlignedMenu.add(rightAlignedItem);
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

// A JMenuItem that paints its accelerator text right-aligned without using any
// internal API such as sun.swing.MenuItemLayoutHelper.
class RightAlignedAcceleratorItem extends JMenuItem {
  private static final String ACTION_KEY = "doClick";
  private final KeyStroke accelerator;

  protected RightAlignedAcceleratorItem(String text, KeyStroke accelerator) {
    super(text);
    // JMenuItem#setAccelerator(...) is intentionally not called, so the
    // LookAndFeel never paints the accelerator text by itself.
    this.accelerator = accelerator;
    // The binding is registered in the component's own InputMap/ActionMap,
    // which survive updateUI() (a LookAndFeel only replaces their UI parent
    // maps), so registering it once here is enough.
    initAcceleratorBinding();
  }

  // JMenuBar#processKeyBinding(...) searches the whole menu hierarchy
  // recursively, so a WHEN_IN_FOCUSED_WINDOW binding also works for a
  // JMenuItem inside a JPopupMenu that is not showing.
  private void initAcceleratorBinding() {
    if (accelerator == null) {
      return;
    }
    getActionMap().put(ACTION_KEY, new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        doClick(0);
      }
    });
    getInputMap(WHEN_IN_FOCUSED_WINDOW).put(accelerator, ACTION_KEY);
  }

  // @see javax.swing.plaf.basic.BasicMenuItemUI#getAcceleratorText()
  private String getAcceleratorText() {
    StringBuilder buf = new StringBuilder();
    if (Objects.nonNull(accelerator)) {
      String delimiter = Optional
          .ofNullable(UIManager.getString("MenuItem.acceleratorDelimiter"))
          .orElse("+");
      int modifiers = accelerator.getModifiers();
      if (modifiers > 0) {
        buf.append(InputEvent.getModifiersExText(modifiers)).append(delimiter);
      }
      int keyCode = accelerator.getKeyCode();
      if (keyCode == 0) {
        buf.append(accelerator.getKeyChar());
      } else {
        buf.append(KeyEvent.getKeyText(keyCode));
      }
    }
    return buf.toString();
  }

  private Font getAcceleratorFont() {
    return Optional
        .ofNullable(UIManager.getFont("MenuItem.acceleratorFont"))
        .orElseGet(this::getFont);
  }

  // NimbusLookAndFeel does not define MenuItem.acceleratorForeground and so on,
  // so fall back to the Synth style keys and then to the item's own foreground.
  private Color getAcceleratorColor() {
    ButtonModel model = getModel();
    Color color;
    if (model.isEnabled()) {
      color = model.isArmed() || isSelected()
          ? getUiColor("MenuItem.acceleratorSelectionForeground",
              "MenuItem[MouseOver].textForeground")
          : getUiColor("MenuItem.acceleratorForeground", "MenuItem[Enabled].textForeground");
    } else {
      color = getUiColor("MenuItem.disabledForeground", "MenuItem[Disabled].textForeground");
    }
    return Optional.ofNullable(color).orElseGet(this::getForeground);
  }

  private static Color getUiColor(String basicKey, String synthKey) {
    return Optional
        .ofNullable(UIManager.getColor(basicKey))
        .orElseGet(() -> UIManager.getColor(synthKey));
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    String txt = getAcceleratorText();
    if (Objects.nonNull(d) && !txt.isEmpty()) {
      // Reserve the room the LookAndFeel would have used for the accelerator
      d.width += getIconTextGap() + getFontMetrics(getAcceleratorFont()).stringWidth(txt);
    }
    return d;
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    String txt = getAcceleratorText();
    if (txt.isEmpty()) {
      return;
    }
    Graphics2D g2 = (Graphics2D) g.create();
    Object hints = Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
    if (hints instanceof Map<?, ?>) {
      g2.addRenderingHints((Map<?, ?>) hints);
    }
    g2.setFont(getAcceleratorFont());
    g2.setColor(getAcceleratorColor());
    FontMetrics fm = g2.getFontMetrics();
    Insets i = getInsets();
    int gap = getIconTextGap();
    // ComponentOrientation.RIGHT_TO_LEFT is also taken into account
    int x = getComponentOrientation().isLeftToRight()
        ? getWidth() - i.right - gap - fm.stringWidth(txt)
        : i.left + gap;
    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
    g2.drawString(txt, x, y);
    g2.dispose();
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
      AbstractButton b = createButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton createButton(UIManager.LookAndFeelInfo info) {
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
