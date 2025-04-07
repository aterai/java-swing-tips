// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JEditorPane editor = new JEditorPane() {
      @Override public void updateUI() {
        super.updateUI();
        ThemeUtils.updateTheme(this);
        loadHtml(this);
        setEditable(false);
        setSelectedTextColor(null);
        setSelectionColor(new Color(0x64_88_AA_AA, true));
      }
    };

    JRadioButtonMenuItem sys = new JRadioButtonMenuItem("System", true);
    sys.addActionListener(e -> {
      ThemeUtils.updateTheme(editor);
      loadHtml(editor);
    });
    JRadioButtonMenuItem light = new JRadioButtonMenuItem("Light");
    light.addActionListener(e -> {
      ThemeUtils.updateTheme(editor, false);
      loadHtml(editor);
    });
    JRadioButtonMenuItem dark = new JRadioButtonMenuItem("Dark");
    dark.addActionListener(e -> {
      ThemeUtils.updateTheme(editor, true);
      loadHtml(editor);
    });
    ButtonGroup group = new ButtonGroup();
    group.add(sys);
    group.add(light);
    group.add(dark);

    JMenu theme = new JMenu("Theme");
    theme.add(sys);
    theme.add(light);
    theme.add(dark);

    String key = "gnome.Net/ThemeName";
    Toolkit.getDefaultToolkit().addPropertyChangeListener(key, e -> {
      if (sys.isSelected()) {
        boolean isDark = Objects.toString(e.getNewValue()).contains("dark");
        ThemeUtils.updateTheme(editor, isDark);
        loadHtml(editor);
      }
    });

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    mb.add(theme);
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    add(new JScrollPane(editor));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static void loadHtml(JEditorPane editor) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Optional.ofNullable(cl.getResource("example/test.html"))
        .ifPresent(url -> {
          try {
            editor.setPage(url);
          } catch (IOException ex) {
            UIManager.getLookAndFeel().provideErrorFeedback(editor);
            editor.setText(ex.getMessage());
          }
        });
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
      return;
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = makeButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton makeButton(UIManager.LookAndFeelInfo info) {
    boolean selected = info.getClassName().equals(lookAndFeel);
    return new JRadioButtonMenuItem(info.getName(), selected);
  }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        Logger.getGlobal().severe(ex::getMessage);
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}

final class ThemeUtils {
  private ThemeUtils() {
    /* Singleton */
  }

  public static void updateTheme(JEditorPane editor) {
    updateTheme(editor, isDarkMode());
  }

  public static void updateTheme(JEditorPane editor, boolean isDark) {
    EditorKit kit = editor.getEditorKit();
    HTMLEditorKit htmlEditorKit;
    if (kit instanceof HTMLEditorKit) {
      htmlEditorKit = (HTMLEditorKit) kit;
    } else {
      htmlEditorKit = new HTMLEditorKit();
    }
    if (isDark) {
      htmlEditorKit.setStyleSheet(makeDarkStyleSheet());
      editor.setBackground(new Color(0x1E_1F_22));
    } else {
      htmlEditorKit.setStyleSheet(makeLightStyleSheet());
      editor.setBackground(new Color(0xEE_EE_EE));
    }
    editor.setEditorKit(htmlEditorKit);
  }

  public static boolean isDarkMode() {
    boolean isDark;
    String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
    if (os.contains("windows")) {
      isDark = isWindowsDarkMode();
    } else if (os.contains("linux")) {
      isDark = isLinuxDarkMode();
    } else if (os.contains("mac")) {
      isDark = isMacDarkMode();
    } else {
      isDark = false;
    }
    return isDark;
  }

  public static String getProcessOutput(List<String> cmd) throws IOException, InterruptedException {
    ProcessBuilder builder = new ProcessBuilder(cmd);
    builder.redirectErrorStream(true);
    Process p = builder.start();
    String str;
    try (BufferedReader pr = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
      str = pr.lines().collect(Collectors.joining(System.lineSeparator()));
    }
    return str;
  }

  public static boolean isWindowsDarkMode() {
    List<String> cmd = Arrays.asList(
        "powershell.exe",
        "Get-ItemPropertyValue",
        "-Path",
        "HKCU:\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
        "-Name",
        "AppsUseLightTheme;"
    );
    boolean isDarkMode;
    try {
      isDarkMode = Objects.equals("0", getProcessOutput(cmd).trim());
    } catch (IOException | InterruptedException ex) {
      Logger.getGlobal().severe(ex::getMessage);
      isDarkMode = false;
    }
    return isDarkMode;
  }

  public static boolean isLinuxDarkMode() {
    Toolkit tk = Toolkit.getDefaultToolkit();
    Object theme = tk.getDesktopProperty("gnome.Net/ThemeName");
    return Objects.toString(theme).contains("dark");
  }

  public static boolean isMacDarkMode() {
    return false;
  }

  public static StyleSheet makeLightStyleSheet() {
    StyleSheet styleSheet = new StyleSheet();
    styleSheet.addRule("pre{background:#eeeeee}");
    styleSheet.addRule(".str{color:#008800}");
    styleSheet.addRule(".kwd{color:#000088}");
    styleSheet.addRule(".com{color:#880000}");
    styleSheet.addRule(".typ{color:#660066}");
    styleSheet.addRule(".lit{color:#006666}");
    styleSheet.addRule(".pun{color:#666600}");
    styleSheet.addRule(".pln{color:#000000}");
    styleSheet.addRule(".tag{color:#000088}");
    styleSheet.addRule(".atn{color:#660066}");
    styleSheet.addRule(".atv{color:#008800}");
    styleSheet.addRule(".dec{color:#660066}");
    return styleSheet;
  }

  public static StyleSheet makeDarkStyleSheet() {
    StyleSheet styleSheet = new StyleSheet();
    styleSheet.addRule("pre{background:#1e1f22}");
    styleSheet.addRule(".str{color:#ffa0a0}");
    styleSheet.addRule(".kwd{color:#f0e68c;font-weight:bold}");
    styleSheet.addRule(".com{color:#87ceeb}");
    styleSheet.addRule(".typ{color:#98fb98}");
    styleSheet.addRule(".lit{color:#cd5c5c}");
    styleSheet.addRule(".pun{color:#ffffff}");
    styleSheet.addRule(".pln{color:#ffffff}");
    styleSheet.addRule(".tag{color:#f0e68c;font-weight:bold}");
    styleSheet.addRule(".atn{color:#bdb76b;font-weight:bold}");
    styleSheet.addRule(".atv{color:#ffa0a0}");
    styleSheet.addRule(".dec{color:#98fb98}");
    return styleSheet;
  }
}
