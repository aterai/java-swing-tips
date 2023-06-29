// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.basic.BasicInternalFrameUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JDesktopPane desktop = new JDesktopPane();
    desktop.add(createFrame("title #", 1));
    desktop.add(createFrame("title #", 0));

    AtomicInteger idx = new AtomicInteger(2);
    JButton button = new JButton("add");
    button.addActionListener(e -> desktop.add(createFrame("#", idx.getAndIncrement())));

    JToolBar toolBar = new JToolBar();
    toolBar.add(button);

    add(desktop);
    add(toolBar, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JInternalFrame createFrame(String t, int i) {
    JInternalFrame f = new JInternalFrame(t + i, true, true, true, true);
    f.setSize(200, 100);
    f.setLocation(5 + 40 * i, 5 + 50 * i);
    JPopupMenu popup = new InternalFramePopupMenu();
    f.setComponentPopupMenu(popup);
    // ((BasicInternalFrameUI) f.getUI()).getNorthPane().setComponentPopupMenu(popup);
    ((BasicInternalFrameUI) f.getUI()).getNorthPane().setInheritsPopupMenu(true);
    f.getDesktopIcon().setComponentPopupMenu(popup);
    EventQueue.invokeLater(() -> f.setVisible(true));
    return f;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
      // UIManager.put("InternalFrame.useTaskBar", Boolean.TRUE);
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

final class InternalFramePopupMenu extends JPopupMenu {
  /* default */ InternalFramePopupMenu() {
    super();
    JTextField field = new JTextField(24) {
      private transient AncestorListener listener;
      @Override public void updateUI() {
        removeAncestorListener(listener);
        super.updateUI();
        listener = new FocusAncestorListener();
        addAncestorListener(listener);
      }
    };
    String cmd = "Edit Title";
    add(cmd).addActionListener(e -> {
      Component c = getInternalFrame(getInvoker());
      if (c instanceof JInternalFrame) {
        JInternalFrame frame = (JInternalFrame) c;
        field.setText(frame.getTitle());
        Container p = frame.getDesktopPane();
        int ret = JOptionPane.showConfirmDialog(
            p, field, cmd, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ret == JOptionPane.OK_OPTION) {
          renameInternalFrameTitle(frame, field.getText().trim());
        }
      }
    });
  }

  private static void renameInternalFrameTitle(JInternalFrame frame, String title) {
    if (!title.equals(frame.getTitle())) {
      frame.setTitle(title);
    }
  }

  @Override public void show(Component c, int x, int y) {
    if (getInternalFrame(c) instanceof JInternalFrame) {
      super.show(c, x, y);
    }
  }

  private static Component getInternalFrame(Component c) {
    // System.out.println(c.getClass().getName());
    Component f;
    if (c instanceof JInternalFrame.JDesktopIcon) {
      f = ((JInternalFrame.JDesktopIcon) c).getInternalFrame();
    } else if (c instanceof JInternalFrame) {
      f = c;
    } else { // if (c instanceof BasicInternalFrameTitlePane) {
      f = SwingUtilities.getAncestorOfClass(JInternalFrame.class, c);
    }
    return f;
  }
}

class FocusAncestorListener implements AncestorListener {
  @Override public void ancestorAdded(AncestorEvent e) {
    e.getComponent().requestFocusInWindow();
  }

  @Override public void ancestorMoved(AncestorEvent e) {
    /* not needed */
  }

  @Override public void ancestorRemoved(AncestorEvent e) {
    /* not needed */
  }
}
