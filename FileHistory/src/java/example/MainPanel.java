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
  private static final BarFactory BAR_FACTORY = new BarFactory("resources.Main");

  private final List<Path> fileHistoryCache = new ArrayList<>();
  private final JMenuItem noFile = new JMenuItem("(Empty)");
  private JMenu fileHistoryMenu;


  private MainPanel() {
    super(new BorderLayout());
    initActions(getActions());
    JPanel menupanel = new JPanel(new BorderLayout());
    JMenuBar menuBar = BAR_FACTORY.createMenuBar();
    menupanel.add(menuBar, BorderLayout.NORTH);
    initHistory();

    JToolBar toolBar = BAR_FACTORY.createToolBar();
    if (Objects.nonNull(toolBar)) {
      menupanel.add(toolBar, BorderLayout.SOUTH);
    }
    add(menupanel, BorderLayout.NORTH);
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
    if (fileHistoryCache.isEmpty()) {
      noFile.setEnabled(false);
      fileHistoryMenu.add(noFile);
    } else {
      fm.remove(noFile);
      for (int i = 0; i < fileHistoryCache.size(); i++) {
        Path name = fileHistoryCache.get(i);
        String num = Integer.toString(i + 1);
        // String path = Paths.get(name).toAbsolutePath().toString();
        JMenuItem mi = fileHistoryMenu.add(new HistoryAction(name));
        mi.setText(num + ": " + name);
        mi.setMnemonic(num.codePointAt(0));
        // fileHistoryMenu.add(mi);
      }
    }
  }

  protected void updateHistory(Path path) {
    fileHistoryMenu.removeAll();
    fileHistoryCache.remove(path);
    fileHistoryCache.add(0, path);
    if (fileHistoryCache.size() > MAX_HISTORY) {
      fileHistoryCache.remove(fileHistoryCache.size() - 1);
    }
    for (int i = 0; i < fileHistoryCache.size(); i++) {
      Path name = fileHistoryCache.get(i);
      String num = Integer.toString(i + 1);
      JMenuItem mi = new JMenuItem(new HistoryAction(name));
      mi.setText(num + ": " + name);
      mi.setMnemonic(num.codePointAt(0));
      fileHistoryMenu.add(mi, i);
    }
  }

  private class HistoryAction extends AbstractAction {
    private final Path path;

    protected HistoryAction(Path path) {
      super();
      this.path = path;
    }

    @Override public void actionPerformed(ActionEvent e) {
      Object[] obj = {
        "Open the file.\n",
        "This example do nothing\n",
        " and move the file to the beginning of the history."
      };
      JComponent c = (JComponent) e.getSource();
      JOptionPane.showMessageDialog(c.getRootPane(), obj, VersionAction.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
      updateHistory(path);
    }
  }

  protected void initActions(Action... actlist) {
    BAR_FACTORY.initActions(actlist);
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
      Object[] obj = {
        "Create a new file.\n",
        "This example do nothing\n",
        " and pretend to generate an appropriate file name and open it."
      };
      JComponent c = (JComponent) e.getSource();
      JOptionPane.showMessageDialog(c.getRootPane(), obj, VersionAction.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
      String fileName = "C:/tmp/dummy.jpg." + counter + "~";
      updateHistory(Paths.get(fileName));
      counter++;
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

class SaveAsAction extends AbstractAction {
  protected SaveAsAction() {
    super("saveAs");
  }

  @Override public void actionPerformed(ActionEvent e) {
    // dummy
  }
}

class ExitAction extends AbstractAction {
  protected ExitAction() {
    super("exit");
  }

  @Override public void actionPerformed(ActionEvent e) {
    Component root = null;
    Container parent = SwingUtilities.getUnwrappedParent((Component) e.getSource());
    if (parent instanceof JPopupMenu) {
      JPopupMenu popup = (JPopupMenu) parent;
      root = SwingUtilities.getRoot(popup.getInvoker());
    } else if (parent instanceof JToolBar) {
      JToolBar toolbar = (JToolBar) parent;
      if (((BasicToolBarUI) toolbar.getUI()).isFloating()) {
        root = SwingUtilities.getWindowAncestor(toolbar).getOwner();
      } else {
        root = SwingUtilities.getRoot(toolbar);
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
    // dummy
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
    JComponent c = (JComponent) e.getSource();
    Object[] obj = {APP_NAME + " - Version " + VERSION + "." + RELEASE, COPYRIGHT};
    JOptionPane.showMessageDialog(c.getRootPane(), obj, APP_NAME, JOptionPane.INFORMATION_MESSAGE);
  }
}
