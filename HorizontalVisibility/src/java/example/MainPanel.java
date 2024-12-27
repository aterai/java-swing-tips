// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;

public final class MainPanel extends JPanel {
  private final JTextField field1 = new JTextField();
  private final JTextField field2 = new JTextField();
  private final JScrollBar scrollbar1 = new JScrollBar(Adjustable.HORIZONTAL);
  private final JScrollBar scrollbar2 = new JScrollBar(Adjustable.HORIZONTAL) {
    @Override public void updateUI() {
      super.updateUI();
      setUI(new ArrowButtonlessScrollBarUI());
    }

    @Override public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      d.height = 10;
      return d;
    }
  };
  private final transient EmptyThumbHandler handler = new EmptyThumbHandler(field1, scrollbar1);

  private MainPanel() {
    super(new BorderLayout());
    String js = makeOneLineCode();
    field1.setText(js);
    field2.setText(js);
    scrollbar1.setModel(field1.getHorizontalVisibility());
    scrollbar2.setModel(field2.getHorizontalVisibility());
    JCheckBox check = new JCheckBox("add EmptyThumbHandler");
    check.addActionListener(e -> {
      if (((JCheckBox) e.getSource()).isSelected()) {
        field1.addComponentListener(handler);
        field1.getDocument().addDocumentListener(handler);
      } else {
        field1.removeComponentListener(handler);
        field1.getDocument().removeDocumentListener(handler);
      }
    });

    Box p = Box.createVerticalBox();
    JScrollPane scroll = new JScrollPane(new JTextField(js));
    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    p.add(new JLabel("JScrollPane + VERTICAL_SCROLLBAR_NEVER"));
    p.add(scroll);
    p.add(Box.createVerticalStrut(5));
    p.add(new JLabel("BoundedRangeModel: textField.getHorizontalVisibility()"));
    p.add(field1);
    p.add(Box.createVerticalStrut(2));
    p.add(scrollbar1);
    p.add(Box.createVerticalStrut(2));
    p.add(check);
    p.add(Box.createVerticalStrut(5));
    p.add(new JLabel("BoundedRangeModel+textField.ArrowButtonlessScrollBarUI"));
    p.add(field2);
    p.add(Box.createVerticalStrut(2));
    p.add(scrollbar2);
    p.add(Box.createVerticalStrut(5));
    add(p, BorderLayout.NORTH);
    add(makeButtons(), BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(20, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private Box makeButtons() {
    JButton caretButton = new JButton("setCaretPosition: 0");
    caretButton.addActionListener(e -> {
      field1.requestFocusInWindow();
      field1.setCaretPosition(0);
      scrollbar1.revalidate();
      field2.requestFocusInWindow();
      field2.setCaretPosition(0);
      scrollbar2.revalidate();
    });

    JButton offsetButton = new JButton("setScrollOffset: 0");
    offsetButton.addActionListener(e -> {
      field1.setScrollOffset(0);
      scrollbar1.revalidate();
      field2.setScrollOffset(0);
      scrollbar2.revalidate();
    });
    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(caretButton);
    box.add(Box.createHorizontalStrut(5));
    box.add(offsetButton);
    return box;
  }

  private static String makeOneLineCode() {
    String loc = "var l=location,m=l.href.match('^(https?://)(.+)(api[^+]+|technotes[^+]+)');";
    String code = "if(m)l.href=m[1]+'docs.oracle.com/javase/8/docs/'+decodeURIComponent(m[3])";
    String replace = ".replace(/\\+.*$/,'').replace(/\\[\\]/g,':A').replace(/, |\\(|\\)/g,'-');";
    return String.format("javascript:(function(){%s%s%s}());", loc, code, replace);
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

class EmptyThumbHandler extends ComponentAdapter implements DocumentListener {
  private final BoundedRangeModel emptyThumbModel = new DefaultBoundedRangeModel(0, 1, 0, 1);
  private final JTextField textField;
  private final JScrollBar scrollbar;

  protected EmptyThumbHandler(JTextField textField, JScrollBar scrollbar) {
    super();
    this.textField = textField;
    this.scrollbar = scrollbar;
  }

  private void changeThumbModel() {
    EventQueue.invokeLater(() -> {
      BoundedRangeModel m = textField.getHorizontalVisibility();
      int iv = m.getMaximum() - m.getMinimum() - m.getExtent() - 1; // -1: bug?
      if (iv <= 0) {
        scrollbar.setModel(emptyThumbModel);
      } else {
        scrollbar.setModel(textField.getHorizontalVisibility());
      }
    });
  }

  @Override public void componentResized(ComponentEvent e) {
    changeThumbModel();
  }

  @Override public void insertUpdate(DocumentEvent e) {
    changeThumbModel();
  }

  @Override public void removeUpdate(DocumentEvent e) {
    changeThumbModel();
  }

  @Override public void changedUpdate(DocumentEvent e) {
    changeThumbModel();
  }
}

class ZeroSizeButton extends JButton {
  private static final Dimension ZERO_SIZE = new Dimension();

  @Override public Dimension getPreferredSize() {
    return ZERO_SIZE;
  }
}

class ArrowButtonlessScrollBarUI extends BasicScrollBarUI {
  private static final Color DEFAULT_COLOR = new Color(220, 100, 100, 100);
  private static final Color DRAGGING_COLOR = new Color(200, 100, 100, 100);
  private static final Color ROLLOVER_COLOR = new Color(255, 120, 100, 100);

  @Override protected JButton createDecreaseButton(int orientation) {
    return new ZeroSizeButton();
  }

  @Override protected JButton createIncreaseButton(int orientation) {
    return new ZeroSizeButton();
  }

  @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
    // Graphics2D g2 = (Graphics2D) g.create();
    // g2.setPaint(new Color(100, 100, 100, 100));
    // g2.fillRect(r.x, r.y, r.width - 1, r.height - 1);
    // g2.dispose();
  }

  @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
    JScrollBar sb = (JScrollBar) c;
    if (!sb.isEnabled()) {
      return;
    }
    BoundedRangeModel m = sb.getModel();
    int iv = m.getMaximum() - m.getMinimum() - m.getExtent() - 1; // -1: bug?
    if (iv > 0) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      Color color;
      if (isDragging) {
        color = DRAGGING_COLOR;
      } else if (isThumbRollover()) {
        color = ROLLOVER_COLOR;
      } else {
        color = DEFAULT_COLOR;
      }
      g2.setPaint(color);
      g2.fillRect(r.x, r.y, r.width - 1, r.height - 1);
      g2.dispose();
    }
  }
}
