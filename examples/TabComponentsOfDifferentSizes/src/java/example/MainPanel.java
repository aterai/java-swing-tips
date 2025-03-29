// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
  private final Dimension tabAreaSize = new Dimension(40, 40);

  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.LEFT);
    tabbedPane.setMinimumSize(tabAreaSize);
    tabbedPane.setFocusable(false);
    tabbedPane.addChangeListener(e -> updateDividerLocation(tabbedPane));
    tabbedPane.addMouseListener(new MouseAdapter() {
      private int prev = -1;
      @Override public void mouseClicked(MouseEvent e) {
        JTabbedPane tabs = (JTabbedPane) e.getComponent();
        // boolean isDoubleClick = e.getClickCount() > 1;
        if (prev == tabs.getSelectedIndex() && SwingUtilities.isLeftMouseButton(e)) {
          tabClicked(tabs);
        }
        prev = tabs.getSelectedIndex();
      }
    });
    Stream.of("computer", "directory", "file").forEach(title -> {
      Icon icon = UIManager.getIcon(String.format("FileView.%sIcon", title));
      JLabel label = new JLabel(title, icon, SwingConstants.CENTER);
      label.setPreferredSize(new Dimension(100, 100));
      label.addComponentListener(new ComponentAdapter() {
        @Override public void componentResized(ComponentEvent e) {
          tabComponentResized(e, tabbedPane);
        }
      });
      Icon tabIcon = makeVerticalTabIcon(title, icon);
      tabbedPane.addTab(null, tabIcon, label, title);
    });
    splitPane.setLeftComponent(tabbedPane);
    splitPane.setRightComponent(new JScrollPane(new JTree()));
    add(splitPane);
    setPreferredSize(new Dimension(320, 240));
  }

  public void tabComponentResized(ComponentEvent e, JTabbedPane tabs) {
    Component c = e.getComponent();
    if (c.equals(tabs.getSelectedComponent())) {
      Dimension d = c.getPreferredSize();
      if (isTopBottomTabPlacement(tabs.getTabPlacement())) {
        d.height = splitPane.getDividerLocation() - tabAreaSize.height;
      } else {
        d.width = splitPane.getDividerLocation() - tabAreaSize.width;
      }
      c.setPreferredSize(d);
    }
  }

  public void updateDividerLocation(JTabbedPane tabs) {
    Component c = tabs.getSelectedComponent();
    if (isTopBottomTabPlacement(tabs.getTabPlacement())) {
      splitPane.setDividerLocation(c.getPreferredSize().height + tabAreaSize.height);
    } else {
      splitPane.setDividerLocation(c.getPreferredSize().width + tabAreaSize.width);
    }
  }

  public void tabClicked(JTabbedPane tabs) {
    Component c = tabs.getSelectedComponent();
    if (isTopBottomTabPlacement(tabs.getTabPlacement())) {
      if (c.getPreferredSize().height == 0) {
        splitPane.setDividerLocation(120);
      } else {
        splitPane.setDividerLocation(tabAreaSize.height);
      }
    } else {
      if (c.getPreferredSize().width == 0) {
        splitPane.setDividerLocation(120);
      } else {
        splitPane.setDividerLocation(tabAreaSize.width);
      }
    }
  }

  private static boolean isTopBottomTabPlacement(int tabPlacement) {
    return tabPlacement == SwingConstants.TOP || tabPlacement == SwingConstants.BOTTOM;
  }

  private Icon makeVerticalTabIcon(String title, Icon icon) {
    JLabel label = new JLabel(title, icon, SwingConstants.LEADING);
    label.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
    Dimension d = label.getPreferredSize();
    int w = d.height;
    int h = d.width;
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = (Graphics2D) bi.getGraphics();
    AffineTransform at = AffineTransform.getTranslateInstance(0, h);
    at.quadrantRotate(-1);
    g2.setTransform(at);
    SwingUtilities.paintComponent(g2, label, this, 0, 0, d.width, d.height);
    g2.dispose();
    return new ImageIcon(bi);
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
