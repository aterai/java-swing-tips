// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JTextField textField0 = new JTextField("Initially has BasicTextUI$TextTransferHandler");
    textField0.setName("default");

    JTextField textField1 = new JTextField("setEditable(false)");
    textField1.setEditable(false);

    JTextField textField2 = new JTextField("setEnabled(false)");
    textField2.setEnabled(false);

    JTextField textField3 = new JTextField("setTransferHandler(null)");
    textField3.setTransferHandler(null);

    JTextField textField4 = new JTextField("setDropTarget(null)");
    textField4.setDropTarget(null);

    JTextField textField5 = new JTextField("TransferHandler#canImport(...): false");
    textField5.setTransferHandler(new TransferHandler() {
      @Override public boolean canImport(TransferSupport info) {
        return false;
      }
    });

    EventQueue.invokeLater(() -> {
      Component c = SwingUtilities.getRoot(getRootPane());
      if (c instanceof JFrame) {
        ((JFrame) c).setTransferHandler(new TransferHandler() {
          @Override public boolean canImport(TransferSupport info) {
            return true;
          }

          @Override public boolean importData(TransferSupport support) {
            System.out.println(support.getTransferable());
            return true;
          }
        });
      }
    });

    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    Stream.of(textField0, textField1, textField2, textField3, textField4, textField5).forEach(tf -> {
      tf.setDragEnabled(true);
      box.add(tf);
      box.add(Box.createVerticalStrut(10));
    });

    add(box, BorderLayout.NORTH);
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
    } catch (ClassNotFoundException | InstantiationException
         | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
