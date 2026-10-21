// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.basic.BasicArrowButton;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPanel p1 = new JPanel(new BorderLayout(5, 5));
    p1.setBorder(BorderFactory.createTitledBorder("Default JSpinner"));
    p1.add(new JSpinner(new SpinnerNumberModel(50, 0, 100, 1)));

    JPanel p2 = new JPanel(new BorderLayout(5, 5));
    p2.setBorder(BorderFactory.createTitledBorder("EnlargedButtonSpinner"));
    p2.add(new EnlargedButtonSpinner(new SpinnerNumberModel(50, 0, 100, 1)));

    JPanel panel = new JPanel(new BorderLayout(10, 10));
    panel.add(p1, BorderLayout.NORTH);
    panel.add(p2, BorderLayout.SOUTH);
    panel.setBorder(BorderFactory.createEmptyBorder(10, 80, 10, 80));
    add(panel, BorderLayout.NORTH);

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
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

class EnlargedButtonSpinner extends JSpinner {
  private transient MouseAdapter listener;

  protected EnlargedButtonSpinner(SpinnerModel model) {
    super(model);
  }

  @Override public void updateUI() {
    JComponent editor = getEditor();
    if (editor instanceof DefaultEditor) {
      JTextField field = ((DefaultEditor) editor).getTextField();
      field.removeMouseListener(listener);
      super.updateUI();
      // Recreate the listener so that its JPopupMenu follows the new LookAndFeel
      listener = new ArrowButtonEnlargeListener();
      field.addMouseListener(listener);
    } else {
      super.updateUI();
    }
  }
}

class ArrowButtonEnlargeListener extends MouseAdapter {
  private final JPopupMenu popup = new JPopupMenu();

  protected ArrowButtonEnlargeListener() {
    super();
    popup.setLayout(new GridLayout(2, 1));
    popup.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    popup.setFocusable(false);
  }

  @Override public void mousePressed(MouseEvent e) {
    Component c = SwingUtilities.getAncestorOfClass(JSpinner.class, e.getComponent());
    if (SwingUtilities.isLeftMouseButton(e) && c instanceof JSpinner) {
      JSpinner spinner = (JSpinner) c;
      popup.removeAll();
      popup.add(createArrowButton(spinner, true));
      popup.add(createArrowButton(spinner, false));
      popup.pack();
      // Show the popup to the right of the editor, vertically centered on it
      JComponent editor = spinner.getEditor();
      Rectangle r = SwingUtilities.calculateInnerArea(editor, null);
      int px = (int) r.getMaxX();
      int py = (int) r.getCenterY() - popup.getPreferredSize().height / 2;
      popup.show(editor, px, py);
    }
  }

  private static JButton createArrowButton(JSpinner spinner, boolean isNext) {
    int direction = isNext ? SwingConstants.NORTH : SwingConstants.SOUTH;
    JButton arrowButton = new BasicArrowButton(direction) {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width *= 4;
        d.height *= 2;
        return d;
      }
    };
    String name = isNext ? "increment" : "decrement";
    ArrowButtonHandler handler = new ArrowButtonHandler(spinner, name, isNext);
    arrowButton.addActionListener(handler);
    arrowButton.addMouseListener(handler);
    return arrowButton;
  }
}

class ArrowButtonHandler extends AbstractAction implements MouseListener {
  private final Timer repeatTimer;
  private final boolean isNext;
  private final JSpinner spinner;
  private JButton arrowButton;

  protected ArrowButtonHandler(JSpinner spinner, String name, boolean isNext) {
    super(name);
    this.spinner = spinner;
    this.isNext = isNext;
    repeatTimer = new Timer(60, this);
    repeatTimer.setInitialDelay(300);
  }

  @Override public void actionPerformed(ActionEvent e) {
    boolean released = arrowButton != null && !arrowButton.getModel().isPressed();
    if (e.getSource() instanceof Timer && released) {
      // The button was released without a mouseReleased event, stop repeating
      // and do not change the value
      repeatTimer.stop();
      setArrowButton(null);
      return;
    }
    Object value = isNext ? spinner.getNextValue() : spinner.getPreviousValue();
    if (value != null) {
      spinner.setValue(value);
    }
  }

  private void setArrowButton(JButton button) {
    this.arrowButton = button;
  }

  @Override public void mousePressed(MouseEvent e) {
    Component c = e.getComponent();
    if (SwingUtilities.isLeftMouseButton(e) && c.isEnabled() && c instanceof JButton) {
      setArrowButton((JButton) c);
      repeatTimer.start();
    }
  }

  @Override public void mouseReleased(MouseEvent e) {
    repeatTimer.stop();
    setArrowButton(null);
  }

  @Override public void mouseClicked(MouseEvent e) {
    // not needed
  }

  @Override public void mouseEntered(MouseEvent e) {
    // Resume repeating if the mouse comes back while the button is still pressed
    if (arrowButton != null && arrowButton.getModel().isPressed() && !repeatTimer.isRunning()) {
      repeatTimer.start();
    }
  }

  @Override public void mouseExited(MouseEvent e) {
    repeatTimer.stop();
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
      AbstractButton b = createButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton createButton(UIManager.LookAndFeelInfo info) {
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
