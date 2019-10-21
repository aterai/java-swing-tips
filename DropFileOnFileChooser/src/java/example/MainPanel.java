// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea log = new JTextArea();
    JCheckBox check = new JCheckBox("isMultiSelection");

    JButton button1 = new JButton("Default");
    button1.addActionListener(e -> {
      JFileChooser chooser = new JFileChooser();
      chooser.setMultiSelectionEnabled(check.isSelected());
      int retValue = chooser.showOpenDialog(log.getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        log.setText(chooser.getSelectedFile().getAbsolutePath());
      }
    });

    JButton button2 = new JButton("TransferHandler");
    button2.addActionListener(e -> {
      JFileChooser chooser = new JFileChooser();
      chooser.setMultiSelectionEnabled(check.isSelected());
      chooser.setTransferHandler(new FileChooserTransferHandler());
      int retValue = chooser.showOpenDialog(log.getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        log.setText(chooser.getSelectedFile().getAbsolutePath());
      }
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

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
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
    //     List<?> list = (List<?>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
    //     isMultiSelection = list.size() != 1;
    //   } catch (IOException | UnsupportedFlavorException ex) {
    //     ex.printStackTrace();
    //   }
    // }
    return support.isDrop() && canDrop && isTarget; // && !isMultiSelection;
  }

  @Override public boolean importData(TransferSupport support) {
    try {
      JFileChooser fc = (JFileChooser) support.getComponent();
      List<?> list = (List<?>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
      File[] files = new File[list.size()];
      for (int i = 0; i < list.size(); i++) {
        files[i] = (File) list.get(i);
      }
      if (fc.isMultiSelectionEnabled()) {
        fc.setSelectedFiles(files);
      } else {
        File f = files[0];
        if (f.isDirectory()) {
          fc.setCurrentDirectory(f);
        } else {
          fc.setSelectedFile(f);
        }
      }
      return true;
    } catch (IOException | UnsupportedFlavorException ex) {
      // ex.printStackTrace();
      return false;
    }
  }
}
