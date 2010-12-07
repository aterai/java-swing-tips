package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
public class MainPanel extends JPanel {
    private final JTextPane editor1 = new JTextPane();
    private final JEditorPane editor2 = new JEditorPane();
    public MainPanel() {
        super(new BorderLayout());
        SimpleAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setFontSize(attr, 32);

        SimpleAttributeSet a = new SimpleAttributeSet();
        StyleConstants.setLineSpacing(a, .5f);
        //StyleConstants.setSpaceAbove(a, 5.0f);
        //StyleConstants.setSpaceBelow(a, 5.0f);
        //StyleConstants.setLeftIndent(a, 5.0f);
        //StyleConstants.setRightIndent(a, 5.0f);
        editor1.setParagraphAttributes(a, true);
        editor1.setText("JTextPane, StyleConstants.setLineSpacing(...);\naaaaaaaa");
        setDummyText(editor1, "1234567890", attr);

//         StyleSheet styleSheet = new StyleSheet();
//         styleSheet.addRule("body {font-size: 24pt; line-height: 2.0}"); //XXX
//         HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
//         htmlEditorKit.setStyleSheet(styleSheet);
//         editor1.setEditorKit(htmlEditorKit);
//         editor1.setText("<html><body>12341234<br />asdf_fASdfasf_fasdf_affffFSDdfasdf<br />nasdfasFasdf<font size='32'>12341234<br />asdfasdf</font></body></html>");

        editor2.setEditorKit(new BottomInsetEditorKit());
        editor2.setText("JEditorPane, BottomInsetEditorKit\nbbbbbbb");
        setDummyText(editor2, "0987654321\nasdf", attr);

        final JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        sp.setTopComponent(new JScrollPane(editor1));
        sp.setBottomComponent(new JScrollPane(editor2));
        sp.setResizeWeight(0.5);
        add(sp);
        setPreferredSize(new Dimension(320, 240));
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                sp.setDividerLocation(sp.getSize().height/2);
            }
        });
    }
    private static void setDummyText(JEditorPane editor, String str, MutableAttributeSet attr) {
        Document doc = editor.getDocument();
        try{
            doc.insertString(doc.getLength(), str, attr);
            editor.setCaretPosition(doc.getLength());
        }catch(BadLocationException e) {
            e.printStackTrace();
        }
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
class BottomInsetEditorKit extends StyledEditorKit {
    @Override public ViewFactory getViewFactory() {
        return new ViewFactory() {
            @Override public View create(Element elem) {
                String kind = elem.getName();
                if(kind!=null) {
                    if(kind.equals(AbstractDocument.ContentElementName)) {
                        return new LabelView(elem);
                    }else if(kind.equals(AbstractDocument.ParagraphElementName)) {
                        return new javax.swing.text.ParagraphView(elem) {
                            @Override protected short getBottomInset() {
                                return 5;
                            }
                        };
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
        };
    }
}
