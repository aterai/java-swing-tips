// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.*;
import javax.swing.plaf.basic.BasicToolBarUI;

public final class MainPanel extends JPanel {
  private static final int MAX_HISTORY = 3;
  private static final BarFactory BAR_FACTORY = new BarFactory("example.Main");

  private static final List<Path> RECENT_FILES = new ArrayList<>();
  private final JMenuItem noFile = new JMenuItem("(Empty)");
  private JMenu fileHistoryMenu;

  private MainPanel() {
    super(new BorderLayout());
    initActions(getActions());

    JMenuBar menuBar = BAR_FACTORY.createMenuBar();
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(menuBar));
    initHistory();

    JToolBar toolBar = BAR_FACTORY.createToolBar();
    if (Objects.nonNull(toolBar)) {
      add(toolBar, BorderLayout.NORTH);
    }

    add(new JScrollPane(new JTextArea()));
    setPreferredSize(new Dimension(320, 240));
  }

  private void initHistory() {
    JMenu fm = BAR_FACTORY.getMenu("file");
    if (Objects.nonNull(fileHistoryMenu)) {
      fileHistoryMenu.removeAll();
    } else {
      fileHistoryMenu = new JMenu("Recent Items");
      fileHistoryMenu.setMnemonic(KeyEvent.VK_R);
      JMenuItem exit = BAR_FACTORY.getMenuItem("exit");
      fm.remove(exit);
      fm.add(fileHistoryMenu);
      fm.addSeparator();
      fm.add(exit);
    }
    if (RECENT_FILES.isEmpty()) {
      noFile.setEnabled(false);
      fileHistoryMenu.add(noFile);
    } else {
      fm.remove(noFile);
      for (int i = 0; i < RECENT_FILES.size(); i++) {
        JMenuItem mi = makeHistoryMenuItem(RECENT_FILES.get(i), i);
        fileHistoryMenu.add(mi);
      }
    }
  }

  public void updateHistory(Path path) {
    fileHistoryMenu.removeAll();
    RECENT_FILES.remove(path);
    RECENT_FILES.add(0, path);
    if (RECENT_FILES.size() > MAX_HISTORY) {
      RECENT_FILES.remove(RECENT_FILES.size() - 1);
    }
    for (int i = 0; i < RECENT_FILES.size(); i++) {
      JMenuItem mi = makeHistoryMenuItem(RECENT_FILES.get(i), i);
      fileHistoryMenu.add(mi, i);
    }
  }

  private JMenuItem makeHistoryMenuItem(Path name, int idx) {
    String num = Integer.toString(idx + 1);
    JMenuItem mi = new JMenuItem(new HistoryAction(name));
    mi.setText(num + ": " + name);
    mi.setMnemonic(num.codePointAt(0));
    return mi;
  }

  private class HistoryAction extends AbstractAction {
    private final transient Path path;

    protected HistoryAction(Path path) {
      super();
      this.path = path;
    }

    @Override public void actionPerformed(ActionEvent e) {
      Component c = ((JComponent) e.getSource()).getRootPane();
      Object[] obj = {
          "Open the file.\n",
          "This example do nothing\n",
          " and move the file to the beginning of the history."
      };
      String title = VersionAction.APP_NAME;
      JOptionPane.showMessageDialog(c, obj, title, JOptionPane.INFORMATION_MESSAGE);
      updateHistory(path);
    }
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
  }

  private class NewAction extends AbstractAction {
    private int counter;

    protected NewAction() {
      super("new");
    }

    @Override public void actionPerformed(ActionEvent e) {
      Component c = ((JComponent) e.getSource()).getRootPane();
      Object[] msg = {
          "Create a new file.\n",
          "This example do nothing\n",
          " and pretend to generate an appropriate file name and open it."
      };
      String title = VersionAction.APP_NAME;
      JOptionPane.showMessageDialog(c, msg, title, JOptionPane.INFORMATION_MESSAGE);
      String fileName = "C:/tmp/sample.jpg." + counter + "~";
      updateHistory(Paths.get(fileName));
      counter++;
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
