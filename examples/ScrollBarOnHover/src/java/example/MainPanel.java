// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    String text = String.join("\n", Collections.nCopies(100, "12345"));

    JTextArea textArea = new JTextArea("Mouse cursor flickers over the JScrollBar.\n" + text);
    textArea.addMouseListener(new MouseAdapter() {
      @Override public void mouseEntered(MouseEvent e) {
        Container c = SwingUtilities.getAncestorOfClass(JScrollPane.class, e.getComponent());
        if (c instanceof JScrollPane) {
          JScrollPane scroll = (JScrollPane) c;
          scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        }
      }

      @Override public void mouseExited(MouseEvent e) {
        Container c = SwingUtilities.getAncestorOfClass(JScrollPane.class, e.getComponent());
        if (c instanceof JScrollPane) {
          JScrollPane scroll = (JScrollPane) c;
          scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        }
      }
    });
    add(makeTitledPanel("MouseListener", makeScrollPane(textArea)));

    JScrollPane scroll = makeScrollPane(new JTextArea(text));
    add(makeTitledPanel("JLayer", new JLayer<>(scroll, new ScrollBarOnHoverLayerUI())));

    setPreferredSize(new Dimension(320, 240));
  }

  private static JScrollPane makeScrollPane(JComponent c) {
    JScrollPane scroll = new JScrollPane(c);
    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    return scroll;
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

class ScrollBarOnHoverLayerUI extends LayerUI<JScrollPane> {
  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
    super.uninstallUI(c);
  }

  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
    int id = e.getID();
    if (id == MouseEvent.MOUSE_ENTERED) {
      l.getView().setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    } else if (id == MouseEvent.MOUSE_EXITED) {
      l.getView().setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    }
    // super.processMouseEvent(e, l);
  }
}
