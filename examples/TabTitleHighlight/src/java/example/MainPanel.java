// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabbedPane = new JTabbedPane() {
      private transient MouseMotionListener hoverHandler;

      @Override public void updateUI() {
        removeMouseMotionListener(hoverHandler);
        super.updateUI();
        hoverHandler = new MouseAdapter() {
          @Override public void mouseMoved(MouseEvent e) {
            JTabbedPane source = (JTabbedPane) e.getComponent();
            int num = source.indexAtLocation(e.getX(), e.getY());
            for (int i = 0; i < source.getTabCount(); i++) {
              source.setForegroundAt(i, i == num ? Color.GREEN : Color.BLACK);
            }
          }
        };
        addMouseMotionListener(hoverHandler);
      }
    };
    tabbedPane.addTab("JTree", new JScrollPane(new JTree()));
    tabbedPane.addTab("JTable", new JScrollPane(new JTable(5, 3)));
    tabbedPane.addTab("JSplitPane", new JSplitPane());
    tabbedPane.addTab("JLabel", new JLabel("JLabel"));
    tabbedPane.addTab("JPanel", new JScrollPane(new JPanel()));

    add(tabbedPane);
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
