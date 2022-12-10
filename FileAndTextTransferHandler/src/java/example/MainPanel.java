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
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import javax.swing.*;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
  private final JTabbedPane tabbedPane = new JTabbedPane();

  private MainPanel() {
    super(new BorderLayout());
    EventQueue.invokeLater(() -> {
      JRootPane root = getRootPane();
      root.setTransferHandler(new FileTransferHandler());
      JLayeredPane layer = root.getLayeredPane();
      layer.setTransferHandler(new FileTransferHandler());
      Container container = root.getContentPane();
      if (container instanceof JComponent) {
        ((JComponent) container).setTransferHandler(new FileTransferHandler());
      }
      Window window = SwingUtilities.getWindowAncestor(tabbedPane);
      if (window instanceof JFrame) {
        ((JFrame) window).setTransferHandler(new FileTransferHandler());
      }
      // Component glassPane = root.getGlassPane();
      // if (glassPane instanceof JComponent) {
      //   ((JComponent) glassPane).setTransferHandler(new FileTransferHandler());
      //   glassPane.setVisible(true);
      // }
    });

    JTextArea textArea = new JTextArea();
    textArea.setDragEnabled(true);

    tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    tabbedPane.addTab("Default", new JScrollPane(textArea));
    addTab(null);

    JButton button = new JButton("open");
    button.addActionListener(e -> {
      JFileChooser chooser = new JFileChooser();
      int retValue = chooser.showOpenDialog(tabbedPane.getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        addTab(chooser.getSelectedFile());
      }
    });

    JTextField field = new JTextField(16);
    field.setText("setDragEnabled(true)");
    field.setDragEnabled(true);

    JPanel p = new JPanel();
    p.add(field);
    p.add(button);

    add(p, BorderLayout.NORTH);
    add(tabbedPane);
    setPreferredSize(new Dimension(320, 240));
  }

  public void addTab(File file) {
    JTextArea textArea = new JTextArea();
    textArea.setDragEnabled(true);

    // TransferHandler textHandler = textArea.getTransferHandler();
    // // System.out.println("TextTransferHandler" + textHandler);
    // TransferHandler fileHandler = new FileTransferHandler();
    // textArea.setTransferHandler(new TransferHandler() {
    //   @Override public boolean canImport(TransferSupport support) {
    //     return fileHandler.canImport(support) || textHandler.canImport(support);
    //   }
    //
    //   @Override public boolean importData(TransferSupport support) {
    //     if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
    //       return fileHandler.importData(support);
    //     } else {
    //       return textHandler.importData(support);
    //     }
    //   }
    // });

    new DropTarget(textArea, DnDConstants.ACTION_COPY, new FileDropTargetListener(), true);

    if (file != null) {
      try (Reader in = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
        textArea.read(in, "File");
        tabbedPane.addTab(file.getName(), new JScrollPane(textArea));
      } catch (IOException ex) {
        UIManager.getLookAndFeel().provideErrorFeedback(textArea);
      }
    } else {
      tabbedPane.addTab("*untitled*", new JScrollPane(textArea));
    }
    tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
  }

  public void addFile(Transferable transferable) throws UnsupportedFlavorException, IOException {
    List<?> list = (List<?>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
    new SwingWorker<Void, Void>() {
      @Override protected Void doInBackground() {
        for (Object o : list) {
          if (o instanceof File) {
            addTab((File) o);
          }
        }
        return null;
      }
    }.execute();
  }

  private class FileDropTargetListener extends DropTargetAdapter {
    @Override public void drop(DropTargetDropEvent e) {
      Transferable transferable = e.getTransferable();
      if (e.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        e.acceptDrop(DnDConstants.ACTION_COPY);
        try {
          addFile(transferable);
          e.dropComplete(true);
        } catch (UnsupportedFlavorException | IOException ex) {
          e.dropComplete(false);
        }
      } else {
        JTextComponent textArea = (JTextComponent) e.getSource();
        TransferHandler textHandler = textArea.getTransferHandler();
        if (textHandler != null) {
          textHandler.importData(textArea, transferable);
        }
      }
    }
  }

  private class FileTransferHandler extends TransferHandler {
    @Override public boolean canImport(TransferSupport support) {
      // System.out.println(support.getComponent().getClass().getName());
      boolean isDrop = support.isDrop();
      boolean supported = support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
      return supported && isDrop;
    }

    @Override public boolean importData(TransferSupport support) {
      // System.out.println(support.getComponent().getClass().getName());
      Transferable transferable = support.getTransferable();
      try {
        addFile(transferable);
        return true;
      } catch (IOException | UnsupportedFlavorException ex) {
        return false;
      }
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
