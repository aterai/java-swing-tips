package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.text.html.*;

public final class MainPanel extends JPanel {
    public MainPanel() {
        super(new GridLayout(2, 1));
        add(new JScrollPane(makeEditorPane(true)));
        add(new JScrollPane(makeEditorPane(false)));
        setPreferredSize(new Dimension(320, 240));
    }

    private JComponent makeEditorPane(boolean isDefault) {
        JEditorPane pane = new JEditorPane();
        pane.setContentType("text/html");
        pane.setEditable(false);
        String bullet;
        if(isDefault) {
            bullet = "Default";
        }else{
            bullet = "bullet.png";
            HTMLEditorKit htmlEditorKit = (HTMLEditorKit)pane.getEditorKit();
            StyleSheet styleSheet = htmlEditorKit.getStyleSheet();
            String u = getClass().getResource(bullet).toString();
            styleSheet.addRule(String.format("ul{list-style-image:url(%s);margin:0px 20px;}", u));

            //styleSheet.addRule("ul{list-style-type:circle;margin:0px 20px;}");
            //styleSheet.addRule("ul{list-style-type:disc;margin:0px 20px;}");
            //styleSheet.addRule("ul{list-style-type:square;margin:0px 20px;}");
            //styleSheet.addRule("ul{list-style-type:decimal;margin:0px 20px;}");

            //Pseudo element is not supported in javax.swing.text.html.CSS
            //styleSheet.addRule("ul{list-style-type:none;margin:0px 20px;}");
            //styleSheet.addRule("ul li:before{content: "\u00BB";}");
        }
        pane.setText("<html><h2>H2</h2>text<ul><li>list: "+bullet+"</li></ul></html>");
        return pane;
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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
