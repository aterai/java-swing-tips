// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyVetoException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JCheckBox check = new JCheckBox("Icons should be relocated", true);

    JDesktopPane desktop = new JDesktopPane();
    desktop.setDesktopManager(new ReIconifyDesktopManager());
    desktop.addComponentListener(new ComponentAdapter() {
      @Override public void componentResized(ComponentEvent e) {
        if (!check.isSelected()) {
          return;
        }
        doReIconify((JDesktopPane) e.getComponent());
      }
    });

    JButton button = new JButton("relocate");
    button.addActionListener(e -> doReIconify(desktop));

    AtomicInteger count = new AtomicInteger();
    JButton addButton = new JButton("add");
    addButton.addActionListener(e -> {
      int n = count.getAndIncrement();
      JInternalFrame f = createFrame("#" + n, n * 10, n * 10);
      desktop.add(f);
      desktop.getDesktopManager().activateFrame(f);
    });

    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);
    toolBar.add(addButton);
    toolBar.addSeparator();
    toolBar.add(button);
    toolBar.addSeparator();
    toolBar.add(check);

    addIconifiedFrame(desktop, createFrame("Frame", 30, 10));
    addIconifiedFrame(desktop, createFrame("Frame", 50, 30));
    add(desktop);
    add(toolBar, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void doReIconify(JDesktopPane desktop) {
    DesktopManager dm = desktop.getDesktopManager();
    if (dm instanceof ReIconifyDesktopManager) {
      ReIconifyDesktopManager rdm = (ReIconifyDesktopManager) dm;
      Stream.of(desktop.getAllFrames())
          .filter(JInternalFrame::isIcon)
          .forEach(rdm::reIconifyFrame);
      // for (JInternalFrame f : desktop.getAllFrames()) {
      //   if (f.isIcon()) {
      //     rdm.reIconifyFrame(f);
      //   }
      // }
    }
  }

  public static JInternalFrame createFrame(String t, int x, int y) {
    // title, resizable, closable, maximizable, iconifiable
    JInternalFrame f = new JInternalFrame(t, false, true, true, true);
    f.setSize(200, 100);
    f.setLocation(x, y);
    EventQueue.invokeLater(() -> f.setVisible(true));
    return f;
  }

  public static void addIconifiedFrame(JDesktopPane desktop, JInternalFrame f) {
    desktop.add(f);
    try {
      f.setIcon(true);
    } catch (PropertyVetoException ex) {
      throw new IllegalStateException(ex);
    }
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

class ReIconifyDesktopManager extends DefaultDesktopManager {
  public void reIconifyFrame(JInternalFrame f) {
    deiconifyFrame(f);
    Rectangle r = getBoundsForIconOf(f);
    iconifyFrame(f);
    f.getDesktopIcon().setBounds(r);
  }
}
