// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventObject;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JInternalFrame frame = new JInternalFrame("title", true, true, true, true);
    frame.setBounds(10, 10, 160, 100);

    JTextArea log = new JTextArea();
    InternalFrameMaximizedListener listener = new InternalFrameMaximizedListener(log);
    frame.addPropertyChangeListener(listener);
    frame.addInternalFrameListener(listener);

    JDesktopPane desktop = new JDesktopPane();
    desktop.add(frame);
    EventQueue.invokeLater(() -> frame.setVisible(true));

    JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    split.setTopComponent(desktop);
    split.setBottomComponent(new JScrollPane(log));
    split.setResizeWeight(.8);
    add(split);
    setPreferredSize(new Dimension(320, 240));
  }

  // private JMenuBar createMenuBar() {
  //   JMenuBar menuBar = new JMenuBar();
  //   JMenu menu = new JMenu("Window");
  //   menu.setMnemonic(KeyEvent.VK_W);
  //   menuBar.add(menu);
  //   JMenuItem menuItem = new JMenuItem("New");
  //   menuItem.setMnemonic(KeyEvent.VK_N);
  //   menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.ALT_DOWN_MASK));
  //   menuItem.setActionCommand("new");
  //   menuItem.addActionListener(new ActionListener() {
  //     @Override public void actionPerformed(ActionEvent e) {
  //       JInternalFrame frame = createInternalFrame();
  //       desktop.add(frame);
  //       frame.setVisible(true);
  //       // desktop.getDesktopManager().activateFrame(frame);
  //     }
  //   });
  //   menu.add(menuItem);
  //   return menuBar;
  // }

  // private static JInternalFrame createInternalFrame() {
  //   String title = String.format("Document #%s", openFrameCount.getAndIncrement());
  //   JInternalFrame f = new JInternalFrame(title, true, true, true, true);
  //   f.setSize(160, 100);
  //   f.setLocation(OFFSET * openFrameCount.intValue(), OFFSET * openFrameCount.intValue());
  //   return f;
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

class InternalFrameMaximizedListener implements PropertyChangeListener, InternalFrameListener {
  private final JTextArea log;

  protected InternalFrameMaximizedListener(JTextArea log) {
    this.log = log;
  }

  @Override public void propertyChange(PropertyChangeEvent e) {
    String prop = e.getPropertyName();
    if (Objects.equals(JInternalFrame.IS_MAXIMUM_PROPERTY, prop)) {
      String str = Objects.equals(e.getNewValue(), true) ? "maximized" : "minimized";
      log.append(String.format("* Internal frame %s: %s%n", str, e.getSource()));
      log.setCaretPosition(log.getDocument().getLength());
    }
  }

  @Override public void internalFrameClosing(InternalFrameEvent e) {
    displayMessage("Internal frame closing", e);
  }

  @Override public void internalFrameClosed(InternalFrameEvent e) {
    displayMessage("Internal frame closed", e);
  }

  @Override public void internalFrameOpened(InternalFrameEvent e) {
    displayMessage("Internal frame opened", e);
  }

  @Override public void internalFrameIconified(InternalFrameEvent e) {
    displayMessage("Internal frame iconified", e);
  }

  @Override public void internalFrameDeiconified(InternalFrameEvent e) {
    displayMessage("Internal frame deiconified", e);
  }

  @Override public void internalFrameActivated(InternalFrameEvent e) {
    displayMessage("Internal frame activated", e);
  }

  @Override public void internalFrameDeactivated(InternalFrameEvent e) {
    displayMessage("Internal frame deactivated", e);
  }

  private void displayMessage(String prefix, EventObject e) {
    String s = prefix + ": " + e.getSource();
    log.append(s + "\n");
    log.setCaretPosition(log.getDocument().getLength());
  }
}
