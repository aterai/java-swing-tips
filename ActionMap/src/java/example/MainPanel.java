package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.text.DefaultEditorKit;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JTextField pf1 = new JTextField(25);
    ActionMap am = pf1.getActionMap();
    Action beep = new DefaultEditorKit.BeepAction();

    am.put(DefaultEditorKit.cutAction, beep);
    am.put(DefaultEditorKit.copyAction, beep);
    am.put(DefaultEditorKit.pasteAction, beep);
    pf1.setActionMap(am);

    JTextField pf2 = new JTextField() {
      @Override public void copy() {
        // System.out.println("copy");
        UIManager.getLookAndFeel().provideErrorFeedback(this);
        // Toolkit.getDefaultToolkit().beep();
      }

      @Override public void cut() {
        // System.out.println("cut");
        UIManager.getLookAndFeel().provideErrorFeedback(this);
        // Toolkit.getDefaultToolkit().beep();
      }
    };
    am = pf2.getActionMap();
    am.put(DefaultEditorKit.pasteAction, new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        EventQueue.invokeLater(() -> {
          Toolkit.getDefaultToolkit().beep();
          JOptionPane.showMessageDialog(getRootPane(), "paste is disabled", "title", JOptionPane.ERROR_MESSAGE);
        });
      }
    });
    pf2.setActionMap(am);

    Box panel = Box.createVerticalBox();
    panel.setBorder(BorderFactory.createTitledBorder("E-mail Address"));
    panel.add(pf1);
    panel.add(Box.createVerticalStrut(5));
    panel.add(new JLabel("Please enter your email adress twice for confirmation:"));
    panel.add(pf2);
    panel.add(Box.createVerticalStrut(5));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(panel, BorderLayout.NORTH);
    add(new JScrollPane(new JTextArea("Dummy")));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException
         | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
