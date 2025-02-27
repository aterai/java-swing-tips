// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    AbstractButton button = MenuToggleButton.makePopupButton(makePopup(), "JToggleButton", null);

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    box.add(Box.createHorizontalGlue());
    box.add(button);
    box.add(Box.createHorizontalStrut(4));
    box.add(new JLabel("JFrame Footer"));
    box.add(Box.createHorizontalStrut(16));

    add(box, BorderLayout.SOUTH);
    add(new JScrollPane(new JTree()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JPopupMenu makePopup() {
    JPopupMenu popup = new JPopupMenu();
    popup.add(new PopupMenuHeader("Header"));
    popup.add("JMenuItem");
    popup.addSeparator();
    popup.add(new JCheckBoxMenuItem("JCheckBoxMenuItem"));
    popup.add(new JRadioButtonMenuItem("JRadioButtonMenuItem"));
    JMenu menu = new JMenu("JMenu");
    menu.add("Sub JMenuItem 1");
    menu.add("Sub JMenuItem 2");
    popup.add(menu);
    return popup;
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

class PopupMenuHeader extends JLabel {
  private transient MouseAdapter listener;

  protected PopupMenuHeader(String text) {
    super(text, CENTER);
  }

  @Override public void updateUI() {
    removeMouseListener(listener);
    removeMouseMotionListener(listener);
    super.updateUI();
    listener = new PopupHeaderMouseListener();
    addMouseListener(listener);
    addMouseMotionListener(listener);
    // header.setAlignmentX(Component.CENTER_ALIGNMENT);
    setOpaque(true);
    setBackground(Color.LIGHT_GRAY);
  }

  @Override public Dimension getMaximumSize() {
    Dimension d = super.getPreferredSize();
    d.width = Short.MAX_VALUE;
    return d;
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    d.height = 24;
    return d;
  }
}

class PopupHeaderMouseListener extends MouseAdapter {
  private final Point startPt = new Point();

  @Override public void mousePressed(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      startPt.setLocation(e.getPoint());
    }
  }

  @Override public void mouseDragged(MouseEvent e) {
    Component c = e.getComponent();
    Window w = SwingUtilities.getWindowAncestor(c);
    if (w != null && SwingUtilities.isLeftMouseButton(e)) {
      if (w.getType() == Window.Type.POPUP) { // Popup$HeavyWeightWindow
        Point pt = e.getLocationOnScreen();
        w.setLocation(pt.x - startPt.x, pt.y - startPt.y);
      } else { // Popup$LightWeightWindow
        Container popup = SwingUtilities.getAncestorOfClass(JPopupMenu.class, c);
        Point pt = popup.getLocation();
        popup.setLocation(pt.x - startPt.x + e.getX(), pt.y - startPt.y + e.getY());
      }
    }
  }
}

class MenuArrowIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(Color.BLACK);
    g2.drawLine(2, 5, 6, 5);
    g2.drawLine(3, 4, 5, 4);
    g2.drawLine(4, 3, 4, 3);
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
      int y = popup.getPreferredSize().height;
      popup.show(b, 0, -y);
      // popup.show(b, 0, b.getHeight());
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
