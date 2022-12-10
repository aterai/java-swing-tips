// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    MouseListener focusHandler = new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        Component c = ((JLabel) e.getComponent()).getLabelFor();
        if (c != null) {
          c.requestFocusInWindow();
        }
      }
    };

    JLabel label1 = new JLabel("Mail Address:", SwingConstants.RIGHT);
    label1.addMouseListener(focusHandler);
    label1.setDisplayedMnemonic('M');
    Component textField1 = new JTextField(12);
    label1.setLabelFor(textField1);

    JLabel label2 = new JLabel("Password:", SwingConstants.RIGHT);
    label2.addMouseListener(focusHandler);
    label2.setDisplayedMnemonic('P');
    Component textField2 = new JPasswordField(12);
    label2.setLabelFor(textField2);

    JLabel label3 = new JLabel("TextArea:", SwingConstants.RIGHT);
    label3.addMouseListener(focusHandler);
    label3.setDisplayedMnemonic('T');
    Component textField3 = new JTextArea(6, 12);
    label3.setLabelFor(textField3);

    JPanel p = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    addRow(label1, textField1, p, c);
    addRow(label2, textField2, p, c);
    addRow(label3, new JScrollPane(textField3), p, c);

    add(p, BorderLayout.NORTH);
    // add(new JScrollPane());
    setPreferredSize(new Dimension(320, 240));
  }

  private static void addRow(Component c1, Component c2, Container p, GridBagConstraints c) {
    c.gridx = 0;
    c.weightx = 0d;
    c.insets = new Insets(5, 5, 5, 0);
    c.anchor = GridBagConstraints.NORTHEAST;
    p.add(c1, c);

    c.gridx = 1;
    c.weightx = 1d;
    c.insets = new Insets(5, 5, 5, 5);
    c.fill = GridBagConstraints.HORIZONTAL;
    p.add(c2, c);
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
