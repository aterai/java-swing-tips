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
import java.util.Locale;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout(10, 10));
    JLabel label = new JLabel();
    label.setOpaque(true);
    label.setBackground(Color.WHITE);

    Locale locale = getLocale();
    JCheckBox swatches = new JCheckBox(getString("ColorChooser.swatchesNameText", locale));
    JCheckBox hsv = new JCheckBox(getString("ColorChooser.hsvNameText", locale));
    JCheckBox hsl = new JCheckBox(getString("ColorChooser.hslNameText", locale));
    JCheckBox rgb = new JCheckBox(getString("ColorChooser.rgbNameText", locale));
    JCheckBox cmyk = new JCheckBox(getString("ColorChooser.cmykNameText", locale));
    List<JCheckBox> list = Arrays.asList(swatches, hsv, hsl, rgb, cmyk);

    JButton button = new JButton("open JColorChooser");
    button.addActionListener(e -> {
      List<String> selected = list.stream()
          .filter(AbstractButton::isSelected)
          .map(AbstractButton::getText)
          .collect(Collectors.toList());
      Color color = openColorChooser(getRootPane(), selected, label.getBackground());
      if (color != null) {
        label.setBackground(color);
      }
    });

    Box box = Box.createVerticalBox();
    for (AbstractButton b : list) {
      box.add(b);
      box.add(Box.createVerticalStrut(5));
    }
    add(box, BorderLayout.NORTH);
    add(label);
    add(button, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    setPreferredSize(new Dimension(320, 240));
  }

  private static String getString(String key, Locale locale) {
    return UIManager.getString(key, locale);
  }

  public static Color openColorChooser(Component parent, List<String> names, Color initialColor) {
    String title = "JColorChooser";
    return names.isEmpty() // use default JColorChooser
        ? JColorChooser.showDialog(parent, title, initialColor)
        : openCustomColorChooser(parent, title, names, initialColor);
  }

  private static Color openCustomColorChooser(
      Component parent, String title, List<String> names, Color initialColor) {
    JColorChooser chooser = new JColorChooser(initialColor);
    // Keep only the selected panels; setChooserPanels(...) replaces all panels at once
    // instead of removing the unselected panels one by one
    AbstractColorChooserPanel[] panels = Arrays.stream(chooser.getChooserPanels())
        .filter(p -> names.contains(p.getDisplayName()))
        .toArray(AbstractColorChooserPanel[]::new);
    chooser.setChooserPanels(panels);
    ColorTracker okListener = new ColorTracker(chooser);
    JDialog dialog = JColorChooser.createDialog(parent, title, true, chooser, okListener, null);
    // Unlike JColorChooser.showDialog(...), the dialog created by createDialog(...)
    // is not disposed automatically when it is closed
    dialog.addComponentListener(new ComponentAdapter() {
      @Override public void componentHidden(ComponentEvent e) {
        Component c = e.getComponent();
        if (c instanceof Window) {
          ((Window) c).dispose();
        }
      }
    });
    dialog.setVisible(true); // blocks until user brings dialog down...
    return okListener.getColor(); // null if the dialog was canceled
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

class ColorTracker implements ActionListener {
  private final JColorChooser chooser;
  private Color color;

  protected ColorTracker(JColorChooser chooser) {
    this.chooser = chooser;
  }

  @Override public void actionPerformed(ActionEvent e) {
    color = chooser.getColor();
  }

  public Color getColor() {
    return color;
  }
}
