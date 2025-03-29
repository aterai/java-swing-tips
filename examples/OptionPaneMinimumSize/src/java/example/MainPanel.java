// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    JButton button0 = new JButton("Default");
    button0.addActionListener(e -> {
      Component p = ((JComponent) e.getSource()).getRootPane();
      JOptionPane.showMessageDialog(p, "message0", "title0", JOptionPane.PLAIN_MESSAGE);
    });

    JLabel label1 = new JLabel("message1");
    label1.addHierarchyListener(e -> {
      Component c = e.getComponent();
      if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && c.isShowing()) {
        Container o = SwingUtilities.getAncestorOfClass(JOptionPane.class, c);
        if (o instanceof JOptionPane) {
          JOptionPane op = (JOptionPane) o;
          // BasicOptionPaneUI ui = (BasicOptionPaneUI) op.getUI();
          // System.out.println(ui.getMinimumOptionPaneSize());
          // op.setMinimumSize(new Dimension(120, 120));
          op.setPreferredSize(new Dimension(120, 120));
        }
        Window w = SwingUtilities.getWindowAncestor(c);
        w.pack();
        w.setLocationRelativeTo(getRootPane());
      }
    });
    JButton button1 = new JButton("HierarchyListener + setPreferredSize");
    button1.addActionListener(e -> {
      Component p = ((JComponent) e.getSource()).getRootPane();
      JOptionPane.showMessageDialog(p, label1, "title1(120*120)", JOptionPane.PLAIN_MESSAGE);
    });

    String key = "OptionPane.minimumSize";
    JButton button2 = new JButton(key);
    button2.addActionListener(e -> {
      // UIManager.put(key, new DimensionUIResource(120, 120));
      UIManager.put(key, new Dimension(120, 120));
      Component p = ((JComponent) e.getSource()).getRootPane();
      JOptionPane.showMessageDialog(p, "message3", "title3(120*120)", JOptionPane.PLAIN_MESSAGE);
      UIManager.put(key, UIManager.getLookAndFeelDefaults().getDimension(key));
    });

    JButton button3 = new JButton(key + " + JTextArea");
    button3.addActionListener(e -> {
      UIManager.put(key, new Dimension(120, 120));
      Component p = ((JComponent) e.getSource()).getRootPane();
      JScrollPane s = new JScrollPane(new JTextArea(10, 30));
      JOptionPane.showMessageDialog(p, s, "title4(120*120)", JOptionPane.PLAIN_MESSAGE);
      UIManager.put(key, UIManager.getLookAndFeelDefaults().getDimension(key));
    });

    Stream.of(button0, button1, button2, button3).forEach(this::add);
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
