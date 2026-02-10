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
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JLabel label = new JLabel();
    JButton button = new JButton("get Clipboard DataFlavor");
    button.addActionListener(e -> {
      Toolkit tk = Toolkit.getDefaultToolkit();
      Transferable t = tk.getSystemClipboard().getContents(null);
      if(t != null) {
        updateLabel(label, t);
      }
    });
    add(new JScrollPane(label));
    add(button, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void updateLabel(JLabel label, Transferable trans) {
    String str = "";
    ImageIcon img = null;
    try {
      if (trans.isDataFlavorSupported(DataFlavor.imageFlavor)) {
        img = new ImageIcon((Image) trans.getTransferData(DataFlavor.imageFlavor));
      } else if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
        str = Objects.toString(trans.getTransferData(DataFlavor.stringFlavor));
      }
    } catch (UnsupportedFlavorException | IOException ex) {
      UIManager.getLookAndFeel().provideErrorFeedback(label);
      str = ex.getMessage();
    }
    label.setText(str);
    label.setIcon(img);
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
