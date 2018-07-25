package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.*;

public final class BarFactory {
    private static final String IMAGE_SUFFIX = "Image";
    private static final String LABEL_SUFFIX = "Label";
    private static final String ACTION_SUFFIX = "Action";
    private static final String TIP_SUFFIX = "Tooltip";
    private static final String MNE_SUFFIX = "Mnemonic";

    private final ResourceBundle resources;

    private final Map<String, JMenuItem> menuItems = new ConcurrentHashMap<>();
    private final Map<String, JButton> toolButtons = new ConcurrentHashMap<>();
    private final Map<Object, Action> commands = new ConcurrentHashMap<>();
    private final Map<String, JMenu> menus = new ConcurrentHashMap<>();
    // private Action[] actions;

    public BarFactory(String restr) {
        ResourceBundle res;
        try {
            res = ResourceBundle.getBundle(restr, new Utf8ResourceBundleControl());
        } catch (MissingResourceException ex) {
            ex.printStackTrace();
            System.err.println("resources/" + restr + " not found");
            res = null;
            // System.exit(1);
        }
        resources = res;
        // actions = act;
        // initActions();
    }
    public BarFactory(ResourceBundle res) {
        resources = res;
        // actions = act;
        // initActions();
    }

    public void initActions(Action... actlist) {
        // Action[] actlist = getActions();
        for (Action a: actlist) {
            commands.put(a.getValue(Action.NAME), a);
        }
    }

    public Optional<URL> getResource(String key) {
        return Optional.ofNullable(getResourceString(key)).map(path -> getClass().getResource(path));
    }

    private String getResourceString(String nm) {
        String str;
        try {
            str = resources.getString(nm);
        } catch (MissingResourceException ex) {
            str = null;
        }
        return str;
    }

    private String[] tokenize(String input) {
        // List<String> v = new ArrayList<>();
        // StringTokenizer t = new StringTokenizer(input);
        // while (t.hasMoreTokens()) {
        //     v.add(t.nextToken());
        // }
        // String[] cmd = new String[v.size()];
        // for (int i = 0; i < cmd.length; i++) {
        //     cmd[i] = v.get(i);
        // }
        // return cmd;
        return input.split("\\s");
    }

    public JToolBar createToolBar() {
        String tmp = getResourceString("toolbar");
        if (Objects.isNull(tmp)) {
            return null;
        }
        JToolBar toolbar = new JToolBar();
        toolbar.setRollover(true);
        toolbar.setFloatable(false);
        for (String key: tokenize(tmp)) {
            if ("-".equals(key)) {
                toolbar.add(Box.createHorizontalStrut(5));
                toolbar.addSeparator();
                toolbar.add(Box.createHorizontalStrut(5));
            } else {
                toolbar.add(createTool(key));
            }
        }
        toolbar.add(Box.createHorizontalGlue());
        return toolbar;
    }

    private Component createTool(String key) {
        return createToolBarButton(key);
    }

    private JButton createToolBarButton(String key) {
        JButton b = getResource(key + IMAGE_SUFFIX)
            .map(url -> new JButton(new ImageIcon(url)))
            .orElseGet(() -> new JButton(getResourceString(key + LABEL_SUFFIX)));
        b.setAlignmentY(Component.CENTER_ALIGNMENT);
        b.setFocusPainted(false);
        b.setFocusable(false);
        b.setRequestFocusEnabled(false);
        b.setMargin(new Insets(1, 1, 1, 1));

        String acmd = Optional.ofNullable(getResourceString(key + ACTION_SUFFIX)).orElse(key);
        Action a = getAction(acmd);
        if (Objects.nonNull(a)) {
            b.setActionCommand(acmd);
            b.addActionListener(a);
        } else {
            b.setEnabled(false);
        }

        b.setToolTipText(getResourceString(key + TIP_SUFFIX));

        toolButtons.put(key, b);
        return b;
    }

    // protected Container getToolbar() {
    //     return toolbar;
    // }

    public JButton getToolButton(String key) {
        return (JButton) toolButtons.get(key);
    }

