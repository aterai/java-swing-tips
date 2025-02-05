// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.text.DefaultEditorKit;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Action pasteAction = new DefaultEditorKit.PasteAction();
    pasteAction.putValue(Action.LARGE_ICON_KEY, new ColorIcon(Color.GREEN));

    JButton button = new JButton("text");
    button.setFocusable(false);
    button.setAction(pasteAction);
    button.setIcon(new ColorIcon(Color.RED));
    button.addActionListener(e -> Toolkit.getDefaultToolkit().beep());

    JPanel p = new JPanel();
    p.add(button);
    p.add(Box.createRigidArea(new Dimension(1, 26)));

    JRadioButton r0 = new JRadioButton("setAction", true);
    r0.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        button.setAction(pasteAction);
      }
    });

    JRadioButton r1 = new JRadioButton("null");
    r1.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        button.setAction(null);
      }
    });

    JRadioButton r2 = new JRadioButton("setText");
    r2.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        button.setText("text");
        button.setIcon(new ColorIcon(Color.RED));
      }
    });

    JCheckBox check = new JCheckBox("setHideActionText");
    check.addActionListener(e -> button.setHideActionText(check.isSelected()));

    ButtonGroup bg = new ButtonGroup();
    Box box = Box.createHorizontalBox();
    Stream.of(r0, r1, r2).forEach(r -> {
      bg.add(r);
      box.add(r);
    });

    JPanel pp = new JPanel(new GridLayout(0, 1));
    pp.add(check);
    pp.add(box);
    pp.add(p);

    add(pp, BorderLayout.NORTH);
    add(new JScrollPane(new JTextArea()));
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

class ColorIcon implements Icon {
  private final Color color;

  protected ColorIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(color);
    g2.fillRect(1, 1, getIconWidth() - 2, getIconHeight() - 2);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 16;
  }

  @Override public int getIconHeight() {
    return 16;
  }
}
