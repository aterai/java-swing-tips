package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class MainPanel extends JPanel {
    protected static final String TEXT = "javascript:(function(){var l=location,m=l.href.match('^(https?://)(.+)(api[^+]+|technotes[^+]+)');if(m)l.href=m[1]+'docs.oracle.com/javase/8/docs/'+decodeURIComponent(m[3]).replace(/\\+.*$/,'').replace(/\\[\\]/g,':A').replace(/, |\\(|\\)/g,'-');}());";
    protected final JCheckBox check = new JCheckBox("add EmptyThumbHandler");
    protected final JButton caretButton = new JButton("setCaretPosition: 0");
    protected final JButton offsetButton = new JButton("setScrollOffset: 0");
    protected final JTextField textField1 = new JTextField(TEXT);
    protected final JTextField textField2 = new JTextField(TEXT);
    protected final JScrollBar scroller1 = new JScrollBar(Adjustable.HORIZONTAL);
    protected final JScrollBar scroller2 = new JScrollBar(Adjustable.HORIZONTAL) {
        @Override public void updateUI() {
            super.updateUI();
            setUI(new ArrowButtonlessScrollBarUI());
        }
        @Override public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.height = 10;
            return d;
        }
    };
    protected final transient EmptyThumbHandler handler = new EmptyThumbHandler(textField1, scroller1);

    public MainPanel() {
        super(new BorderLayout());
        scroller1.setModel(textField1.getHorizontalVisibility());
        scroller2.setModel(textField2.getHorizontalVisibility());

        check.addActionListener(e -> {
            if (((JCheckBox) e.getSource()).isSelected()) {
                textField1.addComponentListener(handler);
                textField1.getDocument().addDocumentListener(handler);
            } else {
                textField1.removeComponentListener(handler);
                textField1.getDocument().removeDocumentListener(handler);
            }
        });

        caretButton.addActionListener(e -> {
            textField1.requestFocusInWindow();
            textField1.setCaretPosition(0);
            scroller1.revalidate();
            textField2.requestFocusInWindow();
            textField2.setCaretPosition(0);
            scroller2.revalidate();
        });

        offsetButton.addActionListener(e -> {
            textField1.setScrollOffset(0);
            scroller1.revalidate();
            textField2.setScrollOffset(0);
            scroller2.revalidate();
        });

        Box p = Box.createVerticalBox();
        JScrollPane scroll = new JScrollPane(new JTextField(TEXT));
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        p.add(new JLabel("JScrollPane + VERTICAL_SCROLLBAR_NEVER"));
        p.add(scroll);
        p.add(Box.createVerticalStrut(5));
        p.add(new JLabel("BoundedRangeModel: textField.getHorizontalVisibility()"));
        p.add(textField1);
        p.add(Box.createVerticalStrut(2));
        p.add(scroller1);
        p.add(Box.createVerticalStrut(2));
        p.add(check);
        p.add(Box.createVerticalStrut(5));
        p.add(new JLabel("BoundedRangeModel+textField.ArrowButtonlessScrollBarUI"));
        p.add(textField2);
        p.add(Box.createVerticalStrut(2));
        p.add(scroller2);
        p.add(Box.createVerticalStrut(5));

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(caretButton);
        box.add(Box.createHorizontalStrut(5));
        box.add(offsetButton);

        add(p, BorderLayout.NORTH);
        add(box, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(20, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }

    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGui();
            }
        });
    }
    public static void createAndShowGui() {
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

class EmptyThumbHandler extends ComponentAdapter implements DocumentListener {
    private final BoundedRangeModel emptyThumbModel = new DefaultBoundedRangeModel(0, 1, 0, 1);
    private final JTextField textField;
    private final JScrollBar scroller;
    protected EmptyThumbHandler(JTextField textField, JScrollBar scroller) {
        super();
        this.textField = textField;
        this.scroller = scroller;
    }
    private void changeThumbModel() {
        EventQueue.invokeLater(() -> {
            BoundedRangeModel m = textField.getHorizontalVisibility();
            int iv = m.getMaximum() - m.getMinimum() - m.getExtent() - 1; // -1: bug?
            if (iv <= 0) {
                scroller.setModel(emptyThumbModel);
            } else {
                scroller.setModel(textField.getHorizontalVisibility());
            }
        });
    }
    @Override public void componentResized(ComponentEvent e) {
        changeThumbModel();
    }
    @Override public void insertUpdate(DocumentEvent e) {
        changeThumbModel();
    }
    @Override public void removeUpdate(DocumentEvent e) {
        changeThumbModel();
    }
    @Override public void changedUpdate(DocumentEvent e) {
        changeThumbModel();
    }
}

class ZeroSizeButton extends JButton {
    private static final Dimension ZERO_SIZE = new Dimension();
    @Override public Dimension getPreferredSize() {
        return ZERO_SIZE;
    }
}

class ArrowButtonlessScrollBarUI extends BasicScrollBarUI {
    private static final Color DEFAULT_COLOR = new Color(220, 100, 100, 100);
    private static final Color DRAGGING_COLOR = new Color(200, 100, 100, 100);
    private static final Color ROLLOVER_COLOR = new Color(255, 120, 100, 100);
    @Override protected JButton createDecreaseButton(int orientation) {
        return new ZeroSizeButton();
    }
    @Override protected JButton createIncreaseButton(int orientation) {
        return new ZeroSizeButton();
    }
    @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
        // Graphics2D g2 = (Graphics2D) g.create();
        // g2.setPaint(new Color(100, 100, 100, 100));
        // g2.fillRect(r.x, r.y, r.width - 1, r.height - 1);
        // g2.dispose();
    }
    @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
        JScrollBar sb = (JScrollBar) c;
        if (!sb.isEnabled()) {
            return;
        }
        BoundedRangeModel m = sb.getModel();
        int iv = m.getMaximum() - m.getMinimum() - m.getExtent() - 1; // -1: bug?
        if (iv > 0) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color color;
            if (isDragging) {
                color = DRAGGING_COLOR;
            } else if (isThumbRollover()) {
                color = ROLLOVER_COLOR;
            } else {
                color = DEFAULT_COLOR;
            }
            g2.setPaint(color);
            g2.fillRect(r.x, r.y, r.width - 1, r.height - 1);
            g2.dispose();
        }
    }
}
