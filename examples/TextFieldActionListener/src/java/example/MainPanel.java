// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public final class MainPanel extends JPanel {
  private final JTextArea log = new JTextArea();

  private MainPanel() {
    super(new BorderLayout());
    JButton button = new JButton("JButton");
    button.addActionListener(e -> append("JButton clicked"));

    JCheckBox check = new JCheckBox("setDefaultButton");
    check.addActionListener(e -> button.getRootPane().setDefaultButton(button));

    JTextField textField1 = new JTextField("addDocumentListener");
    textField1.getDocument().addDocumentListener(new DocumentListener() {
      @Override public void insertUpdate(DocumentEvent e) {
        append("insertUpdate");
      }

      @Override public void removeUpdate(DocumentEvent e) {
        append("removeUpdate");
      }

      @Override public void changedUpdate(DocumentEvent e) {
        /* not needed */
      }
    });

    JTextField textField2 = new JTextField("addActionListener");
    textField2.addActionListener(e -> append(((JTextField) e.getSource()).getText()));

    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    box.add(new JTextField("JTextField"));
    box.add(Box.createVerticalStrut(10));
    box.add(textField1);
    box.add(Box.createVerticalStrut(10));
    box.add(textField2);

    JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    p.add(check);
    p.add(button);
    add(box, BorderLayout.NORTH);
    add(new JScrollPane(log));
    add(p, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  public void append(String text) {
    log.append(text + "\n");
    log.setCaretPosition(log.getDocument().getLength());
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
