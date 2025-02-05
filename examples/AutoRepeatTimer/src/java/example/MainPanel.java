// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigInteger;
import java.util.Objects;
import javax.swing.*;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridBagLayout());
    JTextField field = new JTextField("0") {
      @Override public Dimension getMaximumSize() {
        return getPreferredSize();
      }

      @Override public Dimension getPreferredSize() {
        return new Dimension(100, 50);
      }
    };
    field.setHorizontalAlignment(SwingConstants.CENTER);
    field.setFont(field.getFont().deriveFont(30f));
    field.setEditable(false);

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(makeButton(-5, field));
    box.add(makeButton(-1, field));
    box.add(field);
    box.add(makeButton(+1, field));
    box.add(makeButton(+5, field));
    box.add(Box.createHorizontalGlue());

    add(box);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JButton makeButton(int extent, JTextField view) {
    String title = String.format("%+d", extent);
    JButton button = new JButton(title) {
      @Override public Dimension getMaximumSize() {
        return new Dimension(50, 50);
      }
    };
    AutoRepeatHandler handler = new AutoRepeatHandler(extent, view);
    button.addActionListener(handler);
    button.addMouseListener(handler);
    return button;
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

class AutoRepeatHandler extends MouseAdapter implements ActionListener {
  private final Timer autoRepeatTimer;
  private final BigInteger extent;
  private final JTextComponent view;
  private JButton arrowButton;

  protected AutoRepeatHandler(int extent, JTextComponent view) {
    super();
    this.extent = BigInteger.valueOf(extent);
    this.view = view;
    autoRepeatTimer = new Timer(60, this);
    autoRepeatTimer.setInitialDelay(300);
  }

  @Override public void actionPerformed(ActionEvent e) {
    Object o = e.getSource();
    if (o instanceof Timer) {
      boolean released = Objects.nonNull(arrowButton) && !arrowButton.getModel().isPressed();
      if (released && autoRepeatTimer.isRunning()) {
        autoRepeatTimer.stop();
        // arrowButton = null;
      }
    } else if (o instanceof JButton) {
      arrowButton = (JButton) o;
    }
    BigInteger i = new BigInteger(view.getText());
    view.setText(i.add(extent).toString());
  }

  @Override public void mousePressed(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e) && e.getComponent().isEnabled()) {
      autoRepeatTimer.start();
    }
  }

  @Override public void mouseReleased(MouseEvent e) {
    autoRepeatTimer.stop();
    // arrowButton = null;
  }

  @Override public void mouseExited(MouseEvent e) {
    if (autoRepeatTimer.isRunning()) {
      autoRepeatTimer.stop();
    }
  }
}
