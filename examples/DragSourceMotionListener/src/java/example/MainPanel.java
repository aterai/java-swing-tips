// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel {
  private MainPanel() {
    /* Singleton */
  }

  private static void addLabel(JPanel p) {
    p.add(new JLabel(UIManager.getIcon("OptionPane.warningIcon")));
    p.add(new JLabel(UIManager.getIcon("OptionPane.questionIcon")));
    p.add(new JLabel(UIManager.getIcon("OptionPane.informationIcon")));
    p.add(new JLabel(UIManager.getIcon("OptionPane.errorIcon")));
    p.add(new JLabel("Text"));
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
    JFrame f1 = new JFrame("@title@");
    JFrame f2 = new JFrame();
    Stream.of(f1, f2).forEach(f -> {
      f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      f.setSize(320, 240);
    });

    DragPanel p1 = new DragPanel();
    addLabel(p1);
    DragPanel p2 = new DragPanel();
    MouseListener handler = new Handler();
    LabelTransferHandler th = new LabelTransferHandler();
    Stream.of(p1, p2).forEach(p -> {
      p.addMouseListener(handler);
      p.setTransferHandler(th);
      p.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    });

    JPanel p = new JPanel(new GridLayout(2, 1));
    p.add(new JScrollPane(new JTextArea()));
    p.add(p2);
    f1.getContentPane().add(p1);
    f2.getContentPane().add(p);
    f1.setLocationRelativeTo(null);
    Point pt = f1.getLocation();
    pt.translate(340, 0);
    f2.setLocation(pt);
    f1.setVisible(true);
    f2.setVisible(true);
  }
}

class DragPanel extends JPanel {
  private JLabel draggingLabel;

  public JLabel getDraggingLabel() {
    return draggingLabel;
  }

  public void setDraggingLabel(JLabel draggingLabel) {
    this.draggingLabel = draggingLabel;
  }
}

class Handler extends MouseAdapter {
  @Override public void mousePressed(MouseEvent e) {
    DragPanel p = (DragPanel) e.getComponent();
    Component c = SwingUtilities.getDeepestComponentAt(p, e.getX(), e.getY());
    if (c instanceof JLabel) {
      p.setDraggingLabel((JLabel) c);
      p.getTransferHandler().exportAsDrag(p, e, TransferHandler.MOVE);
    }
  }
}

class LabelTransferHandler extends TransferHandler {
  private final DataFlavor localObjectFlavor = new DataFlavor(DragPanel.class, "DragPanel");
  private final JLabel label = new JLabel() {
    @Override public boolean contains(int x, int y) {
      return false;
    }
  };
  private final JWindow window = new JWindow();

  protected LabelTransferHandler() {
    super("Text");
    // System.out.println("LabelTransferHandler");
    // flavor = new ActivationDataFlavor(
    //     DragPanel.class, DataFlavor.javaJVMLocalObjectMimeType, "JLabel");
    window.add(label);
    // AccessControlException: access denied ("java.awt.AWTPermission" "setWindowAlwaysOnTop")
    // window.setAlwaysOnTop(true);
    // AWTUtilities.setWindowOpaque(window, false); // JDK 1.6.0
    GraphicsConfiguration gc = window.getGraphicsConfiguration();
    if (gc != null && gc.isTranslucencyCapable()) {
      window.setBackground(new Color(0x0, true)); // Java 1.7.0
    }
    DragSource.getDefaultDragSource().addDragSourceMotionListener(e -> {
      Point pt = e.getLocation();
      // pt.translate(5, 5); // offset
      window.setLocation(pt);
    });
  }

  @Override protected Transferable createTransferable(JComponent c) {
    // System.out.println("createTransferable" + localObjectFlavor.getMimeType());
    DragPanel p = (DragPanel) c;
    return new LabelTransferable(localObjectFlavor, p);
    //  DataHandler dh = new DataHandler(c, localObjectFlavor.getMimeType());
    //  return Optional.ofNullable(l.getText())
    //    // .map(text -> (Transferable) new LabelTransferable(dh, localObjectFlavor, text))
    //    .<Transferable>map(text -> new LabelTransferable(dh, localObjectFlavor, text));
    //    .orElse(dh);
    //  // String text = l.getText();
    //  // if (Objects.nonNull(text)) {
    //  //   return new LabelTransferable(dh, localObjectFlavor, text);
    //  // } else {
    //  //   return dh;
    //  // }
  }

