// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabbedPane0 = new CloseableTabbedPane();
    JTabbedPane tabbedPane1 = new JTabbedPane();
    Stream.of(tabbedPane0, tabbedPane1).forEach(t -> {
      t.addTab("JLabel: 111111111111111111111", new JLabel("JLabel1"));
      t.addTab("JLabel: 22222", new JLabel("JLabel2"));
      t.addTab("333", new JLabel("JLabel3"));
      t.addTab("4", new JLabel("JLabel4"));
    });

    // TEST:
    // EventQueue.invokeLater(new Runnable() {
    //   @Override public void run() {
    //     JPanel gp = new CloseableTabbedPaneGlassPane(tabbedPane);
    //     tabbedPane.getRootPane().setGlassPane(gp);
    //     gp.setOpaque(false);
    //     gp.setVisible(true);
    //   }
    // });

    JButton addTabButton = new JButton("add tab");
    addTabButton.addActionListener(e -> {
      String title = LocalTime.now(ZoneId.systemDefault()).toString();
      Stream.of(tabbedPane0, tabbedPane1).forEach(t -> t.addTab(title, new JLabel(title)));
    });

    JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    sp.setTopComponent(tabbedPane0);
    sp.setBottomComponent(new JLayer<>(tabbedPane1, new CloseableTabbedPaneLayerUI()));

    add(sp);
    add(addTabButton, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      int g = CloseableTabbedPaneLayerUI.GAP;
      UIManager.put("TabbedPane.tabInsets", new Insets(g, 16 + g, g, 16 + g));
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

class CloseTabIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    if (c instanceof AbstractButton && ((AbstractButton) c).getModel().isRollover()) {
      g2.setPaint(Color.ORANGE);
    } else {
      g2.setPaint(Color.BLACK);
    }
    g2.drawLine(4, 4, 11, 11);
    g2.drawLine(4, 5, 10, 11);
    g2.drawLine(5, 4, 11, 10);
    g2.drawLine(11, 4, 4, 11);
    g2.drawLine(11, 5, 5, 11);
    g2.drawLine(10, 4, 4, 10);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 16;
  }

  @Override public int getIconHeight() {
    return 16;
  }
}

class CloseableTabbedPane extends JTabbedPane {
  private static final Icon CLOSE_ICON = new CloseTabIcon();

  @Override public void addTab(String title, Component content) {
    JPanel tab = new JPanel(new BorderLayout());
    tab.setOpaque(false);
    JLabel label = new JLabel(title);
    label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
    JButton button = new JButton(CLOSE_ICON);
    button.setBorder(BorderFactory.createEmptyBorder());
    button.setContentAreaFilled(false);
    button.addActionListener(e -> removeTabAt(indexOfComponent(content)));
    tab.add(label, BorderLayout.WEST);
    tab.add(button, BorderLayout.EAST);
    tab.setBorder(BorderFactory.createEmptyBorder(2, 1, 1, 1));
    super.addTab(title, content);
    setTabComponentAt(getTabCount() - 1, tab);
  }
}

class CloseableTabbedPaneLayerUI extends LayerUI<JTabbedPane> {
  public static final int GAP = 2;
  private final Container rubberStamp = new JPanel();
  private final Point pt = new Point();
  private final JButton button = new JButton(new CloseTabIcon()) {
    @Override public void updateUI() {
      super.updateUI();
      setBorder(BorderFactory.createEmptyBorder());
      setFocusPainted(false);
      setBorderPainted(false);
      setContentAreaFilled(false);
      setRolloverEnabled(false);
    }
  };
  private final Dimension dim = button.getPreferredSize();
  private final Rectangle repaintRect = new Rectangle(dim.width * 2, dim.height * 2);

  private Rectangle getTabButtonRect(JTabbedPane tabbedPane, int index) {
    Rectangle r = tabbedPane.getBoundsAt(index);
    // Dimension dim = button.getPreferredSize();
    r.translate(r.width - dim.width - GAP, (r.height - dim.height) / 2);
    r.setSize(dim);
    return r;
  }

