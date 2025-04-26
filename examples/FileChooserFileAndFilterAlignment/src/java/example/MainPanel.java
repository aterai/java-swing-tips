// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsFileChooserUI;
import java.awt.*;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.plaf.metal.MetalFileChooserUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea log = new JTextArea();

    JButton button1 = new JButton("Default");
    button1.addActionListener(e -> {
      JFileChooser chooser = new JFileChooser();
      int retValue = chooser.showOpenDialog(getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        log.setText(chooser.getSelectedFile().getAbsolutePath());
      }
    });

    JButton button2 = new JButton("Alignment: Right");
    button2.addActionListener(e -> {
      JFileChooser chooser = new RightAlignmentFileChooser();
      int retValue = chooser.showOpenDialog(getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        log.setText(chooser.getSelectedFile().getAbsolutePath());
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

class RightAlignmentMetalFileChooserUI extends MetalFileChooserUI {
  protected RightAlignmentMetalFileChooserUI(JFileChooser fc) {
    super(fc);
  }

  @Override public void installComponents(JFileChooser fc) {
    super.installComponents(fc);
    SwingUtils.descendants(getBottomPanel())
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
    SwingUtils.descendants(getBottomPanel())
        .filter(JLabel.class::isInstance)
        .map(JLabel.class::cast)
        .forEach(l -> l.setAlignmentX(Component.RIGHT_ALIGNMENT));
  }
}

class RightAlignmentFileChooser extends JFileChooser {
  @Override public void updateUI() {
    super.updateUI();
    if (getUI() instanceof WindowsFileChooserUI) {
      setUI(new RightAlignmentWindowsFileChooserUI(this));
    } else {
      setUI(new RightAlignmentMetalFileChooserUI(this));
    }
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
