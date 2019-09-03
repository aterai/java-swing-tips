// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    box.add(new JTextField("Another focusable component"));
    box.add(Box.createVerticalStrut(5));
    ButtonGroup bg1 = new ButtonGroup();
    box.add(makeButtonGroupPanel("Default", bg1));
    box.add(Box.createVerticalStrut(5));

    ButtonGroup bg2 = new ButtonGroup();
    Container buttons = makeButtonGroupPanel("FocusTraversalPolicy", bg2);
    buttons.setFocusTraversalPolicyProvider(true);
    buttons.setFocusTraversalPolicy(new LayoutFocusTraversalPolicy() {
      @Override public Component getDefaultComponent(Container focusCycleRoot) {
        ButtonModel selection = bg2.getSelection();
        // for (Component c: focusCycleRoot.getComponents()) {
        //   JRadioButton r = (JRadioButton) c;
        //   if (r.getModel().equals(selection)) {
        //     return r;
        //   }
        // }
        // return super.getDefaultComponent(focusCycleRoot);
        return Stream.of(focusCycleRoot.getComponents())
          .filter(c -> ((JRadioButton) c).getModel().equals(selection))
          .findFirst().orElse(super.getDefaultComponent(focusCycleRoot));
      }
    });
    box.add(buttons);
    box.add(Box.createVerticalStrut(5));

    JButton clear = new JButton("clear selection");
    clear.addActionListener(e -> {
      bg1.clearSelection();
      bg2.clearSelection();
    });

    Box b = Box.createHorizontalBox();
    b.add(Box.createHorizontalGlue());
    b.add(clear);
    box.add(b);
    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Container makeButtonGroupPanel(String title, ButtonGroup bg) {
    JPanel p = new JPanel();
    Stream.of("aaa", "bbb", "ccc", "ddd", "eee").forEach(s -> {
      JRadioButton rb = new JRadioButton(s, "ccc".equals(s));
      bg.add(rb);
      p.add(rb);
    });
    p.setBorder(BorderFactory.createTitledBorder(title));
    return p;
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
