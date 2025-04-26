// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicCheckBoxMenuItemUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    // Java 9
    // UIManager.put("CheckBoxMenuItem.doNotCloseOnMouseClick", true);

    JToggleButton button = new JToggleButton("JPopupMenu Test");
    JPopupMenu popup = new JPopupMenu();
    TogglePopupHandler handler = new TogglePopupHandler(popup, button);
    popup.addPopupMenuListener(handler);
    button.addActionListener(handler);

    // Java 9
    // JCheckBoxMenuItem checkMenuItem = new JCheckBoxMenuItem("doNotCloseOnMouseClick");
    // checkMenuItem.putClientProperty("CheckBoxMenuItem.doNotCloseOnMouseClick", true);
    // popup.add(checkMenuItem);

    popup.add(new JCheckBox("JCheckBox") {
      @Override public void updateUI() {
        super.updateUI();
        setFocusPainted(false);
      }

      @Override public Dimension getMinimumSize() {
        Dimension d = getPreferredSize();
        d.width = Short.MAX_VALUE;
        return d;
      }
    });
    popup.add(makeStayOpenCheckBoxMenuItem("JMenuItem + JCheckBox"));
    popup.add(new JCheckBoxMenuItem("JCheckBoxMenuItem"));
    popup.add(new JCheckBoxMenuItem("keeping open #1")).addActionListener(e -> {
      // System.out.println("ActionListener");
      Component src = (Component) e.getSource();
      Container c = SwingUtilities.getAncestorOfClass(JPopupMenu.class, src);
      if (c instanceof JPopupMenu) {
        c.setVisible(true);
      }
    });
    popup.add(new JCheckBoxMenuItem("keeping open #2") {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new BasicCheckBoxMenuItemUI() {
          // https://stackoverflow.com/questions/3759379/how-to-prevent-jpopupmenu-disappearing-when-checking-checkboxes-in-it
          @Override protected void doClick(MenuSelectionManager msm) {
            // super.doClick(msm);
            // System.out.println("MenuSelectionManager: doClick");
            menuItem.doClick(0);
          }
        });
      }
    });

    setOpaque(true);
    setComponentPopupMenu(popup);
    add(button);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JMenuItem makeStayOpenCheckBoxMenuItem(String title) {
    JMenuItem mi = new JMenuItem(" ");
    mi.setLayout(new BorderLayout());
    mi.add(new JCheckBox(title) {
      private transient MouseInputListener handler;
      @Override public void updateUI() {
        removeMouseListener(handler);
        removeMouseMotionListener(handler);
        super.updateUI();
        handler = new DispatchParentHandler();
        addMouseListener(handler);
        addMouseMotionListener(handler);
        setFocusable(false);
        setOpaque(false);
      }
    });
    return mi;
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

class DispatchParentHandler extends MouseInputAdapter {
  private void dispatchEvent(MouseEvent e) {
    Component src = e.getComponent();
    Container tgt = SwingUtilities.getUnwrappedParent(src);
    tgt.dispatchEvent(SwingUtilities.convertMouseEvent(src, e, tgt));
  }

  @Override public void mouseEntered(MouseEvent e) {
    dispatchEvent(e);
  }

  @Override public void mouseExited(MouseEvent e) {
    dispatchEvent(e);
  }

  @Override public void mouseMoved(MouseEvent e) {
    dispatchEvent(e);
  }

  @Override public void mouseDragged(MouseEvent e) {
    dispatchEvent(e);
  }
}

class TogglePopupHandler implements PopupMenuListener, ActionListener {
  private final JPopupMenu popup;
  private final AbstractButton button;

  protected TogglePopupHandler(JPopupMenu popup, AbstractButton button) {
    this.popup = popup;
    this.button = button;
  }

  @Override public void actionPerformed(ActionEvent e) {
    AbstractButton b = (AbstractButton) e.getSource();
    if (b.isSelected()) {
      Container p = SwingUtilities.getUnwrappedParent(b);
      Rectangle r = b.getBounds();
      popup.show(p, r.x, r.y + r.height);
    } else {
      popup.setVisible(false);
    }
  }

  @Override public void popupMenuCanceled(PopupMenuEvent e) {
    /* not needed */
  }

  @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    EventQueue.invokeLater(() -> {
      button.getModel().setArmed(false);
      button.getModel().setSelected(false);
    });
  }

  @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    /* not needed */
  }
}
