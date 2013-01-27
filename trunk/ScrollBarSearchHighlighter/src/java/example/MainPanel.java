package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.MetalScrollBarUI;
import com.sun.java.swing.plaf.windows.WindowsScrollBarUI;

public class MainPanel extends JPanel {
    private static final boolean DEBUG = false;
    private static final Highlighter.HighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
    private static final String pattern = "Swing";
    private static final String initTxt =
      "Trail: Creating a GUI with JFC/Swing\n" +
      "Lesson: Learning Swing by Example\n" +
      "This lesson explains the concepts you need to\n" +
      " use Swing components in building a user interface.\n" +
      " First we examine the simplest Swing application you can write.\n" +
      " Then we present several progressively complicated examples of creating\n" +
      " user interfaces using components in the javax.swing package.\n" +
      " We cover several Swing components, such as buttons, labels, and text areas.\n" +
      " The handling of events is also discussed,\n" +
      " as are layout management and accessibility.\n" +
      " This lesson ends with a set of questions and exercises\n" +
      " so you can test yourself on what you've learned.\n" +
      "http://docs.oracle.com/javase/tutorial/uiswing/learn/index.html\n";

    private final JTextArea textArea  = new JTextArea();
    private final JCheckBox check = new JCheckBox(new AbstractAction("LineWrap") {
        @Override public void actionPerformed(ActionEvent e) {
            JCheckBox c = (JCheckBox)e.getSource();
            textArea.setLineWrap(c.isSelected());
        }
    });
    public MainPanel() {
        super(new BorderLayout());
        //textArea.setLineWrap(true);
        textArea.setEditable(false);
        textArea.setText(initTxt+initTxt+initTxt);

        JScrollBar scrollbar = new JScrollBar(JScrollBar.VERTICAL);
        scrollbar.setUnitIncrement(10);

        if(scrollbar.getUI() instanceof WindowsScrollBarUI) {
            scrollbar.setUI(new WindowsScrollBarUI() {
                @Override protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                    super.paintTrack(g, c, trackBounds);

                    Rectangle rect   = textArea.getBounds();
                    Dimension sbSize = scrollbar.getSize();
                    Insets sbInsets  = scrollbar.getInsets();
                    int itemW = sbSize.width - sbInsets.left - sbInsets.right;
                    //boolean squareButtons = DefaultLookup.getBoolean(scrollbar, this, "ScrollBar.squareButtons", false);
                    boolean squareButtons = UIManager.getBoolean("ScrollBar.squareButtons");
                    int decrButtonH = squareButtons ? itemW : decrButton.getPreferredSize().height;
                    int decrButtonY = sbInsets.top;
                    int incrButtonH = squareButtons ? itemW : incrButton.getPreferredSize().height;
                    int incrButtonY = sbSize.height - sbInsets.bottom - incrButtonH;

                    if(DEBUG) {
                        g.setColor(Color.RED);
                        g.fillRect(trackBounds.x, decrButtonY + decrButtonH, trackBounds.width, 2);
                        g.fillRect(trackBounds.x, incrButtonY - 2,           trackBounds.width, 2);

                        System.out.format("t: %d%n", decrButtonY + decrButtonH);
                        System.out.format("b: %d%n", incrButtonY - 2);
                        System.out.println(trackBounds.height - decrButtonH - incrButtonH);
                        System.out.println(incrButtonY - decrButtonH - decrButtonY);
                        System.out.println(rect.height);
                    }

                    double sy = (incrButtonY - decrButtonH - decrButtonY) / rect.getHeight();
                    AffineTransform at = AffineTransform.getScaleInstance(1.0, sy);
                    g.setColor(Color.YELLOW);
                    Document doc = textArea.getDocument();
                    Element root = doc.getDefaultRootElement();
                    try{
                        for(Integer pos: poslist) {
                            int index = root.getElementIndex(pos);
                            Element elem = root.getElement(index);
                            Rectangle r = textArea.modelToView(pos);
                            Rectangle s = at.createTransformedShape(r).getBounds();
                            int h = 2; //Math.max(2, s.height-2);
                            g.fillRect(trackBounds.x, decrButtonH+s.y, trackBounds.width, h);
                        }
                    }catch(BadLocationException e) {
                        e.printStackTrace();
                    }
                }
            });
        }else{
            scrollbar.setUI(new MetalScrollBarUI() {
                @Override protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                    super.paintTrack(g, c, trackBounds);

                    Rectangle rect   = textArea.getBounds();
                    Dimension sbSize = scrollbar.getSize();
                    Insets sbInsets  = scrollbar.getInsets();
                    int itemW = sbSize.width - sbInsets.left - sbInsets.right;
                    //boolean squareButtons = DefaultLookup.getBoolean(scrollbar, this, "ScrollBar.squareButtons", false);
                    boolean squareButtons = UIManager.getBoolean("ScrollBar.squareButtons");
                    int decrButtonH = squareButtons ? itemW : decrButton.getPreferredSize().height;
                    int decrButtonY = sbInsets.top;
                    int incrButtonH = squareButtons ? itemW : incrButton.getPreferredSize().height;
                    int incrButtonY = sbSize.height - sbInsets.bottom - incrButtonH;

                    if(DEBUG) {
                        g.setColor(Color.RED);
                        g.fillRect(trackBounds.x, decrButtonY + decrButtonH, trackBounds.width, 2);
                        g.fillRect(trackBounds.x, incrButtonY - 2,           trackBounds.width, 2);

                        System.out.format("t: %d%n", decrButtonY + decrButtonH);
                        System.out.format("b: %d%n", incrButtonY - 2);
                        System.out.println(trackBounds.height - decrButtonH - incrButtonH);
                        System.out.println(incrButtonY - decrButtonH - decrButtonY);
                        System.out.println(rect.height);
                    }

                    double sy = (incrButtonY - decrButtonH - decrButtonY) / rect.getHeight();
                    AffineTransform at = AffineTransform.getScaleInstance(1.0, sy);
                    g.setColor(Color.YELLOW);
                    Document doc = textArea.getDocument();
                    Element root = doc.getDefaultRootElement();
                    try{
                        for(Integer pos: poslist) {
                            int index = root.getElementIndex(pos);
                            Element elem = root.getElement(index);
                            Rectangle r = textArea.modelToView(pos);
                            Rectangle s = at.createTransformedShape(r).getBounds();
                            int h = 2; //Math.max(2, s.height-2);
                            g.fillRect(trackBounds.x, decrButtonH+s.y, trackBounds.width, h);
                        }
                    }catch(BadLocationException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setVerticalScrollBar(scrollbar);
        add(scroll);

        Box box = Box.createHorizontalBox();
        box.add(check);
        box.add(Box.createHorizontalGlue());
        box.add(new JButton(new AbstractAction("highlight") {
            @Override public void actionPerformed(ActionEvent e) {
                setHighlight(textArea, pattern);
            }
        }));
        box.add(Box.createHorizontalStrut(2));
        box.add(new JButton(new AbstractAction("clear") {
            @Override public void actionPerformed(ActionEvent e) {
                removeHighlights(textArea);
            }
        }));
        add(box, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private final ArrayList<Integer> poslist = new ArrayList<Integer>();
    public void setHighlight(JTextComponent jtc, String pattern) {
        removeHighlights(jtc);
        poslist.clear();
        try{
            Highlighter hilite = jtc.getHighlighter();
            Document doc = jtc.getDocument();
            String text = doc.getText(0, doc.getLength());
            int pos = 0;
            while((pos = text.indexOf(pattern, pos)) >= 0) {
                poslist.add(pos);
                hilite.addHighlight(pos, pos+pattern.length(), highlightPainter);
                pos += pattern.length();
            }
        }catch(BadLocationException e) {
            e.printStackTrace();
        }
        repaint();
    }
    public void removeHighlights(JTextComponent jtc) {
        poslist.clear();
        Highlighter hilite = jtc.getHighlighter();
        Highlighter.Highlight[] hilites = hilite.getHighlights();
        for(int i=0;i<hilites.length;i++) {
            if(hilites[i].getPainter() instanceof DefaultHighlighter.DefaultHighlightPainter) {
                hilite.removeHighlight(hilites[i]);
            }
        }
        repaint();
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
