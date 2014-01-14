package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        JEditorPane editor = new JEditorPane();
        editor.setEditorKit(new MyEditorKit());
        editor.setText("1234123541341234123423\nadfasdfasdfasdf\nffas213441324dfasdfas\n\nretqewrqwr");
        add(new JScrollPane(editor));
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
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class MyEditorKit extends StyledEditorKit {
    @Override public ViewFactory getViewFactory() {
        return new MyViewFactory();
    }
}

class MyViewFactory implements ViewFactory {
    @Override public View create(Element elem) {
        String kind = elem.getName();
        if(kind!=null) {
            if(kind.equals(AbstractDocument.ContentElementName)) {
                return new LabelView(elem);
            }else if(kind.equals(AbstractDocument.ParagraphElementName)) {
                return new MyParagraphView(elem);
            }else if(kind.equals(AbstractDocument.SectionElementName)) {
                return new BoxView(elem, View.Y_AXIS);
            }else if(kind.equals(StyleConstants.ComponentElementName)) {
                return new ComponentView(elem);
            }else if(kind.equals(StyleConstants.IconElementName)) {
                return new IconView(elem);
            }
        }
        return new LabelView(elem);
    }
}

class MyParagraphView extends ParagraphView {
    private static final Color pc = new Color(120, 130, 110);
    public MyParagraphView(Element elem) {
        super(elem);
    }
    @Override public void paint(Graphics g, Shape allocation) {
        super.paint(g, allocation);
        paintCustomParagraph(g, allocation);
    }
    private void paintCustomParagraph(Graphics g, Shape a) {
        try{
            Shape paragraph = modelToView(getEndOffset(), a, Position.Bias.Backward);
            Rectangle r = (paragraph==null)?a.getBounds():paragraph.getBounds();
            int x = r.x;
            int y = r.y;
            int h = r.height;
            Color old = g.getColor();
            g.setColor(pc);
            g.drawLine(x+1, y+h/2, x+1, y+h-4);
            g.drawLine(x+2, y+h/2, x+2, y+h-5);
            g.drawLine(x+3, y+h-6, x+3, y+h-6);
            g.setColor(old);
        }catch(Exception e) { e.printStackTrace(); }
    }
}
