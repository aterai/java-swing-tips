// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    UIManager.put("CheckBoxMenuItem.doNotCloseOnMouseClick", true); // Java 9

    JPopupMenu popup = new JPopupMenu();
    popup.addMouseWheelListener(InputEvent::consume);
    popup.add(Box.createHorizontalStrut(200));
    addCheckBoxAndSlider(popup);
    addCheckBoxAndToggleSlider(popup);
    addCheckBoxMenuItemAndSlider(popup);

    JMenu menu = new JMenu("JSlider");
    menu.getPopupMenu().addMouseWheelListener(InputEvent::consume);
    menu.add(Box.createHorizontalStrut(200));
    addCheckBoxAndSlider(menu);
    addCheckBoxAndToggleSlider(menu);
    addCheckBoxMenuItemAndSlider(menu);

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    mb.add(menu);
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    setComponentPopupMenu(popup);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void addCheckBoxAndSlider(JComponent popup) {
    JSlider slider = makeSlider();
    slider.setEnabled(false);

    JCheckBox check = makeCheckBox();
    check.addActionListener(e -> slider.setEnabled(((JCheckBox) e.getSource()).isSelected()));

    JMenuItem mi = new JMenuItem(" ");
    mi.setLayout(new BorderLayout());
    mi.add(check, BorderLayout.WEST);
    mi.add(slider);
    popup.add(mi);
  }

  private static void addCheckBoxAndToggleSlider(JComponent popup) {
    JMenuItem slider = makeBorderLayoutMenuItem();
    slider.add(makeSlider());

    JCheckBox check = makeCheckBox();
    check.setText("JCheckBox + JSlider");
    check.addActionListener(e -> {
      AbstractButton b = (AbstractButton) e.getSource();
      slider.setVisible(b.isSelected());
      Container p = SwingUtilities.getAncestorOfClass(JPopupMenu.class, b);
      if (p instanceof JPopupMenu) {
        ((JPopupMenu) p).pack();
      }
    });

    JMenuItem mi = new JMenuItem(" ");
    mi.setLayout(new BorderLayout());
    mi.add(check);

    popup.add(mi);
    popup.add(slider);
  }

  private static void addCheckBoxMenuItemAndSlider(JComponent popup) {
    JMenuItem slider = makeBorderLayoutMenuItem();
    slider.add(makeSlider());

    JMenuItem mi = new JCheckBoxMenuItem("JCheckBoxMenuItem + JSlider");
    mi.addActionListener(e -> {
      AbstractButton b = (AbstractButton) e.getSource();
      slider.setVisible(b.isSelected());
      Container p = SwingUtilities.getAncestorOfClass(JPopupMenu.class, b);
      if (p instanceof JPopupMenu) {
        p.setVisible(true);
        ((JPopupMenu) p).pack();
      }
    });

    popup.add(mi);
    popup.add(slider);
  }

  private static JSlider makeSlider() {
    UIManager.put("Slider.paintValue", Boolean.FALSE); // GTKLookAndFeel
    UIManager.put("Slider.focus", UIManager.get("Slider.background"));
    JSlider slider = new JSlider();
    slider.addMouseWheelListener(e -> {
      JSlider s = (JSlider) e.getComponent();
      if (s.isEnabled()) {
        BoundedRangeModel m = s.getModel();
        m.setValue(m.getValue() - e.getWheelRotation());
      }
      e.consume();
    });
    return slider;
  }

  private static JCheckBox makeCheckBox() {
    return new JCheckBox() {
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
    };
  }

  private static JMenuItem makeBorderLayoutMenuItem() {
    JMenuItem p = new JMenuItem(" ");
    p.setLayout(new BorderLayout());
    p.setVisible(false);
    int w = UIManager.getInt("MenuItem.minimumTextOffset");
    p.add(Box.createHorizontalStrut(w), BorderLayout.WEST);
    return p;
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
        ex.printStackTrace();
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
