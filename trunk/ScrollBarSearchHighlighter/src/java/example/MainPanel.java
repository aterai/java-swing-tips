package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Objects;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.plaf.metal.MetalScrollBarUI;
import com.sun.java.swing.plaf.windows.WindowsScrollBarUI;

public class MainPanel extends JPanel {
    private static final Highlighter.HighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
    private static final String PATTERN = "Swing";
    private static final String INITTXT =
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

    private final JTextArea textArea   = new JTextArea();
    private final JScrollPane scroll   = new JScrollPane(textArea);
    private final JScrollBar scrollbar = new JScrollBar(JScrollBar.VERTICAL);
    private final JCheckBox check      = new JCheckBox(new AbstractAction("LineWrap") {
        @Override public void actionPerformed(ActionEvent e) {
            JCheckBox c = (JCheckBox)e.getSource();
            textArea.setLineWrap(c.isSelected());
        }
    });
    public MainPanel() {
        super(new BorderLayout());
        textArea.setEditable(false);
        textArea.setText(INITTXT+INITTXT+INITTXT);

        scrollbar.setUnitIncrement(10);

        if(scrollbar.getUI() instanceof WindowsScrollBarUI) {
            scrollbar.setUI(new WindowsHighlightScrollBarUI(textArea));
        }else{
            scrollbar.setUI(new MetalHighlightScrollBarUI(textArea));
        }

        scroll.setVerticalScrollBar(scrollbar);
        scroll.setRowHeaderView(new JLabel(new HighlightIcon(textArea, scrollbar)));

        /*
        // Bug ID: JDK-6826074 JScrollPane does not revalidate the component hierarchy after scrolling
        // http://bugs.sun.com/view_bug.do?bug_id=6826074
        // Affected Versions: 6u12,6u16,7
        // Fixed Versions: 7 (b134)
        JViewport vp = new JViewport() {
            @Override public void setViewPosition(Point p) {
                super.setViewPosition(p);
                revalidate();
            }
        };
        vp.setView(new JLabel(new HighlightIcon(textArea, scrollbar)));
        scroll.setRowHeader(vp);
        */
        add(scroll);

        Box box = Box.createHorizontalBox();
        box.add(check);
        box.add(Box.createHorizontalGlue());
        box.add(new JButton(new AbstractAction("highlight") {
            @Override public void actionPerformed(ActionEvent e) {
                setHighlight(textArea, PATTERN);
            }
        }));
        box.add(Box.createHorizontalStrut(2));
        box.add(new JButton(new AbstractAction("clear") {
            @Override public void actionPerformed(ActionEvent e) {
                textArea.getHighlighter().removeAllHighlights();
                scroll.repaint();
            }
        }));
        add(box, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }
    public void setHighlight(JTextComponent jtc, String pattern) {
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

class HighlightIcon implements Icon {
    private static final Color THUMB_COLOR = new Color(0,0,255,50);
    private final JTextComponent textArea;
    private final JScrollBar scrollbar;
    public HighlightIcon(JTextComponent textArea, JScrollBar scrollbar) {
        this.textArea  = textArea;
        this.scrollbar = scrollbar;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Rectangle rect   = textArea.getBounds();
        Dimension sbSize = scrollbar.getSize();
        Insets sbInsets  = scrollbar.getInsets();
        double sy = (sbSize.height - sbInsets.top - sbInsets.bottom) / rect.getHeight();
        AffineTransform at = AffineTransform.getScaleInstance(1.0, sy);
        Highlighter highlighter = textArea.getHighlighter();

        //paint Highlight
        g.setColor(Color.RED);
        try{
            for(Highlighter.Highlight hh: highlighter.getHighlights()) {
                Rectangle r = textArea.modelToView(hh.getStartOffset());
                Rectangle s = at.createTransformedShape(r).getBounds();
                int h = 2; //Math.max(2, s.height-2);
                g.fillRect(x, y+sbInsets.top+s.y, getIconWidth(), h);
            }
        }catch(BadLocationException e) {
            e.printStackTrace();
        }

        //paint Thumb
        Rectangle crect = c.getBounds();
        JViewport vport = Objects.requireNonNull((JViewport)SwingUtilities.getAncestorOfClass(JViewport.class, textArea));
        crect.y = vport.getViewPosition().y;
        g.setColor(THUMB_COLOR);
        Rectangle rr = at.createTransformedShape(crect).getBounds();
        g.fillRect(x, y+sbInsets.top+rr.y, getIconWidth(), rr.height);
    }
    @Override public int getIconWidth() {
        return 4;
    }
    @Override public int getIconHeight() {
        return scrollbar.getHeight();
    }
}

class WindowsHighlightScrollBarUI extends WindowsScrollBarUI {
    private final JTextComponent textArea;
    public WindowsHighlightScrollBarUI(JTextComponent textArea) {
        super();
        this.textArea = textArea;
    }
    @Override protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        super.paintTrack(g, c, trackBounds);

        Rectangle rect = textArea.getBounds();
        double sy = trackBounds.getHeight() / rect.getHeight();
        AffineTransform at = AffineTransform.getScaleInstance(1.0, sy);
        Highlighter highlighter = textArea.getHighlighter();
        g.setColor(Color.YELLOW);
        try{
            for(Highlighter.Highlight hh: highlighter.getHighlights()) {
                Rectangle r = textArea.modelToView(hh.getStartOffset());
                Rectangle s = at.createTransformedShape(r).getBounds();
                int h = 2; //Math.max(2, s.height-2);
                g.fillRect(trackBounds.x, trackBounds.y+s.y, trackBounds.width, h);
            }
        }catch(BadLocationException e) {
            e.printStackTrace();
        }
    }
}

class MetalHighlightScrollBarUI extends MetalScrollBarUI {
    private final JTextComponent textArea;
    public MetalHighlightScrollBarUI(JTextComponent textArea) {
        super();
        this.textArea = textArea;
    }
    @Override protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        super.paintTrack(g, c, trackBounds);

        Rectangle rect = textArea.getBounds();
        double sy = trackBounds.getHeight() / rect.getHeight();
        AffineTransform at = AffineTransform.getScaleInstance(1.0, sy);
        Highlighter highlighter = textArea.getHighlighter();
        g.setColor(Color.YELLOW);
        try{
            for(Highlighter.Highlight hh: highlighter.getHighlights()) {
                Rectangle r = textArea.modelToView(hh.getStartOffset());
                Rectangle s = at.createTransformedShape(r).getBounds();
                int h = 2; //Math.max(2, s.height-2);
                g.fillRect(trackBounds.x, trackBounds.y+s.y, trackBounds.width, h);
            }
        }catch(BadLocationException e) {
            e.printStackTrace();
        }
    }
}