    public JMenuBar createMenuBar() {
        JMenuBar mb = new JMenuBar();
        for (String key: tokenize(getResourceString("menubar"))) {
            mb.add(createMenu(key));
        }
        return mb;
    }

    private JMenu createMenu(String key) {
        String mitext = getResourceString(key + LABEL_SUFFIX);
        JMenu menu = new JMenu(mitext);
        Optional.ofNullable(getResourceString(key + MNE_SUFFIX))
            .map(txt -> txt.toUpperCase(Locale.ENGLISH).trim())
            .filter(txt -> !txt.isEmpty())
            .ifPresent(txt -> {
                if (mitext.indexOf(txt) < 0) {
                    menu.setText(String.format("%s (%s)", mitext, txt));
                }
                menu.setMnemonic(txt.codePointAt(0));
            });
        for (String m: tokenize(getResourceString(key))) {
            if ("-".equals(m)) {
                menu.addSeparator();
            } else {
                menu.add(createMenuItem(m));
            }
        }
        menus.put(key, menu);
        return menu;
    }

    private JMenuItem createMenuItem(String cmd) {
        String mitext = getResourceString(cmd + LABEL_SUFFIX);
        JMenuItem mi = new JMenuItem(mitext);
        getResource(cmd + IMAGE_SUFFIX).ifPresent(url -> {
            mi.setHorizontalTextPosition(SwingConstants.RIGHT);
            mi.setIcon(new ImageIcon(url));
        });
        Optional.ofNullable(getResourceString(cmd + MNE_SUFFIX))
            .map(txt -> txt.toUpperCase(Locale.ENGLISH).trim())
            .filter(txt -> !txt.isEmpty())
            .ifPresent(txt -> {
                if (mitext.indexOf(txt) < 0) {
                    mi.setText(String.format("%s (%s)", mitext, txt));
                }
                mi.setMnemonic(txt.codePointAt(0));
            });
        String acmd = Optional.ofNullable(getResourceString(cmd + ACTION_SUFFIX)).orElse(cmd);
        mi.setActionCommand(acmd);
        Action a = getAction(acmd);
        if (Objects.nonNull(a)) {
            mi.addActionListener(a);
            // a.addPropertyChangeListener(createActionChangeListener(mi));
            mi.setEnabled(a.isEnabled());
        } else {
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

    // public Action[] getActions() {
    //     return actions;
    // }

    // protected JMenuBar getMenubar() {
    //     return menubar;
    // }
}

// https://docs.oracle.com/javase/8/docs/api/java/util/ResourceBundle.Control.html
// [JDK-8027607] (rb) Provide UTF-8 based properties resource bundles - Java Bug System
// https://bugs.openjdk.java.net/browse/JDK-8027607
class Utf8ResourceBundleControl extends ResourceBundle.Control {
    @Override public List<String> getFormats(String baseName) {
        Objects.requireNonNull(baseName, "baseName must not be null");
        return Arrays.asList("properties");
    }
    @Override public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {
        ResourceBundle bundle = null;
        if ("properties".equals(format)) {
            String bundleName = toBundleName(
                Objects.requireNonNull(baseName, "baseName must not be null"),
                Objects.requireNonNull(locale, "locale must not be null"));
            String resourceName = toResourceName(bundleName, Objects.requireNonNull(format, "format must not be null"));
            InputStream stream = null;
            ClassLoader cloader = Objects.requireNonNull(loader, "loader must not be null");
            if (reload) {
                URL url = cloader.getResource(resourceName);
                if (Objects.nonNull(url)) {
                    URLConnection connection = url.openConnection();
                    if (Objects.nonNull(connection)) {
                        connection.setUseCaches(false);
                        stream = connection.getInputStream();
                    }
                }
            } else {
                stream = cloader.getResourceAsStream(resourceName);
            }
            if (Objects.nonNull(stream)) {
                // BufferedInputStream bis = new BufferedInputStream(stream);
                try (Reader r = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                    bundle = new PropertyResourceBundle(r);
                }
            }
        }
        return bundle;
    }
}
