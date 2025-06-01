// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final JTextArea log = new JTextArea();

  private MainPanel() {
    super(new BorderLayout());
    JButton button0 = new JButton("Default");
    button0.addActionListener(e -> openDefaultFileChooser());
    JPanel p0 = new JPanel();
    String t0 = "JFileChooser resizing may result in incorrect cell editor positioning";
    p0.setBorder(BorderFactory.createTitledBorder(t0));
    p0.add(button0);

    JButton button1 = new JButton("JFileChooser");
    button1.addActionListener(e -> openListViewFileChooser1());
    JButton button2 = new JButton("Dialog");
    button2.addActionListener(e -> openListViewFileChooser2());
    JPanel p1 = new JPanel();
    String t1 = "override ComponentListener#componentResized to cancel editing";
    p1.setBorder(BorderFactory.createTitledBorder(t1));
    p1.add(button1);
    p1.add(button2);

    JPanel p = new JPanel(new GridLayout(2, 1));
    p.add(p0);
    p.add(p1);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));
  }

  private void openDefaultFileChooser() {
    JFileChooser chooser = new JFileChooser();
    int retValue = chooser.showOpenDialog(log.getRootPane());
    if (retValue == JFileChooser.APPROVE_OPTION) {
      log.setText(chooser.getSelectedFile().getAbsolutePath());
    }
  }

  private void openListViewFileChooser1() {
    JFileChooser chooser = new JFileChooser();
    chooser.addComponentListener(new CancelEditListener(chooser));
    int retValue = chooser.showOpenDialog(log.getRootPane());
    if (retValue == JFileChooser.APPROVE_OPTION) {
      log.setText(chooser.getSelectedFile().getAbsolutePath());
    }
  }

  private void openListViewFileChooser2() {
    JFileChooser chooser = new JFileChooser() {
      @Override protected JDialog createDialog(Component parent) {
        JDialog dialog = super.createDialog(parent);
        dialog.addComponentListener(new CancelEditListener(this));
        return dialog;
      }
    };
    int retValue = chooser.showOpenDialog(log.getRootPane());
    if (retValue == JFileChooser.APPROVE_OPTION) {
      log.setText(chooser.getSelectedFile().getAbsolutePath());
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

class CancelEditListener extends ComponentAdapter {
  private final JFileChooser chooser;

  protected CancelEditListener(JFileChooser chooser) {
    super();
    this.chooser = chooser;
  }

  @Override public void componentResized(ComponentEvent e) {
    // sun.swing.FilePane.cancelEdit();
    chooser.setSelectedFile(null);
  }
}
