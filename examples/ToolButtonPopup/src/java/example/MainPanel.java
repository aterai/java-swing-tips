// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.net.URL;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPopupMenu pop1 = new JPopupMenu();
    pop1.add("000");
    pop1.add("11111");
    pop1.addSeparator();
    pop1.add("2222222");

    JPopupMenu pop2 = new JPopupMenu();
    pop2.add("33333333333333");
    pop2.addSeparator();
    pop2.add("4444");
    pop2.add("5555555555");

    JToolBar toolBar = new JToolBar();
    toolBar.add(MenuToggleButton.makePopupButton(pop1, "Text", null));
    Component rigid = Box.createRigidArea(new Dimension(5, 5));
    toolBar.add(rigid);
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    URL url = cl.getResource("example/ei0021-16.png");
    Icon icon = url == null ? UIManager.getIcon("html.missingImage") : new ImageIcon(url);
    toolBar.add(MenuToggleButton.makePopupButton(pop2, "", icon));
    toolBar.add(rigid);
    toolBar.add(MenuToggleButton.makePopupButton(pop2, "Icon+Text", icon));
    toolBar.add(Box.createGlue());

    add(toolBar, BorderLayout.NORTH);
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

class MenuArrowIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(Color.BLACK);
    g2.drawLine(2, 3, 6, 3);
    g2.drawLine(3, 4, 5, 4);
    g2.drawLine(4, 5, 4, 5);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 9;
  }

  @Override public int getIconHeight() {
    return 9;
  }
}

class MenuToggleButton extends JToggleButton {
  private static final Icon ARROW_ICON = new MenuArrowIcon();

  protected MenuToggleButton(String text, Icon icon) {
    super(text, icon);
  }

  public static AbstractButton makePopupButton(JPopupMenu popup, String title, Icon icon) {
    AbstractButton button = new MenuToggleButton(title, icon);
    button.addActionListener(e -> {
      Component b = (Component) e.getSource();
      popup.show(b, 0, b.getHeight());
    });
    popup.addPopupMenuListener(new PopupMenuListener() {
      @Override public void popupMenuCanceled(PopupMenuEvent e) {
        /* not needed */
      }

      @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        /* not needed */
      }

      @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        button.setSelected(false);
      }
    });
    return button;
  }

  @Override public void updateUI() {
    super.updateUI();
    setFocusable(false);
    setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4 + ARROW_ICON.getIconWidth()));
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Rectangle r = SwingUtilities.calculateInnerArea(this, null);
    int x = r.x + r.width;
    int y = r.y + (r.height - ARROW_ICON.getIconHeight()) / 2;
    ARROW_ICON.paintIcon(this, g, x, y);
  }
}
