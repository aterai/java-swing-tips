// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Shape shape = new RoundRectangle2D.Float(0f, 0f, 240f, 64f, 32f, 32f);

    JButton button1 = createButton("use Window#setShape(...)", shape, true);
    JButton button2 = createButton("not use Window#setShape(...)", shape, false);

    JPanel p = new JPanel(new GridLayout(0, 1, 5, 5));
    p.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
    p.add(button1);
    p.add(button2);

    add(p, BorderLayout.NORTH);
    add(new JScrollPane(new JTree()));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JButton createButton(String title, Shape shape, boolean useSetShape) {
    JButton button = new JButton(title);
    button.addActionListener(e -> {
      Component c = ((AbstractButton) e.getSource()).getRootPane();
      showWindow(c, shape, useSetShape);
    });
    return button;
  }

  private static void showWindow(Component parent, Shape shape, boolean useSetShape) {
    JWindow window = new JWindow();
    setTranslucencyBackground(window);
    window.getContentPane().add(createPanel(shape));
    if (useSetShape) {
      // Hard clipping: the edges of the window become jagged
      window.setShape(shape);
    }
    window.pack();
    window.setLocationRelativeTo(parent);
    window.setVisible(true);
  }

  private static void setTranslucencyBackground(JWindow window) {
    GraphicsConfiguration gc = window.getGraphicsConfiguration();
    if (gc != null && gc.isTranslucencyCapable()) {
      window.setBackground(new Color(0x0, true));
    }
  }

  private static Component createPanel(Shape shape) {
    JPanel panel = new JPanel(new BorderLayout()) {
      @Override public Dimension getPreferredSize() {
        return shape.getBounds().getSize();
      }

      @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, .5f));
        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(Color.RED);
        g2.fill(shape);
        g2.dispose();
        super.paintComponent(g);
      }
    };
    panel.setOpaque(false);
    panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    DragWindowListener dwl = new DragWindowListener();
    panel.addMouseListener(dwl);
    panel.addMouseMotionListener(dwl);
    panel.add(createIconBox());
    panel.add(createCloseButton(), BorderLayout.EAST);
    return panel;
  }

  private static Container createIconBox() {
    // Box p = Box.createHorizontalBox();
    JPanel p = new JPanel();
    p.setOpaque(false);
    p.add(new JLabel(UIManager.getIcon("OptionPane.errorIcon")));
    p.add(new JLabel(UIManager.getIcon("OptionPane.questionIcon")));
    p.add(new JLabel(UIManager.getIcon("OptionPane.warningIcon")));
    p.add(new JLabel(UIManager.getIcon("OptionPane.informationIcon")));
    return p;
  }

  private static JButton createCloseButton() {
    JButton close = new JButton("<html><b>X");
    close.setContentAreaFilled(false);
    close.setBorder(BorderFactory.createEmptyBorder());
    close.setForeground(Color.WHITE);
    close.addActionListener(e -> {
      Component c = (Component) e.getSource();
      Optional.ofNullable(SwingUtilities.getWindowAncestor(c)).ifPresent(Window::dispose);
    });
    return close;
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

class DragWindowListener extends MouseAdapter {
  private final Point startPt = new Point();

  @Override public void mousePressed(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      startPt.setLocation(e.getPoint());
    }
  }

  @Override public void mouseDragged(MouseEvent e) {
    Component c = SwingUtilities.getRoot(e.getComponent());
    if (c instanceof Window && SwingUtilities.isLeftMouseButton(e)) {
      Point pt = c.getLocation();
      c.setLocation(pt.x - startPt.x + e.getX(), pt.y - startPt.y + e.getY());
    }
  }
}
