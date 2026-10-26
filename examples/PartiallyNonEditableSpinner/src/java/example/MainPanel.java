// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.synth.SynthUI;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    JSpinner spinner1 = new JSpinner(new SpinnerNumberModel(0d, 0d, 1d, .01));
    JSpinner.NumberEditor editor1 = new JSpinner.NumberEditor(spinner1, "0%");
    spinner1.setEditor(editor1);

    JSpinner spinner2 = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
    JSpinner.NumberEditor editor2 = new JSpinner.NumberEditor(spinner2) {
      @Override public void updateUI() {
        if (getComponentCount() > 0) {
          JFormattedTextField f = getTextField();
          f.setBorder(null); // Nimbus
          super.updateUI();
          initTextFieldBorder(f);
        } else {
          super.updateUI();
        }
      }
    };
    spinner2.setEditor(editor2);
    initTextFieldBorder(editor2.getTextField());

    // // Component Border - Java Tips Weblog
    // // https://tips4java.wordpress.com/2009/09/27/component-border/
    // JLabel label = new JLabel("%");
    // label.setBorder(BorderFactory.createEmptyBorder());
    // label.setOpaque(true);
    // label.setBackground(Color.WHITE);
    // ComponentBorder cb = new ComponentBorder(label);
    // cb.setGap(0);
    // cb.install(editor2);

    add(createTitledPanel("JSpinner+Default", spinner1));
    add(createTitledPanel("JSpinner+StringBorder", spinner2));
    setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static void initTextFieldBorder(JTextField textField) {
    // The child JFormattedTextField is updated after this editor by
    // SwingUtilities.updateComponentTreeUI(...), so wait until its new border is installed
    EventQueue.invokeLater(() -> {
      Border b = new StringBorder("%");
      // Synth(Nimbus) paints the text field background with its border, so keep it
      if (textField.getUI() instanceof SynthUI) {
        Border c = textField.getBorder();
        textField.setBorder(Objects.nonNull(c) ? BorderFactory.createCompoundBorder(c, b) : b);
      } else {
        textField.setBorder(b);
      }
    });
  }

  private static Component createTitledPanel(String title, Component cmp) {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 1d;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(5, 5, 5, 5);
    p.add(cmp, c);
    return p;
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

class StringBorder implements Border {
  private final String text;

  protected StringBorder(String text) {
    this.text = text;
  }

  @Override public Insets getBorderInsets(Component c) {
    // Measure with the current font of the component so that the insets
    // follow font and LookAndFeel changes
    int w = c.getFontMetrics(c.getFont()).stringWidth(text);
    return new Insets(0, 0, 0, w);
  }

  @Override public boolean isBorderOpaque() {
    return false;
  }

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    Graphics2D g2 = (Graphics2D) g.create();
    // Use the same text antialiasing as the text field
    Object hints = Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
    if (hints instanceof Map) {
      g2.addRenderingHints((Map<?, ?>) hints);
    }
    Font font = c.getFont();
    g2.setFont(font);
    boolean disabled = c instanceof JTextComponent && !c.isEnabled();
    g2.setPaint(disabled ? ((JTextComponent) c).getDisabledTextColor() : c.getForeground());
    FontMetrics fm = c.getFontMetrics(font);
    int tx = x + width - fm.stringWidth(text);
    int ty = y + (height - fm.getHeight()) / 2 + fm.getAscent();
    g2.drawString(text, tx, ty);
    g2.dispose();
  }
}
