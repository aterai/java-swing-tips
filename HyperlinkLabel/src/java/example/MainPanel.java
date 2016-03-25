package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private static final String MYSITE = "http://ateraimemo.com/";
    private final Action browseAction = new AbstractAction(MYSITE) {
        @Override public void actionPerformed(ActionEvent e) {
            Toolkit.getDefaultToolkit().beep();
            try {
                if (!Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(new URI(MYSITE));
                }
            } catch (IOException | URISyntaxException ex) {
                ex.printStackTrace();
            }
        }
    };

    protected MainPanel() {
        super(new GridBagLayout());

        JEditorPane editor = new JEditorPane("text/html", String.format("<html><a href='%s'>%s</a>", MYSITE, MYSITE));
        editor.setOpaque(false); //editor.setBackground(getBackground());
        editor.setEditable(false); //REQUIRED
        editor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        editor.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                Toolkit.getDefaultToolkit().beep();
            }
        });

        Border inside  = BorderFactory.createEmptyBorder(2, 5 + 2, 2, 5 + 2);
        Border outside = BorderFactory.createTitledBorder("HyperlinkLabel");
        setBorder(BorderFactory.createCompoundBorder(outside, inside));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 0);

        c.gridx  = 0;
        c.anchor = GridBagConstraints.LINE_END;
        add(new JLabel("JLabel+MouseListener: "), c);
        add(new JLabel("JButton: "), c);
        add(new JLabel("JButton+ButtonUI: "), c);
        add(new JLabel("JEditorPane+HyperlinkListener: "), c);

        c.gridx  = 1;
        c.anchor = GridBagConstraints.LINE_START;
        add(new URILabel(MYSITE), c);
        add(new JButton(browseAction), c);
        add(new HyperlinkButton(browseAction), c);
        add(editor, c);
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

class URILabel extends JLabel {
    private transient MouseListener handler;
    protected URILabel(String h) {
        super(String.format("<html><a href='%s'>%s", h, h));
    }
    @Override public void updateUI() {
        removeMouseListener(handler);
        super.updateUI();
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        handler = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                Toolkit.getDefaultToolkit().beep();
            }
        };
        addMouseListener(handler);
    }
}

class HyperlinkButton extends JButton {
    private static final String UI_CLASS_ID = "LinkViewButtonUI";
//     @Override public String getUIClassID() {
//         return UI_CLASS_ID;
//     }
//     @Override public void setUI(LinkViewButtonUI ui) {
//         super.setUI(ui);
//     }
    @Override public void updateUI() {
        super.updateUI();
        if (Objects.nonNull(UIManager.get(UI_CLASS_ID))) {
            setUI((LinkViewButtonUI) UIManager.getUI(this));
        } else {
            setUI(BasicLinkViewButtonUI.createUI(this));
        }
        setForeground(Color.BLUE);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    @Override public LinkViewButtonUI getUI() {
        return BasicLinkViewButtonUI.createUI(this);
    }
    protected HyperlinkButton() {
        this(null, null);
    }
    protected HyperlinkButton(Icon icon) {
        this(null, icon);
    }
    protected HyperlinkButton(String text) {
        this(text, null);
    }
    protected HyperlinkButton(Action a) {
        this();
        super.setAction(a);
    }
    protected HyperlinkButton(String text, Icon icon) {
        super(text, icon);
    }
}

class LinkViewButtonUI extends BasicButtonUI { /* ButtonUI */ }

class BasicLinkViewButtonUI extends LinkViewButtonUI {
    private static final LinkViewButtonUI LINKVIEW_BUTTON_UI = new BasicLinkViewButtonUI();
    private final Dimension size;
    private final Rectangle viewRect;
    private final Rectangle iconRect;
    private final Rectangle textRect;

    public static LinkViewButtonUI createUI(JButton b) {
//         b.setForeground(Color.BLUE);
//         b.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
//         b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return LINKVIEW_BUTTON_UI;
    }
    protected BasicLinkViewButtonUI() {
        super();
        size = new Dimension();
        viewRect = new Rectangle();
        iconRect = new Rectangle();
        textRect = new Rectangle();
    }
    @Override public synchronized void paint(Graphics g, JComponent c) {
        if (!(c instanceof AbstractButton)) {
            return;
        }
        AbstractButton b = (AbstractButton) c;
        Font f = c.getFont();
        g.setFont(f);
        FontMetrics fm = c.getFontMetrics(f);

        Insets i = c.getInsets();
        b.getSize(size);
        viewRect.x = i.left;
        viewRect.y = i.top;
        viewRect.width = size.width - i.right - viewRect.x;
        viewRect.height = size.height - i.bottom - viewRect.y;
        iconRect.setBounds(0, 0, 0, 0); //.x = iconRect.y = iconRect.width = iconRect.height = 0;
        textRect.setBounds(0, 0, 0, 0); //.x = textRect.y = textRect.width = textRect.height = 0;

        String text = SwingUtilities.layoutCompoundLabel(
            c, fm, b.getText(), null, //altIcon != null ? altIcon : getDefaultIcon(),
            b.getVerticalAlignment(), b.getHorizontalAlignment(),
            b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
            viewRect, iconRect, textRect,
            0); //b.getText() == null ? 0 : b.getIconTextGap());

        if (c.isOpaque()) {
            g.setColor(b.getBackground());
            g.fillRect(0, 0, size.width, size.height);
        }

        ButtonModel model = b.getModel();
        if (!model.isSelected() && !model.isPressed() && !model.isArmed() && b.isRolloverEnabled() && model.isRollover()) {
            g.setColor(Color.BLUE);
            g.drawLine(viewRect.x,                viewRect.y + viewRect.height,
                       viewRect.x + viewRect.width, viewRect.y + viewRect.height);
        }
        View v = (View) c.getClientProperty(BasicHTML.propertyKey);
        if (Objects.nonNull(v)) {
            v.paint(g, textRect);
        } else {
            paintText(g, b, textRect, text);
        }
    }
}
