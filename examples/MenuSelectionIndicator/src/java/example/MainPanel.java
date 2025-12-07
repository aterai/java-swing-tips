// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JMenuBar menuBar = MenuBarUtils.initMenuBar(new SelectionIndicatorMenuBar());
    menuBar.add(LookAndFeelUtils.createLookAndFeelMenu(), 2);
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(menuBar));

    JDesktopPane desktop = new JDesktopPane();
    JInternalFrame frame = new JInternalFrame("JInternalFrame");
    frame.setJMenuBar(MenuBarUtils.initMenuBar(new SelectionHighlightMenuBar()));
    frame.setBounds(50, 50, 240, 120);
    desktop.add(frame);
    frame.setVisible(true);

    JMenuBar menuBar3 = MenuBarUtils.initMenuBar(new JMenuBar());
    add(desktop);
    add(new JLayer<>(menuBar3, new MenuHighlightLayerUI()), BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
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

class SelectionIndicatorMenuBar extends JMenuBar {
  private static final Color SELECTION_COLOR = new Color(0x00_AA_FF);
  private static final int SZ = 3;
  private final Rectangle rect = new Rectangle();
  private transient ChangeListener listener;

  @Override public void updateUI() {
    MenuSelectionManager manager = MenuSelectionManager.defaultManager();
    manager.removeChangeListener(listener);
    super.updateUI();
    Border inside = BorderFactory.createEmptyBorder(SZ + 1, 0, 0, 0);
    Border outside = UIManager.getBorder("MenuBar.border");
    Border border = BorderFactory.createCompoundBorder(outside, inside);
    setBorder(border);
    listener = this::updateTopLevelMenuBorder;
    manager.addChangeListener(listener);
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (!rect.isEmpty()) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setPaint(SELECTION_COLOR);
      g2.fillRect(rect.x, rect.y - SZ, rect.width, SZ);
      g2.dispose();
    }
  }

  private void updateTopLevelMenuBorder(ChangeEvent e) {
    Object o = e.getSource();
    rect.setSize(0, 0);
    MenuElement[] p = ((MenuSelectionManager) o).getSelectedPath();
    if (p != null && p.length > 1 && Objects.equals(this, p[0].getComponent())) {
      updateMenuBorder(p[1].getComponent());
    }
    repaint();
  }

  private void updateMenuBorder(Component c) {
    if (c instanceof JMenu && ((JMenu) c).isTopLevelMenu()) {
      JMenu menu = (JMenu) c;
      ButtonModel m = menu.getModel();
      if (m.isArmed() || m.isPressed() || m.isSelected()) {
        rect.setBounds(menu.getBounds());
      }
    }
  }
}

class SelectionHighlightMenuBar extends JMenuBar {
  private static final Color ALPHA_ZERO = new Color(0x0, true);
  private static final Color SELECTION_COLOR = new Color(0x00_AA_FF);
  private static final int SZ = 3;
  private transient ChangeListener listener;

  @Override public void updateUI() {
    MenuSelectionManager manager = MenuSelectionManager.defaultManager();
    manager.removeChangeListener(listener);
    super.updateUI();
    listener = e -> updateTopLevelMenuBorder();
    manager.addChangeListener(listener);
    EventQueue.invokeLater(this::updateTopLevelMenuBorder);
  }

  private void updateTopLevelMenuBorder() {
    for (MenuElement me : getSubElements()) {
      updateMenuBorder(me.getComponent());
    }
  }

  private void updateMenuBorder(Component c) {
    if (c instanceof JMenu) {
      JMenu menu = (JMenu) c;
      if (menu.isTopLevelMenu() && menu.getParent().equals(this)) {
        ButtonModel model = menu.getModel();
        boolean b = model.isArmed() || model.isPressed() || model.isSelected();
        Color color = b ? SELECTION_COLOR : ALPHA_ZERO;
        Border inside = UIManager.getBorder("Menu.border");
        Border outside = BorderFactory.createMatteBorder(0, 0, SZ, 0, color);
        menu.setBorder(BorderFactory.createCompoundBorder(outside, inside));
      }
    }
  }
}

class MenuHighlightLayerUI extends LayerUI<JMenuBar> {
  private static final Color SELECTION_COLOR = new Color(0x00_AA_FF);
  private static final int SZ = 3;
  private final Rectangle rect = new Rectangle();

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(
          AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
    super.uninstallUI(c);
  }

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (c instanceof JLayer && !rect.isEmpty()) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setPaint(SELECTION_COLOR);
      g2.fillRect(rect.x, rect.y + rect.height - SZ, rect.width, SZ);
      g2.dispose();
    }
  }

  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JMenuBar> l) {
    super.processMouseEvent(e, l);
    if (e.getID() == MouseEvent.MOUSE_EXITED) {
      rect.setSize(0, 0);
    }
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JMenuBar> l) {
    super.processMouseMotionEvent(e, l);
    Component c = e.getComponent();
    if (c instanceof JMenu) {
      rect.setBounds(c.getBounds());
    } else {
      rect.setSize(0, 0);
    }
  }
}

final class MenuBarUtils {
  private MenuBarUtils() {
    /* Singleton */
  }

  public static JMenuBar initMenuBar(JMenuBar menuBar) {
    menuBar.add(createFileMenu());
    menuBar.add(createEditMenu());
    menuBar.add(Box.createGlue());
    menuBar.add(createHelpMenu());
    return menuBar;
  }

  private static JMenu createFileMenu() {
    JMenu menu = new JMenu("File");
    menu.setMnemonic(KeyEvent.VK_F);
    menu.add("New").setMnemonic(KeyEvent.VK_N);
    menu.add("Open").setMnemonic(KeyEvent.VK_O);
    return menu;
  }

  private static JMenu createEditMenu() {
    JMenu menu = new JMenu("Edit");
    menu.setMnemonic(KeyEvent.VK_E);
    menu.add("Cut").setMnemonic(KeyEvent.VK_T);
    menu.add("Copy").setMnemonic(KeyEvent.VK_C);
    menu.add("Paste").setMnemonic(KeyEvent.VK_P);
    menu.add("Delete").setMnemonic(KeyEvent.VK_D);
    return menu;
  }

  private static JMenu createHelpMenu() {
    JMenu menu = new JMenu("Help");
    menu.setMnemonic(KeyEvent.VK_H);
    menu.add("About").setMnemonic(KeyEvent.VK_A);
    menu.add("Version").setMnemonic(KeyEvent.VK_V);
    return menu;
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