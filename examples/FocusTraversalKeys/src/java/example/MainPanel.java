// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final String HELP1 = "FORWARD_TRAVERSAL_KEYS: TAB, RIGHT, DOWN\n";
  private static final String HELP2 = "BACKWARD_TRAVERSAL_KEYS: SHIFT+TAB, LEFT, UP";

  private MainPanel() {
    super(new BorderLayout());
    JTextArea info = new JTextArea(HELP1 + HELP2 + "\n----\n");
    info.setEditable(false);
    JButton button = makeShowOptionDialogButton(info);
    EventQueue.invokeLater(() -> getRootPane().setDefaultButton(button));
    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(new JButton("111"));
    box.add(new JButton("222"));
    box.add(button);
    box.add(new JButton("333"));

    updateFocusTraversalKeys();
    setFocusCycleRoot(true);
    setFocusTraversalPolicy(new LayoutFocusTraversalPolicy() {
      @Override protected boolean accept(Component c) {
        // return !Objects.equals(c, textarea) && super.accept(c);
        return !(c instanceof JTextArea) && super.accept(c);
      }

      @Override public Component getDefaultComponent(Container container) {
        // return button;
        return getRootPane().getDefaultButton();
      }
    });

    add(new JScrollPane(info));
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void updateFocusTraversalKeys() {
    KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    int ftk = KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS;
    // Set<AWTKeyStroke> forwardKeys = new HashSet<>(frame.getFocusTraversalKeys(ftk));
    Set<AWTKeyStroke> defForwardKeys = manager.getDefaultFocusTraversalKeys(ftk);
    Set<AWTKeyStroke> forwardKeys = new HashSet<>(defForwardKeys);
    forwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
    forwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
    // frame.setFocusTraversalKeys(ftk, forwardKeys);
    manager.setDefaultFocusTraversalKeys(ftk, forwardKeys);

    int btk = KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS;
    // Set<AWTKeyStroke> backwardKeys = new HashSet<>(frame.getFocusTraversalKeys(btk));
    Set<AWTKeyStroke> defBackwardKeys = manager.getDefaultFocusTraversalKeys(btk);
    Set<AWTKeyStroke> backwardKeys = new HashSet<>(defBackwardKeys);
    backwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
    backwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
    // frame.setFocusTraversalKeys(btk, backwardKeys);
    manager.setDefaultFocusTraversalKeys(btk, backwardKeys);
  }

  private static JButton makeShowOptionDialogButton(JTextArea info) {
    JButton button = new JButton("showOptionDialog");
    button.addActionListener(e -> {
      JComponent c = (JComponent) e.getSource();
      int retValue = JOptionPane.showConfirmDialog(c.getRootPane(), HELP1 + HELP2);
      if (retValue == JOptionPane.YES_OPTION) {
        info.append("YES_OPTION\n");
      } else if (retValue == JOptionPane.NO_OPTION) {
        info.append("NO_OPTION\n");
      } else if (retValue == JOptionPane.CANCEL_OPTION) {
        info.append("CANCEL_OPTION\n");
      }
    });
    return button;
  }

  // // %JAVA_HOME%/src_b23/javax/swing/JOptionPane.java
  // public static int showOptionDialog(
  //     Component parent, Object message, String title, int optionType, int messageType,
  //     Icon icon, Object[] options, Object initialValue) throws HeadlessException {
  //   JOptionPane pane = new JOptionPane(
  //       message, messageType, optionType, icon, options, initialValue);
  //   pane.setInitialValue(initialValue);
  //   Component p = parent == null ? JOptionPane.getRootFrame() : parent;
  //   pane.setComponentOrientation(p.getComponentOrientation());
  //
  //   // int style = JOptionPane.styleFromMessageType(messageType);
  //   // JDialog dialog = pane.createDialog(parent, title, style);
  //   JDialog dialog = pane.createDialog(parent, title);
  //
  //   Set<AWTKeyStroke> forwardKeys = new HashSet<>(dialog.getFocusTraversalKeys(ftk));
  //   forwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
  //   forwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
  //   dialog.setFocusTraversalKeys(ftk, forwardKeys);
  //
  //   Set<AWTKeyStroke> backwardKeys = new HashSet<>(dialog.getFocusTraversalKeys(btk));
  //   backwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
  //   backwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
  //   dialog.setFocusTraversalKeys(btk, backwardKeys);
  //
  //   pane.selectInitialValue();
  //   // dialog.show();
  //   dialog.setVisible(true);
  //   dialog.dispose();
  //
  //   Object selectedValue = pane.getValue();
  //   if (selectedValue == null)
  //     return JOptionPane.CLOSED_OPTION;
  //   if (options == null) {
  //     if (selectedValue instanceof Integer)
  //       return ((Integer) selectedValue).intValue();
  //     return JOptionPane.CLOSED_OPTION;
  //   }
  //   for (int counter = 0, maxCounter = options.length;
  //     counter < maxCounter; counter++) {
  //     if (options[counter].equals(selectedValue))
  //       return counter;
  //   }
  //   return JOptionPane.CLOSED_OPTION;
  // }

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
