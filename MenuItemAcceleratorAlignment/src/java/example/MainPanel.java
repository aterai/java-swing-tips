// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsMenuItemUI;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.plaf.basic.BasicMenuItemUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    setPreferredSize(new Dimension(320, 240));
    add(new JScrollPane(new JTextArea()));
  }

  private static JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    JMenu menu0 = new JMenu("Default");
    JMenu menu1 = new JMenu("RightAcc");
    // JMenu menu2 = new JMenu("EastAcc");
    // XXX: JMenuItem.setDefaultLocale(Locale.ENGLISH);

    // UIManager.put("MenuItem.acceleratorForeground", menu1.getBackground());
    // UIManager.put("MenuItem.acceleratorSelectionForeground", menu1.getBackground());

    menu0.setMnemonic(KeyEvent.VK_D);
    menu1.setMnemonic(KeyEvent.VK_R);
    // menu2.setMnemonic(KeyEvent.VK_E);
    menuBar.add(menu0);
    menuBar.add(menu1);
    // menuBar.add(menu2);

    List<JMenuItem> list = new ArrayList<>();
    JMenuItem menuItem = new JMenuItem("mi");
    menuItem.setMnemonic(KeyEvent.VK_N);
    menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.ALT_DOWN_MASK));
    list.add(menuItem);

    menuItem = new JMenuItem("aaa");
    menuItem.setMnemonic(KeyEvent.VK_1);
    menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, InputEvent.ALT_DOWN_MASK));
    list.add(menuItem);

    menuItem = new JMenuItem("bbbbb");
    menuItem.setMnemonic(KeyEvent.VK_2);
    int msk2 = InputEvent.ALT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK;
    menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, msk2));
    list.add(menuItem);

    menuItem = new JMenuItem("c");
    menuItem.setMnemonic(KeyEvent.VK_3);
    int msk3 = InputEvent.ALT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK;
    menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, msk3));
    list.add(menuItem);

    for (JMenuItem mi: list) {
      menu0.add(mi);
      menu1.add(makeMenuItem1(mi));
      // menu2.add(makeMenuItem2(mi));
    }
    return menuBar;
  }

  private static JMenuItem makeMenuItem1(JMenuItem mi) {
    JMenuItem menuItem = new JMenuItem(mi.getText()) {
      @Override public void updateUI() {
        super.updateUI();
        // System.out.println(getLocale());
        if (getUI() instanceof WindowsMenuItemUI) {
          setUI(new RaaWindowsMenuItemUI());
        } else {
          setUI(new RaaBasicMenuItemUI());
        }
        // XXX: setLocale(Locale.JAPAN);
      }
    };
    menuItem.setMnemonic(mi.getMnemonic());
    menuItem.setAccelerator(mi.getAccelerator());
    return menuItem;
  }

  // // TEST: work Windows 7 only?
  // private static JMenuItem makeMenuItem2(JMenuItem mi) {
  //   JLabel label = new JLabel(MenuItemHelper.getAccText(mi, "+"));
  //   label.setOpaque(true);
  //   JMenuItem item = new JMenuItem(mi.getText()) {
  //     // @Override public Dimension getPreferredSize() {
  //     //   Dimension d = super.getPreferredSize();
  //     //   label.setText(MenuItemHelper.getAccText(this, "+"));
  //     //   // d.width += label.getPreferredSize().width;
  //     //   d.height = Math.max(label.getPreferredSize().height, d.height);
  //     //   return d;
  //     // }
  //     // @Override protected void fireStateChanged() {
  //     //   super.fireStateChanged();
  //     //   ButtonModel m = getModel();
  //     //   if (m.isSelected() || m.isRollover() || m.isArmed()) {
  //     //     label.setForeground(UIManager.getColor("MenuItem.acceleratorSelectionForeground"));
  //     //     label.setBackground(UIManager.getColor("MenuItem.selectionBackground"));
  //     //   } else {
  //     //     label.setForeground(getForeground());
  //     //     label.setBackground(getBackground());
  //     //   }
  //     // }
  //     @Override public void updateUI() {
  //       super.updateUI();
  //       if (getUI() instanceof WindowsMenuItemUI) {
  //         setUI(new WindowsMenuItemUI() {
  //           @Override protected void installDefaults() {
  //             super.installDefaults();
  //             acceleratorForeground = UIManager.getColor("MenuItem.background");
  //             acceleratorSelectionForeground = acceleratorForeground;
  //           }
  //         });
  //       }
  //     }
  //   };
  //
  //   if (mi.getAccelerator() == null) {
  //     return item;
  //   }
  //
  //   item.setLayout(new GridBagLayout());
  //   GridBagConstraints c = new GridBagConstraints();
  //   c.anchor = GridBagConstraints.LINE_END;
  //   c.insets = new Insets(0, 0, 0, 4);
  //
  //   c.weightx = 1d;
  //   c.fill = GridBagConstraints.HORIZONTAL;
  //   item.add(Box.createHorizontalGlue(), c);
  //
  //   c.weightx = 0d;
  //   c.fill = GridBagConstraints.NONE;
  //   item.add(label, c);
  //
  //   item.setMnemonic(mi.getMnemonic());
  //   item.setAccelerator(mi.getAccelerator());
  //   return item;
  // }

  public static void main(String... args) {
    // Locale.setDefault(Locale.ENGLISH);
    // ResourceBundle awtBundle = ResourceBundle.getBundle(
    //   "sun.awt.resources.awt", sun.util.CoreResourceBundleControl.getRBControlInstance());
    // Locale.setDefault(new Locale("xx"));
    // JMenuItem.setDefaultLocale(Locale.ENGLISH);
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.setJMenuBar(createMenuBar());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

// @see javax/swing/plaf/basic/BasicMenuItemUI.java
final class MenuItemHelper {
  private MenuItemHelper() { /* Singleton */ }

  public static void paintIcon(Graphics g, sun.swing.MenuItemLayoutHelper lh,
                               sun.swing.MenuItemLayoutHelper.LayoutResult lr) { // , Color holdc) {
    Optional.ofNullable(lh.getIcon()).ifPresent(i -> {
      Icon icon;
      JMenuItem menuItem = lh.getMenuItem();
      ButtonModel model = menuItem.getModel();
      if (model.isEnabled()) {
        if (model.isPressed() && model.isArmed()) {
          icon = menuItem.getPressedIcon();
          if (Objects.isNull(icon)) {
            // Use default icon
            icon = menuItem.getIcon();
          }
        } else {
          icon = menuItem.getIcon();
        }
      } else {
        icon = menuItem.getDisabledIcon();
      }
      if (Objects.nonNull(icon)) {
        icon.paintIcon(menuItem, g, lr.getIconRect().x, lr.getIconRect().y);
        // g.setColor(holdc);
      }
    });
  }

  @SuppressWarnings("checkstyle:linelength")
  public static void paintCheckIcon(Graphics g, sun.swing.MenuItemLayoutHelper lh, sun.swing.MenuItemLayoutHelper.LayoutResult lr, Color holdc, Color foreground) {
    Optional.ofNullable(lh.getCheckIcon()).ifPresent(checkIcon -> {
      JMenuItem menuItem = lh.getMenuItem();
      ButtonModel model = menuItem.getModel();
      if (model.isArmed()) {
        g.setColor(foreground);
      } else if (menuItem instanceof JMenu && model.isSelected()) {
        g.setColor(foreground);
      } else {
        g.setColor(holdc);
      }
      if (lh.useCheckAndArrow()) {
        checkIcon.paintIcon(menuItem, g, lr.getCheckRect().x, lr.getCheckRect().y);
      }
      // g.setColor(holdc);
    });
  }

  @SuppressWarnings("checkstyle:linelength")
  public static void paintAccText(Graphics g, sun.swing.MenuItemLayoutHelper lh, sun.swing.MenuItemLayoutHelper.LayoutResult lr, Color disabledFg, Color accFg, Color accSelFg) {
    String text = lh.getAccText();
    if (text.isEmpty()) {
      return;
    }
    Rectangle viewRect = lh.getViewRect();
    Rectangle accRect = lr.getAccRect();
    int ascent = lh.getAccFontMetrics().getAscent();
    JMenuItem menuItem = lh.getMenuItem();
    ButtonModel model = menuItem.getModel();
    g.setFont(lh.getAccFontMetrics().getFont());
    if (model.isEnabled()) {
      // *** paint the accText normally
      if (model.isArmed()) {
        g.setColor(accSelFg);
      } else if (menuItem instanceof JMenu && model.isSelected()) {
        g.setColor(accSelFg);
      } else {
        g.setColor(accFg);
      }
      drawString(menuItem, g, text, viewRect.x + viewRect.width - menuItem.getIconTextGap() - accRect.width, accRect.y + ascent);
    } else {
      // *** paint the accText disabled
      if (Objects.nonNull(disabledFg)) {
        g.setColor(disabledFg);
        drawString(menuItem, g, text, accRect.x, accRect.y + ascent);
      } else {
        g.setColor(menuItem.getBackground().brighter());
        drawString(menuItem, g, text, accRect.x, accRect.y + ascent);
        g.setColor(menuItem.getBackground().darker());
        drawString(menuItem, g, text, accRect.x - 1, accRect.y + lh.getFontMetrics().getAscent() - 1);
      }
    }
  }

  private static void drawString(JComponent c, Graphics g, String text, int x, int y) {
    sun.swing.SwingUtilities2.drawString(c, g, text, x, y);

    // JDK-8132119 Provide public API for text related methods in SwingUtilities2 - Java Bug System
    // https://bugs.openjdk.java.net/browse/JDK-8132119
    // Java 9
    // // import javax.swing.plaf.basic.BasicGraphicsUtils;
    // BasicGraphicsUtils.drawString(c, (Graphics2D) g, text, x, y);
  }

  @SuppressWarnings("checkstyle:linelength")
  public static void paintArrowIcon(Graphics g, sun.swing.MenuItemLayoutHelper lh, sun.swing.MenuItemLayoutHelper.LayoutResult lr, Color foreground) {
    Optional.ofNullable(lh.getArrowIcon()).ifPresent(arrowIcon -> {
      JMenuItem menuItem = lh.getMenuItem();
      ButtonModel model = menuItem.getModel();
      if (model.isArmed()) {
        g.setColor(foreground);
      } else if (menuItem instanceof JMenu && model.isSelected()) {
        g.setColor(foreground);
      }
      if (lh.useCheckAndArrow()) {
        arrowIcon.paintIcon(menuItem, g, lr.getArrowRect().x, lr.getArrowRect().y);
      }
    });
  }

  public static void applyInsets(Rectangle rect, Insets insets) {
    Optional.ofNullable(insets).ifPresent(i -> {
      rect.x += i.left;
      rect.y += i.top;
      rect.width -= i.right + rect.x;
      rect.height -= i.bottom + rect.y;
    });
  }

  public static String getAccText(JMenuItem mi, String acceleratorDelimiter) {
    StringBuilder accText = new StringBuilder();
    // KeyStroke accelerator = mi.getAccelerator();
    Optional.ofNullable(mi.getAccelerator()).ifPresent(accelerator -> {
      int modifiers = accelerator.getModifiers();
      if (modifiers > 0) {
        // accText.append(KeyEvent.getKeyModifiersText(modifiers)).append(acceleratorDelimiter);
        accText.append(InputEvent.getModifiersExText(modifiers)).append(acceleratorDelimiter);
      }
      int keyCode = accelerator.getKeyCode();
      if (keyCode == 0) {
        accText.append(accelerator.getKeyChar());
      } else {
        accText.append(KeyEvent.getKeyText(keyCode));
      }
    });
    return accText.toString();
  }
}

class RaaWindowsMenuItemUI extends WindowsMenuItemUI {
  @Override protected void paintMenuItem(Graphics g, JComponent c, Icon checkIcon, Icon arrowIcon, Color background, Color foreground, int defaultTextIconGap) {
    // // Save original graphics font and color
    // Font holdf = g.getFont();
    // Color holdc = g.getColor();

    Graphics2D g2 = (Graphics2D) g.create();
    // System.out.println(defaultTextIconGap);

    JMenuItem mi = (JMenuItem) c;
    g2.setFont(mi.getFont());

    Rectangle viewRect = new Rectangle(mi.getWidth(), mi.getHeight());
    MenuItemHelper.applyInsets(viewRect, mi.getInsets());

    sun.swing.MenuItemLayoutHelper lh = new sun.swing.MenuItemLayoutHelper(
        mi, checkIcon, arrowIcon, viewRect, defaultTextIconGap, "+", // acceleratorDelimiter,
        true, mi.getFont(), acceleratorFont,
        sun.swing.MenuItemLayoutHelper.useCheckAndArrow(menuItem), getPropertyPrefix());
    sun.swing.MenuItemLayoutHelper.LayoutResult lr = lh.layoutMenuItem();

    paintBackground(g2, mi, background);
    MenuItemHelper.paintCheckIcon(g2, lh, lr, g.getColor(), foreground);
    MenuItemHelper.paintIcon(g2, lh, lr); // , g.getColor());
    paintText(g2, lh, lr);
    MenuItemHelper.paintAccText(g2, lh, lr, disabledForeground, acceleratorForeground, acceleratorSelectionForeground);
    MenuItemHelper.paintArrowIcon(g2, lh, lr, foreground);

    // // Restore original graphics font and color
    // g.setColor(holdc);
    // g.setFont(holdf);
  }

  private void paintText(Graphics g, sun.swing.MenuItemLayoutHelper lh, sun.swing.MenuItemLayoutHelper.LayoutResult lr) {
    if (!lh.getText().isEmpty()) {
      if (Objects.nonNull(lh.getHtmlView())) {
        // Text is HTML
        lh.getHtmlView().paint(g, lr.getTextRect());
      } else {
        // Text isn't HTML
        paintText(g, menuItem, lr.getTextRect(), lh.getText());
      }
    }
  }
}

class RaaBasicMenuItemUI extends BasicMenuItemUI {
  @Override protected void paintMenuItem(Graphics g, JComponent c, Icon checkIcon, Icon arrowIcon, Color background, Color foreground, int defaultTextIconGap) {
    // // Save original graphics font and color
    // Font holdf = g.getFont();
    // Color holdc = g.getColor();

    Graphics2D g2 = (Graphics2D) g.create();
    // System.out.println(defaultTextIconGap);

    JMenuItem mi = (JMenuItem) c;
    g2.setFont(mi.getFont());

    Rectangle viewRect = new Rectangle(mi.getWidth(), mi.getHeight());
    MenuItemHelper.applyInsets(viewRect, mi.getInsets());

    sun.swing.MenuItemLayoutHelper lh = new sun.swing.MenuItemLayoutHelper(
        mi, checkIcon, arrowIcon, viewRect, defaultTextIconGap, "+", // acceleratorDelimiter,
        true, mi.getFont(), acceleratorFont,
        sun.swing.MenuItemLayoutHelper.useCheckAndArrow(menuItem), getPropertyPrefix());
    sun.swing.MenuItemLayoutHelper.LayoutResult lr = lh.layoutMenuItem();

    paintBackground(g2, mi, background);
    MenuItemHelper.paintCheckIcon(g2, lh, lr, g.getColor(), foreground);
    MenuItemHelper.paintIcon(g2, lh, lr); // , g.getColor());
    paintText(g2, lh, lr);
    MenuItemHelper.paintAccText(g2, lh, lr, disabledForeground, acceleratorForeground, acceleratorSelectionForeground);
    MenuItemHelper.paintArrowIcon(g2, lh, lr, foreground);

    // // Restore original graphics font and color
    // g.setColor(holdc);
    // g.setFont(holdf);
  }

  private void paintText(Graphics g, sun.swing.MenuItemLayoutHelper lh, sun.swing.MenuItemLayoutHelper.LayoutResult lr) {
    if (!lh.getText().isEmpty()) {
      if (Objects.nonNull(lh.getHtmlView())) {
        // Text is HTML
        lh.getHtmlView().paint(g, lr.getTextRect());
      } else {
        // Text isn't HTML
        paintText(g, lh.getMenuItem(), lr.getTextRect(), lh.getText());
      }
    }
  }
}
