// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Box accordion = Box.createVerticalBox();
    accordion.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    createExpansionPanelList().forEach(p -> {
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

  private static List<ExpansionPanel> createExpansionPanelList() {
    return Arrays.asList(
        new ExpansionPanel("System Tasks", createSystemTasksPanel()),
        new ExpansionPanel("Other Places", createOtherPlacesPanel()),
        new ExpansionPanel("Details", createDetailsPanel()));
  }

  private static JPanel createSystemTasksPanel() {
    JPanel p = new JPanel(new GridLayout(0, 1));
    Stream.of("1111", "222222")
        .map(JCheckBox::new)
        .forEach(p::add);
    return p;
  }

  private static JPanel createOtherPlacesPanel() {
    JPanel p = new JPanel(new GridLayout(0, 1));
    Stream.of("Desktop", "My Network Places", "My Documents", "Shared Documents")
        .map(JLabel::new)
        .forEach(p::add);
    return p;
  }

  private static JPanel createDetailsPanel() {
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

final class ExpansionPanel extends JPanel {
  private final JToggleButton toggleButton = new JToggleButton() {
    @Override public void updateUI() {
      super.updateUI();
      setContentAreaFilled(false);
      setBorderPainted(false);
      setFocusable(false);
    }
  };
  private final JButton titleButton = new JButton() {
    @Override public void updateUI() {
      super.updateUI();
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
    }
  };
  private final JComponent contentPanel;

  // The content is passed in instead of being created by an abstract factory
  // method called from a field initializer, which would run before the
  // subclass fields are initialized.
  /* default */ ExpansionPanel(String title, JComponent contentPanel) {
    super(new BorderLayout());
    this.contentPanel = contentPanel;
    Icon arrowIcon = UIManager.getIcon("Menu.arrowIcon");
    AnimatableIcon animatedIcon = new AnimatableIcon(arrowIcon, toggleButton);
    toggleButton.setIcon(animatedIcon);
    // An ItemListener is also notified by setSelected(), so it handles both buttons.
    toggleButton.addItemListener(e -> updateExpandedState(animatedIcon));
    titleButton.add(new JLabel(title));
    titleButton.add(Box.createHorizontalGlue());
    titleButton.add(toggleButton);
    titleButton.addActionListener(e -> toggleButton.setSelected(!toggleButton.isSelected()));
    add(titleButton, BorderLayout.NORTH);
    contentPanel.setVisible(false);
    contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    add(contentPanel);
  }

  private void updateExpandedState(AnimatableIcon animatedIcon) {
    boolean expanded = toggleButton.isSelected();
    animatedIcon.animateTo(expanded ? 90d : 0d);
    contentPanel.setVisible(expanded);
    revalidate();
    if (expanded) {
      // The rectangle is relative to contentPanel itself, so getBounds() cannot be used.
      EventQueue.invokeLater(() -> {
        contentPanel.scrollRectToVisible(new Rectangle(contentPanel.getSize()));
      });
    }
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = titleButton.getPreferredSize();
    if (contentPanel.isVisible()) {
      d.height += contentPanel.getPreferredSize().height;
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
  private static final double EASING_RATE = .5;
  private static final double SNAP_THRESHOLD = .5;
  private final Icon icon;
  private final Component parent;
  private final Timer timer = new Timer(15, e -> updateAngle());
  // MetalIconFactory.MenuArrowIcon casts the component passed to paintIcon() to JMenuItem.
  private final Component menuItem = new JMenuItem();
  private double currentAngle;
  private double targetAngle;

  protected AnimatableIcon(Icon icon, Component parent) {
    this.icon = icon;
    this.parent = parent;
  }

  private void updateAngle() {
    double diff = targetAngle - currentAngle;
    if (Math.abs(diff) < SNAP_THRESHOLD) {
      currentAngle = targetAngle;
      timer.stop();
    } else {
      currentAngle += diff * EASING_RATE;
    }
    parent.repaint();
  }

  public void animateTo(double angle) {
    targetAngle = angle;
    if (!timer.isRunning()) {
      timer.start();
    }
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    // Use floating point so that an icon with an odd size rotates around its true center.
    double cx = x + icon.getIconWidth() / 2d;
    double cy = y + icon.getIconHeight() / 2d;
    g2.rotate(Math.toRadians(currentAngle), cx, cy);
    icon.paintIcon(menuItem, g2, x, y);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return icon.getIconWidth();
  }

  @Override public int getIconHeight() {
    return icon.getIconHeight();
  }
}
