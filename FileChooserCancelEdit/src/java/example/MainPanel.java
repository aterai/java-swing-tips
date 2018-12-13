package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.util.Optional;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    // UIManager.put("FileChooser.readOnly", Boolean.TRUE);
    JTextArea log = new JTextArea();

    JFileChooser fileChooser0 = new JFileChooser();
    JButton button0 = new JButton("default");
    button0.addActionListener(e -> {
      setViewTypeDetails(fileChooser0);
      stream(fileChooser0)
        .filter(JTable.class::isInstance).map(JTable.class::cast)
        .findFirst()
        .ifPresent(table -> append(log, "isEditing: " + table.isEditing()));
      int retvalue = fileChooser0.showOpenDialog(getRootPane());
      if (retvalue == JFileChooser.APPROVE_OPTION) {
        append(log, fileChooser0.getSelectedFile().getAbsolutePath());
      }
    });

    JFileChooser fileChooser1 = new JFileChooser();
    JButton button1 = new JButton("removeEditor");
    button1.addActionListener(e -> {
      setViewTypeDetails(fileChooser1);
      stream(fileChooser1)
        .filter(JTable.class::isInstance).map(JTable.class::cast)
        .peek(table -> append(log, "isEditing: " + table.isEditing()))
        .findFirst()
        .filter(JTable::isEditing).ifPresent(JTable::removeEditor);
      int retvalue = fileChooser1.showOpenDialog(getRootPane());
      if (retvalue == JFileChooser.APPROVE_OPTION) {
        append(log, fileChooser1.getSelectedFile().getAbsolutePath());
      }
    });

    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder("JFileChooser(viewTypeDetails)"));
    p.add(button0);
    p.add(button1);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));
  }

  private static void setViewTypeDetails(JFileChooser fileChooser) {
    Optional.ofNullable(fileChooser.getActionMap().get("viewTypeDetails"))
      .ifPresent(a -> a.actionPerformed(null));
  }

  private static void append(JTextArea log, String str) {
    log.append(str + "\n");
    log.setCaretPosition(log.getDocument().getLength());
  }

  public static Stream<Component> stream(Container parent) {
    return Stream.of(parent.getComponents())
      .filter(Container.class::isInstance)
      .map(c -> stream(Container.class.cast(c)))
      .reduce(Stream.of(parent), Stream::concat);
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
