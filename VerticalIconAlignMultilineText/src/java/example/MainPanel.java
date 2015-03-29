package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.plaf.basic.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import com.sun.java.swing.plaf.windows.WindowsCheckBoxUI;

public final class MainPanel extends JPanel {
    private static final String TEXT = "<html>The vertical alignment of this text gets offset when the font changes.";
    private final JCheckBox check1 = new JCheckBox(TEXT);
    private final JCheckBox check2 = new JCheckBox(TEXT) {
        @Override public void updateUI() {
            super.updateUI();
            if (getUI() instanceof WindowsCheckBoxUI) {
                setUI(new WindowsVerticalAlignmentCheckBoxUI());
            } else {
                setUI(new BasicVerticalAlignmentCheckBoxUI());
            }
            setVerticalTextPosition(SwingConstants.TOP);
        }
    };
    private final List<? extends JComponent> list = Arrays.asList(check1, check2);
    private final Font font0 = check1.getFont();
    private final Font font1 = font0.deriveFont(20f);
    private final JToggleButton button = new JToggleButton(new AbstractAction("setFont: 24pt") {
        @Override public void actionPerformed(ActionEvent e) {
            boolean flag = button.isSelected();
            for (JComponent c: list) {
               c.setFont(flag ? font1 : font0);
            }
        }
    });
    private MainPanel() {
        super(new BorderLayout());
        check1.setVerticalTextPosition(SwingConstants.TOP);
        JPanel p = new JPanel(new GridLayout(1, 2, 2, 2));
        p.add(makeTitledPanel(check1, "SwingConstants.TOP"));
        p.add(makeTitledPanel(check2, "First line center"));
        add(p);
        add(button, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static JComponent makeTitledPanel(JComponent c, String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(c);
        p.setBorder(BorderFactory.createTitledBorder(title));
        return p;
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

final class HtmlViewUtil {
    private HtmlViewUtil() { /* Singleton */ }
    public static int getFirstLineCenterY(String text, AbstractButton c, Rectangle iconRect) {
        int y = 0;
        if (Objects.nonNull(text) && c.getVerticalTextPosition() == SwingConstants.TOP) {
            View v = (View) c.getClientProperty(BasicHTML.propertyKey);
            if (Objects.nonNull(v)) {
                try {
                    Element e = v.getElement().getElement(0);
                    Shape s = new Rectangle();
                    Position.Bias b = Position.Bias.Forward;
                    s = v.modelToView(e.getStartOffset(), b, e.getEndOffset(), b, s);
                    //System.out.println("v.h: " + s.getBounds());
                    y = (int) (.5 + Math.abs(s.getBounds().height - iconRect.height) * .5);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return y;
    }
}

class WindowsVerticalAlignmentCheckBoxUI extends WindowsCheckBoxUI {
    private Dimension size;
    private final Rectangle viewRect = new Rectangle();
    private final Rectangle iconRect = new Rectangle();
    private final Rectangle textRect = new Rectangle();
    @Override public synchronized void paint(Graphics g, JComponent c) {
        if (!(c instanceof AbstractButton)) {
            return;
        }
        AbstractButton b = (AbstractButton) c;

        Font f = c.getFont();
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics(f);

        Insets i = c.getInsets();
        size = b.getSize(size);
        viewRect.x = i.left;
        viewRect.y = i.top;
        viewRect.width = size.width - i.right - viewRect.x;
        viewRect.height = size.height - i.bottom - viewRect.y;
        iconRect.setBounds(0, 0, 0, 0); //iconRect.x = iconRect.y = iconRect.width = iconRect.height = 0;
        textRect.setBounds(0, 0, 0, 0); //textRect.x = textRect.y = textRect.width = textRect.height = 0;

        String text = SwingUtilities.layoutCompoundLabel(
            c, fm, b.getText(), getDefaultIcon(),
            b.getVerticalAlignment(), b.getHorizontalAlignment(),
            b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
            viewRect, iconRect, textRect,
            Objects.nonNull(b.getText()) ? b.getIconTextGap() : 0);

//         // fill background
//         if (c.isOpaque()) {
//             g.setColor(b.getBackground());
//             g.fillRect(0, 0, size.width, size.height);
//         }

        // Paint the radio button
        int y = HtmlViewUtil.getFirstLineCenterY(text, b, iconRect);
        getDefaultIcon().paintIcon(c, g, iconRect.x, iconRect.y + y);

        // Draw the Text
        if (Objects.nonNull(text)) {
            View v = (View) c.getClientProperty(BasicHTML.propertyKey);
            if (Objects.nonNull(v)) {
                v.paint(g, textRect);
            } else {
                paintText(g, b, textRect, text);
            }
            if (b.hasFocus() && b.isFocusPainted()) {
                paintFocus(g, textRect, size);
            }
        }
    }
    @Override protected void paintFocus(Graphics g, Rectangle textRect, Dimension size) {
        if (textRect.width > 0 && textRect.height > 0) {
            super.paintFocus(g, textRect, size);
        }
    }
}

class BasicVerticalAlignmentCheckBoxUI extends BasicCheckBoxUI {
    private Dimension size;
    private final Rectangle viewRect = new Rectangle();
    private final Rectangle iconRect = new Rectangle();
    private final Rectangle textRect = new Rectangle();
    @Override public synchronized void paint(Graphics g, JComponent c) {
        if (!(c instanceof AbstractButton)) {
            return;
        }
        AbstractButton b = (AbstractButton) c;

        Font f = c.getFont();
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics(f);

        Insets i = c.getInsets();
        size = b.getSize(size);
        viewRect.x = i.left;
        viewRect.y = i.top;
        viewRect.width = size.width - i.right - viewRect.x;
        viewRect.height = size.height - i.bottom - viewRect.y;
        iconRect.setBounds(0, 0, 0, 0); //iconRect.x = iconRect.y = iconRect.width = iconRect.height = 0;
        textRect.setBounds(0, 0, 0, 0); //textRect.x = textRect.y = textRect.width = textRect.height = 0;

        String text = SwingUtilities.layoutCompoundLabel(
            c, fm, b.getText(), getDefaultIcon(),
            b.getVerticalAlignment(), b.getHorizontalAlignment(),
            b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
            viewRect, iconRect, textRect,
            Objects.nonNull(b.getText()) ? b.getIconTextGap() : 0);

//         // fill background
//         if (c.isOpaque()) {
//             g.setColor(b.getBackground());
//             g.fillRect(0, 0, size.width, size.height);
//         }

        // Paint the radio button
        int y = HtmlViewUtil.getFirstLineCenterY(text, b, iconRect);
        getDefaultIcon().paintIcon(c, g, iconRect.x, iconRect.y + y);

        // Draw the Text
        if (text != null) {
            View v = (View) c.getClientProperty(BasicHTML.propertyKey);
            if (Objects.nonNull(v)) {
                v.paint(g, textRect);
            } else {
                paintText(g, b, textRect, text);
            }
            if (b.hasFocus() && b.isFocusPainted()) {
                paintFocus(g, textRect, size);
            }
        }
    }
    @Override protected void paintFocus(Graphics g, Rectangle textRect, Dimension size) {
        if (textRect.width > 0 && textRect.height > 0) {
            super.paintFocus(g, textRect, size);
        }
    }
}
