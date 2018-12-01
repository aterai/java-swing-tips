package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.LayerUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

public class MainPanel extends JPanel {
    private static final String INIT_TXT = "Trail: Creating a GUI with JFC/Swing\n"
        + "https://docs.oracle.com/javase/tutorial/uiswing/learn/index.html\n"
        + "Lesson: Learning Swing by Example\n"
        + "This lesson explains the concepts you need to\n"
        + " use Swing components in building a user interface.\n"
        + " First we examine the simplest Swing application you can write.\n"
        + " Then we present several progressively complicated examples of creating\n"
        + " user interfaces using components in the javax.swing package.\n"
        + " We cover several Swing components, such as buttons, labels, and text areas.\n"
        + " The handling of events is also discussed,\n"
        + " as are layout management and accessibility.\n"
        + " This lesson ends with a set of questions and exercises\n"
        + " so you can test yourself on what you've learned.\n"
        + "https://docs.oracle.com/javase/tutorial/uiswing/learn/index.html\n";

    private static final Color WARNING_COLOR = new Color(255, 200, 200);
    private final transient Highlighter.HighlightPainter currentPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.ORANGE);
    private final transient Highlighter.HighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
    private final JTextArea textArea = new JTextArea();
    private final JTextField field = new JTextField("Swing");
    private final JButton prevButton = new JButton("⋀");
    private final JButton nextButton = new JButton("⋁");
    private final JCheckBox checkCase = new JCheckBox("Match case");
    private final JCheckBox checkWord = new JCheckBox("Match whole word only");
    private final PlaceholderLayerUI<JTextComponent> layerUI = new PlaceholderLayerUI<>();
    protected int current;

    public MainPanel() {
        super(new BorderLayout());

        textArea.setEditable(false);
        textArea.setText(INIT_TXT);
        prevButton.setActionCommand("prev");
        nextButton.setActionCommand("next");

        HighlightHandler handler = new HighlightHandler();
        field.getDocument().addDocumentListener(handler);
        Stream.of(prevButton, nextButton, checkCase, checkWord).forEach(b -> {
            b.setFocusable(false);
            b.addActionListener(handler);
        });

        JPanel bp = new JPanel(new GridLayout(1, 2));
        bp.add(prevButton);
        bp.add(nextButton);

        JPanel cp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        cp.add(checkCase);
        cp.add(checkWord);

        JPanel sp = new JPanel(new BorderLayout(5, 5));
        sp.setBorder(BorderFactory.createTitledBorder("Search"));
        sp.add(new JLayer<>(field, layerUI));
        sp.add(bp, BorderLayout.EAST);
        sp.add(cp, BorderLayout.SOUTH);

        EventQueue.invokeLater(this::changeHighlight);

        add(sp, BorderLayout.NORTH);
        add(new JScrollPane(textArea));
        setPreferredSize(new Dimension(320, 240));
    }

    private static void scrollToCenter(JTextComponent tc, int pos) throws BadLocationException {
        Rectangle rect = tc.modelToView(pos);
        Container c = SwingUtilities.getAncestorOfClass(JViewport.class, tc);
        if (Objects.nonNull(rect) && c instanceof JViewport) {
            rect.x = (int) (rect.x - c.getWidth() * .5);
            rect.width = c.getWidth();
            rect.height = (int) (c.getHeight() * .5);
            tc.scrollRectToVisible(rect);
        }
    }

    private Optional<Pattern> getPattern() {
        String text = field.getText();
        if (Objects.isNull(text) || text.isEmpty()) {
            return Optional.empty();
        }
        String cw = checkWord.isSelected() ? "\\b" : "";
        String pattern = String.format("%s%s%s", cw, text, cw);
        int flags = checkCase.isSelected() ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        try {
            return Optional.of(Pattern.compile(pattern, flags));
        } catch (PatternSyntaxException ex) {
            field.setBackground(WARNING_COLOR);
            return Optional.empty();
        }
    }

    protected final void changeHighlight() {
        field.setBackground(Color.WHITE);
        Highlighter highlighter = textArea.getHighlighter();
        highlighter.removeAllHighlights();
        Document doc = textArea.getDocument();
        getPattern().ifPresent(pattern -> {
            try {
                Matcher matcher = pattern.matcher(doc.getText(0, doc.getLength()));
                int pos = 0;
                while (matcher.find(pos)) {
                    int start = matcher.start();
                    int end = matcher.end();
                    highlighter.addHighlight(start, end, highlightPainter);
                    pos = end;
                }
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        });
        JLabel label = layerUI.hint;
        Highlighter.Highlight[] array = highlighter.getHighlights();
        int hits = array.length;
        if (hits == 0) {
            current = -1;
            label.setOpaque(true);
        } else {
            current = (current + hits) % hits;
            label.setOpaque(false);
            Highlighter.Highlight hh = highlighter.getHighlights()[current];
            highlighter.removeHighlight(hh);
            try {
                highlighter.addHighlight(hh.getStartOffset(), hh.getEndOffset(), currentPainter);
                scrollToCenter(textArea, hh.getStartOffset());
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        }
        label.setText(String.format("%02d / %02d%n", current + 1, hits));
        field.repaint();
    }

    private class HighlightHandler implements DocumentListener, ActionListener {
        @Override public void changedUpdate(DocumentEvent e) { /* not needed */ }
        @Override public void insertUpdate(DocumentEvent e) {
            changeHighlight();
        }
        @Override public void removeUpdate(DocumentEvent e) {
            changeHighlight();
        }
        @Override public void actionPerformed(ActionEvent e) {
            Object o = e.getSource();
            if (o instanceof AbstractButton) {
                String cmd = ((AbstractButton) o).getActionCommand();
                if ("prev".equals(cmd)) {
                    current--;
                } else if ("next".equals(cmd)) {
                    current++;
                }
            }
            changeHighlight();
        }
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

class PlaceholderLayerUI<V extends JTextComponent> extends LayerUI<V> {
    public final JLabel hint = new JLabel() {
        @Override public void updateUI() {
            super.updateUI();
            setForeground(UIManager.getColor("TextField.inactiveForeground"));
            setBackground(Color.RED);
        }
    };
    @Override public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        if (c instanceof JLayer) {
            JTextComponent tc = (JTextComponent) ((JLayer<?>) c).getView();
            if (!tc.getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(hint.getForeground());
                Insets i = tc.getInsets();
                Dimension d = hint.getPreferredSize();
                int x = tc.getWidth() - i.right - d.width - 2;
                int y = (tc.getHeight() - d.height) / 2;
                SwingUtilities.paintComponent(g2, hint, tc, x, y, d.width, d.height);
                g2.dispose();
            }
        }
    }
}
