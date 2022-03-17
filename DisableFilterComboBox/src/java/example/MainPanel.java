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
    JTextArea log = new JTextArea();
    JFileChooser chooser = new JFileChooser();

    JButton button1 = new JButton("FILES_AND_DIRECTORIES");
    button1.addActionListener(e -> {
      chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
      updateFileChooser(chooser);
      int retValue = chooser.showOpenDialog(getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        log.append(String.format("%s%n", chooser.getSelectedFile()));
      }
    });

    JButton button2 = new JButton("DIRECTORIES_ONLY");
    button2.addActionListener(e -> {
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      updateFileChooser(chooser);
      int retValue = chooser.showOpenDialog(getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        log.append(String.format("%s%n", chooser.getSelectedFile()));
      }
    });

    JPanel p = new JPanel(new GridLayout(0, 1, 5, 5));
    p.setBorder(BorderFactory.createTitledBorder("JFileChooser#showOpenDialog(...)"));
    p.add(button1);
    p.add(button2);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static void updateFileChooser(JFileChooser fileChooser) {
    boolean f = fileChooser.getFileSelectionMode() != JFileChooser.DIRECTORIES_ONLY;
    fileChooser.setAcceptAllFileFilterUsed(f);
    String txt = UIManager.getString("FileChooser.filesOfTypeLabelText", fileChooser.getLocale());
    SwingUtils.descendants(fileChooser)
        .filter(JLabel.class::isInstance).map(JLabel.class::cast)
        .forEach(label -> {
          if (txt.equals(label.getText())) {
            Component c = label.getLabelFor();
            label.setEnabled(f);
            if (c instanceof JComboBox) {
              JComboBox<?> combo = (JComboBox<?>) c;
              combo.setEnabled(f);
              ((JComponent) combo.getRenderer()).setOpaque(f);
            }
          }
        });
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
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

final class SwingUtils {
  private SwingUtils() {
    /* Singleton */
  }

  public static Stream<Component> descendants(Container parent) {
    return Stream.of(parent.getComponents())
        .filter(Container.class::isInstance).map(Container.class::cast)
        .flatMap(c -> Stream.concat(Stream.of(c), descendants(c)));
  }
}
