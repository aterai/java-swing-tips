// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    JLabel l = new JLabel(new DragHereIcon());
    l.setText("<html>Drag <b>Files</b> Here");
    l.setVerticalTextPosition(SwingConstants.BOTTOM);
    l.setHorizontalTextPosition(SwingConstants.CENTER);
    l.setForeground(Color.GRAY);
    l.setFont(new Font(Font.SERIF, Font.PLAIN, 24));
    l.setDropTarget(new DropTarget(l, DnDConstants.ACTION_COPY, new FileDropListener(), true));
    // Test: l.setTransferHandler(new FileTransferHandler());
    add(l);
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

class DragHereIcon implements Icon {
  private static final int ICON_WIDTH = 100;
  private static final int ICON_HEIGHT = 100;
  private static final float BORDER_SIZE = 8f;
  private static final float SLIT_WIDTH = 8f;
  private static final int ARC_SIZE = 16;
  private static final int SLIT_NUM = 3;
  private static final Shape BORDER = new RoundRectangle2D.Double(
      BORDER_SIZE, BORDER_SIZE,
      ICON_WIDTH - 2 * BORDER_SIZE - 1, ICON_HEIGHT - 2 * BORDER_SIZE - 1,
      ARC_SIZE, ARC_SIZE);
  private static final Font FONT = new Font(Font.MONOSPACED, Font.BOLD, ICON_WIDTH);
  // FontRenderContext FRC = new FontRenderContext(null, true, true);
  // U+21E9: DOWNWARDS WHITE ARROW
  // Shape ARROW = new TextLayout("\u21E9", FONT, FRC).getOutline(null);
  // U+2B07: DOWNWARDS BLACK ARROW
  // private static final Shape ARROW = new TextLayout("\u2B07", font, frc).getOutline(null);
  private static final Color LINE_COLOR = Color.GRAY;

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.translate(x, y);

    g2.setStroke(new BasicStroke(BORDER_SIZE));
    g2.setPaint(LINE_COLOR);
    g2.draw(BORDER);

    g2.setStroke(new BasicStroke(SLIT_WIDTH));
    g2.setPaint(UIManager.getColor("Panel.background"));

    int n = SLIT_NUM + 1;
    int v = ICON_WIDTH / n;
    int m = n * v;
    for (int i = 1; i < n; i++) {
      int a = i * v;
      g2.drawLine(a, 0, a, m);
      g2.drawLine(0, a, m, a);
    }

    // g2.drawLine(1 * v, 0 * v, 1 * v, 4 * v);
    // g2.drawLine(2 * v, 0 * v, 2 * v, 4 * v);
    // g2.drawLine(3 * v, 0 * v, 3 * v, 4 * v);
    // g2.drawLine(0 * v, 1 * v, 4 * v, 1 * v);
    // g2.drawLine(0 * v, 2 * v, 4 * v, 2 * v);
    // g2.drawLine(0 * v, 3 * v, 4 * v, 3 * v);

    FontRenderContext frc = g2.getFontRenderContext();
    Shape arrow = new TextLayout("⇩", FONT, frc).getOutline(null);
    g2.setPaint(LINE_COLOR);
    Rectangle2D b = arrow.getBounds2D();
    double cx = ICON_WIDTH / 2d - b.getCenterX();
    double cy = ICON_HEIGHT / 2d - b.getCenterY();
    AffineTransform toCenterAt = AffineTransform.getTranslateInstance(cx, cy);
    g2.fill(toCenterAt.createTransformedShape(arrow));
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return ICON_WIDTH;
  }

  @Override public int getIconHeight() {
    return ICON_HEIGHT;
  }
}

class FileDropListener extends DropTargetAdapter {
  @Override public void dragOver(DropTargetDragEvent e) {
    if (e.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
      e.acceptDrag(DnDConstants.ACTION_COPY);
      return;
    }
    e.rejectDrag();
  }

  @Override public void drop(DropTargetDropEvent e) {
    try {
      if (e.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        e.acceptDrop(DnDConstants.ACTION_COPY);
        Transferable t = e.getTransferable();
        List<?> list = (List<?>) t.getTransferData(DataFlavor.javaFileListFlavor);
        String str = list.stream()
            .filter(File.class::isInstance)
            .map(o -> ((File) o).getAbsolutePath() + "<br>")
            .reduce("<html>", String::concat);
        JOptionPane.showMessageDialog(null, str);
        e.dropComplete(true);
      } else {
        e.rejectDrop();
      }
    } catch (UnsupportedFlavorException | IOException ex) {
      e.rejectDrop();
    }
  }
}

// class FileTransferHandler extends TransferHandler {
//   @Override public boolean importData(TransferSupport support) {
//     Transferable transferable = support.getTransferable();
//     try {
//       for (Object o : (List<?>) transferable.getTransferData(DataFlavor.javaFileListFlavor)) {
//         if (o instanceof File) {
//           File file = (File) o;
//           System.out.println(file.getAbsolutePath());
//         }
//       }
//       return true;
//     } catch (Exception ex) {
//       ex.printStackTrace();
//     }
//     return false;
//   }
//
//   @Override public boolean canImport(TransferSupport support) {
//     return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
//   }
// }
