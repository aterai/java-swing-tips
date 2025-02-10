// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;

public final class MainPanel extends JPanel {
  private static final String SITE = "https://ateraimemo.com/";

  private MainPanel() {
    super(new BorderLayout());
    JTextArea textArea = new JTextArea();
    String html = String.format("<html><a href='%s'>%s</a>", SITE, SITE);
    JEditorPane editor = makeEditorPane(html, textArea);
    JPanel p = new JPanel();
    p.add(editor);
    p.setBorder(BorderFactory.createTitledBorder("Desktop.getDesktop().browse(URI)"));
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(textArea));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JEditorPane makeEditorPane(String html, JTextArea textArea) {
    JEditorPane editor = new JEditorPane("text/html", html);
    editor.setOpaque(false);
    editor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    editor.setEditable(false);
    editor.addHyperlinkListener(e -> {
      boolean activated = e.getEventType() == HyperlinkEvent.EventType.ACTIVATED;
      if (Desktop.isDesktopSupported() && activated) {
        try {
          Desktop.getDesktop().browse(new URI(SITE));
        } catch (IOException | URISyntaxException ex) {
          ex.printStackTrace();
          textArea.setText(ex.getMessage());
        }
        textArea.setText(e.toString());
      }
    });
    return editor;
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
