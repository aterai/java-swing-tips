// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.plaf.basic.BasicToolBarUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPopupMenu popup = new JPopupMenu();
    initMenu(popup);

    JMenuBar menuBar = new JMenuBar();
    JMenu menu = new JMenu("File");
    initMenu(menu);
    menuBar.add(menu);
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(menuBar));

    JToolBar toolBar = new JToolBar();
    toolBar.add(new JLabel("Floatable JToolBar:"));
    toolBar.add(Box.createGlue());
    toolBar.add(new ExitAction());

    JTree tree = new JTree();
    tree.setComponentPopupMenu(popup);

    add(toolBar, BorderLayout.SOUTH);
    add(new JScrollPane(tree));
    setPreferredSize(new Dimension(320, 240));
  }

  private static void initMenu(JComponent p) {
    Stream.of(
        new JMenuItem("Open(sample)"), new JMenuItem("Save(sample)"),
        new JSeparator(), new JMenuItem(new ExitAction())).forEach(p::add);
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

class ExitAction extends AbstractAction {
  protected ExitAction() {
    super("Exit");
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