  @Override public boolean canImport(TransferSupport support) {
    return support.isDrop() && support.isDataFlavorSupported(localObjectFlavor);
  }

  @Override public int getSourceActions(JComponent c) {
    // System.out.println("getSourceActions");
    if (c instanceof DragPanel) {
      DragPanel p = (DragPanel) c;
      JLabel l = p.getDraggingLabel();
      label.setIcon(l.getIcon());
      label.setText(l.getText());
      window.pack();
      Point pt = l.getLocation();
      SwingUtilities.convertPointToScreen(pt, p);
      window.setLocation(pt);
      window.setVisible(true);
    }
    return MOVE;
  }

  @Override public boolean importData(TransferSupport support) {
    // System.out.println("importData");
    return getTransferableData(support.getTransferable())
        .filter(DragPanel.class::isInstance)
        .map(DragPanel.class::cast)
        .map(o -> copyDragPanel(o, support.getComponent()))
        .orElse(false);
  }

  private static boolean copyDragPanel(DragPanel src, Component tgt) {
    boolean b = tgt instanceof DragPanel;
    if (b) {
      JLabel l = new JLabel();
      l.setIcon(src.getDraggingLabel().getIcon());
      l.setText(src.getDraggingLabel().getText());
      DragPanel tgtPanel = (DragPanel) tgt;
      tgtPanel.add(l);
      tgtPanel.revalidate();
    }
    return b;
  }

  private Optional<Object> getTransferableData(Transferable transferable) {
    Optional<Object> src;
    try {
      src = Optional.of(transferable.getTransferData(localObjectFlavor));
    } catch (UnsupportedFlavorException | IOException ex) {
      src = Optional.empty();
    }
    return src;
  }

  @Override protected void exportDone(JComponent c, Transferable data, int action) {
    // System.out.println("exportDone");
    DragPanel src = (DragPanel) c;
    if (action == MOVE) {
      src.remove(src.getDraggingLabel());
      src.revalidate();
      src.repaint();
    }
    src.setDraggingLabel(null);
    window.setVisible(false);
  }
}

class LabelTransferable implements Transferable {
  // private final DataHandler dh;
  private final DataFlavor localObjectFlavor;
  private final StringSelection ss;
  private final DragPanel panel;

  protected LabelTransferable(DataFlavor localObjectFlavor, DragPanel panel) {
    // this.dh = dh;
    this.localObjectFlavor = localObjectFlavor;
    this.panel = panel;
    String txt = panel.getDraggingLabel().getText();
    // this.ss = Objects.nonNull(txt) ? new StringSelection(txt + "\n") : null;
    this.ss = Optional.ofNullable(txt)
        .map(s -> new StringSelection(s + "\n"))
        .orElse(null);
  }

  @Override public DataFlavor[] getTransferDataFlavors() {
    List<DataFlavor> list = new ArrayList<>();
    if (Objects.nonNull(ss)) {
      Collections.addAll(list, ss.getTransferDataFlavors());
    }
    list.add(localObjectFlavor);
    // return list.toArray(dh.getTransferDataFlavors());
    return list.toArray(new DataFlavor[0]);
  }

  @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
    // return Stream.of(getTransferDataFlavors()).anyMatch(f -> f.equals(flavor));
    return Arrays.asList(getTransferDataFlavors()).contains(flavor);
  }

  @Override public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    return Objects.equals(flavor, localObjectFlavor) ? panel : ss.getTransferData(flavor);
    // if (flavor.equals(DataFlavor.stringFlavor)) {
    //   return ss.getTransferData(flavor);
    // } else if (flavor.equals(DataFlavor.plainTextFlavor)) {
    //   return ss.getTransferData(flavor);
    // } else if (flavor.equals(localObjectFlavor)) {
    //   return dh.getTransferData(flavor);
    // } else {
    //   throw new UnsupportedFlavorException(flavor);
    // }
  }
}
