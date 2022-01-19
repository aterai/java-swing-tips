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
    mb.add(LookAndFeelUtil.createLookAndFeelMenu());
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
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
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

// @see https://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtil {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtil() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup lafGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo lafInfo : UIManager.getInstalledLookAndFeels()) {
      menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lafGroup));
    }
    return menu;
  }

  private static JMenuItem createLookAndFeelItem(String laf, String lafClass, ButtonGroup bg) {
    JMenuItem lafItem = new JRadioButtonMenuItem(laf, lafClass.equals(lookAndFeel));
    lafItem.setActionCommand(lafClass);
    lafItem.setHideActionText(true);
    lafItem.addActionListener(e -> {
      ButtonModel m = bg.getSelection();
      try {
        setLookAndFeel(m.getActionCommand());
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        UIManager.getLookAndFeel().provideErrorFeedback((Component) e.getSource());
      }
    });
    bg.add(lafItem);
    return lafItem;
  }

  private static void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
    String oldLookAndFeel = LookAndFeelUtil.lookAndFeel;
    if (!oldLookAndFeel.equals(lookAndFeel)) {
      UIManager.setLookAndFeel(lookAndFeel);
      LookAndFeelUtil.lookAndFeel = lookAndFeel;
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
