// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(3, 1));
    JTabbedPane tab1 = new TabbedPaneWithCloseButton();
    JTabbedPane tab2 = new TabbedPaneWithCloseIcons();
    JTabbedPane tab3 = new CloseableTabbedPane();

    Stream.of(tab1, tab2, tab3).map(MainPanel::makeTabbedPane).forEach(this::add);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTabbedPane makeTabbedPane(JTabbedPane tabbedPane) {
    tabbedPane.addTab("aaa", new JLabel("JLabel A"));
    tabbedPane.addTab("bb", new JLabel("JLabel B"));
    tabbedPane.addTab("c", new JLabel("JLabel C"));
    tabbedPane.addTab("dd dd", new JLabel("JLabel D"));
    return tabbedPane;
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
