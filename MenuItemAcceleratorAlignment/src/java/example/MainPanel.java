package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.plaf.basic.BasicMenuItemUI;
import com.sun.java.swing.plaf.windows.WindowsMenuItemUI;
//import sun.swing.*;

public final class MainPanel {
    private MainPanel() { /* Singleton */ }
    private static JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu0 = new JMenu("Default");
        JMenu menu1 = new JMenu("RightAcc");
        JMenu menu2 = new JMenu("EastAcc");
        //XXX: JMenuItem.setDefaultLocale(Locale.ENGLISH);

        //UIManager.put("MenuItem.acceleratorForeground", menu1.getBackground());
        //UIManager.put("MenuItem.acceleratorSelectionForeground", menu1.getBackground());

        menu0.setMnemonic(KeyEvent.VK_D);
        menu1.setMnemonic(KeyEvent.VK_R);
        menu2.setMnemonic(KeyEvent.VK_E);
        menuBar.add(menu0);
        menuBar.add(menu1);
        menuBar.add(menu2);

        List<JMenuItem> list = new ArrayList<>();
        JMenuItem menuItem = new JMenuItem("mi");
        menuItem.setMnemonic(KeyEvent.VK_N);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
        list.add(menuItem);

        menuItem = new JMenuItem("aaa");
        menuItem.setMnemonic(KeyEvent.VK_1);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, ActionEvent.ALT_MASK));
        list.add(menuItem);

        menuItem = new JMenuItem("bbbbb");
        menuItem.setMnemonic(KeyEvent.VK_2);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, ActionEvent.ALT_MASK | ActionEvent.CTRL_MASK));
        list.add(menuItem);

        menuItem = new JMenuItem("c");
        menuItem.setMnemonic(KeyEvent.VK_3);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, ActionEvent.ALT_MASK | ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
        list.add(menuItem);

        for (JMenuItem mi: list) {
            menu0.add(mi);
            menu1.add(makeMenuItem1(mi));
            menu2.add(makeMenuItem2(mi));
        }
        return menuBar;
    }
    private static JMenuItem makeMenuItem1(JMenuItem mi) {
        JMenuItem menuItem = new JMenuItem(mi.getText()) {
            @Override public void updateUI() {
                super.updateUI();
                //System.out.println(getLocale());
                if (getUI() instanceof WindowsMenuItemUI) {
                    setUI(new RAAWindowsMenuItemUI());
                } else {
                    setUI(new RAABasicMenuItemUI());
                }
                //XXX: setLocale(Locale.JAPAN);
            }
        };
        menuItem.setMnemonic(mi.getMnemonic());
        menuItem.setAccelerator(mi.getAccelerator());
        return menuItem;
    }

    private static JMenuItem makeMenuItem2(JMenuItem mi) {
        final JLabel label = new JLabel(MenuItemUIHelper.getAccText(mi, "+"));
        label.setOpaque(true);
        JMenuItem item = new JMenuItem(mi.getText()) {
//             @Override public Dimension getPreferredSize() {
//                 Dimension d = super.getPreferredSize();
//                 label.setText(MenuItemUIHelper.getAccText(this, "+"));
//                 //d.width += label.getPreferredSize().width;
//                 d.height = Math.max(label.getPreferredSize().height, d.height);
//                 return d;
//             }
//             @Override protected void fireStateChanged() {
//                 super.fireStateChanged();
//                 ButtonModel m = getModel();
//                 if (m.isSelected() || m.isRollover() || m.isArmed()) {
//                     label.setForeground(UIManager.getColor("MenuItem.acceleratorSelectionForeground"));
//                     label.setBackground(UIManager.getColor("MenuItem.selectionBackground"));
//                 } else {
//                     label.setForeground(getForeground());
//                     label.setBackground(getBackground());
//                 }
//             }
            @Override public void updateUI() {
                super.updateUI();
                if (getUI() instanceof WindowsMenuItemUI) {
                    setUI(new WindowsMenuItemUI() {
                        @Override protected void installDefaults() {
                            super.installDefaults();
                            acceleratorForeground = UIManager.getColor("MenuItem.background");
                            acceleratorSelectionForeground = acceleratorForeground;
                        }
                    });
                }
            }
        };

        if (mi.getAccelerator() == null) {
            return item;
        }

        GridBagConstraints c = new GridBagConstraints();
        item.setLayout(new GridBagLayout());
        c.gridheight = 1;
        c.gridwidth  = 1;
        c.gridy = 0;
        c.gridx = 0;
        c.insets = new Insets(0, 0, 0, 4);

        c.weightx = 1d;
        c.fill = GridBagConstraints.HORIZONTAL;
        item.add(Box.createHorizontalGlue(), c);
        c.gridx = 1;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0d;
        c.anchor = GridBagConstraints.EAST;
        item.add(label, c);

        item.setMnemonic(mi.getMnemonic());
        item.setAccelerator(mi.getAccelerator());
        return item;
    }

    public static void main(String... args) {
        //Locale.setDefault(Locale.ENGLISH);
        //java.util.ResourceBundle awtBundle = java.util.ResourceBundle.getBundle(
        //    "sun.awt.resources.awt", sun.util.CoreResourceBundleControl.getRBControlInstance());
        //Locale.setDefault(new Locale("xx"));
        //JMenuItem.setDefaultLocale(Locale.ENGLISH);
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        //frame.getContentPane().add(new MainPanel());
        frame.setJMenuBar(createMenuBar());
        frame.setSize(320, 240);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

//@see javax/swing/plaf/basic/BasicMenuItemUI.java
final class MenuItemUIHelper {
    private MenuItemUIHelper() { /* Singleton */ }
    public static void paintIcon(Graphics g, sun.swing.MenuItemLayoutHelper lh, sun.swing.MenuItemLayoutHelper.LayoutResult lr, Color holdc) {
        if (lh.getIcon() != null) {
            Icon icon;
            ButtonModel model = lh.getMenuItem().getModel();
            if (model.isEnabled()) {
                if (model.isPressed() && model.isArmed()) {
                    icon = lh.getMenuItem().getPressedIcon();
                    if (icon == null) {
                        // Use default icon
                        icon = lh.getMenuItem().getIcon();
                    }
                } else {
                    icon = lh.getMenuItem().getIcon();
                }
            } else {
                icon = lh.getMenuItem().getDisabledIcon();
            }
            if (icon != null) {
                icon.paintIcon(lh.getMenuItem(), g, lr.getIconRect().x, lr.getIconRect().y);
                g.setColor(holdc);
            }
        }
    }

    public static void paintCheckIcon(Graphics g, sun.swing.MenuItemLayoutHelper lh, sun.swing.MenuItemLayoutHelper.LayoutResult lr, Color holdc, Color foreground) {
        if (lh.getCheckIcon() != null) {
            ButtonModel model = lh.getMenuItem().getModel();
            if (model.isArmed() || lh.getMenuItem() instanceof JMenu && model.isSelected()) {
                g.setColor(foreground);
            } else {
                g.setColor(holdc);
            }
            if (lh.useCheckAndArrow()) {
                lh.getCheckIcon().paintIcon(lh.getMenuItem(), g, lr.getCheckRect().x, lr.getCheckRect().y);
            }
            g.setColor(holdc);
        }
    }

    public static void paintAccText(Graphics g, sun.swing.MenuItemLayoutHelper lh, sun.swing.MenuItemLayoutHelper.LayoutResult lr, Color disabledForeground, Color acceleratorForeground, Color acceleratorSelectionForeground) {
        if (!lh.getAccText().equals("")) {
            ButtonModel model = lh.getMenuItem().getModel();
            g.setFont(lh.getAccFontMetrics().getFont());
            if (model.isEnabled()) {
                // *** paint the accText normally
                if (model.isArmed() || lh.getMenuItem() instanceof JMenu && model.isSelected()) {
                    g.setColor(acceleratorSelectionForeground);
                } else {
                    g.setColor(acceleratorForeground);
                }
                sun.swing.SwingUtilities2.drawString(
                    lh.getMenuItem(), g, lh.getAccText(),
                    lh.getViewRect().x + lh.getViewRect().width - lh.getMenuItem().getIconTextGap() - lr.getAccRect().width,
                    lr.getAccRect().y  + lh.getAccFontMetrics().getAscent());
            } else {
                // *** paint the accText disabled
                if (disabledForeground == null) {
                    g.setColor(lh.getMenuItem().getBackground().brighter());
                    sun.swing.SwingUtilities2.drawString(lh.getMenuItem(), g, lh.getAccText(), lr.getAccRect().x, lr.getAccRect().y + lh.getAccFontMetrics().getAscent());
                    g.setColor(lh.getMenuItem().getBackground().darker());
                    sun.swing.SwingUtilities2.drawString(lh.getMenuItem(), g, lh.getAccText(), lr.getAccRect().x - 1, lr.getAccRect().y + lh.getFontMetrics().getAscent() - 1);
                } else {
                    g.setColor(disabledForeground);
                    sun.swing.SwingUtilities2.drawString(lh.getMenuItem(), g, lh.getAccText(), lr.getAccRect().x, lr.getAccRect().y + lh.getAccFontMetrics().getAscent());
                }
            }
        }
    }

    public static void paintArrowIcon(Graphics g, sun.swing.MenuItemLayoutHelper lh, sun.swing.MenuItemLayoutHelper.LayoutResult lr, Color foreground) {
        if (lh.getArrowIcon() != null) {
            ButtonModel model = lh.getMenuItem().getModel();
            if (model.isArmed() || lh.getMenuItem() instanceof JMenu && model.isSelected()) {
                g.setColor(foreground);
            }
            if (lh.useCheckAndArrow()) {
                lh.getArrowIcon().paintIcon(lh.getMenuItem(), g, lr.getArrowRect().x, lr.getArrowRect().y);
            }
        }
    }

    public static void applyInsets(Rectangle rect, Insets insets) {
        if (insets != null) {
            rect.x += insets.left;
            rect.y += insets.top;
            rect.width  -= insets.right  + rect.x;
            rect.height -= insets.bottom + rect.y;
        }
    }

    public static String getAccText(JMenuItem mi, String acceleratorDelimiter) {
        StringBuilder accText = new StringBuilder();
        KeyStroke accelerator = mi.getAccelerator();
        if (accelerator != null) {
            int modifiers = accelerator.getModifiers();
            if (modifiers > 0) {
                accText.append(KeyEvent.getKeyModifiersText(modifiers))
                       .append(acceleratorDelimiter);
            }
            int keyCode = accelerator.getKeyCode();
            if (keyCode == 0) {
                accText.append(accelerator.getKeyChar());
            } else {
                accText.append(KeyEvent.getKeyText(keyCode));
            }
        }
        return accText.toString();
    }
}

class RAAWindowsMenuItemUI extends WindowsMenuItemUI {
    @Override protected void paintMenuItem(Graphics g, JComponent c, Icon checkIcon, Icon arrowIcon, Color background, Color foreground, int defaultTextIconGap) {
        // Save original graphics font and color
        Font holdf = g.getFont();
        Color holdc = g.getColor();

        //System.out.println(defaultTextIconGap);

        JMenuItem mi = (JMenuItem) c;
        g.setFont(mi.getFont());

        Rectangle viewRect = new Rectangle(0, 0, mi.getWidth(), mi.getHeight());
        MenuItemUIHelper.applyInsets(viewRect, mi.getInsets());

        sun.swing.MenuItemLayoutHelper lh = new sun.swing.MenuItemLayoutHelper(
            mi, checkIcon, arrowIcon, viewRect, defaultTextIconGap, "+", //acceleratorDelimiter,
            true, mi.getFont(), acceleratorFont, sun.swing.MenuItemLayoutHelper.useCheckAndArrow(menuItem), getPropertyPrefix());
        sun.swing.MenuItemLayoutHelper.LayoutResult lr = lh.layoutMenuItem();

        paintBackground(g, mi, background);
        MenuItemUIHelper.paintCheckIcon(g, lh, lr, holdc, foreground);
        MenuItemUIHelper.paintIcon(g, lh, lr, holdc);
        paintText(g, lh, lr);
        MenuItemUIHelper.paintAccText(g, lh, lr, disabledForeground, acceleratorForeground, acceleratorSelectionForeground);
        MenuItemUIHelper.paintArrowIcon(g, lh, lr, foreground);

        // Restore original graphics font and color
        g.setColor(holdc);
        g.setFont(holdf);
    }
    private void paintText(Graphics g, sun.swing.MenuItemLayoutHelper lh, sun.swing.MenuItemLayoutHelper.LayoutResult lr) {
        if (!lh.getText().isEmpty()) {
            if (lh.getHtmlView() == null) {
                // Text isn't HTML
                paintText(g, lh.getMenuItem(), lr.getTextRect(), lh.getText());
            } else {
                // Text is HTML
                lh.getHtmlView().paint(g, lr.getTextRect());
            }
        }
    }
}

class RAABasicMenuItemUI extends BasicMenuItemUI {
    @Override protected void paintMenuItem(Graphics g, JComponent c, Icon checkIcon, Icon arrowIcon, Color background, Color foreground, int defaultTextIconGap) {
        // Save original graphics font and color
        Font holdf = g.getFont();
        Color holdc = g.getColor();

        //System.out.println(defaultTextIconGap);

        JMenuItem mi = (JMenuItem) c;
        g.setFont(mi.getFont());

        Rectangle viewRect = new Rectangle(0, 0, mi.getWidth(), mi.getHeight());
        MenuItemUIHelper.applyInsets(viewRect, mi.getInsets());

        sun.swing.MenuItemLayoutHelper lh = new sun.swing.MenuItemLayoutHelper(
            mi, checkIcon, arrowIcon, viewRect, defaultTextIconGap, "+", //acceleratorDelimiter,
            true, mi.getFont(), acceleratorFont, sun.swing.MenuItemLayoutHelper.useCheckAndArrow(menuItem), getPropertyPrefix());
        sun.swing.MenuItemLayoutHelper.LayoutResult lr = lh.layoutMenuItem();

        paintBackground(g, mi, background);
        MenuItemUIHelper.paintCheckIcon(g, lh, lr, holdc, foreground);
        MenuItemUIHelper.paintIcon(g, lh, lr, holdc);
        paintText(g, lh, lr);
        MenuItemUIHelper.paintAccText(g, lh, lr, disabledForeground, acceleratorForeground, acceleratorSelectionForeground);
        MenuItemUIHelper.paintArrowIcon(g, lh, lr, foreground);

        // Restore original graphics font and color
        g.setColor(holdc);
        g.setFont(holdf);
    }
    private void paintText(Graphics g, sun.swing.MenuItemLayoutHelper lh, sun.swing.MenuItemLayoutHelper.LayoutResult lr) {
        if (!lh.getText().isEmpty()) {
            if (lh.getHtmlView() == null) {
                // Text isn't HTML
                paintText(g, lh.getMenuItem(), lr.getTextRect(), lh.getText());
            } else {
                // Text is HTML
                lh.getHtmlView().paint(g, lr.getTextRect());
            }
        }
    }
}
