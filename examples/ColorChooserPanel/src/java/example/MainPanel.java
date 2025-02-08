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
    // JDialog dialog = new JDialog(this, "JColorChooser", Dialog.ModalityType.APPLICATION_MODAL);
    // JPanel buttonPanel = new JPanel();
    // buttonPanel.add(new JButton("OK"));
    // buttonPanel.add(new JButton("Cancel"));
    // buttonPanel.add(new JButton("Reset"));
    JLabel label = new JLabel();
    label.setOpaque(true);
    label.setBackground(Color.WHITE);

    Locale loc = getLocale();
    JCheckBox swatches = new JCheckBox(UIManager.getString("ColorChooser.swatchesNameText", loc));
    JCheckBox hsv = new JCheckBox(UIManager.getString("ColorChooser.hsvNameText", loc));
    JCheckBox hsl = new JCheckBox(UIManager.getString("ColorChooser.hslNameText", loc));
    JCheckBox rgb = new JCheckBox(UIManager.getString("ColorChooser.rgbNameText", loc));
    JCheckBox cmyk = new JCheckBox(UIManager.getString("ColorChooser.cmykNameText", loc));
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

  public static Color openColorChooser(Component p, List<String> names, Color initColor) {
    Color color;
    String title = "JColorChooser";
    if (names.isEmpty()) { // use default JColorChooser
      color = JColorChooser.showDialog(p, title, initColor);
    } else {
      JColorChooser cc = new JColorChooser();
      cc.setColor(initColor);
      for (AbstractColorChooserPanel chooserPanel : cc.getChooserPanels()) {
        if (!names.contains(chooserPanel.getDisplayName())) {
          cc.removeChooserPanel(chooserPanel);
        }
      }
      ColorTracker ok = new ColorTracker(cc);
      JDialog dialog = JColorChooser.createDialog(p, title, true, cc, ok, null);
      // dialog.addComponentListener(new ColorChooserDialog.DisposeOnClose());
      dialog.addComponentListener(new ComponentAdapter() {
        @Override public void componentHidden(ComponentEvent e) {
          ((Window) e.getComponent()).dispose();
        }
      });
      dialog.setVisible(true); // blocks until user brings dialog down...
      color = ok.getColor();
    }
    return color;
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
