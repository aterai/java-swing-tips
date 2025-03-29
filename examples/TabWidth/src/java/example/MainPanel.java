// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI;
import java.awt.*;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public final class MainPanel extends JPanel {
  public static final int MIN_TAB_WIDTH = 100;

  private MainPanel() {
    super(new GridLayout(2, 1, 0, 10));
    JTabbedPane tabbedPane = new JTabbedPane() {
      @Override public void updateUI() {
        super.updateUI();
        if (getUI() instanceof WindowsTabbedPaneUI) {
          setUI(new WindowsTabbedPaneUI() {
            @Override protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
              int tabWidth = super.calculateTabWidth(tabPlacement, tabIndex, metrics);
              return Math.max(MIN_TAB_WIDTH, tabWidth);
            }
          });
        } else {
          setUI(new BasicTabbedPaneUI() {
            @Override protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
              int tabWidth = super.calculateTabWidth(tabPlacement, tabIndex, metrics);
              return Math.max(MIN_TAB_WIDTH, tabWidth);
            }
          });
        }
      }
    };
    Stream.of(new JTabbedPane(), tabbedPane).forEach(tab -> {
      tab.addTab("111111", new JLabel("JLabel 1"));
      tab.addTab("22222222222222", new JLabel("JLabel 2"));
      tab.addTab("3", new JLabel("JLabel 3"));
      add(tab);
    });

    setPreferredSize(new Dimension(320, 240));
  }

  // // TEST:
  // public String makeTitle(String title) {
  //   return "<html><table width='100'><tr><td align='center'>" + title + "</td></tr></table>";
  // }

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
