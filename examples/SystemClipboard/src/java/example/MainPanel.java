// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JLabel label = new JLabel();
    JButton button = new JButton("get Clipboard DataFlavor");
    button.addActionListener(e -> {
      String str = "";
      ImageIcon image = null;
      try {
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (Objects.isNull(t)) {
          Toolkit.getDefaultToolkit().beep();
          return;
        }
        if (t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
          image = new ImageIcon((Image) t.getTransferData(DataFlavor.imageFlavor));
        } else if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
          str = Objects.toString(t.getTransferData(DataFlavor.stringFlavor));
        }
      } catch (UnsupportedFlavorException | IOException ex) {
        Toolkit.getDefaultToolkit().beep();
        str = ex.getMessage();
        // image = null;
      }
      label.setText(str);
      label.setIcon(image);
    });

    add(new JScrollPane(label));
    add(button, BorderLayout.SOUTH);
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
