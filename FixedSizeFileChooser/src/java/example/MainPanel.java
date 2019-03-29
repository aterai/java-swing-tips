// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    JPanel p1 = new JPanel();
    p1.setBorder(BorderFactory.createTitledBorder("JFileChooser setResizable"));
    p1.add(new JButton(new DefaultFileChooserAction()));
    p1.add(new JButton(new FixedSizeFileChooserAction()));

    JPanel p2 = new JPanel();
    p2.setBorder(BorderFactory.createTitledBorder("JFileChooser setMinimumSize"));
    p2.add(new JButton(new MinimumSizeFileChooserAction()));
    // p2.add(new JButton(new CustomSizeFileChooserAction()));
    add(p1);
    add(p2);
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

class DefaultFileChooserAction extends AbstractAction {
  protected DefaultFileChooserAction() {
    super("Default");
  }

  @Override public void actionPerformed(ActionEvent e) {
    JFileChooser fileChooser = new JFileChooser();
    JComponent c = (JComponent) e.getSource();
    int retvalue = fileChooser.showOpenDialog(c.getRootPane());
    System.out.println(retvalue);
  }
}

class FixedSizeFileChooserAction extends AbstractAction {
  protected FixedSizeFileChooserAction() {
    super("Resizable(false)");
  }

  @Override public void actionPerformed(ActionEvent e) {
    JFileChooser fileChooser = new JFileChooser() {
      @Override protected JDialog createDialog(Component parent) { // throws HeadlessException {
        JDialog dialog = super.createDialog(parent);
        dialog.setResizable(false);
        return dialog;
      }
    };
    JComponent c = (JComponent) e.getSource();
    int retvalue = fileChooser.showOpenDialog(c.getRootPane());
    System.out.println(retvalue);
  }
}

class MinimumSizeFileChooserAction extends AbstractAction {
  protected MinimumSizeFileChooserAction() {
    super("MinimumSize(640, 480)");
  }

  @Override public void actionPerformed(ActionEvent e) {
    JFileChooser fileChooser = new JFileChooser() {
      @Override protected JDialog createDialog(Component parent) { // throws HeadlessException {
        JDialog dialog = super.createDialog(parent);
        dialog.setMinimumSize(new Dimension(640, 480));
        return dialog;
      }
    };
    JComponent c = (JComponent) e.getSource();
    int retvalue = fileChooser.showOpenDialog(c.getRootPane());
    System.out.println(retvalue);
  }
}

// class CustomSizeFileChooserAction extends AbstractAction {
//   protected CustomSizeFileChooserAction() {
//     super("MinimumSize(640, 480)");
//   }
//   @Override public void actionPerformed(ActionEvent e) {
//     JFileChooser fileChooser = new JFileChooser() {
//       @Override protected JDialog createDialog(Component parent) { // throws HeadlessException {
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
//     int retvalue = fileChooser.showOpenDialog(c.getRootPane());
//     System.out.println(retvalue);
//   }
// }
