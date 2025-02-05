// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
  private static final Font FONT = new Font(Font.SERIF, Font.PLAIN, 300);
  private transient JFrame frame;

  private MainPanel() {
    super();
    JTextField textField = new JTextField("★", 20);
    JLabel label = new JLabel("", SwingConstants.CENTER);

    JToggleButton button = new JToggleButton("show");
    button.addActionListener(e -> {
      AbstractButton btn = (AbstractButton) e.getSource();
      if (Objects.isNull(frame)) {
        frame = new JFrame();
        frame.setUndecorated(true);
        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        frame.getContentPane().add(label);
        frame.getContentPane().setBackground(Color.GREEN);
        frame.pack();
      }
      if (btn.isSelected()) {
        String str = textField.getText().trim();
        // label.setText(str);
        TextLayout tl = new TextLayout(str, FONT, FRC);
        Rectangle2D b = tl.getBounds();
        Shape shape = tl.getOutline(AffineTransform.getTranslateInstance(-b.getX(), -b.getY()));

        // int w = 300;
        // int h = 300;
        // GeneralPath p = new GeneralPath();
        // p.moveTo(-w / 4f, -h / 12f);
        // p.lineTo(+w / 4f, -h / 12f);
        // p.lineTo(-w / 6f, +h /  4f);
        // p.lineTo(   0f, -h /  4f);
        // p.lineTo(+w / 6f, +h /  4f);
        // p.closePath();
        // AffineTransform at = AffineTransform.getTranslateInstance(w / 4, h / 4);
        // shape = at.createTransformedShape(p);

        frame.setBounds(shape.getBounds());
        // frame.setSize(shape.getBounds().width, shape.getBounds().height);
        // AWTUtilities.setWindowShape(frame, shape); // JDK 1.6.0
        frame.setShape(shape); // JDK 1.7.0
        frame.setLocationRelativeTo(btn.getRootPane());
        frame.setVisible(true);
      } else {
        frame.setVisible(false);
      }
    });

    add(textField);
    add(button);
    DragWindowListener dwl = new DragWindowListener();
    label.addMouseListener(dwl);
    label.addMouseMotionListener(dwl);
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
