package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public final class BarFactory{
    final static private String imageSuffix  = "Image";
    final static private String labelSuffix  = "Label";
    final static private String actionSuffix = "Action";
    final static private String tipSuffix    = "Tooltip";
    final static private String mneSuffix    = "Mnemonic";

    private final ResourceBundle resources;

    private final Hashtable<String, JMenuItem> menuItems   = new Hashtable<String, JMenuItem>();
    private final Hashtable<String, JButton>   toolButtons = new Hashtable<String, JButton>();
    private final Hashtable<Object, Action>    commands    = new Hashtable<Object, Action>();
    private final Hashtable<String, JMenu>     menus       = new Hashtable<String, JMenu>();
    //private Action[] actions;

    public BarFactory(String restr) {
        ResourceBundle res;
        try{
            res = ResourceBundle.getBundle(restr, new ResourceBundle.Control() {
                //http://docs.oracle.com/javase/jp/6/api/java/util/ResourceBundle.Control.html
                @Override public List<String> getFormats(String baseName) {
                    if(baseName == null) throw new NullPointerException();
                    return Arrays.asList("properties");
                }
                @Override public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {
                    if(baseName == null || locale == null || format == null || loader == null) throw new NullPointerException();
                    ResourceBundle bundle = null;
                    if(format.equals("properties")) {
                        String bundleName = toBundleName(baseName, locale);
                        String resourceName = toResourceName(bundleName, format);
                        InputStream stream = null;
                        if(reload) {
                            URL url = loader.getResource(resourceName);
                            if(url != null) {
                                URLConnection connection = url.openConnection();
                                if(connection != null) {
                                    connection.setUseCaches(false);
                                    stream = connection.getInputStream();
                                }
                            }
                        }else{
                            stream = loader.getResourceAsStream(resourceName);
                        }
                        if(stream != null) {
                            //BufferedInputStream bis = new BufferedInputStream(stream);
                            Reader r = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                            bundle = new PropertyResourceBundle(r);
                            r.close();
                        }
                    }
                    return bundle;
                }
            });
        }catch(MissingResourceException mre) {
            mre.printStackTrace();
            System.err.println("resources/"+restr+" not found");
            res = null;
            //System.exit(1);
        }
        resources = res;
        //actions   = act;
        //initActions();
    }
    public BarFactory(ResourceBundle res_) {
        resources = res_;
        //actions   = act;
        //initActions();
    }

    public void initActions(Action[] actlist) {
        //Action[] actlist = getActions();
        for(int i=0; i<actlist.length;i++) {
            Action a = actlist[i];
            commands.put(a.getValue(Action.NAME), a);
        }
    }

    public URL getResource(String key) {
        String name = getResourceString(key);
        if(name != null) {
            URL url = this.getClass().getResource(name);
            return url;
        }
        return null;
    }

    private String getResourceString(String nm) {
        String str;
        try{
            str = resources.getString(nm);
        }catch(MissingResourceException mre) {
            str = null;
        }
        return str;
    }

    private String[] tokenize(String input) {
        ArrayList<String> v = new ArrayList<String>();
        StringTokenizer t = new StringTokenizer(input);
        String[] cmd;
        while(t.hasMoreTokens()) {
            v.add(t.nextToken());
        }
        cmd = new String[v.size()];
        for(int i=0;i<cmd.length;i++) {
            cmd[i] = v.get(i);
        }
        return cmd;
    }

    public JToolBar createToolbar() {
        String tmp = getResourceString("toolbar");
        if(tmp==null) return null;
        JToolBar toolbar = new JToolBar();
        toolbar.setRollover(true);
        toolbar.setFloatable(false);
        String[] toolKeys = tokenize(tmp);
        for(int i = 0; i < toolKeys.length; i++) {
            if(toolKeys[i].equals("-")) {
                toolbar.add(Box.createHorizontalStrut(5));
                toolbar.addSeparator();
                toolbar.add(Box.createHorizontalStrut(5));
            }else{
                toolbar.add(createTool(toolKeys[i]));
            }
        }
        toolbar.add(Box.createHorizontalGlue());
        return toolbar;
    }

    private Component createTool(String key) {
        return createToolbarButton(key);
    }

    private JButton createToolbarButton(String key) {
        URL url = getResource(key + imageSuffix);
        JButton b;
        if(url!=null) {
            b = new JButton(new ImageIcon(url)) {
                @Override public float getAlignmentY() { return 0.5f; }
            };
        }else{
            b = new JButton(getResourceString(key + labelSuffix)) {
                @Override public float getAlignmentY() { return 0.5f; }
            };
        }
        b.setFocusPainted(false);
        b.setFocusable(false);
        b.setRequestFocusEnabled(false);
        b.setMargin(new Insets(1,1,1,1));

        String astr = getResourceString(key + actionSuffix);
        if(astr == null) {
            astr = key;
        }
        Action a = getAction(astr);
        if(a != null) {
            b.setActionCommand(astr);
            b.addActionListener(a);
        }else{
            b.setEnabled(false);
        }

        String tip = getResourceString(key + tipSuffix);
        if(tip != null) {
            b.setToolTipText(tip);
        }

        toolButtons.put(key, b);
        return b;
    }

//    protected Container getToolbar() {
//        return toolbar;
//    }

    public JButton getToolButton(String key) {
        return (JButton)toolButtons.get(key);
    }

    public JMenuBar createMenubar() {
        JMenuBar mb = new JMenuBar();
        String[] menuKeys = tokenize(getResourceString("menubar"));
        for(int i=0;i<menuKeys.length;i++) {
            JMenu m = createMenu(menuKeys[i]);
            if(m != null) {
                mb.add(m);
            }
        }
        return mb;
    }

    private JMenu createMenu(String key) {
        String[] itemKeys = tokenize(getResourceString(key));
        String mitext = getResourceString(key + labelSuffix);
        JMenu menu = new JMenu(mitext);
        String mn = getResourceString(key + mneSuffix);
        if(mn!=null) {
            String tmp = mn.toUpperCase().trim();
            if(tmp.length()==1) {
                if(mitext.indexOf(tmp)<0) {
                    menu.setText(mitext+" ("+tmp+")");
                }
                //byte[] bt = tmp.getBytes();
                menu.setMnemonic((int) tmp.charAt(0));
            }
        }
        for(int i=0;i<itemKeys.length;i++) {
            if(itemKeys[i].equals("-")) {
                menu.addSeparator();
            }else{
                JMenuItem mi = createMenuItem(itemKeys[i]);
                menu.add(mi);
            }
        }
        menus.put(key, menu);
        return menu;
    }

    private JMenuItem createMenuItem(String cmd) {
        String mitext = getResourceString(cmd + labelSuffix);
        JMenuItem mi = new JMenuItem(mitext);
        URL url = getResource(cmd+imageSuffix);
        if(url!=null) {
            mi.setHorizontalTextPosition(JButton.RIGHT);
            mi.setIcon(new ImageIcon(url));
        }
        String astr = getResourceString(cmd + actionSuffix);
        if(astr == null) {
            astr = cmd;
        }
        String mn = getResourceString(cmd + mneSuffix);
        //System.out.println(mn);
        if(mn!=null) {
            String tmp = mn.toUpperCase().trim();
            if(tmp.length()==1) {
                if(mitext.indexOf(tmp)<0) {
                    mi.setText(mitext+" ("+tmp+")");
                }
                //byte[] bt = tmp.getBytes();
                mi.setMnemonic((int) tmp.charAt(0));
                //System.out.println(cmd+", "+tmp);
            }
        }
        mi.setActionCommand(astr);
        Action a = getAction(astr);
        if(a!=null) {
            mi.addActionListener(a);
        //    a.addPropertyChangeListener(createActionChangeListener(mi));
            mi.setEnabled(a.isEnabled());
        }else{
            mi.setEnabled(false);
        }
        menuItems.put(cmd, mi);
        return mi;
    }

    public JMenuItem getMenuItem(String cmd) {
        return menuItems.get(cmd);
    }

    public JMenu getMenu(String cmd) {
        return menus.get(cmd);
    }

    public Action getAction(String cmd) {
        return commands.get(cmd);
    }

//     public Action[] getActions() {
//         return actions;
//     }

//    protected JMenuBar getMenubar() {
//        return menubar;
//    }
}
