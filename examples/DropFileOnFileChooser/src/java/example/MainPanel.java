// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea log = new JTextArea();
    JCheckBox check = new JCheckBox("isMultiSelection");

    JButton button1 = new JButton("Default");
    button1.addActionListener(e -> {
      JFileChooser chooser = new JFileChooser();
      showOpenDialog(chooser, check.isSelected(), log);
    });

    JButton button2 = new JButton("TransferHandler");
    button2.addActionListener(e -> {
      JFileChooser chooser = new JFileChooser();
      chooser.setTransferHandler(new FileChooserTransferHandler());
      showOpenDialog(chooser, check.isSelected(), log);
    });

    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder("JFileChooser"));
    p.add(button1);
    p.add(button2);
    p.add(check);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));
  }

  private static void showOpenDialog(JFileChooser chooser, boolean multi, JTextArea log) {
    chooser.setMultiSelectionEnabled(multi);
    int retValue = chooser.showOpenDialog(log.getRootPane());
    if (retValue == JFileChooser.APPROVE_OPTION) {
      // In multi-selection mode, log all selected files, not just the first one
      File[] files = multi ? chooser.getSelectedFiles() : new File[] {chooser.getSelectedFile()};
      log.setText("");
      for (File f : files) {
        log.append(f.getAbsolutePath() + "\n");
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

class FileChooserTransferHandler extends TransferHandler {
  @Override public boolean canImport(TransferSupport support) {
    boolean canDrop = support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    boolean isTarget = support.getComponent() instanceof JFileChooser;
    // boolean isMultiSelection = true;
    // if (isTarget && canDrop) {
    //   try {
    //     JFileChooser fc = (JFileChooser) support.getComponent();
    //     // XXX: java.awt.dnd.InvalidDnDOperationException: No drop current
    //     List<?> list = (List<?>) support.getTransferable()
    //         .getTransferData(DataFlavor.javaFileListFlavor);
    //     isMultiSelection = list.size() != 1;
    //   } catch (IOException | UnsupportedFlavorException ex) {
    //     ex.printStackTrace();
    //   }
    // }
    return support.isDrop() && canDrop && isTarget; // && !isMultiSelection;
  }

  @Override public boolean importData(TransferSupport support) {
    File[] files = getFiles(support.getTransferable());
    Component c = support.getComponent();
    boolean ret = c instanceof JFileChooser && files.length > 0;
    if (ret) {
      updateSelectedFiles((JFileChooser) c, files);
    }
    return ret;
  }

  private static File[] getFiles(Transferable transferable) {
    Object data;
    try {
      data = transferable.getTransferData(DataFlavor.javaFileListFlavor);
    } catch (IOException | UnsupportedFlavorException ex) {
      data = Collections.emptyList();
    }
    // Skip unexpected elements instead of throwing ClassCastException
    return data instanceof List<?>
        ? ((List<?>) data).stream()
            .filter(File.class::isInstance)
            .map(File.class::cast)
            .toArray(File[]::new)
        : new File[0];
  }

  private static void updateSelectedFiles(JFileChooser fileChooser, File... files) {
    if (fileChooser.isMultiSelectionEnabled()) {
      fileChooser.setSelectedFiles(files);
    } else {
      File file = files[0];
      if (file.isDirectory()) {
        fileChooser.setCurrentDirectory(file);
      } else {
        fileChooser.setSelectedFile(file);
      }
    }
  }
}
