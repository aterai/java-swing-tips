// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsFileChooserUI;

import java.awt.*;
import java.io.File;
import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicDirectoryModel;
import javax.swing.plaf.metal.MetalFileChooserUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JTextArea log = new JTextArea();

    JButton readOnlyButton = new JButton("readOnly");
    readOnlyButton.addActionListener(e -> {
      UIManager.put("FileChooser.readOnly", Boolean.TRUE);
      JFileChooser fileChooser = new JFileChooser();
      int retvalue = fileChooser.showOpenDialog(getRootPane());
      if (retvalue == JFileChooser.APPROVE_OPTION) {
        log.setText(fileChooser.getSelectedFile().getAbsolutePath());
      }
    });

    JButton writableButton = new JButton("Rename only File#canWrite() == true");
    writableButton.addActionListener(e -> {
      UIManager.put("FileChooser.readOnly", Boolean.FALSE);
      JFileChooser fileChooser = new JFileChooser() {
        @Override protected void setUI(ComponentUI ui) {
          if (ui instanceof WindowsFileChooserUI) {
            super.setUI(WindowsCanWriteFileChooserUI.createUI(this));
          } else {
            super.setUI(MetalCanWriteFileChooserUI.createUI(this));
          }
        }
      };

      // ActionMap am = fc.getActionMap();
      // // WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, editFileName, pressed F2
      // Action editFileNameAction = am.get("editFileName");
      // am.put("editFileName", new AbstractAction("editFileName2") {
      //   @Override public void actionPerformed(ActionEvent e) {
      //     File file = fc.getSelectedFile();
      //     if (file != null && file.canWrite()) {
      //       editFileNameAction.actionPerformed(e);
      //     }
      //   }
      // });
      // Action newFolder = am.get("New Folder");
      // newFolder.setEnabled(false);

      int retvalue = fileChooser.showOpenDialog(getRootPane());
      if (retvalue == JFileChooser.APPROVE_OPTION) {
        log.setText(fileChooser.getSelectedFile().getAbsolutePath());
      }
    });

    JPanel p = new JPanel(new GridLayout(2, 1, 5, 5));
    p.setBorder(BorderFactory.createTitledBorder("JFileChooser"));
    p.add(readOnlyButton);
    p.add(writableButton);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
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

class WindowsCanWriteFileChooserUI extends WindowsFileChooserUI {
  private BasicDirectoryModel model2;

  protected WindowsCanWriteFileChooserUI(JFileChooser chooser) {
    super(chooser);
  }

  public static ComponentUI createUI(JComponent c) {
    if (c instanceof JFileChooser) {
      return new WindowsCanWriteFileChooserUI((JFileChooser) c);
    }
    throw new InternalError("Should never happen");
  }

  @Override public void createModel() {
    if (model2 != null) {
      model2.invalidateFileCache();
    }
    model2 = new BasicDirectoryModel(getFileChooser()) {
      @Override public boolean renameFile(File oldFile, File newFile) {
        return oldFile.canWrite() && super.renameFile(oldFile, newFile);
      }
    };
  }

  @Override public BasicDirectoryModel getModel() {
    return model2;
  }
}

class MetalCanWriteFileChooserUI extends MetalFileChooserUI {
  private BasicDirectoryModel model2;

  protected MetalCanWriteFileChooserUI(JFileChooser chooser) {
    super(chooser);
  }

  public static ComponentUI createUI(JComponent c) {
    if (c instanceof JFileChooser) {
      return new MetalCanWriteFileChooserUI((JFileChooser) c);
    }
    throw new InternalError("Should never happen");
  }

  @Override public void createModel() {
    if (model2 != null) {
      model2.invalidateFileCache();
    }
    model2 = new BasicDirectoryModel(getFileChooser()) {
      @Override public boolean renameFile(File oldFile, File newFile) {
        return oldFile.canWrite() && super.renameFile(oldFile, newFile);
      }
    };
  }

  @Override public BasicDirectoryModel getModel() {
    return model2;
  }
}
