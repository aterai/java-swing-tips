// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Optional;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
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

  public BarFactory(String base) {
    resources = ResourceBundle.getBundle(base, new Utf8ResourceBundleControl());
    // Java 9
    // resources = ResourceBundle.getBundle(base);
    // initActions();
  }

  // public BarFactory(ResourceBundle res) {
  //   resources = res;
  //   // initActions();
  // }

  public void initActions(Action... actions) {
    // Action[] actions = getActions();
    for (Action a : actions) {
      commands.put(a.getValue(Action.NAME), a);
    }
  }

  public Optional<URL> getResource(String key) {
    return Optional.ofNullable(getResourceString(key)).map(path -> getClass().getResource(path));
  }

  private String getResourceString(String nm) {
    Optional<String> op;
    try {
      op = Optional.ofNullable(resources).map(r -> r.getString(nm));
    } catch (MissingResourceException ex) {
      op = Optional.empty();
    }
    return op.orElse(null);
  }

  private String[] tokenize(String input) {
    // List<String> v = new ArrayList<>();
    // StringTokenizer t = new StringTokenizer(input);
    // while (t.hasMoreTokens()) {
    //   v.add(t.nextToken());
    // }
    // String[] cmd = new String[v.size()];
    // for (int i = 0; i < cmd.length; i++) {
    //   cmd[i] = v.get(i);
    // }
    // return cmd;
    return input == null ? new String[0] : input.split("\\s");
  }

  public JToolBar createToolBar() {
    JToolBar toolBar = null;
    String tmp = getResourceString("toolbar");
    if (Objects.nonNull(tmp)) {
      toolBar = new JToolBar();
      toolBar.setRollover(true);
      toolBar.setFloatable(false);
      for (String key : tokenize(tmp)) {
        if (Objects.equals("-", key)) {
          toolBar.add(Box.createHorizontalStrut(5));
          toolBar.addSeparator();
          toolBar.add(Box.createHorizontalStrut(5));
        } else {
          toolBar.add(createTool(key));
        }
      }
      toolBar.add(Box.createHorizontalGlue());
    }
    return toolBar;
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

    String cmd = Optional.ofNullable(getResourceString(key + ACTION_SUFFIX)).orElse(key);
    Action a = getAction(cmd);
    if (Objects.nonNull(a)) {
      b.setActionCommand(cmd);
      b.addActionListener(a);
    } else {
      b.setEnabled(false);
    }

    b.setToolTipText(getResourceString(key + TIP_SUFFIX));

    toolButtons.put(key, b);
    return b;
  }

  // protected Container getToolbar() {
  //   return toolBar;
  // }

  public JButton getToolButton(String key) {
    return toolButtons.get(key);
  }

  public JMenuBar createMenuBar() {
    JMenuBar mb = new JMenuBar();
    for (String key : tokenize(getResourceString("menubar"))) {
      mb.add(createMenu(key));
    }
    return mb;
  }

  private JMenu createMenu(String key) {
    String miText = getResourceString(key + LABEL_SUFFIX);
    JMenu menu = new JMenu(miText);
    Optional.ofNullable(getResourceString(key + MNE_SUFFIX))
        .map(txt -> txt.toUpperCase(Locale.ENGLISH).trim())
        .filter(txt -> !txt.isEmpty())
        .ifPresent(txt -> {
          if (!miText.contains(txt)) {
            menu.setText(String.format("%s (%s)", miText, txt));
          }
          menu.setMnemonic(txt.codePointAt(0));
        });
    for (String m : tokenize(getResourceString(key))) {
      if (Objects.equals("-", m)) {
        menu.addSeparator();
      } else {
        menu.add(createMenuItem(m));
      }
    }
    menus.put(key, menu);
    return menu;
  }

  private JMenuItem createMenuItem(String cmd) {
    String miText = getResourceString(cmd + LABEL_SUFFIX);
    JMenuItem mi = new JMenuItem(miText);
    getResource(cmd + IMAGE_SUFFIX).ifPresent(url -> {
      mi.setHorizontalTextPosition(SwingConstants.RIGHT);
      mi.setIcon(new ImageIcon(url));
    });
    Optional.ofNullable(getResourceString(cmd + MNE_SUFFIX))
        .map(txt -> txt.toUpperCase(Locale.ENGLISH).trim())
        .filter(txt -> !txt.isEmpty())
        .ifPresent(txt -> {
          if (!miText.contains(txt)) {
            mi.setText(String.format("%s (%s)", miText, txt));
          }
          mi.setMnemonic(txt.codePointAt(0));
        });
    String actCmd = Optional.ofNullable(getResourceString(cmd + ACTION_SUFFIX)).orElse(cmd);
    mi.setActionCommand(actCmd);
    Action a = getAction(actCmd);
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
  //   return actions;
  // }

  // protected JMenuBar getMenubar() {
  //   return menubar;
  // }
}

// https://docs.oracle.com/javase/8/docs/api/java/util/ResourceBundle.Control.html
// [JDK-8027607] (rb) Provide UTF-8 based properties resource bundles - Java Bug System
// https://bugs.openjdk.org/browse/JDK-8027607
class Utf8ResourceBundleControl extends ResourceBundle.Control {
  @Override public List<String> getFormats(String baseName) {
    Objects.requireNonNull(baseName, "baseName must not be null");
    return Collections.singletonList("properties");
  }

  @Override public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IOException {
    ResourceBundle bundle = null;
    if (Objects.equals("properties", format)) {
      String bundleName = toBundleName(
          Objects.requireNonNull(baseName, "baseName must not be null"),
          Objects.requireNonNull(locale, "locale must not be null"));
      String resourceName = toResourceName(
          bundleName,
          Objects.requireNonNull(format, "format must not be null"));
      InputStream is = null;
      ClassLoader cl = Objects.requireNonNull(loader, "loader must not be null");
      if (reload) {
        URL url = cl.getResource(resourceName);
        if (Objects.nonNull(url)) {
          URLConnection connection = url.openConnection();
          if (Objects.nonNull(connection)) {
            connection.setUseCaches(false);
            is = connection.getInputStream();
          }
        }
      } else {
        is = cl.getResourceAsStream(resourceName);
      }
      if (Objects.nonNull(is)) {
        // BufferedInputStream bis = new BufferedInputStream(is);
        try (Reader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
          bundle = new PropertyResourceBundle(r);
        }
      }
    }
    return bundle;
  }
}
