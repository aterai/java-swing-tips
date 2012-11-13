package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class MainPanel extends JPanel {
    private static final String INFO =
        " Start editing: Double-Click, Enter-Key\n"+
        " Commit rename: field-focusLost, Enter-Key\n"+
        "Cancel editing: Esc-Key, title.isEmpty\n";
    private final JTabbedPane tabbedPane = new JTabbedPane();
    public MainPanel() {
        super(new BorderLayout());
        TabTitleEditListener l = new TabTitleEditListener(tabbedPane);
        tabbedPane.addChangeListener(l);
        tabbedPane.addMouseListener(l);
        tabbedPane.addTab("Shortcuts", new JTextArea(INFO));
        tabbedPane.addTab("badfasdfa", new JLabel("bbbbbbbbbbbafasdf"));
        tabbedPane.addTab("cccc",      new JScrollPane(new JTree()));
        tabbedPane.addTab("dddddddd",  new JLabel("dadfasdfasd"));
        add(tabbedPane);
        setPreferredSize(new Dimension(320, 240));
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
        frame.setMinimumSize(new Dimension(256, 100));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class TabTitleEditListener extends MouseAdapter implements ChangeListener{
    private final JTextField editor = new JTextField();
    private final JTabbedPane tabbedPane;
    public TabTitleEditListener(final JTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
        editor.setBorder(BorderFactory.createEmptyBorder());
        editor.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                renameTabTitle();
            }
        });
        editor.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_ENTER) {
                    renameTabTitle();
                }else if(e.getKeyCode()==KeyEvent.VK_ESCAPE) {
                    cancelEditing();
                }else{
                    editor.setPreferredSize(editor.getText().length()>len ? null : dim);
                    tabbedPane.revalidate();
                }
            }
        });
        tabbedPane.getInputMap(JComponent.WHEN_FOCUSED).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "start-editing");
        tabbedPane.getActionMap().put("start-editing", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                startEditing();
            }
        });
    }
    @Override public void stateChanged(ChangeEvent e) {
        renameTabTitle();
    }
    @Override public void mouseClicked(MouseEvent me) {
        Rectangle rect = tabbedPane.getUI().getTabBounds(tabbedPane, tabbedPane.getSelectedIndex());
        if(rect!=null && rect.contains(me.getPoint()) && me.getClickCount()==2) {
            startEditing();
        }else{
            renameTabTitle();
        }
    }
    private int editing_idx = -1;
    private int len = -1;
    private Dimension dim;
    private Component tabComponent = null;
    private void startEditing() {
        editing_idx = tabbedPane.getSelectedIndex();
        tabComponent = tabbedPane.getTabComponentAt(editing_idx);
        tabbedPane.setTabComponentAt(editing_idx, editor);
        editor.setVisible(true);
        editor.setText(tabbedPane.getTitleAt(editing_idx));
        editor.selectAll();
        editor.requestFocusInWindow();
        len = editor.getText().length();
        dim = editor.getPreferredSize();
        editor.setMinimumSize(dim);
    }
    private void cancelEditing() {
        if(editing_idx>=0) {
            tabbedPane.setTabComponentAt(editing_idx, tabComponent);
            editor.setVisible(false);
            editing_idx = -1;
            len = -1;
            tabComponent = null;
            editor.setPreferredSize(null);
            tabbedPane.requestFocusInWindow();
        }
    }
    private void renameTabTitle() {
        String title = editor.getText().trim();
        if(editing_idx>=0 && !title.isEmpty()) {
            tabbedPane.setTitleAt(editing_idx, title);
        }
        cancelEditing();
    }
}
