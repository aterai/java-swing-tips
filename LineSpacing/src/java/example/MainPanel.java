package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private final JTextPane editor1 = new JTextPane();
    private final JTextPane editor2 = new JTextPane();
    public MainPanel() {
        super(new BorderLayout());
        SimpleAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, Color.RED);
        StyleConstants.setFontSize(attr, 32);

        SimpleAttributeSet a = new SimpleAttributeSet();
        StyleConstants.setLineSpacing(a, .5f);
        //StyleConstants.setSpaceAbove(a, 5f);
        //StyleConstants.setSpaceBelow(a, 5f);
        //StyleConstants.setLeftIndent(a, 5f);
        //StyleConstants.setRightIndent(a, 5f);
        editor1.setParagraphAttributes(a, false);
        setDummyText(editor1, attr);

//         StyleSheet styleSheet = new StyleSheet();
//         styleSheet.addRule("body {font-size: 24pt; line-height: 2.0}"); //XXX
//         HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
//         htmlEditorKit.setStyleSheet(styleSheet);
//         editor1.setEditorKit(htmlEditorKit);
//         editor1.setText("<html><body>12341234<br />asdf_fASdfasf_fasdf_affffFSDdfasdf<br />nasdfasFasdf<font size='32'>12341234<br />asdfasdf</font></body></html>");

        editor2.setEditorKit(new BottomInsetEditorKit());
        setDummyText(editor2, attr);

        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        sp.setTopComponent(new JScrollPane(editor1));
        sp.setBottomComponent(new JScrollPane(editor2));
        sp.setResizeWeight(0.5);
        add(sp);
        setPreferredSize(new Dimension(320, 240));
    }
    private static void setDummyText(JTextPane textPane, MutableAttributeSet attr) {
        textPane.setText("12341234\nasdf fASdfasf fasdf affffFS Ddfasdf\nasdf asFasdf ");
        try {
            StyledDocument doc = textPane.getStyledDocument();
            doc.insertString(doc.getLength(), "134500698\n", attr);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
//         StyledDocument doc = new DefaultStyledDocument();
//         SimpleAttributeSet a = new SimpleAttributeSet();
//         StyleConstants.setLineSpacing(a, .5f);
//         doc.setParagraphAttributes(0, doc.getLength() - 1, a, false);
//         textPane.setStyledDocument(doc);
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

class BottomInsetEditorKit extends StyledEditorKit {
    @Override public ViewFactory getViewFactory() {
        return new BottomInsetViewFactory();
    }
}

class BottomInsetViewFactory implements ViewFactory {
    @Override public View create(Element elem) {
        String kind = elem.getName();
        if (kind != null) {
            if (kind.equals(AbstractDocument.ContentElementName)) {
                return new LabelView(elem);
            } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                return new ParagraphView(elem) {
                    @Override protected short getBottomInset() {
                        return 5;
                    }
                };
            } else if (kind.equals(AbstractDocument.SectionElementName)) {
                return new BoxView(elem, View.Y_AXIS);
            } else if (kind.equals(StyleConstants.ComponentElementName)) {
                return new ComponentView(elem);
            } else if (kind.equals(StyleConstants.IconElementName)) {
                return new IconView(elem);
            }
        }
        return new LabelView(elem);
    }
}
