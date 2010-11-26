package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

public class MainPanel extends JPanel {
    private final JTextField textField = new JTextField("10");
    private final JTextArea textArea = new JTextArea();
    private final JScrollPane scroll = new JScrollPane(textArea);

    public MainPanel(JFrame frame) {
        super(new BorderLayout());
        String dummyStr = "aaaaaaaaaaaaa\n";
        for(int i=0;i<2000;i++) textArea.append(dummyStr);

        scroll.setRowHeaderView(new LineNumberView(textArea));
        textArea.setBorder(BorderFactory.createEmptyBorder(0,2,0,0));

        JButton button = new JButton(new AbstractAction("Goto Line") {
            @Override public void actionPerformed(ActionEvent e) {
                Document doc = textArea.getDocument();
                Element root = doc.getDefaultRootElement();
                int i = 1;
                try{
                    i = Integer.parseInt(textField.getText().trim());
                    i = Math.max(1, Math.min(root.getElementCount(), i));
                }catch(NumberFormatException nfe) {
                    //nfe.printStackTrace();
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    return;
                }
                try{
                    Element elem = root.getElement(i-1);
                    Rectangle rect = textArea.modelToView(elem.getStartOffset());
                    Rectangle vr = scroll.getViewport().getViewRect();
                    rect.setSize(10, vr.height);
                    textArea.scrollRectToVisible(rect);
                    textArea.setCaretPosition(elem.getStartOffset());
                    //textArea.requestFocus();
                }catch(BadLocationException ble) {
                    java.awt.Toolkit.getDefaultToolkit().beep();
                }
            }
        });
        frame.getRootPane().setDefaultButton(button);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(textField);
        panel.add(button,    BorderLayout.EAST);
        add(panel,  BorderLayout.NORTH);
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
    private final JTextArea text;
    private final FontMetrics fontMetrics;
    private final int topInset;
    private final int fontAscent;
    private final int fontHeight;

    public LineNumberView(JTextArea textArea) {
        text = textArea;
        Font font   = text.getFont();
        fontMetrics = getFontMetrics(font);
        fontHeight  = fontMetrics.getHeight();
        fontAscent  = fontMetrics.getAscent();
        topInset    = text.getInsets().top;
        text.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) {
                repaint();
            }
            @Override public void removeUpdate(DocumentEvent e) {
                repaint();
            }
            @Override public void changedUpdate(DocumentEvent e) {}
        });
        text.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                revalidate();
                repaint();
            }
        });
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
        setOpaque(true);
        setBackground(Color.WHITE);
    }

    private int getComponentWidth() {
        Document doc  = text.getDocument();
        Element root  = doc.getDefaultRootElement();
        int lineCount = root.getElementIndex(doc.getLength());
        int maxDigits =  Math.max(3, String.valueOf(lineCount).length());
        return maxDigits*fontMetrics.stringWidth("0")+MARGIN*2;
    }

    public int getLineAtPoint(int y) {
        Element root = text.getDocument().getDefaultRootElement();
        int pos = text.viewToModel(new Point(0, y));
        return root.getElementIndex(pos);
    }

    public Dimension getPreferredSize() {
        return new Dimension(getComponentWidth(), text.getHeight());
    }

    @Override public void paintComponent(Graphics g) {
        Rectangle clip = g.getClipBounds();
        g.setColor(getBackground());
        g.fillRect(clip.x, clip.y, clip.width, clip.height);
        g.setColor(getForeground());
        int base  = clip.y - topInset;
        int start = getLineAtPoint(base);
        int end   = getLineAtPoint(base+clip.height);
        int y = topInset-fontHeight+fontAscent+start*fontHeight;
        for(int i=start;i<=end;i++) {
            String text = String.valueOf(i+1);
            int x = getComponentWidth()-MARGIN-fontMetrics.stringWidth(text);
            y = y + fontHeight;
            g.drawString(text, x, y);
        }
    }
}
