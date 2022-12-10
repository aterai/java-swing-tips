// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea log = new JTextArea();

    JButton b1 = new JButton("Default");
    b1.addActionListener(e -> {
      JFileChooser chooser = new JFileChooser();
      JComponent c = (JComponent) e.getSource();
      int ret = chooser.showOpenDialog(c.getRootPane());
      if (ret == JFileChooser.APPROVE_OPTION) {
        log.append(chooser.getSelectedFile() + "\n");
      }
    });

    JButton b2 = new JButton("Resizable(false)");
    b2.addActionListener(e -> {
      JFileChooser chooser = new JFileChooser() {
        @Override protected JDialog createDialog(Component parent) { // throws HeadlessException {
          JDialog dialog = super.createDialog(parent);
          dialog.setResizable(false);
          return dialog;
        }
      };
      JComponent c = (JComponent) e.getSource();
      int ret = chooser.showOpenDialog(c.getRootPane());
      if (ret == JFileChooser.APPROVE_OPTION) {
        log.append(chooser.getSelectedFile() + "\n");
      }
    });

    JButton b3 = new JButton("MinimumSize(640, 480)");
    b3.addActionListener(e -> {
      JFileChooser chooser = new JFileChooser() {
        @Override protected JDialog createDialog(Component parent) { // throws HeadlessException {
          JDialog dialog = super.createDialog(parent);
          dialog.setMinimumSize(new Dimension(640, 480));
          return dialog;
        }
      };
      JComponent c = (JComponent) e.getSource();
      int ret = chooser.showOpenDialog(c.getRootPane());
      if (ret == JFileChooser.APPROVE_OPTION) {
        log.append(chooser.getSelectedFile() + "\n");
      }
    });

    JPanel p1 = new JPanel();
    p1.add(b1);
    p1.add(b2);
    JPanel p2 = new JPanel();
    p2.add(b3);
    // p2.add(new JButton(new CustomSizeFileChooserAction()));

    JPanel p = new JPanel(new GridLayout(2, 1));
    p.setBorder(BorderFactory.createTitledBorder("JFileChooser"));
    p.add(p1);
    p.add(p2);

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

// class CustomSizeFileChooserAction extends AbstractAction {
//   protected CustomSizeFileChooserAction() {
//     super("MinimumSize(640, 480)");
//   }
//
//   @Override public void actionPerformed(ActionEvent e) {
//     JFileChooser fileChooser = new JFileChooser() {
//       @Override protected JDialog createDialog(Component parent) {
//          JDialog dialog = super.createDialog(parent);
//          dialog.addComponentListener(new ComponentAdapter() {
//            @Override public void componentResized(ComponentEvent e) {
//              int mw = 640;
//              int mh = 480;
//              int fw = dialog.getSize().width;
//              int fh = dialog.getSize().height;
//              dialog.setSize(mw > fw ? mw : fw, mh > fh ? mh : fh);
//            }
//          });
//          return dialog;
//        }
//     };
//     JComponent c = (JComponent) e.getSource();
//     fileChooser.showOpenDialog(c.getRootPane());
//   }
// }
