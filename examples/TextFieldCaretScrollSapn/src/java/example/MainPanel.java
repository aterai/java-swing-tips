// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Collections;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel(new GridLayout(0, 1, 10, 10));
    p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    String txt = String.join(" ", Collections.nCopies(10, "1234567890"));
    JTextField field0 = new JTextField(32);
    field0.setText(txt);
    p.add(makeTitledPanel("LookAndFeel Default", field0));

    JTextField field1 = new JTextField(32) {
      @Override public void updateUI() {
        super.updateUI();
        Caret caret = new DefaultCaret();
        caret.setBlinkRate(UIManager.getInt("TextField.caretBlinkRate"));
        setCaret(caret);
      }
    };
    field1.setText(txt);
    p.add(makeTitledPanel("DefaultCaret", field1));

    JTextField field2 = new JTextField(32) {
      @Override public void updateUI() {
        super.updateUI();
        Caret caret = new HorizontalScrollCaret();
        caret.setBlinkRate(UIManager.getInt("TextField.caretBlinkRate"));
        setCaret(caret);
      }
    };
    field2.setText(txt);
    p.add(makeTitledPanel("override DefaultCaret#adjustVisibility(...)", field2));

    add(p, BorderLayout.NORTH);
    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
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

// @see WindowsTextFieldUI.WindowsFieldCaret
class HorizontalScrollCaret extends DefaultCaret {
  @Override protected void adjustVisibility(Rectangle r) {
    EventQueue.invokeLater(() -> {
      JTextComponent c = getComponent();
      if (c instanceof JTextField) {
        horizontalScroll((JTextField) c, r);
      }
    });
  }

  private void horizontalScroll(JTextField field, Rectangle r) {
    TextUI ui = field.getUI();
    int dot = getDot();
    Position.Bias bias = Position.Bias.Forward;
    Rectangle startRect = null;
    try {
      startRect = ui.modelToView(field, dot, bias);
    } catch (BadLocationException ble) {
      UIManager.getLookAndFeel().provideErrorFeedback(field);
    }
    Insets i = field.getInsets();
    BoundedRangeModel vis = field.getHorizontalVisibility();
    int x = r.x + vis.getValue() - i.left;
    int n = 8;
    int span = vis.getExtent() / n;
    if (r.x < i.left) {
      vis.setValue(x - span);
    } else if (r.x + r.width > i.left + vis.getExtent()) {
      vis.setValue(x - (n - 1) * span);
    }
    if (startRect != null) {
      try {
        Rectangle endRect = ui.modelToView(field, dot, bias);
        if (endRect != null && !endRect.equals(startRect)) {
          damage(endRect);
        }
      } catch (BadLocationException ble) {
        UIManager.getLookAndFeel().provideErrorFeedback(field);
      }
    }
  }
}

final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = makeButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton makeButton(UIManager.LookAndFeelInfo info) {
    boolean selected = info.getClassName().equals(lookAndFeel);
    return new JRadioButtonMenuItem(info.getName(), selected);
  }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        Logger.getGlobal().severe(ex::getMessage);
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
