// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.basic.BasicPopupMenuUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPopupMenu popup = createCustomPopup();
    JTable table = new JTable(16, 3);
    table.setComponentPopupMenu(popup);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JPopupMenu createCustomPopup() {
    JPopupMenu popup = new JPopupMenu() {
      @Override public void updateUI() {
        setUI(new TransparentPopupMenuUI());
        setOpaque(false);
      }
    };
    popup.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    popup.setLayout(new BorderLayout(0, 8));

    JToolBar toolbar = new RoundedToolBar(15);
    toolbar.setFloatable(false);
    toolbar.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
    Arrays.asList("📋", "💾", "🔍", "🔖", "🔋", "🔔")
        .forEach(icon -> toolbar.add(createIconButton(icon)));

    RoundPanel menuPanel = new RoundPanel(15);
    menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
    menuPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    Arrays.asList("Properties", "Rename", "Save", "Delete")
        .forEach(text -> menuPanel.add(createMenuButton(text, popup)));

    popup.add(toolbar, BorderLayout.NORTH);
    popup.add(menuPanel, BorderLayout.WEST);
    return popup;
  }

  private static JButton createIconButton(String text) {
    JButton button = new JButton(text);
    button.setFocusPainted(false);
    button.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    return button;
  }

  private static JButton createMenuButton(String text, JPopupMenu parent) {
    JButton button = new JButton(text) {
      @Override public void updateUI() {
        super.updateUI();
        setAlignmentX(LEFT_ALIGNMENT);
        setHorizontalAlignment(LEFT);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorder(BorderFactory.createEmptyBorder(5, 11, 5, 50));
      }

      @Override public Dimension getMaximumSize() {
        Dimension d = super.getMaximumSize();
        d.width = 150;
        return d;
      }
    };
    button.addMouseListener(new MouseAdapter() {
      @Override public void mouseEntered(MouseEvent e) {
        button.setOpaque(true);
        button.setBackground(new Color(200, 220, 255));
        button.repaint();
      }

      @Override public void mouseExited(MouseEvent e) {
        button.setOpaque(false);
        button.repaint();
      }

      @Override public void mousePressed(MouseEvent e) {
        parent.setVisible(false);
      }
    });
    return button;
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

class TransparentPopupMenuUI extends BasicPopupMenuUI {
  @Override public Popup getPopup(JPopupMenu popup, int x, int y) {
    Popup p = super.getPopup(popup, x, y);
    if (p != null) {
      EventQueue.invokeLater(() -> {
        Window window = SwingUtilities.getWindowAncestor(popup);
        Optional.ofNullable(window)
            .filter(w -> {
              boolean isHeavyWeight = w.getType() == Window.Type.POPUP;
              GraphicsConfiguration gc = w.getGraphicsConfiguration();
              return gc != null && gc.isTranslucencyCapable() && isHeavyWeight;
            })
            .ifPresent(w -> w.setBackground(new Color(0x0, true)));
      });
      Container c = SwingUtilities.getUnwrappedParent(popup);
      if (c instanceof JComponent) {
        ((JComponent) c).setOpaque(false);
      }
    }
    return p;
  }
}

class RoundPanel extends JPanel {
  private final int radius;

  protected RoundPanel(int radius) {
    super();
    this.radius = radius;
  }

  @Override public void updateUI() {
    super.updateUI();
    setOpaque(false);
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    int w = getWidth();
    int h = getHeight();
    g2.setColor(getBackground());
    g2.fill(new RoundRectangle2D.Double(0d, 0d, w, h, radius, radius));
    g2.setColor(Color.LIGHT_GRAY);
    g2.draw(new RoundRectangle2D.Double(0d, 0d, w - 1d, h - 1d, radius, radius));
    g2.dispose();
    super.paintComponent(g);
  }
}

class RoundedToolBar extends JToolBar {
  private final int radius;

  protected RoundedToolBar(int radius) {
    super();
    this.radius = radius;
  }

  @Override public void updateUI() {
    super.updateUI();
    setOpaque(false);
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    int w = getWidth();
    int h = getHeight();
    g2.setColor(getBackground());
    g2.fill(new RoundRectangle2D.Double(0d, 0d, w, h, radius, radius));
    g2.setColor(Color.LIGHT_GRAY);
    g2.draw(new RoundRectangle2D.Double(0d, 0d, w - 1d, h - 1d, radius, radius));
    g2.dispose();
    super.paintComponent(g);
  }
}
