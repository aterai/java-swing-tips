// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new FlowLayout(FlowLayout.LEADING, 50, 50));
    JTextArea editor = makeEditor();
    JButton b1 = new JButton("JPopupMenu");
    b1.addActionListener(e -> startEditing1(editor, (JButton) e.getSource()));
    add(b1);
    JButton b2 = new JButton("JFrame#setUndecorated(true)");
    b2.addActionListener(e -> startEditing2(editor, (JButton) e.getSource()));
    add(b2);
    JButton b3 = new JButton("JWindow()");
    b3.addActionListener(e -> startEditing3(editor, (JButton) e.getSource()));
    add(b3);
    JButton b4 = new JButton("JWindow(owner)");
    b4.addActionListener(e -> startEditing4(editor, (JButton) e.getSource()));
    add(b4);
    EventQueue.invokeLater(() -> initEditorListener(editor));
    setPreferredSize(new Dimension(320, 240));
  }

  private void startEditing1(JTextArea editor, JButton b) {
    resetEditor(editor, b);
    JPopupMenu popup = new JPopupMenu();
    popup.setBorder(BorderFactory.createEmptyBorder());
    popup.add(editor);
    popup.pack();
    Point p = b.getLocation();
    p.y += b.getHeight();
    popup.show(this, p.x, p.y);
    editor.requestFocusInWindow();
  }

  private static void startEditing2(JTextArea editor, JButton b) {
    resetEditor(editor, b);
    JFrame window = new JFrame();
    window.setUndecorated(true);
    window.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
    // window.setAlwaysOnTop(true);
    window.add(editor);
    window.pack();
    Point p = b.getLocation();
    p.y += b.getHeight();
    SwingUtilities.convertPointToScreen(p, b.getParent());
    window.setLocation(p);
    window.setVisible(true);
    editor.requestFocusInWindow();
  }

  private static void startEditing3(JTextArea editor, JButton b) {
    resetEditor(editor, b);
    Window window = new JWindow();
    window.setFocusableWindowState(true);
    window.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
    // window.setAlwaysOnTop(true);
    window.add(editor);
    window.pack();
    Point p = b.getLocation();
    p.y += b.getHeight();
    SwingUtilities.convertPointToScreen(p, b.getParent());
    window.setLocation(p);
    window.setVisible(true);
    editor.requestFocusInWindow();
  }

  private static void startEditing4(JTextArea editor, JButton b) {
    resetEditor(editor, b);
    Point p = b.getLocation();
    p.y += b.getHeight();
    SwingUtilities.convertPointToScreen(p, b.getParent());
    Window window = new JWindow(SwingUtilities.getWindowAncestor(b));
    window.setFocusableWindowState(true);
    window.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
    // window.setAlwaysOnTop(true);
    window.add(editor);
    window.pack();
    window.setLocation(p);
    window.setVisible(true);
    editor.requestFocusInWindow();
  }

  private void initEditorListener(JTextArea editor) {
    Window window = SwingUtilities.getWindowAncestor(this);
    window.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        resetEditor(editor, null);
      }
    });
    window.addComponentListener(new ComponentAdapter() {
      @Override public void componentResized(ComponentEvent e) {
        resetEditor(editor, null);
      }

      @Override public void componentMoved(ComponentEvent e) {
        resetEditor(editor, null);
      }
    });
  }

  public static JTextArea makeEditor() {
    JTextArea editor = new JTextArea();
    editor.setFont(UIManager.getFont("TextField.font"));
    editor.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    editor.setLineWrap(true);
    editor.setComponentPopupMenu(new TextComponentPopupMenu());
    editor.getDocument().addDocumentListener(new EditorResizeHandler(editor));
    return editor;
  }

  public static void resetEditor(JTextArea editor, JButton b) {
    Window window = SwingUtilities.getWindowAncestor(editor);
    if (window != null) {
      window.dispose();
    }
    if (b != null) {
      editor.setText(b.getText());
      Dimension d = editor.getPreferredSize();
      editor.setBounds(0, 0, b.getWidth(), d.height);
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

class EditorResizeHandler implements DocumentListener {
  private final JTextArea editor;
  private int prev = -1;

  protected EditorResizeHandler(JTextArea editor) {
    this.editor = editor;
  }

  private void update() {
    int h = editor.getPreferredSize().height;
    if (prev != h) {
      Rectangle rect = editor.getBounds();
      rect.height = h;
      editor.setBounds(rect);
      Container p = SwingUtilities.getAncestorOfClass(JPopupMenu.class, editor);
      if (p instanceof JPopupMenu) {
        ((JPopupMenu) p).pack();
        editor.requestFocusInWindow();
      } else {
        Window w = SwingUtilities.getWindowAncestor(editor);
        if (w != null) {
          w.pack();
        }
      }
    }
    prev = h;
  }

  @Override public void insertUpdate(DocumentEvent e) {
    EventQueue.invokeLater(this::update);
  }

  @Override public void removeUpdate(DocumentEvent e) {
    EventQueue.invokeLater(this::update);
  }

  @Override public void changedUpdate(DocumentEvent e) {
    EventQueue.invokeLater(this::update);
  }
}

final class TextComponentPopupMenu extends JPopupMenu {
  /* default */ TextComponentPopupMenu() {
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
