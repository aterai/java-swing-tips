package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        JPanel p = new JPanel(new GridLayout(4,1,5,5));
        setBorder(BorderFactory.createEmptyBorder(5,20,5,20));
        p.add(new JLabel("Default JComboBox"));
        p.add(new JComboBox<String>(new String[] {"Google", "Yahoo!", "Bing"}));
        p.add(new JLabel("SearchBar JComboBox"));

        DefaultComboBoxModel<SearchEngine> model = new DefaultComboBoxModel<SearchEngine>() {
            @Override public void setSelectedItem(Object anObject) {
                //System.out.println("model: "+anObject);
            }
            //@Override public DefaultComboBoxModel getSelectedItem() {
            //    return null;
            //}
        };
        model.addElement(new SearchEngine("Google", "http://www.google.com/", new ImageIcon(getClass().getResource("google.png"))));
        model.addElement(new SearchEngine("Yahoo!", "http://www.yahoo.com/",  new ImageIcon(getClass().getResource("yahoo.png"))));
        model.addElement(new SearchEngine("Bing",   "http://www.bing.com/",   new ImageIcon(getClass().getResource("bing.png"))));

        JComboBox<SearchEngine> combo = new JSearchBar(model);
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

        p.add(combo);
        //p.add(new SearchBarComboBox(makeModel()));
        add(p, BorderLayout.NORTH);
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
            UIManager.put("example.SearchBarComboBox", "SearchBarComboBoxUI");
            UIManager.put("SearchBarComboBoxUI", "example.BasicSearchBarComboBoxUI");
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

class SearchEngine {
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

class JSearchBar extends JComboBox<SearchEngine> {
    private static final String UI_CLASS_ID = "SearchBarComboBoxUI";
    @Override public String getUIClassID() {
        return UI_CLASS_ID;
    }
    @Override public SearchBarComboBoxUI getUI() {
        return (SearchBarComboBoxUI)ui;
    }
//     @Override public void setUI(SearchBarComboBoxUI newUI) {
//         super.setUI(newUI);
//     }
    @Override public void updateUI() {
        super.updateUI();
        if(UIManager.get(getUIClassID())==null) {
            setUI(new BasicSearchBarComboBoxUI());
        }else{
            setUI((SearchBarComboBoxUI)UIManager.getUI(this));
        }
        UIManager.put("ComboBox.font", getFont()); //XXX: ???
        JButton arrowButton = (JButton)getComponent(0);
        SearchEngine se = getItemAt(0);
        if(se!=null) {
            arrowButton.setIcon(se.favicon);
        }
//         ListCellRenderer renderer = getRenderer();
//         if(renderer instanceof Component) {
//             SwingUtilities.updateComponentTreeUI((Component)renderer);
//         }
    }
//     public JSearchBar() {
//         super();
//         setModel(new DefaultComboBoxModel<SearchEngine>());
//         init();
//     }
    public JSearchBar(ComboBoxModel<SearchEngine> aModel) {
        super(aModel);
        //setModel(aModel);
        //init();
    }
    public JSearchBar(SearchEngine[] items) {
        super(items);
        //setModel(new DefaultComboBoxModel<SearchEngine>(items));
        //init();
    }
//     public JSearchBar(Vector<?> items) {
//         super();
//         setModel(new DefaultComboBoxModel(items));
//         init();
//     }
//     private void init() {
//         installAncestorListener();
//         //setUIProperty("opaque", true);
//         updateUI();
//     }
    @Override protected void processFocusEvent(java.awt.event.FocusEvent e) {
        System.out.println("processFocusEvent");
    }
}
