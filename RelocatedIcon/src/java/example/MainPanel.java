// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyVetoException;
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

    JButton addButton = new JButton("add");
    addButton.addActionListener(new ActionListener() {
      private int num;
      @Override public void actionPerformed(ActionEvent e) {
        JInternalFrame f = createFrame("#" + num, num * 10, num * 10);
        desktop.add(f);
        desktop.getDesktopManager().activateFrame(f);
        num++;
      }
    });

    JToolBar toolbar = new JToolBar("toolbar");
    toolbar.setFloatable(false);
    toolbar.add(addButton);
    toolbar.addSeparator();
    toolbar.add(button);
    toolbar.addSeparator();
    toolbar.add(check);

    addIconifiedFrame(desktop, createFrame("Frame", 30, 10));
    addIconifiedFrame(desktop, createFrame("Frame", 50, 30));
    add(desktop);
    add(toolbar, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void doReIconify(JDesktopPane desktop) {
    DesktopManager dm = desktop.getDesktopManager();
    if (dm instanceof ReIconifyDesktopManager) {
      ReIconifyDesktopManager rdm = (ReIconifyDesktopManager) dm;
      for (JInternalFrame f: desktop.getAllFrames()) {
        if (f.isIcon()) {
          rdm.reIconifyFrame(f);
        }
      }
    }
  }

  public static JInternalFrame createFrame(String t, int x, int y) {
    // title, resizable, closable, maximizable, iconifiable
    JInternalFrame f = new JInternalFrame(t, false, true, true, true);
    f.setSize(200, 100);
    f.setLocation(x, y);
    f.setVisible(true);
    return f;
  }

  protected static void addIconifiedFrame(JDesktopPane desktop, JInternalFrame f) {
    desktop.add(f);
    try {
      f.setIcon(true);
    } catch (PropertyVetoException ex) {
      throw new IllegalStateException(ex);
    }
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

class ReIconifyDesktopManager extends DefaultDesktopManager {
  public void reIconifyFrame(JInternalFrame f) {
    deiconifyFrame(f);
    Rectangle r = getBoundsForIconOf(f);
    iconifyFrame(f);
    f.getDesktopIcon().setBounds(r);
  }
}
