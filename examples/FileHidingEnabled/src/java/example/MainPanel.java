// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea log = new JTextArea();

    Toolkit tk = Toolkit.getDefaultToolkit();
    Object showHiddenFiles = tk.getDesktopProperty("awt.file.showHiddenFiles");
    log.setText("awt.file.showHiddenFiles: " + showHiddenFiles + "\n");

    JFileChooser chooser = new JFileChooser();
    // Optional.ofNullable(searchPopupMenu(chooser)).ifPresent(pop -> {
    //   pop.addSeparator();
    //   JCheckBoxMenuItem item = new JCheckBoxMenuItem("isFileHidingEnabled");
    //   item.addActionListener(e -> chooser.setFileHidingEnabled(item.isSelected()));
    //   item.setSelected(chooser.isFileHidingEnabled());
    //   pop.add(item);
    // });
    SwingUtils.descendants(chooser)
        .filter(JComponent.class::isInstance)
        .map(c -> ((JComponent) c).getComponentPopupMenu())
        .filter(Objects::nonNull)
        .findFirst()
        .ifPresent(pop -> {
          pop.addSeparator();
          JCheckBoxMenuItem item = new JCheckBoxMenuItem("isFileHidingEnabled");
          item.addActionListener(e -> chooser.setFileHidingEnabled(item.isSelected()));
          item.setSelected(chooser.isFileHidingEnabled());
          pop.add(item);
        });

    JButton button = new JButton("showOpenDialog");
    button.addActionListener(e -> {
      int retValue = chooser.showOpenDialog(getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        log.append(chooser.getSelectedFile().getAbsolutePath() + "\n");
      }
    });

    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder("JFileChooser"));
    p.add(button);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));
  }

  // private static JPopupMenu searchPopupMenu(Container parent) {
  //   for (Component c : parent.getComponents()) {
  //     if (c instanceof JComponent &&
  //         Objects.nonNull(((JComponent) c).getComponentPopupMenu())) {
  //       return ((JComponent) c).getComponentPopupMenu();
  //     } else {
  //       JPopupMenu pop = searchPopupMenu((Container) c);
  //       if (Objects.nonNull(pop)) {
  //         return pop;
  //       }
  //     }
  //   }
  //   return null;
  // }

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
