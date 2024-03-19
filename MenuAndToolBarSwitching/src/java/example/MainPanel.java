// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JMenuBar mainMenuBar = makeMenuBar();
    JButton button = makeHamburgerMenuButton(mainMenuBar);
    JMenuBar wrappingMenuBar = new JMenuBar();
    wrappingMenuBar.add(makeToolBar(button));
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(wrappingMenuBar));

    PopupMenuListener switchHandler = new PopupMenuListener() {
      @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        // not need
      }

      @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        EventQueue.invokeLater(() -> {
          if (MenuSelectionManager.defaultManager().getSelectedPath().length == 0) {
            getRootPane().setJMenuBar(wrappingMenuBar);
          }
        });
      }

      @Override public void popupMenuCanceled(PopupMenuEvent e) {
        EventQueue.invokeLater(() -> getRootPane().setJMenuBar(wrappingMenuBar));
      }
    };
    MouseAdapter popupKeeper = new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        Component c = e.getComponent();
        if (c instanceof JMenu) {
          ((JMenu) c).doClick();
        }
      }
    };
    for (int i = 0; i < mainMenuBar.getMenuCount(); i++) {
      JMenu menu = mainMenuBar.getMenu(i);
      menu.addMouseListener(popupKeeper);
      menu.getPopupMenu().addPopupMenuListener(switchHandler);
    }
    add(new JScrollPane(new JTextArea()));
    setPreferredSize(new Dimension(320, 240));
  }

  private JButton makeHamburgerMenuButton(JMenuBar menuBar) {
    JButton button = new JButton("Ξ") {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = menuBar.getMenu(0).getPreferredSize().height;
        return d;
      }

      @Override public void updateUI() {
        super.updateUI();
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
      }
    };
    button.addActionListener(e -> {
      getRootPane().setJMenuBar(menuBar);
      getRootPane().revalidate();
      EventQueue.invokeLater(() -> menuBar.getMenu(0).doClick());
    });
    button.setMnemonic('\\');
    button.setToolTipText("Main Menu(Alt+\\)");
    return button;
  }

  private static JToolBar makeToolBar(JButton button) {
    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);
    toolBar.setOpaque(false);
    toolBar.add(button);
    toolBar.add(Box.createHorizontalStrut(5));
    toolBar.add(new JLabel("<- Switch to JMenuBar"));
    JCheckBox check = new JCheckBox("JCheckBox");
    check.setOpaque(false);
    toolBar.add(check);
    return toolBar;
  }

  private static JMenuBar makeMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    menuBar.add(makeMenu("JMenu1"));
    menuBar.add(makeMenu("JMenu2"));
    menuBar.add(makeMenu("JMenu3"));
    menuBar.add(makeMenu("JMenu4"));
    menuBar.add(makeMenu("JMenu5"));
    return menuBar;
  }

  private static JMenu makeMenu(String title) {
    JMenu menu = new JMenu(title);
    menu.add("1");
    menu.add("22");
    menu.add("333");
    menu.addSeparator();
    menu.add("4444");
    menu.add("55555");
    JMenu sub = new JMenu("sub");
    sub.add("666");
    sub.add("777");
    menu.add(sub);
    return menu;
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
