package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.text.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        OvertypeTextArea textArea= new OvertypeTextArea();
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setText("Press the INSERT key to toggle the overwrite mode.\n\u3042\u3042\u3042\naaaaaaaaaaafasdfas");
        add(new JScrollPane(textArea));
        setPreferredSize(new Dimension(320, 200));
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

// https://forums.oracle.com/thread/1385467 JTextPane edit mode (insert or overwrite)???
class OvertypeTextArea extends JTextArea {
    //private static Toolkit toolkit = Toolkit.getDefaultToolkit();
    private boolean isOvertypeMode;
    private final Caret defaultCaret;
    private final Caret overtypeCaret;
    public OvertypeTextArea() {
        super();
        //setCaretColor(Color.RED);
        defaultCaret = getCaret();
        overtypeCaret = new OvertypeCaret();
        overtypeCaret.setBlinkRate(defaultCaret.getBlinkRate());
        setOvertypeMode(true);
    }
    public boolean isOvertypeMode() {
        return isOvertypeMode;
    }

    /*
     *Set the caret to use depending on overtype/insert mode
     */
    public void setOvertypeMode(boolean isOvertypeMode) {
        this.isOvertypeMode = isOvertypeMode;
        int pos = getCaretPosition();
        if(isOvertypeMode()) {
            setCaret(overtypeCaret);
        }else{
            setCaret(defaultCaret);
        }
        setCaretPosition(pos);
    }

    /*
     *  Override method from JComponent
     */
    @Override public void replaceSelection(String text) {
        //  Implement overtype mode by selecting the character at the current
        //  caret position
        if(isOvertypeMode()) {
            int pos = getCaretPosition();
            if(getSelectedText()==null &&  pos<getDocument().getLength()) {
                moveCaretPosition(pos+1);
            }
        }
        super.replaceSelection(text);
    }

    /*
     *  Override method from JComponent
     */
    @Override protected void processKeyEvent(KeyEvent e) {
        super.processKeyEvent(e);
        //  Handle release of Insert key to toggle overtype/insert mode
        if(e.getID()==KeyEvent.KEY_RELEASED &&  e.getKeyCode()==KeyEvent.VK_INSERT) {
            setCaretPosition(getCaretPosition());  //add
            moveCaretPosition(getCaretPosition()); //add
            setOvertypeMode(!isOvertypeMode());
            repaint(); //add
        }
    }

    /*
     *  Paint a horizontal line the width of a column and 1 pixel high
     */
    class OvertypeCaret extends DefaultCaret {
        /*
         *  The overtype caret will simply be a horizontal line one pixel high
         *  (once we determine where to paint it)
         */
        @Override public void paint(Graphics g) {
            if(isVisible()) {
                try{
                    JTextComponent component = getComponent();
                    TextUI mapper = component.getUI();
                    Rectangle r = mapper.modelToView(component, getDot());
                    g.setColor(component.getCaretColor());
                    int width = g.getFontMetrics().charWidth('w');
                    // A patch for double-width CJK character >>>>
                    if(isOvertypeMode()) {
                        int pos = getCaretPosition();
                        if(pos<getDocument().getLength()) {
                            if(getSelectedText()!=null) {
                                width = 0;
                            }else{
                                String str = getText(pos, 1);
                                width = g.getFontMetrics().stringWidth(str);
                            }
                        }
                    } // <<<<
                    int y = r.y + r.height - 2;
                    g.drawLine(r.x, y, r.x + width - 2, y);
                }catch(BadLocationException e) { e.printStackTrace(); }
            }
        }

        /*
         *  Damage must be overridden whenever the paint method is overridden
         *  (The damaged area is the area the caret is painted in. We must
         *  consider the area for the default caret and this caret)
         */
        @Override protected synchronized void damage(Rectangle r) {
            if(r != null) {
                JTextComponent component = getComponent();
                x = r.x;
                y = r.y;
                //width = component.getFontMetrics(component.getFont()).charWidth('w');
                width = component.getFontMetrics(component.getFont()).charWidth('\u3042');
                height = r.height;
                repaint();
            }
        }
    }
}
