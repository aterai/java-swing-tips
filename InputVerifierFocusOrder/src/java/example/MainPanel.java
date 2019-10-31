// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.text.JTextComponent;

public class MainPanel extends JPanel {
  protected static final int MAX_LEN = 6;
  protected final JCheckBox check = new JCheckBox("use FocusTraversalPolicy", true);
  protected final JButton button = new JButton("Next");

  public MainPanel() {
    super(new BorderLayout());
    setFocusTraversalPolicy(new LayoutFocusTraversalPolicy() {
      @Override public Component getComponentAfter(Container focusCycleRoot, Component cmp) {
        System.out.println("getComponentAfter");
        button.setEnabled(isAllValid());
        return super.getComponentAfter(focusCycleRoot, cmp);
      }

      @Override public Component getComponentBefore(Container focusCycleRoot, Component cmp) {
        System.out.println("getComponentBefore");
        button.setEnabled(isAllValid());
        return super.getComponentBefore(focusCycleRoot, cmp);
      }
    });
    setFocusCycleRoot(true);

    button.setEnabled(false);
    check.addActionListener(e -> setFocusCycleRoot(((JCheckBox) e.getSource()).isSelected()));

    Box box = Box.createVerticalBox();
    box.add(check);
    box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    Arrays.asList(makeTextField(), makeTextField()).forEach(c -> {
      box.add(Box.createVerticalStrut(10));
      box.add(c);
    });

    JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    p.add(button);
    add(box, BorderLayout.NORTH);
    add(p, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  protected final boolean isAllValid() {
    return Arrays.stream(getComponents())
        .filter(JTextField.class::isInstance)
        .map(JTextField.class::cast)
        .allMatch(t -> t.getInputVerifier().verify(t));
  }

  protected final JTextField makeTextField() {
    JTextField textField = new JTextField(24);
    textField.setInputVerifier(new InputVerifier() {
      @Override public boolean verify(JComponent c) {
        if (c instanceof JTextComponent) {
          JTextComponent tc = (JTextComponent) c;
          String str = tc.getText().trim();
          return !str.isEmpty() && MAX_LEN - str.length() >= 0;
        }
        return false;
      }

      @Override public boolean shouldYieldFocus(JComponent input) {
        System.out.println("shouldYieldFocus");
        button.setEnabled(isAllValid());
        return super.shouldYieldFocus(input);
      }
    });
    textField.addFocusListener(new FocusAdapter() {
      @Override public void focusLost(FocusEvent e) {
        if (e.isTemporary()) {
          return;
        }
        System.out.println("focusLost");
        button.setEnabled(isAllValid());
      }
    });
    // // TEST:
    // textField.getDocument().addDocumentListener(new DocumentListener() {
    //   @Override public void insertUpdate(DocumentEvent e) {
    //     System.out.println("insertUpdate");
    //   }
    //   @Override public void removeUpdate(DocumentEvent e) {
    //     System.out.println("removeUpdate");
    //   }
    //   @Override public void changedUpdate(DocumentEvent e) {
    //     /* not needed */
    //   }
    // });
    return textField;
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
