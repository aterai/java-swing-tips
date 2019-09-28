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

    JButton button1 = new JButton("Default");
    button1.addActionListener(e -> {
      JFileChooser fileChooser = new JFileChooser();
      int retvalue = fileChooser.showOpenDialog(getRootPane());
      if (retvalue == JFileChooser.APPROVE_OPTION) {
        log.setText(fileChooser.getSelectedFile().getAbsolutePath());
      }
    });

    JButton button2 = new JButton("Alignment: Right");
    button2.addActionListener(e -> {
      JFileChooser fileChooser = new JFileChooser() {
        @Override public void updateUI() {
          super.updateUI();
          if (getUI() instanceof WindowsFileChooserUI) {
            setUI(new RightAlignmentWindowsFileChooserUI(this));
          } else {
            setUI(new RightAlignmentMetalFileChooserUI(this));
          }
        }
      };
      int retvalue = fileChooser.showOpenDialog(getRootPane());
      if (retvalue == JFileChooser.APPROVE_OPTION) {
        log.setText(fileChooser.getSelectedFile().getAbsolutePath());
      }
    });

    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder("JFileChooser"));
    p.add(button1);
    p.add(button2);
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

class RightAlignmentMetalFileChooserUI extends MetalFileChooserUI {
  protected RightAlignmentMetalFileChooserUI(JFileChooser fc) {
    super(fc);
  }

  @Override public void installComponents(JFileChooser fc) {
    super.installComponents(fc);
    SwingUtils.stream(getBottomPanel())
        .filter(JLabel.class::isInstance)
        .map(JLabel.class::cast)
        .forEach(l -> {
          l.setHorizontalAlignment(SwingConstants.RIGHT);
          l.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        });
  }
}

class RightAlignmentWindowsFileChooserUI extends WindowsFileChooserUI {
  protected RightAlignmentWindowsFileChooserUI(JFileChooser fc) {
    super(fc);
  }

  @Override public void installComponents(JFileChooser fc) {
    super.installComponents(fc);
    SwingUtils.stream(getBottomPanel())
      .filter(JLabel.class::isInstance)
      .map(JLabel.class::cast)
      .forEach(l -> l.setAlignmentX(1f));
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
