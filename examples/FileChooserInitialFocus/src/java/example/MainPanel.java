// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.File;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JFileChooser fileChooser = new JFileChooser();
    JTextField field = new JTextField("C:/temp/test.txt");
    JTextArea log = new JTextArea();

    // // TEST: PropertyChangeListener
    // fileChooser.addPropertyChangeListener(e -> {
    //   String s = e.getPropertyName();
    //   if (s.equals("ancestor")) {
    //     if (e.getOldValue() == null && e.getNewValue() != null) {
    //       // Ancestor was added, set initial focus
    //       findFileNameTextField(fileChooser).ifPresent(c -> {
    //         ((JTextField) c).selectAll();
    //         c.requestFocusInWindow();
    //       });
    //     }
    //   }
    // });

    // // TEST: AncestorListener
    // fileChooser.addAncestorListener(new AncestorListener() {
    //   @Override public void ancestorAdded(AncestorEvent e) {
    //     findFileNameTextField(fileChooser).ifPresent(c -> {
    //       ((JTextField) c).selectAll();
    //       c.requestFocusInWindow();
    //     });
    //   }
    //   @Override public void ancestorMoved(AncestorEvent e) {}
    //   @Override public void ancestorRemoved(AncestorEvent e) {}
    // });

    //  // TEST: doAncestorChanged
    //  fileChooser = new JFileChooser() {
    //    @Override public void updateUI() {
    //      super.updateUI();
    //      EventQueue.invokeLater(() -> {
    //        setUI(new sun.swing.plaf.synth.SynthFileChooserUIImpl(fileChooser) {
    //          @Override protected void doAncestorChanged(java.beans.PropertyChangeEvent e) {
    //            findFileNameTextField(fileChooser).ifPresent(c -> {
    //              ((JTextField) c).selectAll();
    //              c.requestFocusInWindow();
    //            });
    //          }
    //        });
    //      });
    //    }
    //  };

    JRadioButton radio = new JRadioButton("set initial focus on JTextField", true);
    ButtonGroup bg = new ButtonGroup();
    JPanel p2 = new JPanel();
    Stream.of(new JRadioButton("default"), radio).forEach(b -> {
      bg.add(b);
      p2.add(b);
    });

    JButton button = new JButton("JFileChooser");
    button.addActionListener(e -> {
      fileChooser.setSelectedFile(new File(field.getText().trim()));
      if (radio.isSelected()) {
        EventQueue.invokeLater(() -> {
          // findFileNameTextField(fileChooser).ifPresent(c -> {
          //   ((JTextField) c).selectAll();
          //   c.requestFocusInWindow();
          // });
          Class<JTextField> clz = JTextField.class;
          descendants(fileChooser)
              .filter(clz::isInstance)
              .map(clz::cast)
              .findFirst()
              .ifPresent(tf -> {
                tf.selectAll();
                tf.requestFocusInWindow();
              });
        });
      }
      int ret = fileChooser.showOpenDialog(getRootPane());
      if (ret == JFileChooser.APPROVE_OPTION) {
        String path = fileChooser.getSelectedFile().getAbsolutePath();
        field.setText(path);
        log.append(path + "\n");
      }
    });

    JPanel p1 = new JPanel(new BorderLayout(5, 5));
    p1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    p1.add(field);
    p1.add(button, BorderLayout.EAST);

    JPanel p = new JPanel(new BorderLayout());
    p.add(p1, BorderLayout.NORTH);
    p.add(p2, BorderLayout.SOUTH);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    setPreferredSize(new Dimension(320, 240));
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
      // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
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
