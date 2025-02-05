// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public final class MainPanel extends JPanel {
  private final JTextArea log = new JTextArea();

  private MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel(new GridLayout(1, 2, 5, 5));

    JTextField tf = new JTextField();
    p.add(makeTitledPanel("Default", new JButton(makeAction(tf, tf))));

    JPanel panel = new JPanel(new GridLayout(2, 1));
    JTextField field = new JTextField();
    Border enabledBorder = field.getBorder();
    Insets i = enabledBorder.getBorderInsets(field);
    Border disabledBorder = BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(Color.RED),
        BorderFactory.createEmptyBorder(i.top - 1, i.left - 1, i.bottom - 1, i.right - 1));
    String disabledMessage = "Text is required to create ...";
    JLabel label = new JLabel(" ");
    label.setForeground(Color.RED);
    if (field.getText().isEmpty()) {
      field.setBorder(disabledBorder);
      label.setText(disabledMessage);
    }
    panel.add(field);
    panel.add(label);
    field.addHierarchyListener(e -> {
      Component c = e.getComponent();
      if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && c.isShowing()) {
        EventQueue.invokeLater(c::requestFocusInWindow);
      }
    });
    field.getDocument().addDocumentListener(new DocumentListener() {
      private void update() {
        boolean verified = !field.getText().isEmpty();
        JButton b = field.getRootPane().getDefaultButton();
        if (verified) {
          b.setEnabled(true);
          field.setBorder(enabledBorder);
          label.setText(" ");
        } else {
          b.setEnabled(false);
          field.setBorder(disabledBorder);
          label.setText(disabledMessage);
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
    JButton button = new JButton(makeAction(field, panel));
    button.addActionListener(e -> EventQueue.invokeLater(() -> {
      JButton b = field.getRootPane().getDefaultButton();
      if (b != null && field.getText().isEmpty()) {
        b.setEnabled(false);
      }
    }));
    p.add(makeTitledPanel("Disabled OK button", button));

    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private AbstractAction makeAction(JTextField field, Component c) {
    return new AbstractAction("show") {
      @Override public void actionPerformed(ActionEvent e) {
        Component p = log.getRootPane();
        int ret = JOptionPane.showConfirmDialog(
            p, c, "Input text", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ret == JOptionPane.OK_OPTION) {
          log.setText(field.getText());
        }
      }
    };
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
