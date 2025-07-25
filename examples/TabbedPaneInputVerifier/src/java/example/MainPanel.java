// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    // [JDK-4403182] InputVerifier failed on JTabbedPane & JMenuBar - Java Bug System
    // https://bugs.openjdk.org/browse/JDK-4403182
    // JTabbedPane tabbedPane0 = makeTestTabbedPane();
    // TEST: tabbedPane0.setVerifyInputWhenFocusTarget(true);
    // TEST: tabbedPane0.setFocusCycleRoot(false);
    JTextField field0 = new JTextField("---");
    JTextField field1 = new JTextField("100");
    List<JTextField> list = Arrays.asList(field0, field1);

    EventQueue.invokeLater(field0::requestFocusInWindow);

    list.forEach(tf -> {
      tf.setHorizontalAlignment(SwingConstants.RIGHT);
      tf.setInputVerifier(new IntegerInputVerifier());
    });

    JButton button0 = new JButton("JButton");
    JButton button1 = new JButton("setText(0)");
    button1.addActionListener(e -> list.forEach(tf -> tf.setText("0")));
    button1.setVerifyInputWhenFocusTarget(false);

    JPanel bp = new JPanel();
    bp.add(button0);
    bp.add(button1);

    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    box.add(field0);
    box.add(Box.createVerticalStrut(5));
    box.add(field1);
    box.add(Box.createVerticalStrut(5));
    box.add(bp);
    box.add(Box.createVerticalGlue());

    JPanel p = new JPanel(new BorderLayout());
    p.add(box, BorderLayout.NORTH);
    p.setInputVerifier(new InputVerifier() {
      @Override public boolean verify(JComponent c) {
        return !c.isShowing() || list.stream().allMatch(tf -> tf.getInputVerifier().verify(tf));
      }
    });

    JTabbedPane tabs = makeTabbedPane(p);
    add(makeCheckBox(tabs, p), BorderLayout.NORTH);
    add(tabs);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTabbedPane makeTabbedPane(JPanel p) {
    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab("Integer only", new JScrollPane(p));
    tabs.addTab("JTree", new JScrollPane(new JTree()));
    tabs.addTab("JSplitPane", new JSplitPane());
    tabs.setSelectedIndex(0);
    return tabs;
  }

  private static JCheckBox makeCheckBox(JTabbedPane tabs, JPanel panel) {
    JCheckBox check = new JCheckBox("override SingleSelectionModel#setSelectedIndex(int)");
    SingleSelectionModel ssm = tabs.getModel();
    check.addActionListener(e -> {
      if (((JCheckBox) e.getSource()).isSelected()) {
        tabs.setModel(new InputVerifierSelectionModel(panel));
      } else {
        tabs.setModel(ssm);
      }
    });
    return check;
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

// https://stackoverflow.com/questions/34315657/java-swing-jtextfield-setinputverifier-keep-focus-on-textfield
class InputVerifierSelectionModel extends DefaultSingleSelectionModel {
  private final JPanel panel;

  protected InputVerifierSelectionModel(JPanel panel) {
    super();
    this.panel = panel;
  }

  @Override public void setSelectedIndex(int index) {
    InputVerifier verifier = panel.getInputVerifier();
    // Java 9:
    // if (Objects.nonNull(verifier) && !verifier.shouldYieldFocus(panel, tabs)) {
    if (Objects.nonNull(verifier) && !verifier.shouldYieldFocus(panel)) {
      UIManager.getLookAndFeel().provideErrorFeedback(panel);
      JOptionPane.showMessageDialog(panel, "InputVerifier#verify(...): false");
      return;
    }
    super.setSelectedIndex(index);
  }
}

// Validating Text and Filtering Documents and Accessibility and the Java Access Bridge Tech Tips
// https://web.archive.org/web/20050523001117/http://java.sun.com/developer/JDCTechTips/2005/tt0518.html
// Validating with Input Verifiers
class IntegerInputVerifier extends InputVerifier {
  @Override public boolean verify(JComponent c) {
    boolean verified;
    if (c instanceof JTextComponent) {
      JTextComponent textField = (JTextComponent) c;
      try {
        Integer.parseInt(textField.getText());
        verified = true;
      } catch (NumberFormatException ex) {
        verified = false;
        // System.out.println("InputVerifier#verify: false");
        // UIManager.getLookAndFeel().provideErrorFeedback(c);
      }
    } else {
      verified = false;
    }
    return verified;
  }

  @Override public boolean shouldYieldFocus(JComponent input) {
    boolean verified = verify(input);
    if (!verified) {
      UIManager.getLookAndFeel().provideErrorFeedback(input);
      // JOptionPane.showMessageDialog(input.getParent(), "InputVerifier#verify(...): false");
    }
    return verified;
  }

  // Java 9:
  // @Override public boolean shouldYieldFocus(JComponent source, JComponent target) {
  //   boolean verified = verify(input);
  //   if (!verified) {
  //     UIManager.getLookAndFeel().provideErrorFeedback(input);
  //   }
  //   return verified;
  // }
}
