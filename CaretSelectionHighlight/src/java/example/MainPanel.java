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
    private final JDesktopPane desktop = new JDesktopPane();
    public MainPanel() {
        super(new BorderLayout());
        desktop.add(makeInternalFrame("DefaultCaret", new Point(10, 10), new JScrollPane(makeTextArea(false))));
        desktop.add(makeInternalFrame("FocusCaret", new Point(50, 50), new JScrollPane(makeTextArea(true))));
        desktop.add(makeInternalFrame("FocusCaret", new Point(90, 90), new JScrollPane(makeTextArea(true))));
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                for (JInternalFrame f: desktop.getAllFrames()) {
                    f.setVisible(true);
                }
            }
        });
        add(desktop);
        setPreferredSize(new Dimension(320, 240));
    }

    private static JInternalFrame makeInternalFrame(String title, Point p, JComponent c) {
        JInternalFrame f = new JInternalFrame(title, true, true, true, true);
        f.add(c);
        f.setSize(200, 100);
        f.setLocation(p);
        return f;
    }

    private static JComponent makeTextArea(final boolean flag) {
        final JTextArea textArea = new JTextArea() {
            @Override public void updateUI() {
                setCaret(null);
                super.updateUI();
                if (flag) {
                    Caret oldCaret = getCaret();
                    int blinkRate = oldCaret.getBlinkRate();
                    //int blinkRate = UIManager.getInt("TextField.caretBlinkRate")
                    Caret caret = new FocusCaret();
                    caret.setBlinkRate(blinkRate);
                    setCaret(caret);
                    caret.setSelectionVisible(true);
                }
            }
        };
        textArea.setText("aaa\nbbbbbb\ncccccccccccc\n");
        textArea.selectAll();
        return textArea;
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
            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            for (UIManager.LookAndFeelInfo laf: UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(laf.getName())) {
                    UIManager.setLookAndFeel(laf.getClassName());
                }
            }
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

class FocusCaret extends DefaultCaret {
    private static final Highlighter.HighlightPainter NON_FOCUS_HIGHLIGHT_PAINTER = new DefaultHighlighter.DefaultHighlightPainter(Color.GRAY.brighter());
    @Override public void focusLost(FocusEvent e) {
        super.focusLost(e);
        setSelectionVisible(true);
    }
    @Override public void focusGained(FocusEvent e) {
        super.focusGained(e);
        //http://stackoverflow.com/questions/18237317/how-to-retain-selected-text-in-jtextfield-when-focus-lost
        setSelectionVisible(false); //removeHighlight
        setSelectionVisible(true); //addHighlight
        //TEST
        //setVisible(true);
        //damage(getComponent().getBounds());
        //repaint();
    }
    @Override protected Highlighter.HighlightPainter getSelectionPainter() {
//         JComponent c = getComponent();
//         boolean selected = c.hasFocus();
//         Container f = SwingUtilities.getAncestorOfClass(JInternalFrame.class, c);
//         if (f instanceof JInternalFrame) {
//             System.out.println("bbbbbbbbbbbbbb");
//             JInternalFrame frame = (JInternalFrame) f;
//             selected = frame.isSelected();
//         }
//         return selected ? DefaultHighlighter.DefaultPainter : nonFocusHighlightPainter;
        return getComponent().hasFocus() ? DefaultHighlighter.DefaultPainter : NON_FOCUS_HIGHLIGHT_PAINTER;
    }
}
