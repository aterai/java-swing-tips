// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JSlider slider0 = new JSlider(0, 100, 50);
    box.add(makeTitledSeparator("Default", slider0));
    box.add(Box.createVerticalStrut(2));

    JSlider slider1 = new JSlider(0, 100, 50);
    slider1.setEnabled(false);
    box.add(makeTitledSeparator("JSlider#setEnabled(false)", slider1));
    box.add(Box.createVerticalStrut(2));

    // https://community.oracle.com/thread/1360123
    JSlider slider2 = new JSlider(0, 100, 50) {
      @Override
      public void updateUI() {
        super.updateUI();
        JSlider slider = this;
        AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
          try {
            // https://community.oracle.com/thread/1360123
            Class<BasicSliderUI> uiClass = BasicSliderUI.class;
            Method uninstallListeners = uiClass.getDeclaredMethod("uninstallListeners", JSlider.class);
            uninstallListeners.setAccessible(true);
            uninstallListeners.invoke(getUI(), slider);
            Method uninstallKeyboardActions = uiClass.getDeclaredMethod("uninstallKeyboardActions", JSlider.class);
            uninstallKeyboardActions.setAccessible(true);
            uninstallKeyboardActions.invoke(getUI(), slider);
          } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new UnsupportedOperationException(ex);
          }
          return null;
        });
      }
    };
    box.add(makeTitledSeparator("BasicSliderUI#uninstallListeners(...)", slider2));
    box.add(Box.createVerticalStrut(2));
    // uninstallKeyboardActions
    // removeFocusListener(focusListener);
    // removeComponentListener(componentListener);
    // removePropertyChangeListener(propertyChangeListener);
    // getModel().removeChangeListener(changeListener);

    JSlider slider3 = new JSlider(0, 100, 50) {
      @Override
      public void updateUI() {
        super.updateUI();
        setFocusable(false); // uninstallKeyboardActions
        for (MouseListener l : getMouseListeners()) {
          removeMouseListener(l);
        }
        for (MouseMotionListener l : getMouseMotionListeners()) {
          removeMouseMotionListener(l);
        }
        // removeFocusListener(focusListener);
        // removeComponentListener(componentListener);
        // removePropertyChangeListener(propertyChangeListener);
        // getModel().removeChangeListener(changeListener);
      }
    };
    box.add(makeTitledSeparator("JSlider#removeMouseListener(...)", slider3));
    box.add(Box.createVerticalGlue());

    add(box);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledSeparator(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c, BorderLayout.NORTH);
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
