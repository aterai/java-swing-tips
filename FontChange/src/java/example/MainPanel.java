// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Locale;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;

public class MainPanel extends JPanel {
  protected static final Font FONT12 = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
  protected static final Font FONT24 = new Font(Font.SANS_SERIF, Font.PLAIN, 24);
  protected static final Font FONT32 = new Font(Font.SANS_SERIF, Font.PLAIN, 32);

  public MainPanel() {
    super(new BorderLayout());

    JToggleButton tgb12 = new JToggleButton("12");
    tgb12.addActionListener(e -> updateFont(FONT12));

    JToggleButton tgb24 = new JToggleButton("24");
    tgb24.addActionListener(e -> updateFont(FONT24));

    JToggleButton tgb32 = new JToggleButton("32");
    tgb32.addActionListener(e -> updateFont(FONT32));

    JToolBar toolbar = new JToolBar();
    ButtonGroup bg = new ButtonGroup();
    Stream.of(tgb12, tgb24, tgb32).forEach(b -> {
      b.setFocusPainted(false);
      bg.add(b);
      toolbar.add(b);
    });

    JButton button = new JButton("Dialog");
    button.addActionListener(e -> {
      Toolkit.getDefaultToolkit().beep();
      JOptionPane.showMessageDialog(getRootPane(), "MessageDialog", "Change All Font Size", JOptionPane.ERROR_MESSAGE);
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

    add(toolbar, BorderLayout.NORTH);
    add(panel);
    updateFont(FONT12);
    setPreferredSize(new Dimension(320, 240));
  }

  protected final void updateFont(Font font) {
    FontUIResource fontResource = new FontUIResource(font);
    // for (Object o: UIManager.getLookAndFeelDefaults().keySet()) {
    //   if (o.toString().toLowerCase(Locale.ENGLISH).endsWith("font")) {
    //     UIManager.put(o, fontResource);
    //   }
    // }
    UIManager.getLookAndFeelDefaults().entrySet().stream()
      .filter(e -> e.getKey().toString().toLowerCase(Locale.ENGLISH).endsWith("font"))
      .forEach(e -> UIManager.put(e.getKey(), fontResource));
    recursiveUpdateUI(this); // SwingUtilities.updateComponentTreeUI(this);
    Container c = getTopLevelAncestor();
    if (c instanceof Window) {
      ((Window) c).pack();
    }
  }

  private static void recursiveUpdateUI(Container p) {
    for (Component c: p.getComponents()) {
      if (c instanceof JToolBar) {
        continue;
      } else if (c instanceof JComponent) {
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
