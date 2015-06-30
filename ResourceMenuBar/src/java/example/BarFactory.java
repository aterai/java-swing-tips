package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import javax.swing.*;

public final class BarFactory {
    private static final String IMAGE_SUFFIX  = "Image";
    private static final String LABEL_SUFFIX  = "Label";
    private static final String ACTION_SUFFIX = "Action";
    private static final String TIP_SUFFIX    = "Tooltip";
    private static final String MNE_SUFFIX    = "Mnemonic";

    private final ResourceBundle resources;

    private final ConcurrentMap<String, JMenuItem> menuItems   = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, JButton>   toolButtons = new ConcurrentHashMap<>();
    private final ConcurrentMap<Object, Action>    commands    = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, JMenu>     menus       = new ConcurrentHashMap<>();
    //private Action[] actions;

    public BarFactory(String restr) {
        ResourceBundle res;
        try {
            res = ResourceBundle.getBundle(restr, new UTF8ResourceBundleControl());
        } catch (MissingResourceException mre) {
            mre.printStackTrace();
            System.err.println("resources/" + restr + " not found");
            res = null;
            //System.exit(1);
        }
        resources = res;
        //actions   = act;
        //initActions();
    }
    public BarFactory(ResourceBundle res) {
        resources = res;
        //actions   = act;
        //initActions();
    }

    public void initActions(Action... actlist) {
        //Action[] actlist = getActions();
        for (Action a: actlist) {
            commands.put(a.getValue(Action.NAME), a);
        }
    }

    public URL getResource(String key) {
        String name = getResourceString(key);
        if (name == null) {
            return null;
        }
        return getClass().getResource(name);
    }

    private String getResourceString(String nm) {
        String str;
        try {
            str = resources.getString(nm);
        } catch (MissingResourceException mre) {
            str = null;
        }
        return str;
    }

    private String[] tokenize(String input) {
        List<String> v = new ArrayList<>();
        StringTokenizer t = new StringTokenizer(input);
        while (t.hasMoreTokens()) {
            v.add(t.nextToken());
        }
        String[] cmd = new String[v.size()];
        for (int i = 0; i < cmd.length; i++) {
            cmd[i] = v.get(i);
        }
        return cmd;
    }

