// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.DefaultCaret;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(8, 4, 8, 4);
    c.anchor = GridBagConstraints.WEST;
    JPanel p = new JPanel(new GridBagLayout());
    c.gridy = 0;
    p.add(new JLabel("Default: "), c);
    c.gridy = 1;
    p.add(new JLabel("setEnabled: "), c);
    c.gridy = 2;
    p.add(new JLabel("setEditable: "), c);
    c.gridy = 3;
    p.add(new JLabel("setVisible: "), c);
    c.gridy = 4;
    p.add(new JLabel("setBlinkRate: "), c);

    c.gridx = 1;
    c.weightx = 1.0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridy = 0;
    p.add(makeTextField0(), c);
    c.gridy = 1;
    p.add(makeTextField1(), c);
    c.gridy = 2;
    p.add(makeTextField2(), c);
    c.gridy = 3;
    p.add(makeTextField3(), c);
    c.gridy = 4;
    p.add(makeTextField4(), c);

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    add(p, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTextField makeTextField0() {
    return new JTextField("Default JTextField") {
      @Override public void updateUI() {
        super.updateUI();
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());
        setBackground(new Color(0x0, true)); // Nimbus?
      }
    };
  }

  private static JTextField makeTextField1() {
    return new JTextField("setEnabled(false)") {
      @Override public void updateUI() {
        super.updateUI();
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());
        setBackground(new Color(0x0, true)); // Nimbus?
        setEnabled(false);
      }
    };
  }

  private static JTextField makeTextField2() {
    return new JTextField("setEditable(false)") {
      @Override public void updateUI() {
        super.updateUI();
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());
        setBackground(new Color(0x0, true)); // Nimbus?
        setEditable(false);
      }
    };
  }

  private static JTextField makeTextField3() {
    return new JTextField("DefaultCaret#setVisible(true)") {
      @Override public void updateUI() {
        super.updateUI();
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());
        setBackground(new Color(0x0, true)); // Nimbus?
        setEditable(false);
        setCaret(new DefaultCaret() {
          @Override public void focusGained(FocusEvent e) {
            super.focusGained(e);
            if (getComponent().isEnabled()) {
              setVisible(true);
            }
          }
        });
      }
    };
  }

  private static JTextField makeTextField4() {
    return new JTextField("DefaultCaret#setBlinkRate(...)") {
      @Override public void updateUI() {
        super.updateUI();
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());
        setBackground(new Color(0x0, true)); // Nimbus?
        setEditable(false);
        setCaret(new DefaultCaret() {
          @Override public void focusGained(FocusEvent e) {
            super.focusGained(e);
            if (getComponent().isEnabled()) {
              setBlinkRate(UIManager.getInt("TextField.caretBlinkRate"));
              setVisible(true);
            }
          }
        });
      }
    };
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

// @see SwingSet3/src/com/sun/swingset3/SwingSet3.java
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
