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
import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

public final class MainPanel extends JPanel {
  private final JTabbedPane tabbedPane = new JTabbedPane();

  private MainPanel() {
    super(new BorderLayout());
    EventQueue.invokeLater(this::setFileTransferHandler);

    JTextArea textArea = new JTextArea();
    textArea.setDragEnabled(true);

    tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    tabbedPane.addTab("Default", new JScrollPane(textArea));
    addTab("*untitled*", new PlainDocument());

    JButton button = new JButton("open");
    button.addActionListener(e -> {
      JFileChooser chooser = new JFileChooser();
      int retValue = chooser.showOpenDialog(tabbedPane.getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        addFileTabs(Collections.singletonList(chooser.getSelectedFile()));
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

  private void setFileTransferHandler() {
    JRootPane root = getRootPane();
    FileTransferHandler handler = new FileTransferHandler();
    root.setTransferHandler(handler);
    root.getLayeredPane().setTransferHandler(handler);
    Container container = root.getContentPane();
    if (container instanceof JComponent) {
      ((JComponent) container).setTransferHandler(handler);
    }
    Window window = SwingUtilities.getWindowAncestor(root);
    if (window instanceof JFrame) {
      ((JFrame) window).setTransferHandler(handler);
    }
    // Component glassPane = root.getGlassPane();
    // if (glassPane instanceof JComponent) {
    //   ((JComponent) glassPane).setTransferHandler(handler);
    //   glassPane.setVisible(true);
    // }
  }

  private void addTab(String title, Document doc) {
    JTextArea textArea = new JTextArea(doc);
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
    tabbedPane.addTab(title, new JScrollPane(textArea));
    tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
  }

  private boolean addFileTabs(List<?> list) {
    new FileTabLoader(list).execute();
    return !list.isEmpty();
  }

  private final class FileTabLoader extends SwingWorker<Void, Map.Entry<String, Document>> {
    private final List<?> list;
    // Written in doInBackground() and read in done(),
    // which is guaranteed to run after doInBackground() has completed.
    private boolean failed;

    private FileTabLoader(List<?> list) {
      super();
      this.list = list;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    @Override protected Void doInBackground() {
      // Read the files off the EDT into documents that are not displayed yet,
      // and add the tabs on the EDT in process(...)
      for (Object o : list) {
        if (o instanceof File) {
          File file = (File) o;
          try {
            publish(new AbstractMap.SimpleImmutableEntry<>(file.getName(), readDocument(file)));
          } catch (IOException | BadLocationException ex) {
            failed = true;
          }
        }
      }
      return null;
    }

    @Override protected void process(List<Map.Entry<String, Document>> chunks) {
      for (Map.Entry<String, Document> entry : chunks) {
        addTab(entry.getKey(), entry.getValue());
      }
    }

    @Override protected void done() {
      if (failed) {
        UIManager.getLookAndFeel().provideErrorFeedback(tabbedPane);
      }
    }

    private Document readDocument(File file) throws IOException, BadLocationException {
      Document doc = new PlainDocument();
      try (Reader in = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
        // Same as JTextArea#read(...): DefaultEditorKit normalizes the line separators
        new DefaultEditorKit().read(in, doc, 0);
      }
      return doc;
    }
  }

  private static List<?> getFileList(Transferable transferable) {
    List<?> list;
    try {
      list = (List<?>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
    } catch (UnsupportedFlavorException | IOException ex) {
      list = Collections.emptyList();
    }
    return list;
  }

  private final class FileDropTargetListener extends DropTargetAdapter {
    @Override public void drop(DropTargetDropEvent e) {
      Transferable transferable = e.getTransferable();
      // The source of a DropTargetDropEvent is the DropTargetContext, not the DropTarget
      Component c = e.getDropTargetContext().getComponent();
      if (e.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        e.acceptDrop(DnDConstants.ACTION_COPY);
        e.dropComplete(addFileTabs(getFileList(transferable)));
      } else if (c instanceof JTextComponent) {
        // The drop must be accepted before the dropped data can be transferred
        e.acceptDrop(DnDConstants.ACTION_COPY);
        e.dropComplete(importText((JTextComponent) c, transferable));
      } else {
        e.rejectDrop();
      }
    }

    private boolean importText(JTextComponent textComponent, Transferable transferable) {
      TransferHandler textHandler = textComponent.getTransferHandler();
      return textHandler != null && textHandler.importData(textComponent, transferable);
    }
  }

  private final class FileTransferHandler extends TransferHandler {
    @Override public boolean canImport(TransferSupport support) {
      // System.out.println(support.getComponent().getClass().getName());
      boolean isDrop = support.isDrop();
      boolean supported = support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
      return supported && isDrop;
    }

    @Override public boolean importData(TransferSupport support) {
      // System.out.println(support.getComponent().getClass().getName());
      return addFileTabs(getFileList(support.getTransferable()));
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
