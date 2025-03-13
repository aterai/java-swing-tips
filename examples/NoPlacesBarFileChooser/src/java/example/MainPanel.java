// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final JTextArea log = new JTextArea();

  private MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder("JFileChooser"));
    p.add(makeButton1());
    p.add(makeButton2());
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));
  }

  private JButton makeButton1() {
    JButton button = new JButton("Default");
    button.addActionListener(e -> {
      UIManager.put("FileChooser.noPlacesBar", Boolean.FALSE);
      JFileChooser fileChooser = new JFileChooser();
      int retValue = fileChooser.showOpenDialog(getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        log.setText(fileChooser.getSelectedFile().getAbsolutePath());
      }
    });
    return button;
  }

  private JButton makeButton2() {
    JButton button = new JButton("noPlacesBar");
    button.addActionListener(e -> {
      UIManager.put("FileChooser.noPlacesBar", Boolean.TRUE);
      JFileChooser fileChooser = new JFileChooser();
      int retValue = fileChooser.showOpenDialog(getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        log.setText(fileChooser.getSelectedFile().getAbsolutePath());
      }
    });
    return button;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      // UIManager.put("FileChooser.readOnly", Boolean.TRUE);
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
