package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final TestModel model = new TestModel();
    private final JTable table = new JTable(model);
    private final Action selectAllAction = new AbstractAction("selectAll") {
        @Override public void actionPerformed(ActionEvent e) {
            e.setSource(table);
            table.getActionMap().get("selectAll").actionPerformed(e);
        }
    };
    private final Action copyAction = new AbstractAction("copy") {
        @Override public void actionPerformed(ActionEvent e) {
            e.setSource(table);
            table.getActionMap().get("copy").actionPerformed(e);
        }
    };
    public MainPanel(JFrame frame) {
        super(new BorderLayout());
        model.addTest(new Test("Name 1", "comment..."));
        model.addTest(new Test("Name 2", "Test"));
        model.addTest(new Test("Name d", ""));
        model.addTest(new Test("Name c", "Test cc"));
        model.addTest(new Test("Name b", "Test bb"));
        model.addTest(new Test("Name a", ""));
        model.addTest(new Test("Name 0", "Test aa"));

        frame.setJMenuBar(createMenuBar());

        JPanel p = new JPanel();
        p.add(new JButton(selectAllAction));
        p.add(new JButton(copyAction));

        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT); //Panel(new GridLayout(2,1));
        sp.setTopComponent(new JScrollPane(table));
        sp.setBottomComponent(new JScrollPane(new JTextArea()));
        sp.setResizeWeight(0.5);

        add(p, BorderLayout.NORTH);
        add(sp);
        setPreferredSize(new Dimension(320, 240));
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Edit");
        menu.setMnemonic(KeyEvent.VK_E);
        menuBar.add(menu);
        menu.add(new JMenuItem(selectAllAction));
        menu.add(new JMenuItem(copyAction));
        return menuBar;
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
        frame.getContentPane().add(new MainPanel(frame));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
