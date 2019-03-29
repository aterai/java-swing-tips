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
  private static final String MYSITE = "https://ateraimemo.com/";
  private final JTextArea textArea = new JTextArea();

  private MainPanel() {
    super(new BorderLayout());
    JEditorPane editor = new JEditorPane("text/html", String.format("<html><a href='%s'>%s</a>", MYSITE, MYSITE));
    editor.setOpaque(false);
    editor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    editor.setEditable(false);
    editor.addHyperlinkListener(e -> {
      if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED && Desktop.isDesktopSupported()) {
        try {
          Desktop.getDesktop().browse(new URI(MYSITE));
        } catch (IOException | URISyntaxException ex) {
          ex.printStackTrace();
          textArea.setText(ex.getMessage());
        }
        textArea.setText(e.toString());
      }
    });
    JPanel p = new JPanel();
    p.add(editor);
    p.setBorder(BorderFactory.createTitledBorder("Desktop.getDesktop().browse(URI)"));
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(textArea));
    setPreferredSize(new Dimension(320, 240));
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
