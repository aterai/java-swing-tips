// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextPane textPane = new JTextPane();
    textPane.setText("Shift+Tab");

    JComboBox<Object> combo = new JComboBox<>(new String[] {
        "public", "protected", "private",
        "final", "transient", "super", "this", "return", "class"
    });
    BasicComboPopup popup = makeComboPopup(combo, textPane);
    ActionMap am = popup.getActionMap();
    am.put("loopUp", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        int i = combo.getSelectedIndex();
        int size = combo.getItemCount();
        combo.setSelectedIndex((i - 1 + size) % size);
      }
    });
    am.put("loopDown", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        int i = combo.getSelectedIndex();
        int size = combo.getItemCount();
        combo.setSelectedIndex((i + 1) % size);
      }
    });
    am.put("insert", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        int i = combo.getSelectedIndex();
        Optional.ofNullable(combo.getItemAt(i)).ifPresent(o -> {
          popup.hide();
          TextEditorUtils.append(textPane, Objects.toString(o, ""));
        });
      }
    });

    InputMap im = popup.getInputMap();
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "loopUp");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "loopDown");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "insert");

    textPane.getActionMap().put("popupInsert", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        try {
          // Java 9: rect = textPane.modelToView2D(pos).getBounds();
          Rectangle rect = textPane.modelToView(textPane.getCaretPosition());
          popup.show(textPane, rect.x, (int) rect.getMaxY());
          EventQueue.invokeLater(() -> {
            Optional.ofNullable(popup.getTopLevelAncestor())
                .filter(Window.class::isInstance).map(Window.class::cast)
                .ifPresent(Window::toFront);
            popup.requestFocusInWindow();
          });
        } catch (BadLocationException ex) { // should never happen
          RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
          wrap.initCause(ex);
          throw wrap;
        }
      }
    });
    // KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK);
    textPane.getInputMap().put(KeyStroke.getKeyStroke("shift TAB"), "popupInsert");

    add(new JScrollPane(textPane));
    setPreferredSize(new Dimension(320, 240));
  }

  private static BasicComboPopup makeComboPopup(JComboBox<Object> combo, JTextPane textPane) {
    return new BasicComboPopup(combo) {
      private transient MouseListener listener;

      @Override protected void installListListeners() {
        super.installListListeners();
        listener = new MouseAdapter() {
          @Override public void mouseClicked(MouseEvent e) {
            hide();
            TextEditorUtils.append(textPane, Objects.toString(comboBox.getSelectedItem()));
          }
        };
        if (Objects.nonNull(list)) {
          list.addMouseListener(listener);
        }
      }

      @Override public void uninstallingUI() {
        if (Objects.nonNull(listener)) {
          list.removeMouseListener(listener);
          // listener = null;
        }
        super.uninstallingUI();
      }

      @Override public boolean isFocusable() {
        return true;
      }
    };
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

// class EditorComboPopup extends BasicComboPopup {
//   protected final JTextComponent textArea;
//   private transient MouseListener listener;
//
//   // Java 9:
//   // protected EditorComboPopup(JTextComponent textArea, JComboBox<Object> cb) {
//   protected EditorComboPopup(JTextComponent textArea, JComboBox<?> cb) {
//     super(cb);
//     this.textArea = textArea;
//   }
//
//   @Override protected void installListListeners() {
//     super.installListListeners();
//     listener = new MouseAdapter() {
//       @Override public void mouseClicked(MouseEvent e) {
//         hide();
//         TextEditorUtils.append(textArea, Objects.toString(comboBox.getSelectedItem()));
//       }
//     };
//     if (Objects.nonNull(list)) {
//       list.addMouseListener(listener);
//     }
//   }
//
//   @Override public void uninstallingUI() {
//     if (Objects.nonNull(listener)) {
//       list.removeMouseListener(listener);
//       listener = null;
//     }
//     super.uninstallingUI();
//   }
//
//   @Override public boolean isFocusable() {
//     return true;
//   }
// }

final class TextEditorUtils {
  private TextEditorUtils() {
    /* Singleton */
  }

  public static void append(JTextComponent editor, String str) {
    try {
      Document doc = editor.getDocument();
      doc.insertString(editor.getCaretPosition(), str, null);
    } catch (BadLocationException ex) {
      // should never happen
      RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
      wrap.initCause(ex);
      throw wrap;
    }
  }
}
