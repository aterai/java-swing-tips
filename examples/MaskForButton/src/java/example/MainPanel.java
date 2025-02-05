// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    JTextArea log = new JTextArea();
    log.setEditable(false);
    log.append("MouseInfo.getNumberOfButtons: " + MouseInfo.getNumberOfButtons() + "\n");

    JTabbedPane tabs = new JTabbedPane();
    tabs.setComponentPopupMenu(new TabbedPanePopupMenu());
    tabs.addTab("Title1", new JLabel("Close a tab by the middle mouse button clicking."));
    tabs.addTab("Title2", new JLabel("JLabel 2"));
    tabs.addTab("Title3", new JLabel("JLabel 3"));

    tabs.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        int btn = e.getButton();
        // int maskForButton = InputEvent.getMaskForButton(MouseEvent.BUTTON2);
        // int maskForButton = InputEvent.BUTTON2_DOWN_MASK;
        // boolean isB2Clicked = (e.getModifiersEx() & maskForButton) != 0;
        String mask = btn == MouseEvent.NOBUTTON ? "NOBUTTON" : "BUTTON" + btn;
        log.append(mask + "\n");
        // log.append("Middle mouseClicked: " + isB2Clicked + "\n");

        boolean isDouble = e.getClickCount() >= 2;
        boolean isLeftDouble = SwingUtilities.isLeftMouseButton(e) && isDouble;
        // && InputEvent.getMaskForButton(btn) == InputEvent.BUTTON1_DOWN_MASK;
        // boolean isMiddle = MouseInfo.getNumberOfButtons() > 2 && btn == MouseEvent.BUTTON2;
        // = InputEvent.getMaskForButton(btn) == InputEvent.BUTTON2_DOWN_MASK;
        boolean isMiddle = SwingUtilities.isMiddleMouseButton(e);

        JTabbedPane tabbedPane = (JTabbedPane) e.getComponent();
        int idx = tabbedPane.indexAtLocation(e.getX(), e.getY());
        if (idx >= 0 && (isMiddle || isLeftDouble)) {
          tabbedPane.remove(idx);
        }
      }

      @Override public void mousePressed(MouseEvent e) {
        // int maskForButton = InputEvent.getMaskForButton(MouseEvent.BUTTON2);
        // boolean mousePressed = (e.getModifiersEx() & maskForButton) != 0;
        boolean mousePressed = SwingUtilities.isMiddleMouseButton(e);
        log.append("Middle mousePressed: " + mousePressed + "\n");
      }

      @Override public void mouseReleased(MouseEvent e) {
        // int maskForButton = InputEvent.getMaskForButton(MouseEvent.BUTTON2);
        // boolean mouseReleased = (e.getModifiersEx() & maskForButton) != 0;
        boolean mouseReleased = SwingUtilities.isMiddleMouseButton(e);
        log.append("Middle mouseReleased: " + mouseReleased + "\n");
      }
    });
    add(tabs);
    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));
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

final class TabbedPanePopupMenu extends JPopupMenu {
  private transient int count;
  private final JMenuItem closePage;
  private final JMenuItem closeAll;
  private final JMenuItem closeAllButActive;

  /* default */ TabbedPanePopupMenu() {
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
      int tabIdx = tabbedPane.getSelectedIndex();
      String title = tabbedPane.getTitleAt(tabIdx);
      Component cmp = tabbedPane.getComponentAt(tabIdx);
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
