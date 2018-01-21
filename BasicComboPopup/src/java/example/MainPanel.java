package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.plaf.basic.*;
import javax.swing.text.*;

public class MainPanel extends JPanel {
    protected final JTextPane jtp = new JTextPane();
    protected final JComboBox<String> combo = new JComboBox<>(new String[] {
        "public", "protected", "private",
        "final", "transient", "super", "this", "return", "class"
    });
    protected final BasicComboPopup popup = new EditorComboPopup(jtp, combo);

    public MainPanel() {
        super(new BorderLayout());

        jtp.setText("Shift+Tab");

        ActionMap amc = popup.getActionMap();
        amc.put("myUp", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                int index = combo.getSelectedIndex();
                combo.setSelectedIndex(index == 0 ? combo.getItemCount() - 1 : index - 1);
            }
        });
        amc.put("myDown", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                int index = combo.getSelectedIndex();
                combo.setSelectedIndex(index == combo.getItemCount() - 1 ? 0 : index + 1);
            }
        });
        amc.put("myEnt", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                append((String) combo.getSelectedItem());
            }
        });

        InputMap imc = popup.getInputMap();
        imc.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "myUp");
        imc.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "myDown");
        imc.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "myEnt");

        jtp.getActionMap().put("myPop", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                try {
                    Rectangle rect = jtp.modelToView(jtp.getCaretPosition());
                    popup.show(jtp, rect.x, rect.y + rect.height);
                    EventQueue.invokeLater(() -> {
                        Container c = popup.getTopLevelAncestor();
                        if (c instanceof Window) {
                            ((Window) c).toFront();
                        }
                        popup.requestFocusInWindow();
                    });
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        });
        jtp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK), "myPop");

        add(new JScrollPane(jtp));
        setPreferredSize(new Dimension(320, 240));
    }
    protected final void append(String str) {
        popup.hide();
        try {
            Document doc = jtp.getDocument();
            doc.insertString(jtp.getCaretPosition(), str, null);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
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

class EditorComboPopup extends BasicComboPopup {
    protected final JTextComponent textArea;
    protected transient MouseListener listener;

    protected EditorComboPopup(JTextComponent textArea, JComboBox cb) {
        super(cb);
        this.textArea = textArea;
    }
    @Override protected void installListListeners() {
        super.installListListeners();
        listener = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                hide();
                String str = (String) comboBox.getSelectedItem();
                try {
                    Document doc = textArea.getDocument();
                    doc.insertString(textArea.getCaretPosition(), str, null);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        };
        if (Objects.nonNull(list)) {
            list.addMouseListener(listener);
        }
    }
    @Override public void uninstallingUI() {
        if (Objects.nonNull(listener)) {
            list.removeMouseListener(listener);
            listener = null;
        }
        super.uninstallingUI();
    }
    @Override public boolean isFocusable() {
        return true;
    }
}
