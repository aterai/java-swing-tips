package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

public class MainPanel extends JPanel{
    public MainPanel() {
        super(new BorderLayout());
        JPanel p = new JPanel(new GridLayout(4,1,5,5));
        setBorder(BorderFactory.createEmptyBorder(5,20,5,20));
        p.add(new JLabel("Default JComboBox"));
        p.add(new JComboBox(new DefaultComboBoxModel(new Object[] {"Google", "Yahoo!", "Bing"})));
        p.add(new JLabel("SearchBar JComboBox"));
        p.add(makeSearchBar());
        //p.add(new SearchBarComboBox(makeModel()));
        add(p, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 200));
    }
    private JComboBox makeSearchBar() {
        DefaultComboBoxModel model = new DefaultComboBoxModel() {
            @Override public void setSelectedItem(Object anObject) {
                //System.out.println("model: "+anObject);
            }
            //@Override public Object getSelectedItem() {
            //    return null;
            //}
        };
        model.addElement(new SearchEngine("Google", "http://www.google.com/", new ImageIcon(getClass().getResource("google.png"))));
        model.addElement(new SearchEngine("Yahoo!", "http://www.yahoo.com/",  new ImageIcon(getClass().getResource("yahoo.png"))));
        model.addElement(new SearchEngine("Bing",   "http://www.bing.com/",   new ImageIcon(getClass().getResource("bing.png"))));

        JComboBox combo = new JSearchBar(model);
        combo.getEditor().setItem("java swing");

//         final JComboBox combo = new JComboBox(model);
//         combo.setUI(new BasicSearchBarComboBoxUI());
//         EventQueue.invokeLater(new Runnable() {
//             @Override public void run() {
//                 SearchEngine se = (SearchEngine)combo.getItemAt(0);
//                 JButton arrowButton = (JButton)combo.getComponent(0);
//                 arrowButton.setIcon(se.favicon);
//                 combo.getEditor().setItem("java swing");
//             }
//         });
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
            UIManager.put("example.SearchBarComboBox", "SearchBarComboBoxUI");
            UIManager.put("SearchBarComboBoxUI", "example.BasicSearchBarComboBoxUI");
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

class SearchEngine{
    public final String name;
    public final String url;
    public final ImageIcon favicon;
    public SearchEngine(String name, String url, ImageIcon icon) {
        this.name    = name;
        this.url     = url;
        this.favicon = icon;
    }
    @Override public String toString() {
        return name;
    }
}

class JSearchBar extends JComboBox{
    private static final String uiClassID = "SearchBarComboBoxUI";
    @Override public String getUIClassID() {
        return uiClassID;
    }
    @Override public SearchBarComboBoxUI getUI() {
        return (SearchBarComboBoxUI)ui;
    }
//     public void setUI(SearchBarComboBoxUI newUI) {
//         super.setUI(newUI);
//     }
    @Override public void updateUI() {
        if(UIManager.get(getUIClassID())!=null) {
            setUI((SearchBarComboBoxUI)UIManager.getUI(this));
        }else{
            setUI(new BasicSearchBarComboBoxUI());
        }
        UIManager.put("ComboBox.font", getFont()); //XXX: ???
        JButton arrowButton = (JButton)getComponent(0);
        SearchEngine se = (SearchEngine)getItemAt(0);
        if(se!=null) arrowButton.setIcon(se.favicon);
//         ListCellRenderer renderer = getRenderer();
//         if(renderer instanceof Component) {
//             SwingUtilities.updateComponentTreeUI((Component)renderer);
//         }
    }
    public JSearchBar() {
        super();
        setModel(new DefaultComboBoxModel());
        init();
    }
    public JSearchBar(ComboBoxModel aModel) {
        super();
        setModel(aModel);
        init();
    }
    public JSearchBar(final Object[] items) {
        super();
        setModel(new DefaultComboBoxModel(items));
        init();
    }
    public JSearchBar(java.util.Vector<?> items) {
        super();
        setModel(new DefaultComboBoxModel(items));
        init();
    }
    private void init() {
        installAncestorListener();
        //setUIProperty("opaque", true);
        updateUI();
    }
    @Override protected void processFocusEvent(java.awt.event.FocusEvent e) {
        System.out.println("processFocusEvent");
    }
}
