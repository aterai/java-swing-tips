// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.ChangeListener;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    // UIManager.put("ToolBar.separatorSize", new Dimension(2, 20));
    JToolBar bar = new JToolBar();
    bar.add(new JCheckBox("JCheckBox"));
    bar.addSeparator();
    bar.add(new JRadioButton("JRadioButton"));
    bar.addSeparator(new Dimension(32, 32));
    bar.add(new JButton("JButton"));
    bar.addSeparator(new Dimension(10, 10));
    bar.add(new JToggleButton("JToggleButton"));
    bar.add(Box.createVerticalGlue());

    SpinnerNumberModel mw = new SpinnerNumberModel(10, -10, 50, 1);
    SpinnerNumberModel mh = new SpinnerNumberModel(32, -10, 50, 1);
    ChangeListener cl = e -> {
      Dimension d = new Dimension(mw.getNumber().intValue(), mh.getNumber().intValue());
      for (Component c : bar.getComponents()) {
        if (c instanceof JToolBar.Separator) {
          ((JToolBar.Separator) c).setSeparatorSize(d);
        }
      }
      bar.revalidate();
    };
    mw.addChangeListener(cl);
    mh.addChangeListener(cl);

    JButton button = new JButton("reset");
    button.addActionListener(e -> {
      Component[] list = bar.getComponents();
      bar.removeAll();
      for (Component c : list) {
        if (c instanceof JToolBar.Separator) {
          bar.addSeparator();
        } else {
          bar.add(c);
        }
      }
    });

    JPanel p = new JPanel();
    p.add(new JLabel("width:"));
    p.add(new JSpinner(mw));
    p.add(new JLabel("height:"));
    p.add(new JSpinner(mh));
    p.add(button);

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    add(bar, BorderLayout.NORTH);
    add(p);
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
        ex.printStackTrace();
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
