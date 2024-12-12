// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    ButtonGroup group1 = new ButtonGroup();
    Container p1 = makeUI(group1);
    p1.setFocusTraversalPolicyProvider(true);
    p1.setFocusTraversalPolicy(new ContainerOrderFocusTraversalPolicy());

    ButtonGroup group2 = new ButtonGroup();
    Container p2 = makeUI(group2);
    p2.setFocusTraversalPolicyProvider(true);
    p2.setFocusTraversalPolicy(new ContainerOrderFocusTraversalPolicy() {
      @Override public Component getDefaultComponent(Container focusCycleRoot) {
        ButtonModel selection = group2.getSelection();
        return Stream.of(focusCycleRoot.getComponents())
            .filter(JRadioButton.class::isInstance)
            .filter(c -> ((JRadioButton) c).getModel().equals(selection))
            .findFirst().orElse(super.getDefaultComponent(focusCycleRoot));
      }
    });

    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    tabbedPane.setBorder(BorderFactory.createTitledBorder("FocusTraversalPolicy"));
    tabbedPane.addTab("Layout", makeUI(new ButtonGroup()));
    tabbedPane.addTab("ContainerOrder", p1);
    tabbedPane.addTab("ContainerOrder + ButtonGroup", p2);
    add(tabbedPane);
    setPreferredSize(new Dimension(320, 240));
  }

  private Container makeUI(ButtonGroup group) {
    JPanel p = new JPanel(new GridBagLayout());
    p.setFocusable(false);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 1;
    List<JComponent> list = Arrays.asList(
        new JRadioButton("JRadioButton1"),
        new JRadioButton("JRadioButton2"),
        new JRadioButton("JRadioButton3", true),
        new JLabel("JLabel1"),
        new JLabel("JLabel2"),
        new JCheckBox("JCheckBox1"),
        new JCheckBox("JCheckBox2"));
    for (JComponent c : list) {
      if (c instanceof JRadioButton) {
        group.add((JRadioButton) c);
      } else if (c instanceof JLabel) {
        c.setFocusable(false);
      }
      p.add(c, gbc);
    }
    gbc.gridx = 2;
    gbc.weightx = 1.0;
    list.forEach(c -> p.add(new JTextField(), gbc));
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
