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

    JLabel label = new JLabel(new DragHereIcon());
    label.setText("<html>Drag <b>Files</b> Here");
    label.setVerticalTextPosition(SwingConstants.BOTTOM);
    label.setHorizontalTextPosition(SwingConstants.CENTER);
    label.setForeground(Color.GRAY);
    label.setFont(new Font(Font.SERIF, Font.PLAIN, 24));

    label.setDropTarget(new DropTarget(label, DnDConstants.ACTION_COPY, new FileDropTargetAdapter(), true));
    // Test: label.setTransferHandler(new FileTransferHandler());

    add(label);
    setPreferredSize(new Dimension(320, 240));
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

class DragHereIcon implements Icon {
  private static final int ICON_SIZE = 100;
  private static final float BORDER_WIDTH = 8f;
  private static final float SLIT_WIDTH = 8f;
  private static final int ARC_SIZE = 16;
  private static final int SLIT_NUM = 3;
  private static final Shape BORDER = new RoundRectangle2D.Double(
      BORDER_WIDTH, BORDER_WIDTH,
      ICON_SIZE - 2 * BORDER_WIDTH - 1, ICON_SIZE - 2 * BORDER_WIDTH - 1,
      ARC_SIZE, ARC_SIZE);
  private static final Font FONT = new Font(Font.MONOSPACED, Font.BOLD, ICON_SIZE);
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

    g2.setStroke(new BasicStroke(BORDER_WIDTH));
    g2.setPaint(LINE_COLOR);
    g2.draw(BORDER);

    g2.setStroke(new BasicStroke(SLIT_WIDTH));
    g2.setPaint(UIManager.getColor("Panel.background"));

    int n = SLIT_NUM + 1;
    int v = ICON_SIZE / n;
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
    double cx = ICON_SIZE / 2d - b.getCenterX();
    double cy = ICON_SIZE / 2d - b.getCenterY();
    AffineTransform toCenterAtf = AffineTransform.getTranslateInstance(cx, cy);
    g2.fill(toCenterAtf.createTransformedShape(arrow));
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return ICON_SIZE;
  }

  @Override public int getIconHeight() {
    return ICON_SIZE;
  }
}

class FileDropTargetAdapter extends DropTargetAdapter {
  @Override public void dragOver(DropTargetDragEvent dtde) {
    if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
      dtde.acceptDrag(DnDConstants.ACTION_COPY);
      return;
    }
    dtde.rejectDrag();
  }

  @Override public void drop(DropTargetDropEvent dtde) {
    try {
      if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        dtde.acceptDrop(DnDConstants.ACTION_COPY);
        Transferable t = dtde.getTransferable();
        List<?> list = (List<?>) t.getTransferData(DataFlavor.javaFileListFlavor);
        for (Object o: list) {
          if (o instanceof File) {
            File f = (File) o;
            System.out.println(f.getAbsolutePath());
          }
        }
        dtde.dropComplete(true);
      } else {
        dtde.rejectDrop();
      }
    } catch (UnsupportedFlavorException | IOException ex) {
      dtde.rejectDrop();
    }
  }
}

// class FileTransferHandler extends TransferHandler {
//   @Override public boolean importData(TransferHandler.TransferSupport support) {
//     try {
//       if (canImport(support)) {
//         for (Object o: (List) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor)) {
//           if (o instanceof File) {
//             File file = (File) o;
//             System.out.println(file.getAbsolutePath());
//           }
//         }
//         return true;
//       }
//     } catch (Exception ex) {
//       ex.printStackTrace();
//     }
//     return false;
//   }
//   @Override public boolean canImport(TransferHandler.TransferSupport support) {
//     return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
//   }
// }