  @Override public void updateUI(JLayer<? extends JTabbedPane> l) {
    super.updateUI(l);
    SwingUtilities.updateComponentTreeUI(button);
  }

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (c instanceof JLayer) {
      JTabbedPane tabbedPane = (JTabbedPane) ((JLayer<?>) c).getView();
      for (int i = 0; i < tabbedPane.getTabCount(); i++) {
        Rectangle r = getTabButtonRect(tabbedPane, i);
        button.getModel().setRollover(r.contains(pt));
        SwingUtilities.paintComponent(g, button, rubberStamp, r);
      }
    }
  }

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(
          AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
    super.uninstallUI(c);
  }

  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JTabbedPane> l) {
    if (e.getID() == MouseEvent.MOUSE_CLICKED) {
      pt.setLocation(e.getPoint());
      JTabbedPane tabbedPane = l.getView();
      int index = tabbedPane.indexAtLocation(pt.x, pt.y);
      if (index >= 0 && getTabButtonRect(tabbedPane, index).contains(pt)) {
        tabbedPane.removeTabAt(index);
      }
    }
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JTabbedPane> l) {
    Point loc = e.getPoint();
    pt.setLocation(loc);
    if (l.getView().indexAtLocation(pt.x, pt.y) >= 0) {
      // Dimension dim = button.getPreferredSize();
      loc.translate(-dim.width, -dim.height);
      repaintRect.setLocation(loc);
      l.repaint(repaintRect);
    }
  }
}

// class CloseableTabbedPaneGlassPane extends JPanel {
//   private final Point pt = new Point();
//   private final JButton button = new JButton("x") {
//     @Override public Dimension getPreferredSize() {
//       return new Dimension(16, 16);
//     }
//   };
//   private final JTabbedPane tabbedPane;
//   private final Rectangle buttonRect = new Rectangle(button.getPreferredSize());
//
//   protected CloseableTabbedPaneGlassPane(JTabbedPane tabbedPane) {
//     super();
//     this.tabbedPane = tabbedPane;
//     MouseAdapter h = new Handler();
//     tabbedPane.addMouseListener(h);
//     tabbedPane.addMouseMotionListener(h);
//     button.setBorder(BorderFactory.createEmptyBorder());
//     button.setFocusPainted(false);
//     button.setBorderPainted(false);
//     button.setContentAreaFilled(false);
//     button.setRolloverEnabled(false);
//   }
//
//   @Override protected void paintComponent(Graphics g) {
//     Point glassPt = SwingUtilities.convertPoint(tabbedPane, 0, 0, this);
//     for (int i = 0; i < tabbedPane.getTabCount(); i++) {
//       Rectangle tabRect = tabbedPane.getBoundsAt(i);
//       int x = tabRect.x + tabRect.width - buttonRect.width - 2;
//       int y = tabRect.y + (tabRect.height - buttonRect.height) / 2;
//       buttonRect.setLocation(x, y);
//       button.setForeground(buttonRect.contains(pt) ? Color.RED : Color.BLACK);
//       buttonRect.translate(glassPt.x, glassPt.y);
//       SwingUtilities.paintComponent(g, button, this, buttonRect);
//     }
//   }
//
//   class Handler extends MouseAdapter {
//     @Override public void mouseClicked(MouseEvent e) {
//       pt.setLocation(e.getPoint());
//       int index = tabbedPane.indexAtLocation(pt.x, pt.y);
//       if (index >= 0) {
//         Rectangle tabRect = tabbedPane.getBoundsAt(index);
//         int x = tabRect.x + tabRect.width - buttonRect.width - 2;
//         int y = tabRect.y + (tabRect.height - buttonRect.height) / 2;
//         buttonRect.setLocation(x, y);
//         if (buttonRect.contains(pt)) {
//           tabbedPane.removeTabAt(index);
//         }
//       }
//       tabbedPane.repaint();
//     }
//
//     @Override public void mouseMoved(MouseEvent e) {
//       pt.setLocation(e.getPoint());
//       int index = tabbedPane.indexAtLocation(pt.x, pt.y);
//       if (index >= 0) {
//         tabbedPane.repaint(tabbedPane.getBoundsAt(index));
//       } else {
//         tabbedPane.repaint();
//       }
//     }
//   }
// }
