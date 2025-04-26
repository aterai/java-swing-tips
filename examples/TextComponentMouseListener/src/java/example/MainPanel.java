// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextComponent textArea = makeTextArea();
    MouseAdapter handler = new TextComponentMouseHandler(textArea);
    textArea.addMouseListener(handler);
    textArea.addMouseMotionListener(handler);

    Component c1 = makeTitledPanel("Default", makeTextArea());
    Component c2 = makeTitledPanel("MouseListener", textArea);
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, c1, c2);
    split.setResizeWeight(.5);
    add(split);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTextComponent makeTextArea() {
    String txt = "AAA BBB CCC\nAAa bbb cCC aa1111111111111111 bb2 cc3\naa bb cc\n11 22 33";
    JTextArea textArea = new JTextArea(txt);
    textArea.setLineWrap(true);
    textArea.setComponentPopupMenu(new TextFieldPopupMenu());
    return textArea;
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.add(new JLabel(title), BorderLayout.NORTH);
    p.add(new JScrollPane(c));
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

class TextComponentMouseHandler extends MouseAdapter {
  private final AtomicInteger count = new AtomicInteger(0);
  private final Timer holdTimer = new Timer(1000, null);
  private final List<String> list = Arrays.asList(
      DefaultEditorKit.selectWordAction,
      DefaultEditorKit.selectLineAction,
      DefaultEditorKit.selectParagraphAction,
      DefaultEditorKit.selectAllAction);

  protected TextComponentMouseHandler(JTextComponent textArea) {
    super();
    holdTimer.setInitialDelay(500);
    holdTimer.addActionListener(e -> {
      Timer timer = (Timer) e.getSource();
      if (timer.isRunning()) {
        int i = count.getAndIncrement();
        if (i < list.size()) {
          String cmd = list.get(i);
          textArea.getActionMap().get(cmd).actionPerformed(e);
        } else {
          timer.stop();
          count.set(0);
        }
      }
    });
  }

  @Override public void mousePressed(MouseEvent e) {
    boolean isSingleClick = e.getClickCount() == 1;
    if (isSingleClick) {
      JTextComponent textArea = (JTextComponent) e.getComponent();
      if (!textArea.hasFocus()) {
        textArea.requestFocusInWindow();
      }
      if (textArea.getSelectedText() == null) {
        // Java 9: int pos = textArea.viewToModel2D(e.getPoint());
        int pos = textArea.viewToModel(e.getPoint());
        textArea.setCaretPosition(pos);
      }
      if (SwingUtilities.isLeftMouseButton(e)) {
        holdTimer.start();
      }
    }
  }

  @Override public void mouseReleased(MouseEvent e) {
    holdTimer.stop();
    count.set(0);
  }

  @Override public void mouseDragged(MouseEvent e) {
    mouseReleased(e);
  }
}

final class TextFieldPopupMenu extends JPopupMenu {
  /* default */ TextFieldPopupMenu() {
    super();
    add(new DefaultEditorKit.CutAction());
    add(new DefaultEditorKit.CopyAction());
    add(new DefaultEditorKit.PasteAction());
    add("delete").addActionListener(e -> {
      Component c = getInvoker();
      if (c instanceof JTextComponent) {
        ((JTextComponent) c).replaceSelection(null);
      }
    });
    addSeparator();
    add("cut2").addActionListener(e -> {
      Component c = getInvoker();
      if (c instanceof JTextComponent) {
        ((JTextComponent) c).cut();
      }
    });
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTextComponent) {
      JTextComponent tc = (JTextComponent) c;
      boolean hasSelectedText = Objects.nonNull(tc.getSelectedText());
      for (MenuElement menuElement : getSubElements()) {
        Component m = menuElement.getComponent();
        Action a = m instanceof JMenuItem ? ((JMenuItem) m).getAction() : null;
        if (a instanceof DefaultEditorKit.PasteAction) {
          continue;
        }
        m.setEnabled(hasSelectedText);
      }
      super.show(c, x, y);
    }
  }
}
