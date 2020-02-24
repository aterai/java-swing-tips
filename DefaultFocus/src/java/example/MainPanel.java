// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea ta = new JTextArea("JTextArea");
    ta.setEditable(false);

    JTextField field = new JTextField();
    JButton nb = new JButton("NORTH");
    JButton sb = new JButton("SOUTH");
    JButton wb = new JButton("WEST");
    JButton eb = new JButton("EAST");

    JPanel p = new JPanel(new BorderLayout(5, 5));
    p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    p.add(nb, BorderLayout.NORTH);
    p.add(sb, BorderLayout.SOUTH);
    p.add(wb, BorderLayout.WEST);
    p.add(eb, BorderLayout.EAST);
    p.add(field);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(ta));
    setPreferredSize(new Dimension(320, 240));

    // frame.addWindowListener(new WindowAdapter() {
    //   @Override public void windowOpened(WindowEvent e) {
    //     System.out.println("windowOpened");
    //     field.requestFocus();
    //   }
    // });

    // frame.setFocusTraversalPolicy(new LayoutFocusTraversalPolicy() {
    //   @Override public Component getInitialComponent(Window w) {
    //     System.out.println("getInitialComponent");
    //     return field;
    //   }
    // });

    // frame.addComponentListener(new ComponentAdapter() {
    //   @Override public void componentShown(ComponentEvent e) {
    //     System.out.println("componentShown");
    //     field.requestFocusInWindow();
    //   }
    // });

    // KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    // focusManager.addPropertyChangeListener(new PropertyChangeListener() {
    //   @Override public void propertyChange(PropertyChangeEvent e) {
    //     String prop = e.getPropertyName();
    //     if ("activeWindow".equals(prop) && e.getNewValue() != null) {
    //       System.out.println("activeWindow");
    //       field.requestFocusInWindow();
    //     }
    //   }
    // });

    EventQueue.invokeLater(() -> {
      System.out.println("invokeLater");
      field.requestFocusInWindow();
      System.out.println("getRootPane().setDefaultButton(eb)");
      getRootPane().setDefaultButton(eb);
    });

    System.out.println("this");
    // field.requestFocusInWindow();
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
    System.out.println("frame.pack();");
    frame.pack();
    frame.setLocationRelativeTo(null);
    System.out.println("frame.setVisible(true);");
    frame.setVisible(true);
  }
}
