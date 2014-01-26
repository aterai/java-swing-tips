package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private static final String INFO =
        " Start editing: Double-Click, Enter-Key\n"+
        " Commit rename: field-focusLost, Enter-Key\n"+
        "Cancel editing: Esc-Key, title.isEmpty\n";
    public MainPanel(JFrame frame) {
        super(new BorderLayout());
        JTabbedPane tabbedPane = new EditableTabbedPane(frame);
        //for(int i = 0; i < 5; i++) {
        //    String title = "Tab " + i;
        //    tabbedPane.add(title, new JLabel(title));
        //    tabbedPane.setTabComponentAt(i, new ButtonTabComponent(tabbedPane));
        //}
        JTextArea a = new JTextArea(INFO);
        a.setEditable(false);
        tabbedPane.addTab("Shortcuts", new JScrollPane(a));
        tabbedPane.addTab("badfasdf",  new JLabel("bbbbbbbbbbbafasdf"));
        tabbedPane.addTab("cccc",      new JScrollPane(new JTree()));
        tabbedPane.addTab("ddddddd",   new JButton("dadfasdfasd"));
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
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setMinimumSize(new Dimension(256, 100));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel(frame));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class EditableTabbedPane extends JTabbedPane {
    private final MyGlassPane panel  = new MyGlassPane();
    private final JTextField  editor = new JTextField();
    private final JFrame      frame;
    private Rectangle rect;

    public EditableTabbedPane(JFrame _frame) {
        super();
        this.frame = _frame;
        editor.setBorder(BorderFactory.createEmptyBorder(0,3,0,3));
        editor.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(final FocusEvent e) {
                ((JTextField)e.getSource()).selectAll();
            }
        });
        editor.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_ENTER) {
                    renameTab();
                }else if(e.getKeyCode()==KeyEvent.VK_ESCAPE) {
                    cancelEditing();
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent me) {
                if(me.getClickCount()==2) {
                    startEditing();
                }
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_ENTER) {
                    startEditing();
                }
            }
        });
        panel.add(editor);
        frame.setGlassPane(panel);
        panel.setVisible(false);
    }
    private void initEditor() {
        rect = getUI().getTabBounds(this, getSelectedIndex());
        Point p = SwingUtilities.convertPoint(this, rect.getLocation(), panel);
        rect.setRect(p.x+2, p.y+2, rect.width-4, rect.height-4);
        editor.setBounds(rect);
        editor.setText(getTitleAt(getSelectedIndex()));
    }
    private void startEditing() {
        initEditor();
        panel.setVisible(true);
        editor.requestFocusInWindow();
    }
    private void cancelEditing() {
        panel.setVisible(false);
    }
    private void renameTab() {
        if(editor.getText().trim().length()>0) {
            setTitleAt(getSelectedIndex(), editor.getText());
            //java 1.6.0 ---->
            Component c = getTabComponentAt(getSelectedIndex());
            if(c!=null && c instanceof JComponent) {
                ((JComponent)c).revalidate();
            }
            //<----
        }
        panel.setVisible(false);
    }
    class MyGlassPane extends JComponent {
        public MyGlassPane() {
            super();
            setOpaque(false);
            setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
                @Override public boolean accept(Component c) { return c==editor; }
            });
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent me) {
                    if(rect==null || rect.contains(me.getPoint())) { return; }
                    renameTab();
                }
            });
            requestFocusInWindow();
        }
        @Override public void setVisible(boolean flag) {
            super.setVisible(flag);
            setFocusTraversalPolicyProvider(flag);
            setFocusCycleRoot(flag);
        }
    }
}
