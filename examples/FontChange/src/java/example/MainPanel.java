// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Locale;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;

public final class MainPanel extends JPanel {
  private static final Font FONT12 = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
  private static final Font FONT24 = new Font(Font.SANS_SERIF, Font.PLAIN, 24);
  private static final Font FONT32 = new Font(Font.SANS_SERIF, Font.PLAIN, 32);

  private MainPanel() {
    super(new BorderLayout());
    JToggleButton tgb12 = new JToggleButton("12");
    tgb12.addActionListener(e -> updateFont(FONT12));

    JToggleButton tgb24 = new JToggleButton("24");
    tgb24.addActionListener(e -> updateFont(FONT24));

    JToggleButton tgb32 = new JToggleButton("32");
    tgb32.addActionListener(e -> updateFont(FONT32));

    JToolBar toolBar = new JToolBar();
    ButtonGroup bg = new ButtonGroup();
    Stream.of(tgb12, tgb24, tgb32).forEach(b -> {
      b.setFocusPainted(false);
      bg.add(b);
      toolBar.add(b);
    });

    JButton button = new JButton("Dialog");
    button.addActionListener(e -> {
      Component c = getRootPane();
      UIManager.getLookAndFeel().provideErrorFeedback(c);
      JOptionPane.showMessageDialog(c, "Error message", "title", JOptionPane.ERROR_MESSAGE);
    });

    JPanel panel = new JPanel(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 0d;
    c.insets = new Insets(5, 5, 5, 0);
    // c.anchor = GridBagConstraints.LINE_START;
    panel.add(new JLabel("Test:"), c);

    c.weightx = 1d;
    c.fill = GridBagConstraints.HORIZONTAL;
    panel.add(new JComboBox<>(new String[] {"Test"}), c);

    c.weightx = 0d;
    c.insets = new Insets(5, 5, 5, 5);
    c.anchor = GridBagConstraints.LINE_END;
    panel.add(button, c);

    add(toolBar, BorderLayout.NORTH);
    add(panel);
    updateFont(FONT12);
    setPreferredSize(new Dimension(320, 240));
  }

  private void updateFont(Font font) {
    FontUIResource fontResource = new FontUIResource(font);
    // for (Object o : UIManager.getLookAndFeelDefaults().keySet()) {
    //   if (o.toString().toLowerCase(Locale.ENGLISH).endsWith("font")) {
    //     UIManager.put(o, fontResource);
    //   }
    // }
    // UIManager.getLookAndFeelDefaults().entrySet().stream()
    //     .filter(e -> e.getKey().toString().toLowerCase(Locale.ENGLISH).endsWith("font"))
    //     .forEach(e -> UIManager.put(e.getKey(), fontResource));
    UIManager.getLookAndFeelDefaults().forEach((key, value) -> {
      if (key.toString().toLowerCase(Locale.ENGLISH).endsWith("font")) {
        UIManager.put(key, fontResource);
      }
    });
    recursiveUpdateUI(this); // SwingUtilities.updateComponentTreeUI(this);
    Container c = getTopLevelAncestor();
    if (c instanceof Window) {
      ((Window) c).pack();
    }
  }

  private static void recursiveUpdateUI(Container p) {
    for (Component c : p.getComponents()) {
      if (c instanceof JComponent && !(c instanceof JToolBar)) {
        JComponent jc = (JComponent) c;
        jc.updateUI();
        if (jc.getComponentCount() > 0) {
          recursiveUpdateUI(jc);
        }
      }
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
