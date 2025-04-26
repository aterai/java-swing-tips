// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Optional;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final JLabel label = new JLabel();
  private final transient Icon i1 = makeIcon("example/i03-04.gif");
  private final transient Icon i2 = makeIcon("example/i03-10.gif");
  private File file;

  private MainPanel() {
    super(new BorderLayout());
    label.setVerticalTextPosition(SwingConstants.BOTTOM);
    label.setVerticalAlignment(SwingConstants.CENTER);
    label.setHorizontalTextPosition(SwingConstants.CENTER);
    label.setHorizontalAlignment(SwingConstants.CENTER);
    label.setBorder(BorderFactory.createTitledBorder("Drag Source JLabel"));
    clearFile();

    // // JDK 1.5.0
    // DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
    //       label, DnDConstants.ACTION_MOVE, new DragGestureListener() {
    //   @Override public void dragGestureRecognized(DragGestureEvent e) {
    //     File tmpFile = getFile();
    //     if (Objects.isNull(tmpFile)) {
    //       return;
    //     }
    //     DragSourceAdapter dsa = new DragSourceAdapter() {
    //       @Override public void dragDropEnd(DragSourceDropEvent ev) {
    //         if (ev.getDropSuccess()) {
    //           clearFile();
    //         }
    //       }
    //     };
    //     e.startDrag(DragSource.DefaultMoveDrop, new FileTransferable(tmpFile), dsa);
    //   }
    // });

    label.setTransferHandler(new TransferHandler() {
      @Override public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
      }

      @Override protected Transferable createTransferable(JComponent c) {
        return Optional.ofNullable(getFile()).map(FileTransferable::new).orElse(null);
      }

      @Override protected void exportDone(JComponent c, Transferable data, int action) {
        cleanup(c, action == MOVE);
      }

      private void cleanup(JComponent c, boolean isMoved) {
        if (isMoved) {
          clearFile();
          c.repaint();
        }
      }
    });
    label.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        JComponent c = (JComponent) e.getComponent();
        c.getTransferHandler().exportAsDrag(c, e, TransferHandler.COPY);
      }
    });

    JButton button = new JButton("Create Temp File");
    button.addActionListener(e -> {
      File outfile;
      try {
        outfile = createTempFile();
      } catch (IOException ex) {
        errorFeedback((JComponent) e.getSource());
        return;
      }
      setFile(outfile);
    });

    JButton clearButton = new JButton("Clear");
    clearButton.addActionListener(e -> {
      clearFile();
      label.repaint();
    });

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    box.add(Box.createHorizontalGlue());
    box.add(button);
    box.add(Box.createHorizontalStrut(2));
    box.add(clearButton);
    add(label);
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void errorFeedback(JComponent c) {
    UIManager.getLookAndFeel().provideErrorFeedback(c);
    JRootPane root = c.getRootPane();
    String msg = "Could not create file.";
    String title = "Error";
    JOptionPane.showMessageDialog(root, msg, title, JOptionPane.ERROR_MESSAGE);
  }

  private File createTempFile() throws IOException {
    File tempFile = File.createTempFile("test", ".tmp");
    tempFile.deleteOnExit();
    return tempFile;
  }

  private File getFile() {
    return file;
  }

  public void setFile(File newFile) {
    file = newFile;
    label.setIcon(i2);
    label.setText("tmpFile#exists() = true; // draggable");
  }

  @SuppressWarnings("PMD.NullAssignment")
  public void clearFile() {
    file = null;
    label.setIcon(i1);
    label.setText("tmpFile#exists() = false");
  }

  private static Icon makeIcon(String path) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return new ImageIcon(ImageIO.read(s));
      } catch (IOException ex) {
        return makeMissingIcon();
      }
    }).orElseGet(MainPanel::makeMissingIcon);
  }

  private static Icon makeMissingIcon() {
    return UIManager.getIcon("OptionPane.errorIcon");
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

class FileTransferable implements Transferable {
  private final File file;

  protected FileTransferable(File file) {
    this.file = file;
  }

  @Override public Object getTransferData(DataFlavor flavor) {
    return Collections.singletonList(file);
  }

  @Override public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] {DataFlavor.javaFileListFlavor};
  }

  @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
    return flavor.equals(DataFlavor.javaFileListFlavor);
  }
}
