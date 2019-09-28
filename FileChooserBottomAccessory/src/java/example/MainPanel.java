// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsFileChooserUI;

import java.awt.*;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.plaf.metal.MetalFileChooserUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea log = new JTextArea();

    JButton button1 = new JButton("Metal");
    button1.addActionListener(e -> {
      JFileChooser fileChooser = new JFileChooser() {
        @Override public void updateUI() {
          super.updateUI();
          setUI(new EncodingFileChooserUI(this));
          resetChoosableFileFilters();
        }
      };
      int retValue = fileChooser.showSaveDialog(getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        String enc = "\nEncoding: " + ((EncodingFileChooserUI) fileChooser.getUI()).getSelectedEncoding();
        log.setText(fileChooser.getSelectedFile().getAbsolutePath() + enc);
      }
    });

    JButton button2 = new JButton("Alignment: Right");
    button2.addActionListener(e -> {
      JFileChooser fileChooser = new JFileChooser() {
        @Override public void updateUI() {
          super.updateUI();
          setUI(new WindowsFileChooserUI(this) {
            @Override public void installComponents(JFileChooser fc) {
              super.installComponents(fc);
              JPanel bottomPanel = getBottomPanel();
              SwingUtils.stream(bottomPanel)
                  .filter(JLabel.class::isInstance).map(JLabel.class::cast)
                  .forEach(l -> {
                    l.setAlignmentX(1f);
                    l.setHorizontalAlignment(SwingConstants.RIGHT);
                  });
            }
          });
          resetChoosableFileFilters();
        }
      };
      int retValue = fileChooser.showSaveDialog(getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        log.setText(fileChooser.getSelectedFile().getAbsolutePath());
      }
    });

    JButton button3 = new JButton("Default");
    button3.addActionListener(e -> {
      JFileChooser fileChooser = new JFileChooser();
      int retValue = fileChooser.showSaveDialog(getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        log.setText(fileChooser.getSelectedFile().getAbsolutePath());
      }
    });

    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder("JFileChooser"));
    p.add(button1);
    p.add(button2);
    p.add(button3);
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

class EncodingFileChooserUI extends MetalFileChooserUI {
  private final String[] model = {"UTF-8", "UTF-16", "Shift_JIS", "EUC-JP"};
  private final JComboBox<String> combo = new JComboBox<>(model);

  protected EncodingFileChooserUI(JFileChooser fileChooser) {
    super(fileChooser);
  }

  public String getSelectedEncoding() {
    return combo.getItemAt(combo.getSelectedIndex());
  }

  @Override public void installComponents(JFileChooser fc) {
    super.installComponents(fc);
    JPanel bottomPanel = getBottomPanel();

    JLabel label = new JLabel("Encoding:") {
      @Override public Dimension getPreferredSize() {
        return SwingUtils.stream(bottomPanel)
            .filter(JLabel.class::isInstance).map(JLabel.class::cast)
            .findFirst()
            .map(JLabel::getPreferredSize)
            .orElse(super.getPreferredSize());
      }
    };
    label.setDisplayedMnemonic('E');
    label.setLabelFor(combo);

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
    panel.add(label);
    panel.add(combo);

    // 0: fileNamePanel
    // 1: RigidArea
    // 2: filesOfTypePanel
    bottomPanel.add(Box.createRigidArea(new Dimension(1, 5)), 3);
    bottomPanel.add(panel, 4);

    SwingUtils.stream(bottomPanel)
        .filter(JLabel.class::isInstance).map(JLabel.class::cast)
        .forEach(l -> {
          l.setHorizontalAlignment(SwingConstants.RIGHT);
          l.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        });
  }
}

final class SwingUtils {
  private SwingUtils() {
    /* Singleton */
  }

  public static Stream<Component> stream(Container parent) {
    return Stream.of(parent.getComponents())
        .filter(Container.class::isInstance)
        .map(c -> stream((Container) c))
        .reduce(Stream.of(parent), Stream::concat);
  }
}
