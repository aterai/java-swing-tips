package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private static String text;

    private MainPanel(ExecutorService threadPool) {
        super(new BorderLayout());

        // JTextPane textPane;
        JEditorPane editorPane = new JEditorPane();
        JTextArea textArea = new JTextArea();
        JButton editorPaneButton = new JButton("JEditorPane");
        JButton textAreaButton = new JButton("JTextArea");

        // textPane = new JTextPane() {
        //     // Non Wrapping(Wrap) TextPane : TextField : Swing JFC : Java examples (example source code) Organized by topic
        //     // http://www.java2s.com/Code/Java/Swing-JFC/NonWrappingWrapTextPane.htm
        //     @Override public boolean getScrollableTracksViewportWidth() {
        //         Component p = getParent();
        //         if (Objects.isNull(p)) {
        //             return true;
        //         }
        //         int ewidth = getUI().getPreferredSize(this).width;
        //         return ewidth <= p.getSize().width;
        //     }
        // };
        // textPane.setEditorKit(new NoWrapEditorKit1());

        editorPane.setEditorKit(new NoWrapEditorKit2());

        ActionListener longTextListener = e -> {
            threadPool.execute(() -> {
                if (Objects.nonNull(text)) {
                    if (editorPaneButton.equals(e.getSource())) {
                        editorPane.setText(text);
                    } else {
                        textArea.setText(text);
                    }
                }
            });
        };
        editorPaneButton.addActionListener(longTextListener);
        textAreaButton.addActionListener(longTextListener);

        JButton clearButton = new JButton("clear all");
        clearButton.addActionListener(e -> {
            editorPane.setText("");
            textArea.setText("");
        });

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(editorPaneButton);
        box.add(textAreaButton);
        box.add(clearButton);

        JPanel p = new JPanel(new GridLayout(2, 1));
        p.add(makeTitledPanel("NoWrapEditorKit(JEditorPane)", editorPane));
        p.add(makeTitledPanel("JTextArea", textArea));

        add(box, BorderLayout.NORTH);
        add(p);
        setPreferredSize(new Dimension(320, 240));
    }
    private static Component makeTitledPanel(String title, Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createTitledBorder(title));
        return sp;
    }
    private static void initLongLineStringInBackground(ExecutorService threadPool, int length) {
        threadPool.execute(() -> {
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length - 2; i++) {
                sb.append('a');
            }
            sb.append("x\n");
            text = sb.toString();
        });
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
        ExecutorService threadPool = Executors.newCachedThreadPool();
        initLongLineStringInBackground(threadPool, 1024 * 1024);
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel(threadPool));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

/*
class NoWrapEditorKit1 extends StyledEditorKit {
    @Override public ViewFactory getViewFactory() {
        return new StyledViewFactory();
    }
    static class StyledViewFactory implements ViewFactory {
        @Override public View create(Element elem) {
            String kind = elem.getName();
            if (Objects.nonNull(kind)) {
                if (kind.equals(AbstractDocument.ContentElementName)) {
                    return new LabelView(elem);
                } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                    return new ParagraphView(elem);
                } else if (kind.equals(AbstractDocument.SectionElementName)) {
                    return new NoWrapBoxView(elem, View.Y_AXIS);
                } else if (kind.equals(StyleConstants.ComponentElementName)) {
                    return new ComponentView(elem);
                } else if (kind.equals(StyleConstants.IconElementName)) {
                    return new IconView(elem);
                }
            }
            return new LabelView(elem);
        }
    }
    static class NoWrapBoxView extends BoxView {
        protected NoWrapBoxView(Element elem, int axis) {
            super(elem, axis);
        }
        @Override public void layout(int width, int height) {
            super.layout(Integer.MAX_VALUE - 64, height);
            // ??? Integer.MAX_VALUE - 64 = error?
            // ??? Integer.MAX_VALUE - 64 = ok?
        }
    }
}
/*/
// https://community.oracle.com/thread/1353861 Disabling word wrap for JTextPane
class NoWrapParagraphView extends ParagraphView {
    protected NoWrapParagraphView(Element elem) {
        super(elem);
    }
    @Override protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
        SizeRequirements req = super.calculateMinorAxisRequirements(axis, r);
        req.minimum = req.preferred;
        return req;
    }
    @Override public int getFlowSpan(int index) {
        return Integer.MAX_VALUE;
    }
}

class NoWrapViewFactory implements ViewFactory {
    @Override public View create(Element elem) {
        String kind = elem.getName();
        if (Objects.nonNull(kind)) {
            if (kind.equals(AbstractDocument.ContentElementName)) {
                return new LabelView(elem);
            } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                return new NoWrapParagraphView(elem);
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

class NoWrapEditorKit2 extends StyledEditorKit {
    @Override public ViewFactory getViewFactory() {
        return new NoWrapViewFactory();
    }
}
//*/
