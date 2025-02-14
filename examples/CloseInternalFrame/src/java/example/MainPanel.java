// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

public final class MainPanel extends JPanel {
  public static final String LOGGER_NAME = MethodHandles.lookup().lookupClass().getName();
  public static final Logger LOGGER = Logger.getLogger(LOGGER_NAME);
  private static int openFrameCount;
  private static int row;
  private static int col;

  private MainPanel() {
    super(new BorderLayout());
    LOGGER.setUseParentHandlers(false);
    JTextArea textArea = new JTextArea(2, 0);
    textArea.setEditable(false);
    LOGGER.addHandler(new TextAreaHandler(new TextAreaOutputStream(textArea)));
    JScrollPane scroll = new JScrollPane(textArea);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    JDesktopPane desktop = new JDesktopPane();
    InputMap im = desktop.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
    desktop.getActionMap().put("escape", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        getSelectedFrame(desktop).ifPresent(desktop.getDesktopManager()::closeFrame);
      }
    });

    add(createToolBar(desktop), BorderLayout.NORTH);
    add(desktop);
    add(scroll, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  public static Optional<JInternalFrame> getSelectedFrame(JDesktopPane desktop) {
    return Optional.ofNullable(desktop.getSelectedFrame());
  }

  private static JToolBar createToolBar(JDesktopPane desktop) {
    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);
    JButton b1 = new JButton(UIManager.getIcon("FileView.fileIcon"));
    b1.addActionListener(e -> {
      JInternalFrame frame = makeInternalFrame(desktop);
      try {
        frame.setSelected(true);
        if (openFrameCount % 2 == 0) {
          frame.setIcon(true);
        }
      } catch (PropertyVetoException ex) {
        throw new IllegalStateException(ex);
      }
    });
    b1.setToolTipText("create new InternalFrame");
    toolBar.add(b1);
    toolBar.add(Box.createGlue());

    JButton b2 = new JButton(new CloseIcon(Color.RED));
    b2.addActionListener(e -> getSelectedFrame(desktop)
        .ifPresent(JInternalFrame::dispose));
    b2.setToolTipText("f.dispose();");
    toolBar.add(b2);

    JButton b3 = new JButton(new CloseIcon(Color.GREEN));
    b3.addActionListener(e -> getSelectedFrame(desktop)
        .ifPresent(desktop.getDesktopManager()::closeFrame));
    b3.setToolTipText("desktop.getDesktopManager().closeFrame(f);");
    toolBar.add(b3);

    JButton b4 = new JButton(new CloseIcon(Color.BLUE));
    b4.addActionListener(e -> getSelectedFrame(desktop)
        .ifPresent(JInternalFrame::doDefaultCloseAction));
    b4.setToolTipText("f.doDefaultCloseAction();");
    toolBar.add(b4);

    JButton b5 = new JButton(new CloseIcon(Color.YELLOW));
    b5.addActionListener(e -> getSelectedFrame(desktop).ifPresent(f -> {
      try {
        f.setClosed(true);
      } catch (PropertyVetoException ex) {
        throw new IllegalStateException(ex);
      }
    }));
    b5.setToolTipText("f.setClosed(true);");
    toolBar.add(b5);
    return toolBar;
  }

  private static JInternalFrame makeInternalFrame(JDesktopPane desktop) {
    String title = String.format("Document #%s", ++openFrameCount);
    JInternalFrame f = new JInternalFrame(title, true, true, true, true);
    desktop.add(f);
    row += 1;
    f.setSize(240, 120);
    f.setLocation(20 * row + 20 * col, 20 * row);
    EventQueue.invokeLater(() -> {
      f.setVisible(true);
      Rectangle rect = desktop.getBounds();
      rect.setLocation(0, 0);
      if (!rect.contains(f.getBounds())) {
        row = 0;
        col += 1;
      }
    });
    f.addInternalFrameListener(new InternalFrameHandler());
    return f;
  }

  private static final class InternalFrameHandler implements InternalFrameListener {
    @Override public void internalFrameClosing(InternalFrameEvent e) {
      LOGGER.info(() -> "internalFrameClosing: " + e.getInternalFrame().getTitle());
    }

    @Override public void internalFrameClosed(InternalFrameEvent e) {
      LOGGER.info(() -> "internalFrameClosed: " + e.getInternalFrame().getTitle());
    }

    @Override public void internalFrameOpened(InternalFrameEvent e) {
      LOGGER.info(() -> "internalFrameOpened: " + e.getInternalFrame().getTitle());
    }

    @Override public void internalFrameIconified(InternalFrameEvent e) {
      LOGGER.info(() -> "internalFrameIconified: " + e.getInternalFrame().getTitle());
    }

    @Override public void internalFrameDeiconified(InternalFrameEvent e) {
      LOGGER.info(() -> "internalFrameDeiconified: " + e.getInternalFrame().getTitle());
    }

    @Override public void internalFrameActivated(InternalFrameEvent e) {
      LOGGER.info(() -> "internalFrameActivated: " + e.getInternalFrame().getTitle());
    }

    @Override public void internalFrameDeactivated(InternalFrameEvent e) {
      LOGGER.info(() -> "internalFrameDeactivated: " + e.getInternalFrame().getTitle());
    }
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

// class ToolBarButton extends JButton {
//   private transient MouseListener handler;
//   protected ToolBarButton(Icon icon) {
//     super(icon);
//   }
//
//   @Override public void updateUI() {
//     removeMouseListener(handler);
//     super.updateUI();
//     setContentAreaFilled(false);
//     setFocusPainted(false);
//     handler = new MouseAdapter() {
//       @Override public void mouseEntered(MouseEvent e) {
//         setContentAreaFilled(true);
//       }
//
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

class TextAreaOutputStream extends OutputStream {
  private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
  private final JTextArea textArea;

  protected TextAreaOutputStream(JTextArea textArea) {
    super();
    this.textArea = textArea;
  }

  // // Java 10:
  // @Override public void flush() {
  //   textArea.append(buffer.toString(StandardCharsets.UTF_8));
  //   buffer.reset();
  // }

  @Override public void flush() throws IOException {
    textArea.append(buffer.toString("UTF-8"));
    buffer.reset();
  }

  @Override public void write(int b) {
    buffer.write(b);
  }

  @Override public void write(byte[] b, int off, int len) {
    buffer.write(b, off, len);
  }
}

class TextAreaHandler extends StreamHandler {
  protected TextAreaHandler(OutputStream os) {
    super(os, new SimpleFormatter());
  }

  @Override public String getEncoding() {
    return StandardCharsets.UTF_8.name();
  }

  // [UnsynchronizedOverridesSynchronized]
  // Unsynchronized method publish overrides synchronized method in StreamHandler
  @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
  @Override public synchronized void publish(LogRecord logRecord) {
    super.publish(logRecord);
    flush();
  }

  // [UnsynchronizedOverridesSynchronized]
  // Unsynchronized method close overrides synchronized method in StreamHandler
  @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
  @Override public synchronized void close() {
    flush();
  }
}
