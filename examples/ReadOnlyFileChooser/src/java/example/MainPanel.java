// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final JTextArea log = new JTextArea();

  private MainPanel() {
    super(new BorderLayout());
    JButton readOnlyButton = new JButton("readOnly");
    readOnlyButton.addActionListener(e -> showOpenDialog(true));

    JButton defaultButton = new JButton("Default");
    defaultButton.addActionListener(e -> showOpenDialog(false));

    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder("JFileChooser"));
    p.add(readOnlyButton);
    p.add(defaultButton);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));
  }

  private void showOpenDialog(boolean readOnly) {
    // The FileChooserUI reads "FileChooser.readOnly" when it is installed,
    // so the value must be set before the JFileChooser is created.
    UIManager.put("FileChooser.readOnly", readOnly);
    JFileChooser fileChooser = new JFileChooser();
    int retValue = fileChooser.showOpenDialog(getRootPane());
    if (retValue == JFileChooser.APPROVE_OPTION) {
      log.setText(fileChooser.getSelectedFile().getAbsolutePath());
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      // UIManager.put("FileChooser.readOnly", true);
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
