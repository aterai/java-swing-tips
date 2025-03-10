// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.Objects;
import javax.swing.*;
import javax.swing.plaf.basic.BasicToolBarUI;

public final class MainPanel extends JPanel {
  private static final BarFactory BAR_FACTORY = new BarFactory("example.Main");

  private MainPanel() {
    super(new BorderLayout());
    initActions(getActions());

    JMenuBar menuBar = BAR_FACTORY.createMenuBar();
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(menuBar));

    JToolBar toolBar = BAR_FACTORY.createToolBar();
    if (Objects.nonNull(toolBar)) {
      add(toolBar, BorderLayout.NORTH);
    }

    add(new JScrollPane(new JTextArea()));
    setPreferredSize(new Dimension(320, 240));
  }

  public void initActions(Action... actions) {
    BAR_FACTORY.initActions(actions);
  }

  private Action[] getActions() {
    return new Action[] {
        new NewAction(),
        new ExitAction(),
        new HelpAction(),
        new VersionAction()
    };
    // return defaultActions;
  }

  // private final Action[] defaultActions = {
  //   new NewAction(),
  //   new ExitAction(),
  //   new HelpAction(),
  //   new VersionAction(),
  // };

  private static class NewAction extends AbstractAction {
    protected NewAction() {
      super("new");
    }

    @Override public void actionPerformed(ActionEvent e) {
      // new action...
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

// class SaveAsAction extends AbstractAction {
//   protected SaveAsAction() {
//     super("saveAs");
//   }
//
//   @Override public void actionPerformed(ActionEvent e) {
//     // save as...
//   }
// }

class ExitAction extends AbstractAction {
  protected ExitAction() {
    super("exit");
  }

  @Override public void actionPerformed(ActionEvent e) {
    Component root;
    Container parent = SwingUtilities.getUnwrappedParent((Component) e.getSource());
    if (parent instanceof JPopupMenu) {
      JPopupMenu popup = (JPopupMenu) parent;
      root = SwingUtilities.getRoot(popup.getInvoker());
    } else if (parent instanceof JToolBar) {
      JToolBar toolBar = (JToolBar) parent;
      if (((BasicToolBarUI) toolBar.getUI()).isFloating()) {
        root = SwingUtilities.getWindowAncestor(toolBar).getOwner();
      } else {
        root = SwingUtilities.getRoot(toolBar);
      }
    } else {
      root = SwingUtilities.getRoot(parent);
    }
    if (root instanceof Window) {
      Window window = (Window) root;
      window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
    }
  }
}

class HelpAction extends AbstractAction {
  protected HelpAction() {
    super("help");
  }

  @Override public void actionPerformed(ActionEvent e) {
    // help
  }
}

class VersionAction extends AbstractAction {
  public static final String APP_NAME = "@title@";
  private static final String COPYRIGHT = "Copyright(C) 2006";
  private static final String VERSION = "0.0";
  private static final int RELEASE = 1;

  protected VersionAction() {
    super("version");
  }

  @Override public void actionPerformed(ActionEvent e) {
    Component c = ((JComponent) e.getSource()).getRootPane();
    Object[] msg = {APP_NAME + " - Version " + VERSION + "." + RELEASE, COPYRIGHT};
    JOptionPane.showMessageDialog(c, msg, APP_NAME, JOptionPane.INFORMATION_MESSAGE);
  }
}
