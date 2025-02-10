// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabs = new JTabbedPane() {
      @Override public void addTab(String title, Component content) {
        super.addTab(title, content);
        JCheckBox check = new JCheckBox();
        check.setOpaque(false);
        check.setFocusable(false);
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        p.setOpaque(false);
        p.add(check, BorderLayout.WEST);
        p.add(new JLabel(title));
        setTabComponentAt(getTabCount() - 1, p);
        // TEST: setTabComponentAt(getTabCount() - 1, new TestIconCheckBox(title));
      }
    };
    tabs.addTab("JTree", new JScrollPane(new JTree()));
    tabs.addTab("JLabel", new JLabel("JLabel"));

    AtomicInteger counter = new AtomicInteger(0);
    JButton button = new JButton("Add");
    button.addActionListener(e -> {
      int i = counter.getAndIncrement();
      Component tab = i % 2 == 0 ? new JTree() : new JLabel("JLabel: " + i);
      tabs.addTab("Tab" + i, tab);
      tabs.setSelectedIndex(tabs.getTabCount() - 1);
    });

    add(tabs);
    add(button, BorderLayout.SOUTH);
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

/*
class TestIconCheckBox extends JCheckBox {
  private final Rectangle viewRect = new Rectangle();
  private final Rectangle textRect = new Rectangle();
  private final Rectangle iconRect = new Rectangle();

  protected TestIconCheckBox(String title) {
    super(title);
  }

  @Override public void updateUI() {
    super.updateUI();
    setOpaque(false);
    setFocusable(false);
  }

  @Override public boolean contains(int x, int y) {
    Icon icon;
    ButtonUI ui = getUI();
    if (ui instanceof BasicRadioButtonUI) {
      icon = ((BasicRadioButtonUI) ui).getDefaultIcon();
    } else if (ui instanceof SynthButtonUI) {
      // icon = ((SynthButtonUI) ui).getDefaultIcon(this);
      SynthContext context = ((SynthButtonUI) ui).getContext(this);
      icon = context.getStyle().getIcon(context, "CheckBox.icon");
      // context.dispose();
    } else {
      icon = getIcon();
    }
    if (Objects.nonNull(icon)) {
      // layout the text and icon
      int width = getWidth();
      int height = getHeight();
      FontMetrics fm = getFontMetrics(getFont());
      Insets i = getInsets();
      viewRect.setBounds(
          i.left, i.top, width - i.right - i.left, height - i.bottom - i.top);
      textRect.setBounds(0, 0, 0, 0);
      iconRect.setBounds(0, 0, 0, 0);
      SwingUtilities.layoutCompoundLabel(
          this, fm, getText(), icon,
          getVerticalAlignment(), getHorizontalAlignment(),
          getVerticalTextPosition(), getHorizontalTextPosition(),
          viewRect, iconRect, textRect,
          getIconTextGap());
      return iconRect.contains(x, y);
    } else {
      return super.contains(x, y);
    }
  }
}
*/
