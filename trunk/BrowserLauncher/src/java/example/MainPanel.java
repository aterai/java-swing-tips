package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;
import javax.swing.*;
import javax.swing.event.*;

public class MainPanel extends JPanel {
    private static String MYSITE = "http://terai.xrea.jp/";
    private final JTextArea textArea = new JTextArea();
    public MainPanel() {
        super(new BorderLayout());
        JEditorPane editor = new JEditorPane("text/html", "<html><a href='"+MYSITE+"'>"+MYSITE+"</a>");
        editor.setOpaque(false);
        editor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        editor.setEditable(false);
        editor.addHyperlinkListener(new HyperlinkListener() {
            @Override public void hyperlinkUpdate(HyperlinkEvent e) {
                if(e.getEventType()==HyperlinkEvent.EventType.ACTIVATED) {
                    BrowserLauncher.openURL(MYSITE);
                    textArea.setText(e.toString());
                }
            }
        });
        JPanel p = new JPanel();
        p.add(editor);
        p.setBorder(BorderFactory.createTitledBorder("BrowserLauncher.openURL(...)"));
        add(p, BorderLayout.NORTH);
        add(new JScrollPane(textArea));
        setPreferredSize(new Dimension(320, 200));
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e) {
            e.printStackTrace();
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
//     String url = "http://www.centerkey.com/";       //
//     BareBonesBrowserLaunch.openURL(url);            //
//  Public Domain Software -- Free to Use as You Like  //
/////////////////////////////////////////////////////////
//class BareBonesBrowserLaunch {
class BrowserLauncher {
    private static final String errMsg = "Error attempting to launch web browser";
    public static void openURL(String url) {
        String osName = System.getProperty("os.name");
        try{
            if(osName.startsWith("Mac OS")) {
                Class fileMgr = Class.forName("com.apple.eio.FileManager");
                //Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] {String.class});
                @SuppressWarnings("unchecked") Method openURL = fileMgr.getDeclaredMethod("openURL", String.class);
                openURL.invoke(null, new Object[] {url});
            }else if(osName.startsWith("Windows")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            }else{ //assume Unix or Linux
                String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
                String browser = null;
                for(int count = 0; count < browsers.length && browser == null; count++) {
                    if(Runtime.getRuntime().exec(new String[] {"which", browsers[count]}).waitFor() == 0) {
                        browser = browsers[count];
                    }
                }
                if(browser == null) {
                    throw new Exception("Could not find web browser");
                }else{
                    Runtime.getRuntime().exec(new String[] {browser, url});
                }
            }
        }catch(Exception e) {
            java.awt.Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null, errMsg + ":\n" + e.getLocalizedMessage(), "titlebar", JOptionPane.ERROR_MESSAGE);
        }
    }
}
