// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea log = new JTextArea();
    JPanel p = new JPanel(new GridLayout(1, 2, 5, 5));

    JTextField field1 = new JTextField();
    JButton button1 = new JButton("show");
    button1.addActionListener(e -> {
      Component p1 = log.getRootPane();
      int ret = JOptionPane.showConfirmDialog(
          p1, field1, "Input text", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
      if (ret == JOptionPane.OK_OPTION) {
        log.setText(field1.getText());
      }
    });
    p.add(makeTitledPanel("Default", button1));

    JPanel panel2 = new JPanel(new GridLayout(2, 1));
    JTextField field2 = new JTextField();
    Border enabledBorder = field2.getBorder();
    Insets i = enabledBorder.getBorderInsets(field2);
    Border disabledBorder = BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(Color.RED),
        BorderFactory.createEmptyBorder(i.top - 1, i.left - 1, i.bottom - 1, i.right - 1));
    String disabledMessage = "Text is required to create ...";
    JLabel label2 = new JLabel(" ");
    label2.setForeground(Color.RED);
    panel2.add(field2);
    panel2.add(label2);
    if (field2.getText().isEmpty()) {
      field2.setBorder(disabledBorder);
      label2.setText(disabledMessage);
    }
    field2.addHierarchyListener(e -> {
      Component c = e.getComponent();
      if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && c.isShowing()) {
        EventQueue.invokeLater(c::requestFocusInWindow);
      }
    });
    field2.getDocument().addDocumentListener(new DocumentListener() {
      private void update() {
        boolean verified = !field2.getText().isEmpty();
        JButton b = field2.getRootPane().getDefaultButton();
        if (verified) {
          b.setEnabled(true);
          field2.setBorder(enabledBorder);
          label2.setText(" ");
        } else {
          b.setEnabled(false);
          field2.setBorder(disabledBorder);
          label2.setText(disabledMessage);
        }
      }

      @Override public void insertUpdate(DocumentEvent e) {
        update();
      }

      @Override public void removeUpdate(DocumentEvent e) {
        update();
      }

      @Override public void changedUpdate(DocumentEvent e) {
        update();
      }
    });
    JButton button2 = new JButton("show");
    button2.addActionListener(e -> {
      Component p2 = log.getRootPane();
      EventQueue.invokeLater(() -> {
        JButton b = field2.getRootPane().getDefaultButton();
        if (b != null && field2.getText().isEmpty()) {
          b.setEnabled(false);
        }
      });
      int ret = JOptionPane.showConfirmDialog(
          p2, panel2, "Input text", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
      if (ret == JOptionPane.OK_OPTION) {
        log.setText(field2.getText());
      }
    });
    p.add(makeTitledPanel("Disabled OK button", button2));

    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
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
