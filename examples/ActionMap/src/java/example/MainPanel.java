// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

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
        UIManager.getLookAndFeel().provideErrorFeedback(this);
      }

      @Override public void cut() {
        UIManager.getLookAndFeel().provideErrorFeedback(this);
      }
    };
    am = pf2.getActionMap();
    am.put(DefaultEditorKit.pasteAction, new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        Component c = (Component) e.getSource();
        EventQueue.invokeLater(() -> {
          UIManager.getLookAndFeel().provideErrorFeedback(c);
          String msg = "paste is disabled";
          JOptionPane.showMessageDialog(getRootPane(), msg, "Error", JOptionPane.ERROR_MESSAGE);
        });
      }
    });
    pf2.setActionMap(am);

    Box panel = Box.createVerticalBox();
    panel.setBorder(BorderFactory.createTitledBorder("E-mail Address"));
    panel.add(pf1);
    panel.add(Box.createVerticalStrut(5));
    panel.add(new JLabel("Please enter your email address twice for confirmation:"));
    panel.add(pf2);
    panel.add(Box.createVerticalStrut(5));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(panel, BorderLayout.NORTH);
    add(new JScrollPane(new JTextArea("JTextArea")));
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
