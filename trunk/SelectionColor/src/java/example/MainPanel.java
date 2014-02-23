package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.IOException;
import java.net.URL;
import java.util.regex.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import javax.swing.border.Border;
import javax.swing.text.*;
import javax.swing.text.html.*;

public final class MainPanel extends JPanel {
    private static final String PATTERN = "[Ff]rame";
    private static final Color SELECTION_COLOR = new Color(0xC86464FF,true);
    private final transient Highlighter.HighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(new Color(255,255,50,100));
    private final JEditorPane area = new JEditorPane();
    //private final JTextArea area = new JTextArea();

    private final JCheckBox check = new JCheckBox(new AbstractAction("setSelectionColor(#C86464FF)") {
        @Override public void actionPerformed(ActionEvent e) {
            JCheckBox c = (JCheckBox)e.getSource();
            //http://docs.oracle.com/javase/7/docs/api/javax/swing/text/JTextComponent.html#setSelectionColor(java.awt.Color)
            //DOUBT?: Setting the color to null is the same as setting Color.white.
            area.setSelectionColor(c.isSelected() ? SELECTION_COLOR : null);
        }
    });

    public MainPanel() {
        super(new BorderLayout());
        check.setSelected(true);

        //http://terai.xrea.jp/Swing/StyleSheet.html
        StyleSheet styleSheet = new StyleSheet();
        styleSheet.addRule(".highlight {color: blue; background: #FF5533; opacity: 0.5;}"); //INCOMPLETE: opacity
        //INCOMPLETE: styleSheet.addRule(".highlight {background: rgba(255,100,100,0.6); opacity: 0.5;}");
        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
        htmlEditorKit.setStyleSheet(styleSheet);
        area.setEditorKit(htmlEditorKit);
        area.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        area.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        area.setOpaque(false);
        area.setForeground(new Color(200,200,200));
        area.setSelectedTextColor(Color.WHITE);
        area.setBackground(new Color(0, true)); //Nimbus
        area.setSelectionColor(SELECTION_COLOR);
        area.setText("<html><pre>"
          + "public static void createAndShowGUI() {<br />"
          + "    <span class='highlight'>JFrame</span> frame = new JFrame();<br />"
          + "    frame.setDefaultCloseOperation(EXIT_ON_CLOSE);<br />"
          + "    frame.getContentPane().add(new MainPanel());<br />"
          + "    frame.pack();<br />"
          + "    frame.setLocationRelativeTo(null);<br />"
          + "    frame.setVisible(true);<br />"
          + "}<br />"
        );

        //TEST: http://terai.xrea.jp/Swing/DrawsLayeredHighlights.html
        //DefaultHighlighter dh = (DefaultHighlighter)area.getHighlighter();
        //dh.setDrawsLayeredHighlights(false);

        URL url = getClass().getResource("tokeidai.jpg");
        BufferedImage bi = getFilteredImage(url);
        JScrollPane scroll = new JScrollPane(area);
        scroll.getViewport().setOpaque(false);
        scroll.setViewportBorder(new CentredBackgroundBorder(bi));
        scroll.getVerticalScrollBar().setUnitIncrement(25);
        add(scroll);

        Box box = Box.createHorizontalBox();
        box.add(check);
        box.add(Box.createHorizontalGlue());
        box.add(new JToggleButton(new AbstractAction("highlight") {
            @Override public void actionPerformed(ActionEvent e) {
                JToggleButton t = (JToggleButton)e.getSource();
                if(t.isSelected()) {
                    setHighlight(area, PATTERN);
                }else{
                    area.getHighlighter().removeAllHighlights();
                }
            }
        }));
        box.add(Box.createHorizontalStrut(2));
        add(box, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }

    private BufferedImage getFilteredImage(URL url) {
        BufferedImage image;
        try{
            image = ImageIO.read(url);
        }catch(IOException ioe) {
            ioe.printStackTrace();
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        }
        BufferedImage dest = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        byte[] b = new byte[256];
        for(int i=0;i<256;i++) {
            b[i] = (byte)(i*0.2f);
        }
        BufferedImageOp op = new LookupOp(new ByteLookupTable(0, b), null);
        op.filter(image, dest);
        return dest;
    }

    //http://terai.xrea.jp/Swing/Highlighter.html
    private void setHighlight(JTextComponent jtc, String pattern) {
        Highlighter highlighter = jtc.getHighlighter();
        highlighter.removeAllHighlights();
        Document doc = jtc.getDocument();
        try{
            String text = doc.getText(0, doc.getLength());
            Matcher matcher = Pattern.compile(pattern).matcher(text);
            int pos = 0;
            while(matcher.find(pos)) {
                int start = matcher.start();
                int end   = matcher.end();
                highlighter.addHighlight(start, end, highlightPainter);
                pos = end;
            }
        }catch(BadLocationException e) {
            e.printStackTrace();
        }
        jtc.repaint();
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
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

// https://forums.oracle.com/thread/1395763 How can I use TextArea with Background Picture ?
// http://terai.xrea.jp/Swing/CentredBackgroundBorder.html
class CentredBackgroundBorder implements Border {
    private final Insets insets = new Insets(0, 0, 0, 0);
    private final BufferedImage image;
    public CentredBackgroundBorder(BufferedImage image) {
        this.image = image;
    }
    @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        int cx = x + (width-image.getWidth())/2;
        int cy = y + (height-image.getHeight())/2;
        Graphics2D g2 = (Graphics2D)g.create();
        g2.drawRenderedImage(image, AffineTransform.getTranslateInstance(cx, cy));
        g2.dispose();
    }
    @Override public Insets getBorderInsets(Component c) {
        return insets;
    }
    @Override public boolean isBorderOpaque() {
        return true;
    }
}
