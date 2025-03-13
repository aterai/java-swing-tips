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
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    JLabel smallLabel = makeLabel(new Dimension(16, 16));
    JLabel largeLabel = makeLabel(new Dimension(32, 32));
    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createTitledBorder("drop File"));
    box.add(smallLabel);
    box.add(Box.createHorizontalStrut(5));
    box.add(largeLabel);
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    add(box);
    String s1 = "Warning: ShellFolder is internal proprietary API";
    String s2 = "and may be removed in a future release";
    add(new JLabel(String.format("<html>%s<br>%s", s1, s2)));
    DropTargetListener dtl = new DropTargetAdapter() {
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
            Object o = list.get(0);
            if (o instanceof File) {
              File file = (File) o;
              smallLabel.setIcon(FileSystemView.getFileSystemView().getSystemIcon(file));
              largeLabel.setIcon(new ImageIcon(getLargeIconImage(file)));
            }
            e.dropComplete(true);
          } else {
            e.rejectDrop();
          }
        } catch (UnsupportedFlavorException | IOException ex) {
          e.rejectDrop();
        }
      }

      private Image getLargeIconImage(File file) throws FileNotFoundException {
        return sun.awt.shell.ShellFolder.getShellFolder(file).getIcon(true);
      }
    };
    setDropTarget(new DropTarget(this, DnDConstants.ACTION_COPY, dtl, true));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JLabel makeLabel(Dimension size) {
    return new JLabel() {
      @Override public Dimension getPreferredSize() {
        return new Dimension(size.width + 1, size.height + 1);
      }

      @Override public Dimension getMaximumSize() {
        return getPreferredSize();
      }

      @Override public void updateUI() {
        super.updateUI();
        setBorder(BorderFactory.createLineBorder(Color.GRAY));
        setAlignmentY(BOTTOM_ALIGNMENT);
      }
    };
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
