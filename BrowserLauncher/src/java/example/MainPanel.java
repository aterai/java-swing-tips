// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;

public final class MainPanel extends JPanel {
  private static final String MYSITE = "https://ateraimemo.com/";

  private MainPanel() {
    super(new BorderLayout());
    JTextArea textArea = new JTextArea();

    JEditorPane editor = new JEditorPane("text/html", String.format("<html><a href='%s'>%s</a>", MYSITE, MYSITE));
    editor.setOpaque(false);
    editor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    editor.setEditable(false);
    editor.addHyperlinkListener(e -> {
      if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        BrowserLauncher.openUrl(MYSITE);
        textArea.setText(e.toString());
      }
    });
    JPanel p = new JPanel();
    p.add(editor);
    p.setBorder(BorderFactory.createTitledBorder("BrowserLauncher.openUrl(...)"));
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

/////////////////////////////////////////////////////////
//  Bare Bones Browser Launch                          //
//  Version 1.5                                        //
//  December 10, 2005                                  //
//  Supports: Mac OS X, GNU/Linux, Unix, Windows XP    //
//  Example Usage:                                     //
//   String url = "http://www.centerkey.com/";         //
//   BareBonesBrowserLaunch.openUrl(url);              //
//  Public Domain Software -- Free to Use as You Like  //
/////////////////////////////////////////////////////////
// class BareBonesBrowserLaunch {
@SuppressWarnings("PMD.ClassNamingConventions")
final class BrowserLauncher {
  private static final String ERR_MSG = "Error attempting to launch web browser";

  private BrowserLauncher() { /* Singleton */ }

  public static void openUrl(String url) {
    String osName = System.getProperty("os.name");
    try {
      if (osName.startsWith("Mac OS")) {
        Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
        // Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] {String.class});
        @SuppressWarnings("unchecked")
        Method openUrl = fileMgr.getDeclaredMethod("openURL", String.class);
        openUrl.invoke(null, new Object[] {url});
      } else if (osName.startsWith("Windows")) {
        Runtime.getRuntime().exec("rundll32 url.dll, FileProtocolHandler " + url);
      } else { // assume Unix or Linux
        String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
        String browser = null;
        for (int count = 0; count < browsers.length && Objects.isNull(browser); count++) {
          String[] cmd = {"which", browsers[count]};
          if (Runtime.getRuntime().exec(cmd).waitFor() == 0) {
            browser = browsers[count];
          }
        }
        if (Objects.nonNull(browser)) {
          Runtime.getRuntime().exec(new String[] {browser, url});
        } else {
          throw new UnsupportedOperationException("Could not find web browser");
        }
      }
    } catch (IOException | InterruptedException | ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
      Toolkit.getDefaultToolkit().beep();
      String msg = ERR_MSG + ":\n" + ex.getLocalizedMessage();
      JOptionPane.showMessageDialog(null, msg, "titlebar", JOptionPane.ERROR_MESSAGE);
    }
  }
}
