// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea log = new JTextArea("press and hold the button for 1000 milliseconds\n");
    log.setEditable(false);

    JPopupMenu popupMenu = new JPopupMenu();
    popupMenu.setLayout(new GridLayout(0, 3, 5, 5));
    ButtonGroup bg = new ButtonGroup();
    makeMenuList().stream()
        .map(MainPanel::makeMenuButton)
        .forEach(b -> {
          b.addActionListener(e -> {
            String cmd = e.getActionCommand();
            log.append(String.format("Selected JRadioButton command: %s%n", cmd));
            popupMenu.setVisible(false);
          });
          popupMenu.add(b);
          bg.add(b);
        });

    Icon icon = UIManager.getIcon("FileChooser.detailsViewIcon");
    JButton button = new PressAndHoldButton(icon, popupMenu);
    // button.getAction().putValue(Action.NAME, text);
    button.getAction().putValue(Action.SMALL_ICON, icon);
    button.addActionListener(e -> {
      ButtonModel m = bg.getSelection();
      String cmd = Objects.nonNull(m) ? m.getActionCommand() : "null";
      // Optional.ofNullable(m).map(ButtonModel::getActionCommand).orElse("null");
      log.append(String.format("Selected action command: %s%n", cmd));
    });

    JToolBar toolBar = new JToolBar();
    toolBar.add(button);

    add(toolBar, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));
  }

  private static AbstractButton makeMenuButton(MenuContext m) {
    AbstractButton b = new JRadioButton(m.getCommand());
    b.setActionCommand(m.getCommand());
    b.setForeground(m.getColor());
    b.setBorder(BorderFactory.createEmptyBorder());
    return b;
  }

  private static List<MenuContext> makeMenuList() {
    return Arrays.asList(
        new MenuContext("BLACK", Color.BLACK),
        new MenuContext("BLUE", Color.BLUE),
        new MenuContext("CYAN", Color.CYAN),
        new MenuContext("GREEN", Color.GREEN),
        new MenuContext("MAGENTA", Color.MAGENTA),
        new MenuContext("ORANGE", Color.ORANGE),
        new MenuContext("PINK", Color.PINK),
        new MenuContext("RED", Color.RED),
        new MenuContext("YELLOW", Color.YELLOW));
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

class PressAndHoldButton extends JButton {
  private static final Icon ARROW_ICON = new MenuArrowIcon();
  private PressAndHoldHandler handler;
  private final JPopupMenu popupMenu;

  protected PressAndHoldButton(Icon icon, JPopupMenu popupMenu) {
    super(icon);
    this.popupMenu = popupMenu;
  }

  @Override public void updateUI() {
    removeMouseListener(handler);
    super.updateUI();
    if (popupMenu != null) {
      SwingUtilities.updateComponentTreeUI(popupMenu);
    }
    handler = new PressAndHoldHandler();
    setAction(handler);
    addMouseListener(handler);
    setFocusable(false);
    setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4 + ARROW_ICON.getIconWidth()));
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    // Dimension dim = getSize();
    // Insets ins = getInsets();
    // int x = dim.width - ins.right;
    // int y = ins.top + (dim.height - ins.top - ins.bottom - ARROW_ICON.getIconHeight()) / 2;
    // ARROW_ICON.paintIcon(this, g, x, y);
    Rectangle r = SwingUtilities.calculateInnerArea(this, null);
    int cy = (r.height - ARROW_ICON.getIconHeight()) / 2;
    ARROW_ICON.paintIcon(this, g, r.x + r.width, r.y + cy);
  }

  private class PressAndHoldHandler extends AbstractAction implements MouseListener {
    private final Timer holdTimer = new Timer(1000, e -> {
      Timer timer = (Timer) e.getSource();
      if (popupMenu != null && getModel().isPressed() && timer.isRunning()) {
        timer.stop();
        popupMenu.show(PressAndHoldButton.this, 0, getHeight());
        popupMenu.requestFocusInWindow();
      }
    });

    protected PressAndHoldHandler() {
      super();
      holdTimer.setInitialDelay(1000);
    }

    @Override public void actionPerformed(ActionEvent e) {
      if (holdTimer.isRunning()) {
        holdTimer.stop();
      }
    }

    @Override public void mousePressed(MouseEvent e) {
      Component c = e.getComponent();
      if (SwingUtilities.isLeftMouseButton(e) && c.isEnabled()) {
        holdTimer.start();
      }
    }

    @Override public void mouseReleased(MouseEvent e) {
      holdTimer.stop();
    }

    @Override public void mouseExited(MouseEvent e) {
      if (holdTimer.isRunning()) {
        holdTimer.stop();
      }
    }

    @Override public void mouseEntered(MouseEvent e) {
      /* not needed */
    }

    @Override public void mouseClicked(MouseEvent e) {
      /* not needed */
    }
  }
}

class MenuContext {
  private final String command;
  private final Color color;

  protected MenuContext(String cmd, Color c) {
    command = cmd;
    color = c;
  }

  public String getCommand() {
    return command;
  }

  public Color getColor() {
    return color;
  }
}

class MenuArrowIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(Color.BLACK);
    g2.drawLine(2, 3, 6, 3);
    g2.drawLine(3, 4, 5, 4);
    g2.drawLine(4, 5, 4, 5);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 9;
  }

  @Override public int getIconHeight() {
    return 9;
  }
}
