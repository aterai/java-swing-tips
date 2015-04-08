package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

public final class MainPanel extends JPanel {
    private static final String LINK = "http://ateraimemo.com/";
    private static final String HTML_TEXT = "<html><body>"
                                          + "html tag: <br /><a href='" + LINK + "'>" + LINK + "</a>"
                                          + "</body></html>";

    private MainPanel() {
        super(new BorderLayout());
        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        sp.setResizeWeight(.5);
        sp.setTopComponent(new JScrollPane(makeEditorPane(false)));
        sp.setBottomComponent(new JScrollPane(makeEditorPane(true)));
        add(sp);
        setPreferredSize(new Dimension(320, 240));
    }
    private static JEditorPane makeEditorPane(boolean editable) {
        final JEditorPane editorPane = new JEditorPane();
        editorPane.setEditable(editable);
        editorPane.setContentType("text/html");
        editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        editorPane.setText(HTML_TEXT);
        editorPane.addHyperlinkListener(new HyperlinkListener() {
            private String tooltip;
            @Override public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    JOptionPane.showMessageDialog(editorPane, "You click the link with the URL " + e.getURL());
                } else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                    tooltip = editorPane.getToolTipText();
                    URL url = e.getURL();
                    editorPane.setToolTipText(Objects.nonNull(url) ? url.toExternalForm() : null);
                } else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
                    editorPane.setToolTipText(tooltip);
                }
            }
        });

        HTMLDocument doc = (HTMLDocument) editorPane.getDocument();
        Style s = doc.addStyle("button", null);
        StyleConstants.setAlignment(s, StyleConstants.ALIGN_CENTER);
        HyperlinkButton button = new HyperlinkButton(new AbstractAction(LINK) {
            @Override public void actionPerformed(ActionEvent e) {
                AbstractButton b = (AbstractButton) e.getSource();
                editorPane.setBackground(b.isSelected() ? Color.RED : Color.WHITE);
                JOptionPane.showMessageDialog(editorPane, "You click the link with the URL " + LINK);
            }
        });
        button.setToolTipText("button: " + LINK);
        button.setOpaque(false);
        StyleConstants.setComponent(s, button);
        try {
            doc.insertString(doc.getLength(), "\n----\nJButton:\n", null);
            doc.insertString(doc.getLength(), LINK + "\n", doc.getStyle("button"));
            //doc.insertString(doc.getLength(), "\n", null);
        } catch (BadLocationException ble) {
            ble.printStackTrace();
        }
        return editorPane;
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

// class URILabel extends JLabel {
//     private final String href;
//     public URILabel(String str) {
//         super("<html><a href='" + str + "'>" + str + "</a>");
//         href = str;
//         setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//         addMouseListener(new MouseAdapter() {
//             @Override public void mousePressed(MouseEvent e) {
//                 Toolkit.getDefaultToolkit().beep();
//             }
//         });
//     }
// }

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
    public BasicLinkViewButtonUI() {
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
