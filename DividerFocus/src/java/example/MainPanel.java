// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    JSplitPane splitPane = new JSplitPane();
    Container divider = ((BasicSplitPaneUI) splitPane.getUI()).getDivider();
    divider.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        splitPane.requestFocusInWindow();
        // or
        // String cmd = "startResize";
        // Action startResize = splitPane.getActionMap().get(cmd);
        // ActionEvent ae = new ActionEvent(splitPane, ActionEvent.ACTION_PERFORMED, cmd);
        // startResize.actionPerformed(ae);
      }
    });

    add(makeTitledPanel("Default", new JSplitPane()));
    add(makeTitledPanel("Divider.addMouseListener", splitPane));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
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
