// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.util.Optional;
import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

public final class MainPanel extends JPanel {
  private static int openFrameCount;
  private static int row;
  private static int col;

  private MainPanel() {
    super(new BorderLayout());
    JDesktopPane desktop = new JDesktopPane();

    desktop.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
    desktop.getActionMap().put("escape", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        getSelectedFrame(desktop).ifPresent(desktop.getDesktopManager()::closeFrame);
      }
    });

    add(desktop);
    add(createToolBar(desktop), BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  protected static Optional<? extends JInternalFrame> getSelectedFrame(JDesktopPane desktop) {
    return Optional.ofNullable(desktop.getSelectedFrame());
  }

  private static JToolBar createToolBar(JDesktopPane desktop) {
    JToolBar toolbar = new JToolBar("toolbar");
    toolbar.setFloatable(false);
    JButton b = new JButton(new ImageIcon(MainPanel.class.getResource("icon_new-file.png")));
    b.addActionListener(e -> {
      JInternalFrame frame = makeInternalFrame(desktop);
      // frame.setVisible(true);
      desktop.add(frame);
      try {
        frame.setSelected(true);
        if (openFrameCount % 2 == 0) {
          frame.setIcon(true);
        }
      } catch (PropertyVetoException ex) {
        ex.printStackTrace();
      }
    });
    b.setToolTipText("create new InternalFrame");
    toolbar.add(b);
    toolbar.add(Box.createGlue());

    b = new JButton(new CloseIcon(Color.RED));
    b.addActionListener(e -> getSelectedFrame(desktop).ifPresent(JInternalFrame::dispose));
    b.setToolTipText("f.dispose();");
    toolbar.add(b);

    b = new JButton(new CloseIcon(Color.GREEN));
    b.addActionListener(e -> getSelectedFrame(desktop).ifPresent(desktop.getDesktopManager()::closeFrame));
    b.setToolTipText("desktop.getDesktopManager().closeFrame(f);");
    toolbar.add(b);

    b = new JButton(new CloseIcon(Color.BLUE));
    b.addActionListener(e -> getSelectedFrame(desktop).ifPresent(JInternalFrame::doDefaultCloseAction));
    b.setToolTipText("f.doDefaultCloseAction();");
    toolbar.add(b);

    b = new JButton(new CloseIcon(Color.YELLOW));
    b.addActionListener(e -> {
      getSelectedFrame(desktop).ifPresent(f -> {
        try {
          f.setClosed(true);
        } catch (PropertyVetoException ex) {
          ex.printStackTrace();
        }
      });
    });
    b.setToolTipText("f.setClosed(true);");
    toolbar.add(b);

    return toolbar;
  }

  private static JInternalFrame makeInternalFrame(JDesktopPane desktop) {
    JInternalFrame f = new JInternalFrame(String.format("Document #%s", ++openFrameCount), true, true, true, true);
    row += 1;
    f.setSize(240, 120);
    f.setLocation(20 * row + 20 * col, 20 * row);
    f.setVisible(true);
    EventQueue.invokeLater(() -> {
      Rectangle drect = desktop.getBounds();
      drect.setLocation(0, 0);
      if (!drect.contains(f.getBounds())) {
        row = 0;
        col += 1;
      }
    });
    f.addInternalFrameListener(new TestInternalFrameListener());
    return f;
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class TestInternalFrameListener implements InternalFrameListener {
  @Override public void internalFrameClosing(InternalFrameEvent e) {
    System.out.println("internalFrameClosing: " + e.getInternalFrame().getTitle());
  }

  @Override public void internalFrameClosed(InternalFrameEvent e) {
    System.out.println("internalFrameClosed: " + e.getInternalFrame().getTitle());
  }

  @Override public void internalFrameOpened(InternalFrameEvent e) {
    System.out.println("internalFrameOpened: " + e.getInternalFrame().getTitle());
  }

  @Override public void internalFrameIconified(InternalFrameEvent e) {
    System.out.println("internalFrameIconified: " + e.getInternalFrame().getTitle());
  }

  @Override public void internalFrameDeiconified(InternalFrameEvent e) {
    System.out.println("internalFrameDeiconified: " + e.getInternalFrame().getTitle());
  }

  @Override public void internalFrameActivated(InternalFrameEvent e) {
    // System.out.println("internalFrameActivated: " + e.getInternalFrame().getTitle());
  }

  @Override public void internalFrameDeactivated(InternalFrameEvent e) {
    System.out.println("internalFrameDeactivated: " + e.getInternalFrame().getTitle());
  }
}

// class ToolBarButton extends JButton {
//   private transient MouseListener handler;
//   protected ToolBarButton(Icon icon) {
//     super(icon);
//   }
//   @Override public void updateUI() {
//     removeMouseListener(handler);
//     super.updateUI();
//     setContentAreaFilled(false);
//     setFocusPainted(false);
//     handler = new MouseAdapter() {
//       @Override public void mouseEntered(MouseEvent e) {
//         setContentAreaFilled(true);
//       }
//       @Override public void mouseExited(MouseEvent e) {
//         setContentAreaFilled(false);
//       }
//     };
//     addMouseListener(handler);
//   }
// }

class CloseIcon implements Icon {
  private final Color color;

  protected CloseIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(color);
    g2.drawLine(4,  4, 11, 11);
    g2.drawLine(4,  5, 10, 11);
    g2.drawLine(5,  4, 11, 10);
    g2.drawLine(11, 4,  4, 11);
    g2.drawLine(11, 5,  5, 11);
    g2.drawLine(10, 4,  4, 10);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 16;
  }

  @Override public int getIconHeight() {
    return 16;
  }
}
