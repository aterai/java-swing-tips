package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.text.html.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());

        StyleSheet styleSheet = new StyleSheet();
        //styleSheet.addRule("body {font-size: 24pt; font-family: IPAexGothic;}");
        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
        htmlEditorKit.setStyleSheet(styleSheet);
        JEditorPane editor1 = new JEditorPane();
        editor1.setEditorKit(htmlEditorKit);

        final URL url = getClass().getResource("SurrogatePair.html");
        try{
            editor1.read(new InputStreamReader(url.openStream(), "UTF-8"), "html");
        }catch(Exception ex) {
            editor1.setText("<html><p>(&#xD85B;&#xDE40;) (&#x26E40;)<br />(&#xD842;&#xDF9F;) (&#x00020B9F;)</p></html>");
        }

        JEditorPane editor2 = new JEditorPane();
        //editor2.setFont(new Font("IPAexGothic", Font.PLAIN, 24));
        editor2.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        editor2.setText("(\uD85B\uDE40) (\u26E40)\n(\uD842\uDF9F) (\u20B9F)");

        JPanel p = new JPanel(new GridLayout(0,1));
        p.add(makeTitledPane(editor1, "Numeric character reference"));
        p.add(makeTitledPane(editor2, "Unicode escapes"));

        add(new JButton(new AbstractAction("browse: SurrogatePair.html") {
            @Override public void actionPerformed(ActionEvent e) {
                javax.jnlp.BasicService bs = null;
                try{
                    bs = (javax.jnlp.BasicService)javax.jnlp.ServiceManager.lookup("javax.jnlp.BasicService");
                }catch(Throwable t) {
                    bs = null;
                }
                if(bs!=null) {
                    bs.showDocument(url);
                }else if(Desktop.isDesktopSupported()) {
                    try{
                        File tmp = File.createTempFile("_tmp", ".html");
                        tmp.deleteOnExit();
                        InputStream in   = new BufferedInputStream(url.openStream());
                        OutputStream out = new BufferedOutputStream(new FileOutputStream(tmp));
                        byte buf[] = new byte[256];
                        int len;
                        while((len = in.read(buf)) != -1) {
                            out.write(buf, 0, len);
                        }
                        out.flush();
                        out.close();
                        in.close();

                        Desktop.getDesktop().browse(tmp.toURI());
                    }catch(Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }), BorderLayout.SOUTH);
        add(p);
        setPreferredSize(new Dimension(320, 240));
    }
    public JComponent makeTitledPane(JComponent c, String title) {
        JScrollPane scroll = new JScrollPane(c);
        scroll.setBorder(BorderFactory.createTitledBorder(title));
        return scroll;
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
