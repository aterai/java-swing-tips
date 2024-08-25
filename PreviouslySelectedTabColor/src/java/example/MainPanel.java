// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.InsetsUIResource;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabs = new LineFocusTabbedPane();
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

class LineFocusTabbedPane extends JTabbedPane {
  private transient ChangeListener listener;

  protected LineFocusTabbedPane() {
    super();
  }

  @Override public void updateUI() {
    removeChangeListener(listener);
    UIManager.put("TabbedPane.tabInsets", new InsetsUIResource(1, 4, 0, 4));
    UIManager.put("TabbedPane.selectedTabPadInsets", new InsetsUIResource(1, 1, 1, 1));
    UIManager.put("TabbedPane.tabAreaInsets", new InsetsUIResource(3, 2, 0, 2));
    UIManager.put("TabbedPane.selectedLabelShift", 0);
    UIManager.put("TabbedPane.labelShift", 0);
    UIManager.put("TabbedPane.focus", new ColorUIResource(new Color(0x0, true)));
    super.updateUI();
    listener = new TabSelectionListener();
    addChangeListener(listener);
    setTabLayoutPolicy(SCROLL_TAB_LAYOUT);
  }

  @Override public void insertTab(String title, Icon icon, Component c, String tip, int index) {
    super.insertTab(title, icon, c, tip, index);
    JLabel label = new JLabel(title, icon, CENTER);
    setTabComponentAt(index, label);
  }
}

class TabSelectionListener implements ChangeListener {
  private static final Color ALPHA_ZERO = new Color(0x0, true);
  private static final Color SELECTION_COLOR = new Color(0x00_AA_FF);
  private static final Color PREV_COLOR = new Color(0x48_00_AA_FF, true);
  private int prev = -1;

  @Override public void stateChanged(ChangeEvent e) {
    JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
    if (tabbedPane.getTabCount() <= 0) {
      return;
    }
    int idx = tabbedPane.getSelectedIndex();
    for (int i = 0; i < tabbedPane.getTabCount(); i++) {
      Component tab = tabbedPane.getTabComponentAt(i);
      if (tab instanceof JComponent) {
        Color color;
        if (i == idx) {
          color = SELECTION_COLOR;
        } else if (i == prev) {
          color = PREV_COLOR;
        } else {
          color = ALPHA_ZERO;
        }
        ((JComponent) tab).setBorder(BorderFactory.createMatteBorder(3, 0, 0, 0, color));
      }
    }
    prev = idx;
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
