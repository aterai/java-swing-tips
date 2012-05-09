package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.plaf.basic.*;

public class MainPanel extends JPanel {
    private final TestModel model = new TestModel();
    private final JTable table = new JTable(model);
    public MainPanel() {
        super(new BorderLayout());

        UIManager.put("ComboBox.buttonDarkShadow", UIManager.getColor("TextField.foreground"));
        JComboBox combo = makeComboBox();

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setMinWidth(60);
        col.setMaxWidth(60);
        col.setResizable(false);

        col = table.getColumnModel().getColumn(1);
        col.setCellEditor(new DefaultCellEditor(combo));
        //table.setDefaultEditor(JComboBox.class, new DefaultCellEditor(combo));

        model.addTest(new Test("Name 1", "comment..."));
        model.addTest(new Test("Name 2", "Test"));
        model.addTest(new Test("Name d", ""));
        model.addTest(new Test("Name c", "Test cc"));
        model.addTest(new Test("Name b", "Test bb"));
        model.addTest(new Test("Name a", ""));
        model.addTest(new Test("Name 0", "Test aa"));

        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }

    @SuppressWarnings("unchecked")
    private static JComboBox makeComboBox() {
        JComboBox combo = new JComboBox(new String[] {"Name 0", "Name 1", "Name 2"}) {
            @Override public void updateUI() {
                super.updateUI();
                setBorder(BorderFactory.createEmptyBorder());
                setUI(new BasicComboBoxUI() {
                    @Override protected JButton createArrowButton() {
                        JButton button = super.createArrowButton();
                        button.setContentAreaFilled(false);
                        button.setBorder(BorderFactory.createEmptyBorder());
                        return button;
                    }
                });
//                 JTextField editor = (JTextField) getEditor().getEditorComponent();
//                 editor.setBorder(BorderFactory.createEmptyBorder());
//                 editor.setOpaque(true);
//                 editor.setEditable(false);
            }
        };
        //combo.setBorder(BorderFactory.createEmptyBorder());
        //((JTextField)combo.getEditor().getEditorComponent()).setBorder(null);
        //((JTextField)combo.getEditor().getEditorComponent()).setMargin(null);
        //combo.setBackground(Color.WHITE);
        //combo.setOpaque(true);
        //combo.setEditable(true);
        return combo;
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
