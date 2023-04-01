// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsSpinnerUI;
import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.plaf.basic.BasicSpinnerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Box box = Box.createVerticalBox();

    JSpinner spinner1 = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1)) {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new MySpinnerUI());
      }
    };
    box.add(makeTitledPanel("BasicSpinnerUI", spinner1));

    JSpinner spinner2 = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1)) {
      @Override public void updateUI() {
        super.updateUI();
        searchSpinnerButtons(this);
      }
    };
    box.add(makeTitledPanel("getName()", spinner2));

    JSpinner spinner3 = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1)) {
      @Override public void updateUI() {
        super.updateUI();
        if (getUI() instanceof WindowsSpinnerUI) {
          setUI(new MyWinSpinnerUI());
        } else {
          searchSpinnerButtons(this);
        }
      }
    };
    box.add(makeTitledPanel("WindowsSpinnerUI", spinner3));

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    add(box, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static void searchSpinnerButtons(Container comp) {
    for (Component c : comp.getComponents()) {
      // System.out.println(c.getName());
      if (Objects.equals("Spinner.nextButton", c.getName())) {
        ((JComponent) c).setToolTipText("getName: next next");
      } else if (Objects.equals("Spinner.previousButton", c.getName())) {
        ((JComponent) c).setToolTipText("getName: prev prev");
      } else if (c instanceof Container) {
        searchSpinnerButtons((Container) c);
      }
    }
  }

  private static final class MySpinnerUI extends BasicSpinnerUI {
    @Override protected Component createNextButton() {
      JComponent nextButton = (JComponent) super.createNextButton();
      nextButton.setToolTipText("SpinnerUI: next next");
      // nextButton.setBackground(Color.GREEN);
      return nextButton;
    }

    @Override protected Component createPreviousButton() {
      JComponent previousButton = (JComponent) super.createPreviousButton();
      previousButton.setToolTipText("SpinnerUI: prev prev");
      // previousButton.setBackground(Color.RED);
      return previousButton;
    }
  }

  private static final class MyWinSpinnerUI extends WindowsSpinnerUI {
    @Override protected Component createNextButton() {
      JComponent nextButton = (JComponent) super.createNextButton();
      nextButton.setToolTipText("WindowsSpinnerUI: next next");
      return nextButton;
    }

    @Override protected Component createPreviousButton() {
      JComponent previousButton = (JComponent) super.createPreviousButton();
      previousButton.setToolTipText("WindowsSpinnerUI: prev prev");
      return previousButton;
    }
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
