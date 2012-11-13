package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

public class MainPanel extends JPanel {
    private static final String MYSITE = "http://terai.xrea.jp/";
    private static final Action a = new AbstractAction(MYSITE) {
        @Override public void actionPerformed(ActionEvent e) {
            java.awt.Toolkit.getDefaultToolkit().beep();
            try{
                if(!Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(new URI(MYSITE));
                }
            }catch(IOException ioe) {
                ioe.printStackTrace();
            }catch(URISyntaxException use) {
                use.printStackTrace();
            }
        }
    };

    public MainPanel() {
        super(new GridBagLayout());

        JEditorPane editor = new JEditorPane("text/html", "<html><a href='"+MYSITE+"'>"+MYSITE+"</a>");
        editor.setOpaque(false); //editor.setBackground(getBackground());
        editor.setEditable(false); //REQUIRED
        editor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        editor.addHyperlinkListener(new HyperlinkListener() {
            @Override public void hyperlinkUpdate(HyperlinkEvent e) {
                if(e.getEventType()==HyperlinkEvent.EventType.ACTIVATED) {
                    java.awt.Toolkit.getDefaultToolkit().beep();
                }
            }
        });

        Border inside  = BorderFactory.createEmptyBorder(2,5+2,2,5+2);
        Border outside = BorderFactory.createTitledBorder("HyperlinkLabel");
        setBorder(BorderFactory.createCompoundBorder(outside, inside));
        GridBagConstraints c = new GridBagConstraints();
        c.gridheight = 1;

        c.gridx   = 0;
        c.insets  = new Insets(5, 5, 5, 0);
        c.anchor  = GridBagConstraints.EAST;
        c.gridy   = 0; add(new JLabel("JLabel+MouseListener: "), c);
        c.gridy   = 1; add(new JLabel("JButton: "), c);
        c.gridy   = 2; add(new JLabel("JButton+ButtonUI: "), c);
        c.gridy   = 3; add(new JLabel("JEditorPane+HyperlinkListener: "), c);

        c.gridx   = 1;
        c.weightx = 1.0;
        c.anchor  = GridBagConstraints.WEST;
        c.gridy   = 0; add(new URILabel(MYSITE), c);
        c.gridy   = 1; add(new JButton(a), c);
        c.gridy   = 2; add(new HyperlinkButton(a), c);
        c.gridy   = 3; add(editor, c);
        setPreferredSize(new Dimension(320, 180));
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

class URILabel extends JLabel {
    private final String href;
    public URILabel(String str) {
        super("<html><a href='"+str+"'>"+str+"</a>");
        href = str;
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                java.awt.Toolkit.getDefaultToolkit().beep();
            }
        });
    }
}

class HyperlinkButton extends JButton {
    private static final String uiClassID = "LinkViewButtonUI";
//     public String getUIClassID() {
//         return uiClassID;
//     }
//     public void setUI(LinkViewButtonUI ui) {
//         super.setUI(ui);
//     }
    @Override public void updateUI() {
        super.updateUI();
        if(UIManager.get(uiClassID)!=null) {
            setUI((LinkViewButtonUI)UIManager.getUI(this));
        }else{
            setUI(BasicLinkViewButtonUI.createUI(this));
        }
        setForeground(Color.BLUE);
        setBorder(BorderFactory.createEmptyBorder(0,0,2,0));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    public LinkViewButtonUI getUI() {
        return BasicLinkViewButtonUI.createUI(this);
    }
    public HyperlinkButton() {
        this(null, null);
    }
    public HyperlinkButton(Icon icon) {
        this(null, icon);
    }
    public HyperlinkButton(String text) {
        this(text, null);
    }
    public HyperlinkButton(Action a) {
        this();
        super.setAction(a);
    }
    public HyperlinkButton(String text, Icon icon) {
        super(text, icon);
    }
}

abstract class LinkViewButtonUI extends BasicButtonUI {}
class BasicLinkViewButtonUI extends LinkViewButtonUI {
    private final static LinkViewButtonUI linkViewButtonUI = new BasicLinkViewButtonUI();
    public static LinkViewButtonUI createUI(JButton b) {
//         b.setForeground(Color.BLUE);
//         b.setBorder(BorderFactory.createEmptyBorder(0,0,2,0));
//         b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return linkViewButtonUI;
    }
    private BasicLinkViewButtonUI() {
        super();
    }
    private static Dimension size = new Dimension();
    private static Rectangle viewRect = new Rectangle();
    private static Rectangle iconRect = new Rectangle();
    private static Rectangle textRect = new Rectangle();
    @Override public synchronized void paint(Graphics g, JComponent c) {
        AbstractButton b = (AbstractButton) c;
        ButtonModel model = b.getModel();
        Font f = c.getFont();
        g.setFont(f);
        FontMetrics fm = c.getFontMetrics(f);

        Insets i = c.getInsets();
        size = b.getSize(size);
        viewRect.x = i.left;
        viewRect.y = i.top;
        viewRect.width = size.width - i.right - viewRect.x;
        viewRect.height = size.height - i.bottom - viewRect.y;
        iconRect.x = iconRect.y = iconRect.width = iconRect.height = 0;
        textRect.x = textRect.y = textRect.width = textRect.height = 0;

        String text = SwingUtilities.layoutCompoundLabel(
            c, fm, b.getText(), null, //altIcon != null ? altIcon : getDefaultIcon(),
            b.getVerticalAlignment(), b.getHorizontalAlignment(),
            b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
            viewRect, iconRect, textRect,
            0); //b.getText() == null ? 0 : b.getIconTextGap());

        if(c.isOpaque()) {
            g.setColor(b.getBackground());
            g.fillRect(0,0, size.width, size.height);
        }
        if(text==null) return;
        if(!model.isSelected() && !model.isPressed() && !model.isArmed()
           && b.isRolloverEnabled() && model.isRollover()) {
            g.setColor(Color.BLUE);
            g.drawLine(viewRect.x,                viewRect.y+viewRect.height,
                       viewRect.x+viewRect.width, viewRect.y+viewRect.height);
        }
        View v = (View) c.getClientProperty(BasicHTML.propertyKey);
        if(v!=null) {
            v.paint(g, textRect);
        }else{
            paintText(g, b, textRect, text);
        }
    }
}
