package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super();

        JToggleButton button = new JToggleButton("JPopupMenu Test");
        JPopupMenu popup = new JPopupMenu();
        TogglePopupHandler handler = new TogglePopupHandler(popup, button);
        popup.addPopupMenuListener(handler);
        button.addActionListener(handler);

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
        popup.add(new JCheckBoxMenuItem(new AbstractAction("keeping open #1") {
            @Override public void actionPerformed(ActionEvent e) {
                System.out.println("AbstractAction");
                Container c = SwingUtilities.getAncestorOfClass(JPopupMenu.class, (Component) e.getSource());
                if (c instanceof JPopupMenu) {
                    ((JPopupMenu) c).setVisible(true);
                }
            }
        }));
        popup.add(new JCheckBoxMenuItem("keeping open #2") {
            @Override public void updateUI() {
                super.updateUI();
                setUI(new BasicCheckBoxMenuItemUI() {
                    // https://stackoverflow.com/questions/3759379/how-to-prevent-jpopupmenu-disappearing-when-checking-checkboxes-in-it
                    @Override protected void doClick(MenuSelectionManager msm) {
                        //super.doClick(msm);
                        System.out.println("MenuSelectionManager: doClick");
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
    public static void main(String... args) {
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
    @Override public void popupMenuCanceled(PopupMenuEvent e) { /* not needed */ }
    @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        EventQueue.invokeLater(() -> {
            button.getModel().setArmed(false);
            button.getModel().setSelected(false);
        });
    }
    @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) { /* not needed */ }
}
