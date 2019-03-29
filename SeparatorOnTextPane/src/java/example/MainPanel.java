// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    HTMLEditorKit kit = new HTMLEditorKit();
    HTMLDocument doc = new HTMLDocument();
    JTextPane textPane = new JTextPane();
    textPane.setEditorKit(kit);
    textPane.setDocument(doc);
    textPane.setEditable(false);
    textPane.setText("<html>&lt;hr&gt;:<hr />");
    // insertBr(kit, doc);

    textPane.insertComponent(new JLabel("JSeparator: "));
    textPane.insertComponent(new JSeparator(SwingConstants.HORIZONTAL));
    insertBr(kit, doc);

    textPane.insertComponent(new JLabel("MatteBorder1: "));
    textPane.insertComponent(new JLabel() {
      @Override public void updateUI() {
        super.updateUI();
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.RED));
      }

      @Override public Dimension getMaximumSize() {
        return new Dimension(textPane.getSize().width, 1);
      }
    });
    insertBr(kit, doc);

    textPane.insertComponent(new JLabel("MatteBorder2: "));
    textPane.insertComponent(new JLabel() {
      @Override public void updateUI() {
        super.updateUI();
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GREEN));
      }

      @Override public Dimension getPreferredSize() {
        return new Dimension(textPane.getSize().width, 1);
      }

      @Override public Dimension getMaximumSize() {
        return this.getPreferredSize();
      }
    });
    insertBr(kit, doc);

    textPane.insertComponent(new JLabel("SwingConstants.VERTICAL "));
    textPane.insertComponent(new JSeparator(SwingConstants.VERTICAL) {
      // @Override public void updateUI() {
      //   super.updateUI();
      //   setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.ORANGE));
      // }

      @Override public Dimension getPreferredSize() {
        return new Dimension(1, 16);
      }

      @Override public Dimension getMaximumSize() {
        return this.getPreferredSize();
      }
    });
    textPane.insertComponent(new JLabel(" TEST"));
    add(new JScrollPane(textPane));
    setPreferredSize(new Dimension(320, 240));
  }

  private static void insertBr(HTMLEditorKit kit, HTMLDocument doc) {
    try {
      kit.insertHTML(doc, doc.getLength(), "<br />", 0, 0, null);
    } catch (BadLocationException ex) {
      // should never happen
      RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
      wrap.initCause(ex);
      throw wrap;
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
