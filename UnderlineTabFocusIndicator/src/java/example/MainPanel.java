// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    UIManager.put("TabbedPane.tabInsets", new Insets(1, 4, 0, 4));
    UIManager.put("TabbedPane.selectedTabPadInsets", new Insets(1, 1, 1, 1));
    UIManager.put("TabbedPane.tabAreaInsets", new Insets(3, 2, 0, 2));
    UIManager.put("TabbedPane.selectedLabelShift", 0);
    UIManager.put("TabbedPane.labelShift", 0);

    JTabbedPane tabs = new UnderlineFocusTabbedPane();
    tabs.addTab("JTree", new ColorIcon(Color.RED), new JScrollPane(new JTree()));
    tabs.addTab("JTextArea", new ColorIcon(Color.GREEN), new JScrollPane(new JTextArea()));
    tabs.addTab("JTable", new ColorIcon(Color.BLUE), new JScrollPane(new JTable(8, 3)));
    tabs.addTab("JSplitPane", new ColorIcon(Color.ORANGE), new JSplitPane());
    tabs.setSelectedIndex(-1);
    EventQueue.invokeLater(() -> tabs.setSelectedIndex(0));

    add(tabs);
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

class UnderlineFocusTabbedPane extends JTabbedPane {
  private static final Color ALPHA_ZERO = new Color(0x0, true);
  private static final Color SELECTION_COLOR = new Color(0x00_AA_FF);

  protected UnderlineFocusTabbedPane() {
    super();
  }

  @Override public void updateUI() {
    UIManager.put("TabbedPane.focus", new Color(0x0, true));
    super.updateUI();
    // if (getUI() instanceof WindowsTabbedPaneUI) {
    //   setUI(new WindowsTabbedPaneUI() {
    //     @Override protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
    //       super.paintFocusIndicator(
    //           g, tabPlacement, rects, tabIndex, iconRect, textRect, false);
    //     }
    //   });
    // } else {
    //   setUI(new BasicTabbedPaneUI() {
    //     @Override protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
    //       super.paintFocusIndicator(
    //           g, tabPlacement, rects, tabIndex, iconRect, textRect, false);
    //     }
    //   });
    // }
    addChangeListener(e -> {
      JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
      if (tabbedPane.getTabCount() <= 0) {
        return;
      }
      int idx = tabbedPane.getSelectedIndex();
      for (int i = 0; i < tabbedPane.getTabCount(); i++) {
        Component c = tabbedPane.getTabComponentAt(i);
        if (c instanceof JComponent) {
          Color color = i == idx ? SELECTION_COLOR : ALPHA_ZERO;
          ((JComponent) c).setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, color));
        }
      }
    });
  }

  @Override public void insertTab(String title, Icon icon, Component component, String tip, int index) {
    super.insertTab(title, icon, component, tip, index);
    JLabel label = new JLabel(title, icon, CENTER);
    setTabComponentAt(index, label);
  }
}

class ColorIcon implements Icon {
  private final Color color;

  protected ColorIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(color);
    g2.fillRect(1, 1, getIconWidth() - 3, getIconHeight() - 3);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 16;
  }

  @Override public int getIconHeight() {
    return 16;
  }
}
