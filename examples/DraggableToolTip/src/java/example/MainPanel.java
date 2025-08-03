// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

public final class MainPanel extends JPanel {
  private static final String HTML_TEXT = String.join("\n",
      "<html><body>",
      "<a href='https://ateraimemo.com/Swing.html' title='Title: JST'>Java Swing Tips</a>",
      "</body></html>"
  );

  private MainPanel() {
    super(new BorderLayout());
    JEditorPane hint = new JEditorPane();
    hint.setEditorKit(new HTMLEditorKit());
    hint.setEditable(false);
    hint.setOpaque(false);
    JPanel tipPanel = new JPanel(new BorderLayout());
    tipPanel.add(hint);
    tipPanel.add(new DragLabel(), BorderLayout.WEST);

    JPopupMenu popup = new JPopupMenu();
    popup.setLightWeightPopupEnabled(false);
    popup.add(new JScrollPane(tipPanel));
    popup.setBorder(BorderFactory.createEmptyBorder());

    JEditorPane editor = new ToolTipEditorPane(tipPanel);
    editor.setEditorKit(new HTMLEditorKit());
    editor.setText(HTML_TEXT);
    editor.setEditable(false);
    editor.addHyperlinkListener(e -> linkEvent(e, hint));

    add(new JScrollPane(editor));
    setPreferredSize(new Dimension(320, 240));
  }

  private static void linkEvent(HyperlinkEvent e, JEditorPane hint) {
    JEditorPane editorPane = (JEditorPane) e.getSource();
    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      String message = "You click the link with the URL " + e.getURL();
      JOptionPane.showMessageDialog(editorPane, message);
    } else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
      editorPane.setToolTipText("");
      Optional.ofNullable(e.getSourceElement())
          .map(elem -> (AttributeSet) elem.getAttributes().getAttribute(HTML.Tag.A))
          .map(attr -> {
            String title = Objects.toString(attr.getAttribute(HTML.Attribute.TITLE));
            String url = Objects.toString(e.getURL());
            String link = String.format("<a href='%s'>%s</a>", url, url);
            return String.format("<html>%s: %s<br/>...<br/>...", title, link);
          })
          .ifPresent(txt -> {
            hint.setText(txt);
            Window popup = SwingUtilities.getWindowAncestor(hint);
            if (popup != null) {
              popup.pack();
            }
          });
    } else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
      editorPane.setToolTipText(null);
    }
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

class ToolTipEditorPane extends JEditorPane {
  private final JPanel panel;

  protected ToolTipEditorPane(JPanel panel) {
    super();
    this.panel = panel;
  }

  @Override public JToolTip createToolTip() {
    JToolTip tip = super.createToolTip();
    tip.addHierarchyListener(e -> {
      boolean showing = e.getComponent().isShowing();
      if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && showing) {
        panel.setBackground(tip.getBackground());
        Container p = SwingUtilities.getAncestorOfClass(JPopupMenu.class, panel);
        if (p instanceof JPopupMenu) {
          ((JPopupMenu) p).show(tip, 0, 0);
        }
      }
    });
    return tip;
  }
}

class DragLabel extends JLabel {
  private transient MouseAdapter handler;

  @Override public void updateUI() {
    removeMouseListener(handler);
    removeMouseMotionListener(handler);
    super.updateUI();
    handler = new PopupDragListener();
    addMouseListener(handler);
    addMouseMotionListener(handler);
    Color bgc = UIManager.getColor("ToolTip.background");
    if (bgc != null) {
      setBackground(bgc.darker());
    }
    setOpaque(true);
  }

  @Override public Dimension getPreferredSize() {
    return new Dimension(16, 64);
  }
}

class PopupDragListener extends MouseAdapter {
  private final Point startPt = new Point();

  @Override public void mousePressed(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      Component c = e.getComponent();
      Container popup = SwingUtilities.getAncestorOfClass(JPopupMenu.class, c);
      SwingUtilities.convertMouseEvent(c, e, popup);
      startPt.setLocation(e.getPoint());
    }
  }

  @Override public void mouseDragged(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      Component c = e.getComponent();
      Window w = SwingUtilities.getWindowAncestor(c);
      // Popup$HeavyWeightWindow
      if (w != null && w.getType() == Window.Type.POPUP) {
        Point pt = e.getLocationOnScreen();
        w.setLocation(pt.x - startPt.x, pt.y - startPt.y);
      }
    }
  }
}
