// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private Timer animator;
  private boolean isHidden = true;
  private final JPanel controls = new JPanel(new BorderLayout(5, 5) {
    private int controlsHeight;
    @Override public Dimension preferredLayoutSize(Container target) {
      // synchronized (target.getTreeLock()) {
      Dimension ps = super.preferredLayoutSize(target);
      int defaultHeight = ps.height;
      if (Objects.nonNull(animator)) {
        if (isHidden) {
          if (controls.getHeight() < defaultHeight) {
            controlsHeight += 5;
          }
        } else {
          if (controls.getHeight() > 0) {
            controlsHeight -= 5;
          }
        }
        if (controlsHeight <= 0) {
          controlsHeight = 0;
          animator.stop();
        } else if (controlsHeight >= defaultHeight) {
          controlsHeight = defaultHeight;
          animator.stop();
        }
      }
      ps.height = controlsHeight;
      return ps;
    }
  });

  private MainPanel() {
    super(new BorderLayout());
    JButton button = new JButton("Find Next(test)");
    button.setFocusable(false);
    JTextField field = new JTextField("", 10);

    controls.setBorder(BorderFactory.createTitledBorder("Search down"));
    controls.add(new JLabel("Find what:"), BorderLayout.WEST);
    controls.add(field);
    controls.add(button, BorderLayout.EAST);

    Action act = new AbstractAction("Show/Hide Search Box") {
      @Override public void actionPerformed(ActionEvent ev) {
        if (Objects.nonNull(animator) && animator.isRunning()) {
          return;
        }
        isHidden = controls.getHeight() == 0;
        animator = new Timer(5, e -> controls.revalidate());
        animator.start();
      }
    };
    JButton showHideButton = new JButton();
    showHideButton.setAction(act);
    showHideButton.setFocusable(false);
    JPanel p = new JPanel(new BorderLayout());

    int modifiers = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    // Java 10: int modifiers = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    InputMap im = p.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, modifiers), "open-searchbox");
    p.getActionMap().put("open-searchbox", act);
    p.add(controls, BorderLayout.NORTH);

    JTree tree = new JTree();
    p.add(new JScrollPane(tree));
    p.add(showHideButton, BorderLayout.SOUTH);

    add(p);
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
    // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
