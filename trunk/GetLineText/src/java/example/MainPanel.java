package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

public class MainPanel extends JPanel {
    private final JTextArea textArea = new JTextArea();
    private final JScrollPane scroll = new JScrollPane(textArea);

    public MainPanel(final JFrame frame) {
        super(new BorderLayout());
        StringBuilder sb = new StringBuilder();
        String dummy   = "aaaaaaaaaaaaa\n";
        String comment = "#comment\n";
        for(int i=0;i<200;i++) {
            sb.append(dummy);
            if(i%16==0) { sb.append(comment); }
        }
        textArea.setText(sb.toString());
        scroll.setRowHeaderView(new LineNumberView(textArea));
        textArea.setBorder(BorderFactory.createEmptyBorder(0,2,0,0));

        JButton button = new JButton(new AbstractAction("count commented lines: startsWith(\"#\")") {
            @Override public void actionPerformed(ActionEvent e) {
                EventQueue.invokeLater(new Runnable() {
                    @Override public void run() {
                        int count = 0;
                        StringTokenizer st = new StringTokenizer(textArea.getText(), "\n") ;
                        while(st.hasMoreTokens()) {
                            if(st.nextToken().startsWith("#")) {
                                count++;
                            }
                        }

//                         //String#split >>>>
//                         for(String line: textArea.getText().split("\\n")) {
//                             if(line.startsWith("#")) {
//                                 count++;
//                             }
//                         }
//                         //<<<< String#split

//                         //LineNumberReader >>>>
//                         try(LineNumberReader lnr = new LineNumberReader(new StringReader(textArea.getText()))) {
//                             String line = null;
//                             while((line = lnr.readLine()) != null) {
//                                 if(line.startsWith("#")) {
//                                     count++;
//                                 }
//                             }
//                         }catch(IOException ioe) {
//                             ioe.printStackTrace();
//                         }
//                         //<<<< LineNumberReader

//                         //ElementCount >>>>
//                         Document doc = textArea.getDocument();
//                         Element root = doc.getDefaultRootElement();
//                         try{
//                             for(int i=0;i<root.getElementCount();i++) {
//                                 Element elem = root.getElement(i);
//                                 String line = doc.getText(elem.getStartOffset(), elem.getEndOffset()-elem.getStartOffset());
//                                 if(line.startsWith("#")) {
//                                     count++;
//                                 }
//                             }
//                         }catch(BadLocationException ble) {
//                             ble.printStackTrace();
//                         }
//                         //<<<< ElementCount
                        JOptionPane.showMessageDialog(scroll, "commented lines: "+count, "title", JOptionPane.INFORMATION_MESSAGE);
                    }
                });
            }
        });
        frame.getRootPane().setDefaultButton(button);

        add(button, BorderLayout.NORTH);
        add(scroll);
        setPreferredSize(new Dimension(320, 240));
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
        frame.getContentPane().add(new MainPanel(frame));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class LineNumberView extends JComponent {
    private static final int MARGIN = 5;
    private final JTextArea textArea;
    private final FontMetrics fontMetrics;
    //private final int topInset;
    private final int fontAscent;
    private final int fontHeight;
    private final int fontDescent;
    private final int fontLeading;

    public LineNumberView(JTextArea textArea) {
        this.textArea = textArea;
        Font font   = textArea.getFont();
        fontMetrics = getFontMetrics(font);
        fontHeight  = fontMetrics.getHeight();
        fontAscent  = fontMetrics.getAscent();
        fontDescent = fontMetrics.getDescent();
        fontLeading = fontMetrics.getLeading();
        //topInset    = textArea.getInsets().top;

        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) {
                repaint();
            }
            @Override public void removeUpdate(DocumentEvent e) {
                repaint();
            }
            @Override public void changedUpdate(DocumentEvent e) {}
        });
        textArea.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                revalidate();
                repaint();
            }
        });
        Insets i = textArea.getInsets();
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY),
            BorderFactory.createEmptyBorder(i.top, MARGIN, i.bottom, MARGIN - 1)));
        setOpaque(true);
        setBackground(Color.WHITE);
        setFont(font);
    }
    private int getComponentWidth() {
        Document doc  = textArea.getDocument();
        Element root  = doc.getDefaultRootElement();
        int lineCount = root.getElementIndex(doc.getLength());
        int maxDigits = Math.max(3, String.valueOf(lineCount).length());
        Insets i = getBorder().getBorderInsets(this);
        return maxDigits * fontMetrics.stringWidth("0") + i.left + i.right;
        //return 48;
    }
    private int getLineAtPoint(int y) {
        Element root = textArea.getDocument().getDefaultRootElement();
        int pos = textArea.viewToModel(new Point(0, y));
        return root.getElementIndex(pos);
    }
    @Override public Dimension getPreferredSize() {
        return new Dimension(getComponentWidth(), textArea.getHeight());
    }
    @Override public void paintComponent(Graphics g) {
        g.setColor(getBackground());
        Rectangle clip = g.getClipBounds();
        g.fillRect(clip.x, clip.y, clip.width, clip.height);

        g.setColor(getForeground());
        int base  = clip.y;
        int start = getLineAtPoint(base);
        int end   = getLineAtPoint(base + clip.height);
        int y     = start * fontHeight;
        int rmg   = getBorder().getBorderInsets(this).right;
        for(int i=start;i<=end;i++) {
            String text = String.valueOf(i + 1);
            int x = getComponentWidth() - rmg - fontMetrics.stringWidth(text);
            y += fontAscent;
            g.drawString(text, x, y);
            y += fontDescent + fontLeading;
        }
    }
}
