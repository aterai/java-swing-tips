package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

public class MainPanel extends JPanel {
    private final JTextField textField = new JTextField("10");
    private final JTextArea textArea   = new JTextArea();
    private final JScrollPane scroll   = new JScrollPane(textArea);

    public MainPanel(JFrame frame) {
        super(new BorderLayout());
        StringBuilder sb = new StringBuilder();
        String dummyStr = "aaaaaaaaaaaaa\n";
        for(int i=0;i<2000;i++) {
            sb.append(dummyStr);
        }
        textArea.setText(sb.toString());

        scroll.setRowHeaderView(new LineNumberView(textArea));
        textArea.setBorder(BorderFactory.createEmptyBorder(0,2,0,0));
        textArea.setEditable(false);

        JButton button = new JButton(new AbstractAction("Goto Line") {
            @Override public void actionPerformed(ActionEvent e) {
                Document doc = textArea.getDocument();
                Element root = doc.getDefaultRootElement();
                int ln = getDestLineNumber(textField, root);
                if(ln<0) { Toolkit.getDefaultToolkit().beep(); return; }
                try{
                    final Element elem = root.getElement(ln-1);
                    final Rectangle dest = textArea.modelToView(elem.getStartOffset());
                    final Rectangle current = scroll.getViewport().getViewRect();
                    new Timer(20, new ActionListener() {
                        @Override public void actionPerformed(ActionEvent ae) {
                            Timer animator = (Timer)ae.getSource();
                            if(dest.y < current.y && animator.isRunning()) {
                                int d = Math.max(1, (current.y-dest.y)/2);
                                current.y = current.y - d;
                                textArea.scrollRectToVisible(current);
                            }else if(dest.y > current.y && animator.isRunning()) {
                                int d = Math.max(1, (dest.y-current.y)/2);
                                current.y = current.y + d;
                                textArea.scrollRectToVisible(current);
                            }else{
                                textArea.setCaretPosition(elem.getStartOffset());
                                animator.stop();
                            }
                        }
                    }).start();
                }catch(BadLocationException ble) {
                    Toolkit.getDefaultToolkit().beep();
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
    private static int getDestLineNumber(JTextField textField, Element root) {
        int lineNumber = 1;
        try{
            lineNumber = Integer.parseInt(textField.getText().trim());
            lineNumber = Math.max(1, Math.min(root.getElementCount(), lineNumber));
        }catch(NumberFormatException nfe) {
            //nfe.printStackTrace();
            lineNumber = -1;
        }
        return lineNumber;
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
