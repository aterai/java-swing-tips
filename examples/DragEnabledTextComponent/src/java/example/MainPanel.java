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
    JTextField field0 = new JTextField("Initially has BasicTextUI$TextTransferHandler");
    field0.setName("default");

    JTextField field1 = new JTextField("setEditable(false)");
    field1.setEditable(false);

    JTextField field2 = new JTextField("setEnabled(false)");
    field2.setEnabled(false);

    JTextField field3 = new JTextField("setTransferHandler(null)");
    field3.setTransferHandler(null);

    JTextField field4 = new JTextField("setDropTarget(null)");
    field4.setDropTarget(null);

    JTextField field5 = new JTextField("TransferHandler#canImport(...): false");
    field5.setTransferHandler(new TransferHandler() {
      @Override public boolean canImport(TransferSupport info) {
        return false;
      }
    });

    EventQueue.invokeLater(() -> {
      // Component c = SwingUtilities.getRoot(getRootPane());
      Container c = getTopLevelAncestor();
      if (c instanceof JFrame) {
        ((JFrame) c).setTransferHandler(new TransferHandler() {
          @Override public boolean canImport(TransferSupport info) {
            return true;
          }

          @Override public boolean importData(TransferSupport support) {
            // System.out.println(support.getTransferable());
            return true;
          }
        });
      }
    });

    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    Stream.of(field0, field1, field2, field3, field4, field5).forEach(tf -> {
      tf.setDragEnabled(true);
      box.add(tf);
      box.add(Box.createVerticalStrut(10));
    });

    add(box, BorderLayout.NORTH);
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
