// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final JTextArea log = new JTextArea();

  private MainPanel() {
    super(new BorderLayout());
    // UIManager.put("FileChooser.readOnly", Boolean.TRUE);
    JButton button0 = new JButton("default");
    button0.addActionListener(e -> showOpenDialog0());
    JButton button1 = new JButton("removeEditor");
    button1.addActionListener(e -> showOpenDialog1());
    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder("JFileChooser(viewTypeDetails)"));
    p.add(button0);
    p.add(button1);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));
  }

  private void showOpenDialog0() {
    JFileChooser fileChooser = new JFileChooser();
    setViewTypeDetails(fileChooser);
    descendants(fileChooser)
        .filter(JTable.class::isInstance).map(JTable.class::cast).findFirst()
        .ifPresent(table -> append(log, "isEditing: " + table.isEditing()));
    int retValue = fileChooser.showOpenDialog(getRootPane());
    if (retValue == JFileChooser.APPROVE_OPTION) {
      append(log, fileChooser.getSelectedFile().getAbsolutePath());
    }
  }

  private void showOpenDialog1() {
    JFileChooser fileChooser = new JFileChooser();
    setViewTypeDetails(fileChooser);
    descendants(fileChooser)
        .filter(JTable.class::isInstance).map(JTable.class::cast).findFirst()
        .filter(JTable::isEditing).ifPresent(JTable::removeEditor);
    int retValue = fileChooser.showOpenDialog(getRootPane());
    if (retValue == JFileChooser.APPROVE_OPTION) {
      append(log, fileChooser.getSelectedFile().getAbsolutePath());
    }
  }

  private static void setViewTypeDetails(JFileChooser fileChooser) {
    String cmd = "viewTypeDetails";
    Optional.ofNullable(fileChooser.getActionMap().get(cmd))
        .ifPresent(a -> {
          int id = ActionEvent.ACTION_PERFORMED;
          a.actionPerformed(new ActionEvent(fileChooser, id, cmd));
        });
  }

  private static void append(JTextArea log, String str) {
    log.append(str + "\n");
    log.setCaretPosition(log.getDocument().getLength());
  }

  public static Stream<Component> descendants(Container parent) {
    return Stream.of(parent.getComponents())
        .filter(Container.class::isInstance).map(Container.class::cast)
        .flatMap(c -> Stream.concat(Stream.of(c), descendants(c)));
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
