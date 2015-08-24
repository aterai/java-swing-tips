package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private static final String TABTEST = "\n1\taaa\n12\taaa\n123\taaa\n1234\taaa\t\t\t\t\t\t\n";
    private static final String ZSTEST = "adfasdfasdfasdf\nffas2\u3000\u30001 3 dfas\n\n\u300000000\u300012345\u3000\n";
    private static final String ZS_TAB_ZSTEST = "\u3000\u3000\u65E5\u672C\u8A9E\u3000\n";

    private MainPanel() {
        super(new BorderLayout());
        JTextPane editor = new JTextPane();

        editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        editor.setEditorKit(new MyEditorKit());
        editor.setText(ZSTEST + ZS_TAB_ZSTEST + TABTEST);

        add(new JScrollPane(editor));
        setPreferredSize(new Dimension(320, 240));
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

class MyEditorKit extends StyledEditorKit {
    private final SimpleAttributeSet attrs = new SimpleAttributeSet();
    @Override public void install(JEditorPane c) {
        FontMetrics fm = c.getFontMetrics(c.getFont());
        int tabLength = fm.charWidth('m') * 4;
        TabStop[] tabs = new TabStop[100];
        for (int j = 0; j < tabs.length; j++) {
            tabs[j] = new TabStop((j + 1) * tabLength);
        }
        TabSet tabSet = new TabSet(tabs);
        StyleConstants.setTabSet(attrs, tabSet);
        super.install(c);
    }
    @Override public ViewFactory getViewFactory() {
        return new MyViewFactory();
    }
    @Override public Document createDefaultDocument() {
        Document d = super.createDefaultDocument();
        if (d instanceof StyledDocument) {
            ((StyledDocument) d).setParagraphAttributes(0, d.getLength(), attrs, false);
        }
        return d;
    }
}

class MyViewFactory implements ViewFactory {
    @Override public View create(Element elem) {
        String kind = elem.getName();
        if (Objects.nonNull(kind)) {
            if (kind.equals(AbstractDocument.ContentElementName)) {
                return new WhitespaceLabelView(elem);
            } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                return new MyParagraphView(elem);
            } else if (kind.equals(AbstractDocument.SectionElementName)) {
                return new BoxView(elem, View.Y_AXIS);
            } else if (kind.equals(StyleConstants.ComponentElementName)) {
                return new ComponentView(elem);
            } else if (kind.equals(StyleConstants.IconElementName)) {
                return new IconView(elem);
            }
        }
        return new WhitespaceLabelView(elem);
    }
}

class MyParagraphView extends ParagraphView {
    private static final Color MARK_COLOR = new Color(120, 130, 110);
    public MyParagraphView(Element elem) {
        super(elem);
    }
    @Override public void paint(Graphics g, Shape allocation) {
        super.paint(g, allocation);
        paintCustomParagraph(g, allocation);
    }
    private void paintCustomParagraph(Graphics g, Shape a) {
        try {
            Shape paragraph = modelToView(getEndOffset(), a, Position.Bias.Backward);
            Rectangle r = Objects.nonNull(paragraph) ? paragraph.getBounds() : a.getBounds();
            int x = r.x;
            int y = r.y;
            int h = r.height;
            Color old = g.getColor();
            g.setColor(MARK_COLOR);
            g.drawLine(x + 1, y + h / 2, x + 1, y + h - 4);
            g.drawLine(x + 2, y + h / 2, x + 2, y + h - 5);
            g.drawLine(x + 3, y + h - 6, x + 3, y + h - 6);
            g.setColor(old);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }
}

class WhitespaceLabelView extends LabelView {
    private static final String IDEOGRAPHIC_SPACE = "\u3000";
    private static final Color MARK_COLOR = new Color(130, 140, 120);
    private static final BasicStroke DASHED = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] {1f}, 0f);
    public WhitespaceLabelView(Element elem) {
        super(elem);
    }
    @Override public void paint(Graphics g, Shape a) {
        super.paint(g, a);
        Graphics2D g2 = (Graphics2D) g.create();
        Stroke stroke = g2.getStroke();
        Rectangle alloc = a instanceof Rectangle ? (Rectangle) a : a.getBounds();
        FontMetrics fontMetrics = g.getFontMetrics();
        int spaceWidth = fontMetrics.stringWidth(IDEOGRAPHIC_SPACE);
        int sumOfTabs = 0;
        String text = getText(getStartOffset(), getEndOffset()).toString();
        for (int i = 0; i < text.length(); i++) {
            String s = text.substring(i, i + 1);
            int previousStringWidth = fontMetrics.stringWidth(text.substring(0, i)) + sumOfTabs;
            int sx = alloc.x + previousStringWidth;
            int sy = alloc.y + alloc.height - fontMetrics.getDescent();
            if (IDEOGRAPHIC_SPACE.equals(s)) {
                g2.setStroke(DASHED);
                g2.setPaint(MARK_COLOR);
                g2.drawLine(sx + 1, sy - 1, sx + spaceWidth - 2, sy - 1);
                g2.drawLine(sx + 2, sy,     sx + spaceWidth - 2, sy);
            } else if ("\t".equals(s)) {
                int tabWidth = (int) getTabExpander().nextTabStop((float) sx, i) - sx;
                g2.setPaint(MARK_COLOR);
                g2.drawLine(sx + 2, sy - 0, sx + 2 + 2, sy - 0);
                g2.drawLine(sx + 2, sy - 1, sx + 2 + 1, sy - 1);
                g2.drawLine(sx + 2, sy - 2, sx + 2 + 0, sy - 2);
                g2.setStroke(DASHED);
                g2.drawLine(sx + 2, sy, sx + tabWidth - 2, sy);
                sumOfTabs += tabWidth;
            }
            g2.setStroke(stroke);
            g2.dispose();
        }
    }
}
