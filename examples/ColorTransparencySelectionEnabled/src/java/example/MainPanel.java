// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.plaf.basic.BasicColorChooserUI;

public final class MainPanel extends JPanel {
  private final JRadioButton defaultRadio = new JRadioButton("Default");
  private final JRadioButton enabledRadio = new JRadioButton("setEnabled(false)", true);

  private MainPanel() {
    super(new BorderLayout(10, 10));
    JLabel label = new JLabel();
    label.setOpaque(true);
    label.setBackground(Color.WHITE);

    JButton button = new JButton("open JColorChooser");
    button.addActionListener(e -> {
      Color color = getColor();
      if (color != null) {
        label.setBackground(color);
      }
    });

    // JButton button2 = new JButton("JColorChooser");
    // button2.addActionListener(e -> {
    //   Component rp = getRootPane();
    //   // JColorChooser should have a way to disable transparency controls - Java Bug System
    //   // https://bugs.openjdk.org/browse/JDK-8051548
    //   // Java 9:
    //   Color c = JColorChooser.showDialog(rp, "", color, defaultRadio.isSelected());
    //   label.setBackground(c);
    // });

    add(makeBox(), BorderLayout.NORTH);
    add(label);
    add(button, BorderLayout.SOUTH);
    // box.add(button2, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    setPreferredSize(new Dimension(320, 240));
  }

  private Color getColor() {
    JColorChooser cc = new JColorChooser() {
      @Override public void updateUI() {
        super.updateUI();
        boolean isGtk = Objects.equals("GTK", UIManager.getLookAndFeel().getID());
        if (isGtk) {
          setUI(new BasicColorChooserUI());
        }
      }
    };
    String rgbName = UIManager.getString("ColorChooser.rgbNameText", getLocale());
    for (AbstractColorChooserPanel ccPanel : cc.getChooserPanels()) {
      // Java 9: ccPanel.setColorTransparencySelectionEnabled(colorTransparency);
      if (rgbName.equals(ccPanel.getDisplayName())) {
        if (!defaultRadio.isSelected()) {
          EventQueue.invokeLater(() -> setTransparencySelectionEnabled(ccPanel));
        }
      } else {
        cc.removeChooserPanel(ccPanel);
      }
    }
    Component rp = getRootPane();
    ColorTracker ok = new ColorTracker(cc);
    JDialog dialog = JColorChooser.createDialog(rp, "title", true, cc, ok, null);
    dialog.addComponentListener(new ComponentAdapter() {
      @Override public void componentHidden(ComponentEvent e) {
        ((Window) e.getComponent()).dispose();
      }
    });
    dialog.setVisible(true); // blocks until user brings dialog down...
    return ok.getColor();
  }

  private Box makeBox() {
    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createTitledBorder("ColorTransparencySelectionEnabled"));
    ButtonGroup bg = new ButtonGroup();
    JRadioButton visibleRadio = new JRadioButton("setVisible(false)");
    for (JRadioButton r : Arrays.asList(defaultRadio, enabledRadio, visibleRadio)) {
      bg.add(r);
      box.add(r);
    }
    return box;
  }

  private void setTransparencySelectionEnabled(AbstractColorChooserPanel p) {
    String alphaName = UIManager.getString("ColorChooser.rgbAlphaText", p.getLocale());
    List<Component> list = SwingUtils.descendants(p).collect(Collectors.toList());
    int idx0 = 0;
    int idx1 = 0;
    int tgtIndex = 3; // rgbAlpha in RGB ColorChooserPanel
    // int tgtIndex = 4; // cmykAlpha in CMYK ColorChooserPanel
    for (Component c : list) {
      if (c instanceof JLabel && alphaName.equals(((JLabel) c).getText())) {
        setEnabledOrVisible(c);
      } else if (c instanceof JSlider) {
        if (idx0 == tgtIndex) {
          setEnabledOrVisible(c);
        }
        idx0 += 1;
      } else if (c instanceof JSpinner) {
        if (idx1 == tgtIndex) {
          setEnabledOrVisible(c);
        }
        idx1 += 1;
      }
    }
  }

  private void setEnabledOrVisible(Component c) {
    if (enabledRadio.isSelected()) {
      c.setEnabled(false);
    } else {
      c.setVisible(false);
    }
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

class ColorTracker implements ActionListener {
  private final JColorChooser chooser;
  private Color color;

  protected ColorTracker(JColorChooser c) {
    chooser = c;
  }

  @Override public void actionPerformed(ActionEvent e) {
    color = chooser.getColor();
  }

  public Color getColor() {
    return color;
  }
}
