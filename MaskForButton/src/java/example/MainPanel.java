// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
// import java.util.List;
// import java.util.stream.Stream;
import javax.swing.*;
// import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.setComponentPopupMenu(new TabbedPanePopupMenu());
    tabbedPane.addTab("Title a", new JLabel("Close a tab by the middle mouse button clicking."));
    tabbedPane.addTab("Title b", new JLabel());
    tabbedPane.addTab("Title c", new JLabel());

    System.out.println("MouseInfo.getNumberOfButtons: " + MouseInfo.getNumberOfButtons());
    tabbedPane.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        int button = e.getButton();
        System.out.println(button);
        System.out.println("BUTTON2 mouseClicked: " + Objects.toString((e.getModifiersEx() & InputEvent.getMaskForButton(2)) != 0));

        // boolean isB1Double = e.getClickCount() == 2 && InputEvent.getMaskForButton(button) == InputEvent.BUTTON1_DOWN_MASK;
        // boolean isB2Down = InputEvent.getMaskForButton(button) == InputEvent.BUTTON2_DOWN_MASK;
        boolean isB1Double = e.getClickCount() == 2 && button == 1;
        boolean isB2Down = MouseInfo.getNumberOfButtons() > 2 && button == 2;

        JTabbedPane tabbedPane = (JTabbedPane) e.getComponent();
        int idx = tabbedPane.indexAtLocation(e.getX(), e.getY());
        if (idx >= 0 && (isB2Down || isB1Double)) {
          tabbedPane.remove(idx);
        }
      }

      @Override public void mousePressed(MouseEvent e) {
        System.out.println("BUTTON2 mousePressed: " + Objects.toString((e.getModifiersEx() & InputEvent.getMaskForButton(2)) != 0));
      }

      @Override public void mouseReleased(MouseEvent e) {
        System.out.println("BUTTON2 mouseReleased: " + Objects.toString((e.getModifiersEx() & InputEvent.getMaskForButton(2)) != 0));
      }
    });
    add(tabbedPane);
    setPreferredSize(new Dimension(320, 240));
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

class TabbedPanePopupMenu extends JPopupMenu {
  protected transient int count;
  private final JMenuItem closePage;
  private final JMenuItem closeAll;
  private final JMenuItem closeAllButActive;

  protected TabbedPanePopupMenu() {
    super();
    add("New tab").addActionListener(e -> {
      JTabbedPane tabbedPane = (JTabbedPane) getInvoker();
      tabbedPane.addTab("Title: " + count, new JLabel("Tab: " + count));
      tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
      count++;
    });
    addSeparator();
    closePage = add("Close");
    closePage.addActionListener(e -> {
      JTabbedPane tabbedPane = (JTabbedPane) getInvoker();
      tabbedPane.remove(tabbedPane.getSelectedIndex());
    });
    addSeparator();
    closeAll = add("Close all");
    closeAll.addActionListener(e -> {
      JTabbedPane tabbedPane = (JTabbedPane) getInvoker();
      tabbedPane.removeAll();
    });
    closeAllButActive = add("Close all bat active");
    closeAllButActive.addActionListener(e -> {
      JTabbedPane tabbedPane = (JTabbedPane) getInvoker();
      int tabidx = tabbedPane.getSelectedIndex();
      String title = tabbedPane.getTitleAt(tabidx);
      Component cmp = tabbedPane.getComponentAt(tabidx);
      tabbedPane.removeAll();
      tabbedPane.addTab(title, cmp);
    });
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTabbedPane) {
      JTabbedPane tabbedPane = (JTabbedPane) c;
      closePage.setEnabled(tabbedPane.indexAtLocation(x, y) >= 0);
      closeAll.setEnabled(tabbedPane.getTabCount() > 0);
      closeAllButActive.setEnabled(tabbedPane.getTabCount() > 0);
      super.show(c, x, y);
    }
  }
}
