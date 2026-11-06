// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    List<String> options = Arrays.asList(
        "-Duser.country=JP",
        "-Duser.language=ja",
        "-Dfile.encoding=UTF-8",
        "--add-opens=java.desktop/com.sun.java.swing.plaf.windows=ALL-UNNAMED",
        "--add-opens=java.desktop/javax.swing.plaf.basic=ALL-UNNAMED",
        "--add-opens=java.desktop/sun.awt.shell=ALL-UNNAMED");
    ExpandableTextField expandableField = new ExpandableTextField();
    expandableField.setText(String.join(" ", options));
    JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 20));
    p.add(new JLabel("VM Options:"));
    p.add(expandableField);
    add(p, BorderLayout.NORTH);
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

class OverlayLayoutPanel extends JPanel {
  @Override public void updateUI() {
    super.updateUI();
    setLayout(new OverlayLayout(this));
  }

  @Override public boolean isOptimizedDrawingEnabled() {
    return false;
  }
}

class ExpandableTextField extends OverlayLayoutPanel {
  private final JTextField textField = new JTextField(20);
  private final JTextArea textArea = new JTextArea();
  private final JPopupMenu popup = new ExpandPopupMenu();

  protected ExpandableTextField() {
    super();
    ExpandArrowIcon icon = new ExpandArrowIcon();
    JButton expandButton = makeExpandButton(icon);
    expandButton.addActionListener(e -> popup.show(textField, 0, 0));
    add(expandButton);

    textField.setMargin(new Insets(0, 0, 0, icon.getIconWidth()));
    textField.setAlignmentX(RIGHT_ALIGNMENT);
    textField.setAlignmentY(CENTER_ALIGNMENT);
    add(textField);
  }

  @Override public final Component add(Component comp) {
    return super.add(comp);
  }

  private final class ExpandPopupMenu extends JPopupMenu {
    private transient PopupMenuListener listener;

    @Override public void updateUI() {
      removePopupMenuListener(listener);
      super.updateUI();
      removeAll(); // Avoid leaving the previous components when the UI is updated again
      JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
      footer.add(makeResizeLabel(this));
      setLayout(new BorderLayout());
      add(new JScrollPane(textArea));
      add(footer, BorderLayout.SOUTH);
      listener = new ExpandPopupMenuListener();
      addPopupMenuListener(listener);
    }

    @Override public Dimension getPreferredSize() {
      // Keep the popup at least as wide as the text field, even for short text
      Dimension d = super.getPreferredSize();
      d.width = Math.max(d.width, textField.getWidth());
      return d;
    }
  }

  private final class ExpandPopupMenuListener implements PopupMenuListener {
    @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
      textArea.setText(textField.getText().replace(" ", "\n") + "\n");
      // The popup is not showing yet, so request the focus after it is displayed
      EventQueue.invokeLater(textArea::requestFocusInWindow);
    }

    @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
      textField.setText(textArea.getText().replace("\n", " ").trim());
    }

    @Override public void popupMenuCanceled(PopupMenuEvent e) {
      // not needed
    }
  }

  private static JLabel makeResizeLabel(JPopupMenu popup) {
    JLabel resizeLabel = new JLabel("◢");
    resizeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
    MouseAdapter resizer = new PopupMenuResizeHandler(popup);
    resizeLabel.addMouseListener(resizer);
    resizeLabel.addMouseMotionListener(resizer);
    return resizeLabel;
  }

  private static JButton makeExpandButton(ExpandArrowIcon icon) {
    JButton expandButton = new JButton(icon);
    expandButton.setMargin(new Insets(0, 0, 0, 0));
    expandButton.setContentAreaFilled(false);
    expandButton.setFocusable(false);
    expandButton.setBorderPainted(false);
    expandButton.setAlignmentX(RIGHT_ALIGNMENT);
    expandButton.setAlignmentY(CENTER_ALIGNMENT);
    return expandButton;
  }

  public void setText(String text) {
    textField.setText(text);
  }
}

class PopupMenuResizeHandler extends MouseAdapter {
  private static final int MIN_HEIGHT = 100;
  private final Point dragStartPoint = new Point();
  private final Dimension dragStartSize = new Dimension();
  private final JPopupMenu popup;

  protected PopupMenuResizeHandler(JPopupMenu popup) {
    super();
    this.popup = popup;
  }

  @Override public void mousePressed(MouseEvent e) {
    dragStartPoint.setLocation(e.getLocationOnScreen());
    dragStartSize.setSize(popup.getSize());
  }

  @Override public void mouseDragged(MouseEvent e) {
    Point pt = e.getLocationOnScreen();
    int minWidth = popup.getInvoker().getWidth();
    int width = Math.max(minWidth, dragStartSize.width + pt.x - dragStartPoint.x);
    int height = Math.max(MIN_HEIGHT, dragStartSize.height + pt.y - dragStartPoint.y);
    Dimension size = new Dimension(width, height);
    popup.setPreferredSize(size);
    Window window = SwingUtilities.getWindowAncestor(popup);
    if (window != null && window.getType() == Window.Type.POPUP) {
      // Popup$HeavyWeightWindow
      window.setSize(size);
    } else {
      // Popup$LightWeightWindow
      popup.pack();
    }
  }
}

class ExpandArrowIcon implements Icon {
  private static final String ARROW = "⇵"; // "↕";

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(c.getForeground());
    int cx = x + getIconWidth() / 2;
    int cy = y + getIconHeight() / 2;
    g2.rotate(Math.toRadians(45), cx, cy);
    g2.setFont(c.getFont().deriveFont(14f));
    FontMetrics fm = g2.getFontMetrics();
    int tx = cx - fm.stringWidth(ARROW) / 2;
    int ty = cy + (fm.getAscent() - fm.getDescent()) / 2;
    g2.drawString(ARROW, tx, ty);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 18;
  }

  @Override public int getIconHeight() {
    return 18;
  }
}
