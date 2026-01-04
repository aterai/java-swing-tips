// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Box accordion = Box.createVerticalBox();
    accordion.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    makeExpansionPanelList().forEach(p -> {
      accordion.add(p);
      accordion.add(Box.createVerticalStrut(5));
    });
    accordion.add(Box.createVerticalGlue());

    JScrollPane scroll = new JScrollPane(accordion);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.getVerticalScrollBar().setUnitIncrement(25);

    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scroll, new JPanel());
    split.setResizeWeight(.5);
    split.setDividerLocation(160);
    split.setDividerSize(2);

    add(split);
    setPreferredSize(new Dimension(320, 240));
  }

  private List<AbstractExpansionPanel> makeExpansionPanelList() {
    return Arrays.asList(
        new AbstractExpansionPanel("System Tasks") {
          @Override public JPanel makePanel() {
            JPanel p = new JPanel(new GridLayout(0, 1));
            Stream.of("1111", "222222")
                .map(JCheckBox::new)
                .forEach(p::add);
            return p;
          }
        },
        new AbstractExpansionPanel("Other Places") {
          @Override public JPanel makePanel() {
            JPanel p = new JPanel(new GridLayout(0, 1));
            Stream.of("Desktop", "My Network Places", "My Documents", "Shared Documents")
                .map(JLabel::new)
                .forEach(p::add);
            return p;
          }
        },
        new AbstractExpansionPanel("Details") {
          @Override public JPanel makePanel() {
            JPanel p = new JPanel(new GridLayout(0, 1));
            ButtonGroup bg = new ButtonGroup();
            Stream.of("aaa", "bbb", "ccc", "ddd")
                .map(JRadioButton::new)
                .forEach(b -> {
                  b.setSelected(p.getComponentCount() == 0);
                  p.add(b);
                  bg.add(b);
                });
            return p;
          }
        }
    );
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

abstract class AbstractExpansionPanel extends JPanel {
  private final JToggleButton toggleButton = new JToggleButton() {
    @Override public void updateUI() {
      super.updateUI();
      setContentAreaFilled(false);
      setBorderPainted(false);
      setFocusable(false);
    }
  };
  private final JButton button = new JButton() {
    @Override public void updateUI() {
      super.updateUI();
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
    }
  };
  private final JPanel panel = makePanel();

  protected AbstractExpansionPanel(String title) {
    super(new BorderLayout());
    Icon arrowIcon = UIManager.getIcon("Menu.arrowIcon");
    AnimatableIcon animIcon = new AnimatableIcon(arrowIcon, toggleButton);
    toggleButton.setIcon(animIcon);
    toggleButton.addActionListener(e -> rotate(animIcon));
    button.add(new JLabel(title));
    button.add(Box.createHorizontalGlue());
    button.add(toggleButton);
    button.addActionListener(e -> {
      toggleButton.setSelected(!toggleButton.isSelected());
      EventQueue.invokeLater(() -> rotate(animIcon));
    });
    add(button, BorderLayout.NORTH);
    panel.setVisible(false);
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    add(panel);
  }

  private void rotate(AnimatableIcon animeIcon) {
    if (toggleButton.isSelected()) {
      animeIcon.animateTo(90d);
      panel.setVisible(true);
    } else {
      animeIcon.animateTo(0d);
      panel.setVisible(false);
    }
    revalidate();
    EventQueue.invokeLater(() -> panel.scrollRectToVisible(panel.getBounds()));
  }

  public abstract JPanel makePanel();

  @Override public final Component add(Component comp) {
    return super.add(comp);
  }

  @Override public final void add(Component comp, Object constraints) {
    super.add(comp, constraints);
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = button.getPreferredSize();
    if (panel.isVisible()) {
      d.height += panel.getPreferredSize().height;
    }
    return d;
  }

  @Override public Dimension getMaximumSize() {
    Dimension d = getPreferredSize();
    d.width = Short.MAX_VALUE;
    return d;
  }
}

class AnimatableIcon implements Icon {
  private final Icon icon;
  private final Timer timer = new Timer(15, null);
  private double currentAngle;
  private double targetAngle;
  private Component parent;

  protected AnimatableIcon(Icon icon, Component parent) {
    this.icon = icon;
    this.parent = parent;
    double step = .5;
    this.timer.addActionListener(e -> {
      double diff = targetAngle - currentAngle;
      if (Math.abs(diff) < step) {
        currentAngle = targetAngle;
        timer.stop();
      } else {
        currentAngle += diff * step;
      }
      Optional.ofNullable(this.parent).ifPresent(Component::repaint);
    });
  }

  public void animateTo(double angle) {
    this.targetAngle = angle;
    if (!timer.isRunning()) {
      timer.start();
    }
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    if (this.parent == null) {
      this.parent = c;
    }
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    int cx = x + icon.getIconWidth() / 2;
    int cy = y + icon.getIconHeight() / 2;
    g2.rotate(Math.toRadians(currentAngle), cx, cy);
    icon.paintIcon(c, g2, x, y);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return icon.getIconWidth();
  }

  @Override public int getIconHeight() {
    return icon.getIconHeight();
  }
}