    public JToolBar createToolbar() {
        String tmp = getResourceString("toolbar");
        if (tmp == null) {
            return null;
        }
        JToolBar toolbar = new JToolBar();
        toolbar.setRollover(true);
        toolbar.setFloatable(false);
        String[] toolKeys = tokenize(tmp);
        for (int i = 0; i < toolKeys.length; i++) {
            if (toolKeys[i].equals("-")) {
                toolbar.add(Box.createHorizontalStrut(5));
                toolbar.addSeparator();
                toolbar.add(Box.createHorizontalStrut(5));
            } else {
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
        URL url = getResource(key + IMAGE_SUFFIX);
        JButton b;
        if (url == null) {
            b = new JButton(getResourceString(key + LABEL_SUFFIX));
        } else {
            b = new JButton(new ImageIcon(url));
        }
        b.setAlignmentY(Component.CENTER_ALIGNMENT);
        b.setFocusPainted(false);
        b.setFocusable(false);
        b.setRequestFocusEnabled(false);
        b.setMargin(new Insets(1, 1, 1, 1));

        String astr = getResourceString(key + ACTION_SUFFIX);
        if (astr == null) {
            astr = key;
        }
        Action a = getAction(astr);
        if (a == null) {
            b.setEnabled(false);
        } else {
            b.setActionCommand(astr);
            b.addActionListener(a);
        }

        String tip = getResourceString(key + TIP_SUFFIX);
        //if (tip != null) {
        b.setToolTipText(tip);
        //}

        toolButtons.put(key, b);
        return b;
    }

//    protected Container getToolbar() {
//        return toolbar;
//    }

    public JButton getToolButton(String key) {
        return (JButton) toolButtons.get(key);
    }

    public JMenuBar createMenubar() {
        JMenuBar mb = new JMenuBar();
        String[] menuKeys = tokenize(getResourceString("menubar"));
        for (int i = 0; i < menuKeys.length; i++) {
            JMenu m = createMenu(menuKeys[i]);
            //if (m != null)
            mb.add(m);
        }
        return mb;
    }

    private JMenu createMenu(String key) {
        String[] itemKeys = tokenize(getResourceString(key));
        String mitext = getResourceString(key + LABEL_SUFFIX);
        JMenu menu = new JMenu(mitext);
        String mn = getResourceString(key + MNE_SUFFIX);
        if (mn != null) {
            String tmp = mn.toUpperCase(Locale.ENGLISH).trim();
            if (tmp.length() == 1) {
                if (mitext.indexOf(tmp) < 0) {
                    menu.setText(mitext + " (" + tmp + ")");
                }
                //byte[] bt = tmp.getBytes();
                menu.setMnemonic((int) tmp.charAt(0));
            }
        }
        for (int i = 0; i < itemKeys.length; i++) {
            if (itemKeys[i].equals("-")) {
                menu.addSeparator();
            } else {
                JMenuItem mi = createMenuItem(itemKeys[i]);
                menu.add(mi);
            }
        }
        menus.put(key, menu);
        return menu;
    }

    private JMenuItem createMenuItem(String cmd) {
        String mitext = getResourceString(cmd + LABEL_SUFFIX);
        JMenuItem mi = new JMenuItem(mitext);
        URL url = getResource(cmd + IMAGE_SUFFIX);
        if (url != null) {
            mi.setHorizontalTextPosition(SwingConstants.RIGHT);
            mi.setIcon(new ImageIcon(url));
        }
        String astr = getResourceString(cmd + ACTION_SUFFIX);
        if (astr == null) {
            astr = cmd;
        }
        String mn = getResourceString(cmd + MNE_SUFFIX);
        //System.out.println(mn);
        if (mn != null) {
            String tmp = mn.toUpperCase(Locale.ENGLISH).trim();
            if (tmp.length() == 1) {
                if (mitext.indexOf(tmp) < 0) {
                    mi.setText(mitext + " (" + tmp + ")");
                }
                //byte[] bt = tmp.getBytes();
                mi.setMnemonic((int) tmp.charAt(0));
                //System.out.println(cmd + ", " + tmp);
            }
        }
        mi.setActionCommand(astr);
        Action a = getAction(astr);
        if (a == null) {
            mi.setEnabled(false);
        } else {
            mi.addActionListener(a);
            //a.addPropertyChangeListener(createActionChangeListener(mi));
            mi.setEnabled(a.isEnabled());
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

//http://docs.oracle.com/javase/jp/7/api/java/util/ResourceBundle.Control.html
class UTF8ResourceBundleControl extends ResourceBundle.Control {
    @Override public List<String> getFormats(String baseName) {
        Objects.requireNonNull(baseName, "baseName must not be null");
        return Arrays.asList("properties");
    }
    @Override public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {
        ResourceBundle bundle = null;
        if ("properties".equals(format)) {
            String bundleName = toBundleName(
                Objects.requireNonNull(baseName, "baseName must not be null"),
                Objects.requireNonNull(locale,   "locale must not be null"));
            String resourceName = toResourceName(bundleName, Objects.requireNonNull(format, "format must not be null"));
            InputStream stream = null;
            ClassLoader cloader = Objects.requireNonNull(loader, "loader must not be null");
            if (reload) {
                URL url = cloader.getResource(resourceName);
                if (url != null) {
                    URLConnection connection = url.openConnection();
                    if (connection != null) {
                        connection.setUseCaches(false);
                        stream = connection.getInputStream();
                    }
                }
            } else {
                stream = cloader.getResourceAsStream(resourceName);
            }
            if (stream != null) {
                //BufferedInputStream bis = new BufferedInputStream(stream);
                try (Reader r = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
                    bundle = new PropertyResourceBundle(r);
                }
            }
        }
        return bundle;
    }
}
