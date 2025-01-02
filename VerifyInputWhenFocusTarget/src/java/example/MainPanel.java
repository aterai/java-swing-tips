// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout(5, 5));
    JTextField field0 = new JTextField("9999999999999999");
    JTextField field1 = new JTextField("1111111111111111");
    JTextField field2 = new JTextField("9876543210987654");
    List<JTextField> list = Arrays.asList(field0, field1, field2);
    InputVerifier verifier = new IntegerInputVerifier();
    list.forEach(tf -> {
      tf.setHorizontalAlignment(SwingConstants.RIGHT);
      tf.setInputVerifier(verifier);
    });

    JTextArea log = new JTextArea();
    ActionListener al = e -> {
      KeyboardFocusManager fm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
      log.append(fm.getFocusOwner() + "\n");
      list.forEach(tf -> tf.setText(""));
    };

    add(makeBox(list), BorderLayout.NORTH);
    add(new JScrollPane(log));
    add(makeClearButtonBox(al), BorderLayout.SOUTH);
    EventQueue.invokeLater(field0::requestFocusInWindow);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JPanel makeClearButtonBox(ActionListener al) {
    JButton button0 = new JButton("Default");
    button0.addActionListener(al);
    // button0.setVerifyInputWhenFocusTarget(true);

    JButton button1 = new JButton("setFocusable(false)");
    button1.addActionListener(al);
    button1.setFocusable(false);

    JButton button2 = new JButton("setVerifyInputWhenFocusTarget(false)");
    button2.addActionListener(al);
    button2.setVerifyInputWhenFocusTarget(false);

    JPanel p1 = new JPanel();
    p1.add(button0);
    p1.add(button1);

    JPanel p2 = new JPanel();
    p2.add(button2);

    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder("clear all"));
    p.add(p1, BorderLayout.NORTH);
    p.add(p2, BorderLayout.SOUTH);
    return p;
  }

  private static Box makeBox(List<JTextField> list) {
    JButton button0 = new JButton("setText(0)");
    button0.addActionListener(e -> {
      list.forEach(tf -> tf.setText("0"));
      list.get(0).requestFocusInWindow();
    });

    JButton button1 = new JButton("setText(Integer.MAX_VALUE+1)");
    button1.addActionListener(e -> {
      list.forEach(tf -> tf.setText("2147483648"));
      list.get(0).requestFocusInWindow();
    });

    JPanel p = new JPanel();
    p.add(button0);
    p.add(button1);

    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    box.add(p);
    list.forEach(tf -> {
      box.add(Box.createVerticalStrut(5));
      box.add(tf);
    });
    box.add(Box.createVerticalGlue());
    return box;
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

// Validating Text and Filtering Documents and Accessibility and the Java Access Bridge Tech Tips
// https://web.archive.org/web/20050523001117/http://java.sun.com/developer/JDCTechTips/2005/tt0518.html
// Validating with Input Verifiers
class IntegerInputVerifier extends InputVerifier {
  @Override public boolean verify(JComponent c) {
    boolean verified = false;
    if (c instanceof JTextComponent) {
      JTextComponent textField = (JTextComponent) c;
      try {
        Integer.parseInt(textField.getText());
        verified = true;
      } catch (NumberFormatException ex) {
        // System.err.println("InputVerifier#verify: false");
        UIManager.getLookAndFeel().provideErrorFeedback(c);
      }
    }
    return verified;
  }
}
