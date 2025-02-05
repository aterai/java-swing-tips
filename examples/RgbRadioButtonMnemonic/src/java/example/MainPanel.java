// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JLabel label = new JLabel() {
      @Override public Dimension getPreferredSize() {
        return new Dimension(64, 64);
      }
    };
    label.setOpaque(true);
    label.setBackground(new Color(0xFF_FF_00_00, true));
    JPanel pp = new JPanel();
    pp.add(label);
    add(pp);

    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder("JColorChooser:"));
    p.add(makeButton1(label));
    p.add(makeButton2(label));
    add(p, BorderLayout.NORTH);

    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    setPreferredSize(new Dimension(320, 240));
  }

  private JButton makeButton1(JLabel label) {
    JButton button1 = new JButton("Default");
    button1.addActionListener(e -> {
      JColorChooser cc = new JColorChooser();
      cc.setColor(label.getBackground());
      ColorTracker ok = new ColorTracker(cc);
      Component parent = getRootPane();
      String title = "Default JColorChooser";
      JDialog dialog = JColorChooser.createDialog(parent, title, true, cc, ok, null);
      dialog.addComponentListener(new ComponentAdapter() {
        @Override public void componentHidden(ComponentEvent e) {
          ((Window) e.getComponent()).dispose();
        }
      });
      dialog.setVisible(true);
      Color color = ok.getColor();
      if (color != null) {
        label.setBackground(color);
      }
    });
    return button1;
  }

  private JButton makeButton2(JLabel label) {
    JButton button2 = new JButton("Mnemonic");
    button2.addActionListener(e -> {
      // UIManager.put("ColorChooser.rgbRedText", "Red");
      // UIManager.put("ColorChooser.rgbGreenText", "Green");
      // UIManager.put("ColorChooser.rgbBlueText", "Blue");
      JColorChooser cc = new JColorChooser();
      cc.setColor(label.getBackground());
      AbstractColorChooserPanel rgbChooser = getRgbChooser(cc);
      if (rgbChooser != null) {
        AbstractColorChooserPanel[] panels = cc.getChooserPanels();
        List<AbstractColorChooserPanel> choosers = new ArrayList<>(Arrays.asList(panels));
        SwingUtils.descendants(rgbChooser)
            .filter(JRadioButton.class::isInstance)
            .map(JRadioButton.class::cast)
            .forEach(r -> setMnemonic(r, rgbChooser.getLocale()));
        cc.setChooserPanels(choosers.toArray(new AbstractColorChooserPanel[0]));
      }
      ColorTracker ok = new ColorTracker(cc);
      Component parent = getRootPane();
      String title = "ColorChooser.rgbRedTextMnemonic";
      JDialog dialog = JColorChooser.createDialog(parent, title, true, cc, ok, null);
      dialog.addComponentListener(new ComponentAdapter() {
        @Override public void componentHidden(ComponentEvent e) {
          ((Window) e.getComponent()).dispose();
        }
      });
      dialog.setVisible(true);
      Color color = ok.getColor();
      if (color != null) {
        label.setBackground(color);
      }
    });
    return button2;
  }

  private void setMnemonic(JRadioButton r, Locale locale) {
    List<String> rgbKey = Arrays.asList("rgbRed", "rgbGreen", "rgbBlue");
    String fmt = "ColorChooser.%sText";
    List<String> rgbList = Arrays.asList(
        UIManager.getString(String.format(fmt, rgbKey.get(0)), locale),
        UIManager.getString(String.format(fmt, rgbKey.get(1)), locale),
        UIManager.getString(String.format(fmt, rgbKey.get(2)), locale));
    String txt = r.getText();
    int idx = rgbList.indexOf(txt);
    if (idx >= 0) {
      String key = String.format("ColorChooser.%sMnemonic", rgbKey.get(idx));
      int mnemonic = getInteger(key, locale);
      if (mnemonic > 0) {
        r.setMnemonic(Character.toChars(mnemonic)[0]);
        setDisplayedMnemonicIndex(r, key, locale);
      }
    }
  }

  private static void setDisplayedMnemonicIndex(JRadioButton r, String key, Locale locale) {
    int mnemonic = getInteger(key + "Index", locale);
    if (mnemonic >= 0) {
      r.setDisplayedMnemonicIndex(mnemonic);
    }
  }

  // @see javax/swing/colorchooser/ColorModel.java
  private static int getInteger(String key, Locale locale) {
    Object value = UIManager.get(key, locale);
    int iv = -1;
    if (value instanceof Integer) {
      iv = (int) value;
    } else if (value instanceof String) {
      iv = parseInt((String) value);
    }
    return iv;
  }

  private static int parseInt(String value) {
    int iv;
    try {
      iv = Integer.parseInt(value);
    } catch (NumberFormatException ignore) {
      iv = -1;
    }
    return iv;
  }

  private static AbstractColorChooserPanel getRgbChooser(JColorChooser colorChooser) {
    String rgbName = UIManager.getString("ColorChooser.rgbNameText", Locale.getDefault());
    AbstractColorChooserPanel rgbChooser = null;
    for (AbstractColorChooserPanel p : colorChooser.getChooserPanels()) {
      if (Objects.equals(rgbName, p.getDisplayName())) {
        rgbChooser = p;
      }
    }
    return rgbChooser;
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
