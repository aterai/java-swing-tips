// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.plaf.basic.BasicGraphicsUtils;

// The imports cover the whole example, which consists of several small classes
@SuppressWarnings("PMD.ExcessiveImports")
public final class MainPanel extends JPanel {
  private transient EntryLoader worker;

  private MainPanel() {
    super(new BorderLayout());
    Path home = Paths.get(System.getProperty("user.home"));
    DirectoryBreadcrumb breadcrumb = new DirectoryBreadcrumb(home);

    DefaultListModel<DirEntry> model = new DefaultListModel<>();
    JList<DirEntry> list = new JList<>(model);
    list.setCellRenderer(new DirEntryRenderer(null));
    // Fixed cell size: JList would otherwise call the renderer for every item on each layout
    list.setPrototypeCellValue(new DirEntry(home, true));
    list.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        // Ignore a double click on the empty area below the last item
        int index = list.locationToIndex(e.getPoint());
        Rectangle r = list.getCellBounds(index, index);
        if (e.getClickCount() == 2 && Objects.nonNull(r) && r.contains(e.getPoint())) {
          DirEntry entry = list.getModel().getElementAt(index);
          if (entry.isDirectory()) {
            breadcrumb.setCurrentDirectory(entry.getPath());
          }
        }
      }
    });
    breadcrumb.addPropertyChangeListener(
        DirectoryBreadcrumb.CURRENT_DIRECTORY, e -> updateList(model, (Path) e.getNewValue()));
    updateList(model, breadcrumb.getCurrentDirectory());

    add(breadcrumb, BorderLayout.NORTH);
    add(new JScrollPane(list));
    setPreferredSize(new Dimension(320, 240));
  }

  private void updateList(DefaultListModel<DirEntry> model, Path dir) {
    if (Objects.nonNull(worker)) {
      worker.cancel(true);
    }
    model.clear();
    worker = new EntryLoader(dir, model);
    worker.execute();
  }

  // The entries are created on a worker thread and published in chunks
  // because FileSystemView#getSystemIcon(File) is slow on Windows
  private static final class EntryLoader extends SwingWorker<Void, DirEntry> {
    private final Path dir;
    private final DefaultListModel<DirEntry> model;

    private EntryLoader(Path dir, DefaultListModel<DirEntry> model) {
      super();
      this.dir = dir;
      this.model = model;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    @Override protected Void doInBackground() throws IOException {
      Map<Path, BasicFileAttributes> attrs = DirEntry.listAttributes(dir);
      for (Path p : sortPaths(attrs)) {
        if (isCancelled()) {
          break;
        }
        BasicFileAttributes a = attrs.get(p);
        if (!DirEntry.isHidden(p, a)) {
          publish(new DirEntry(p, a.isDirectory()));
        }
      }
      return null;
    }

    @Override protected void process(List<DirEntry> chunks) {
      if (!isCancelled()) {
        chunks.forEach(model::addElement);
      }
    }

    @Override protected void done() {
      logError(this);
    }
  }

  // Directories first, then case-insensitive file names
  private static List<Path> sortPaths(Map<Path, BasicFileAttributes> attrs) {
    List<Path> paths = new ArrayList<>(attrs.keySet());
    paths.sort(Comparator
        .comparing((Path p) -> !attrs.get(p).isDirectory())
        .thenComparing(p -> p.getFileName().toString(), String.CASE_INSENSITIVE_ORDER));
    return paths;
  }

  private static void logError(SwingWorker<?, ?> worker) {
    try {
      if (!worker.isCancelled()) {
        worker.get();
      }
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    } catch (ExecutionException ex) {
      Logger.getGlobal().warning(ex::getMessage);
    }
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
      Logger.getGlobal().severe(ex::getMessage);
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

// Breadcrumb bar: [Icon][>][PC][>][...][>][Dir][>]...[Current][>]
// Clicking on the empty area switches to a JTextField for editing the path.
@SuppressWarnings("PMD.TooManyMethods")
class DirectoryBreadcrumb extends JPanel {
  public static final String CURRENT_DIRECTORY = "currentDirectory";
  private static final String CARD_CRUMBS = "crumbs";
  private static final String CARD_EDIT = "edit";
  private static final String ACTION_EDIT = "editAddress";
  private final CardLayout cards = new CardLayout();
  private final transient BreadcrumbLayout layout = new BreadcrumbLayout();
  private final JPanel crumbs = new JPanel(layout);
  private final JTextField field = new JTextField();
  // The root icon and the field icon show the icon of the current directory
  private final CrumbButton rootIcon = new CrumbButton(null, null);
  private final JLabel fieldIcon = new JLabel();
  private final CrumbButton placesSeparator = new CrumbButton(null, new ArrowIcon());
  private final CrumbButton rootLabel = new CrumbButton("PC", null);
  private final CrumbButton ellipsis = new CrumbButton("...", null);
  private final transient List<Path> chain = new ArrayList<>();
  private transient Path current;

  @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
  protected DirectoryBreadcrumb(Path dir) {
    super();
    setLayout(cards);
    rootIcon.addActionListener(e -> startEditing());
    placesSeparator.addPopupToggle(
        () -> DirectoryPopupMenu.ofPlaces(current, this::setCurrentDirectory));
    // The "PC" button lists the drives, like the separator next to it
    rootLabel.addPopupToggle(
        () -> DirectoryPopupMenu.ofChildren(null, current, this::setCurrentDirectory));
    ellipsis.setToolTipText("Hidden folders");
    ellipsis.addPopupToggle(() -> DirectoryPopupMenu.ofPaths(
        chain.subList(0, layout.getHiddenCount()), this::setCurrentDirectory));

    crumbs.setOpaque(false);
    crumbs.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    crumbs.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          showContextMenu(e);
        }
      }

      @Override public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          showContextMenu(e);
        }
      }

      @Override public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
          startEditing();
        }
      }
    });

    // The icon at the left end of the JTextField is aligned with the root icon
    fieldIcon.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 0));
    fieldIcon.setCursor(Cursor.getDefaultCursor());
    field.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
    field.addActionListener(e -> commitEditing());
    field.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
    field.getActionMap().put("cancel", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        stopEditing();
      }
    });
    field.addFocusListener(new FocusAdapter() {
      @Override public void focusLost(FocusEvent e) {
        if (!e.isTemporary()) {
          stopEditing();
        }
      }
    });

    JPanel editor = new JPanel(new BorderLayout());
    editor.setOpaque(false);
    editor.add(fieldIcon, BorderLayout.WEST);
    editor.add(field);

    add(crumbs, CARD_CRUMBS);
    add(editor, CARD_EDIT);
    initKeyBindings();
    setCurrentDirectory(dir);
  }

  @Override public void updateUI() {
    super.updateUI();
    setOpaque(true);
    setBorder(UIManager.getBorder("TextField.border"));
    setBackground(UIManager.getColor("TextField.background"));
  }

  @Override public final void add(Component comp, Object constraints) {
    super.add(comp, constraints);
  }

  private void initKeyBindings() {
    InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
    ActionMap am = getActionMap();
    im.put(KeyStroke.getKeyStroke("F4"), ACTION_EDIT);
    im.put(KeyStroke.getKeyStroke("alt D"), ACTION_EDIT);
    am.put(ACTION_EDIT, new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        startEditing();
      }
    });
    im.put(KeyStroke.getKeyStroke("alt UP"), "up");
    am.put("up", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        Path parent = current.getParent();
        if (Objects.nonNull(parent)) {
          setCurrentDirectory(parent);
        }
      }
    });
  }

  public Path getCurrentDirectory() {
    return current;
  }

  // Falls back to the nearest existing ancestor when the directory does not exist
  public void setCurrentDirectory(Path dir) {
    Path p = dir.toAbsolutePath().normalize();
    while (Objects.nonNull(p) && !Files.isDirectory(p)) {
      p = p.getParent();
    }
    if (Objects.isNull(p) || p.equals(current)) {
      return;
    }
    Path old = current;
    current = p;
    rebuild();
    firePropertyChange(CURRENT_DIRECTORY, old, p);
  }

  private void rebuild() {
    chain.clear();
    for (Path p = current; Objects.nonNull(p); p = p.getParent()) {
      chain.add(0, p);
    }
    Icon icon = DirEntry.getSystemIcon(current);
    rootIcon.setIcon(icon);
    fieldIcon.setIcon(icon);
    crumbs.removeAll();
    crumbs.add(rootIcon);
    crumbs.add(placesSeparator);
    crumbs.add(rootLabel);
    crumbs.add(createSeparator(null));
    crumbs.add(ellipsis);
    for (Path p : chain) {
      crumbs.add(createDirButton(p));
      crumbs.add(createSeparator(p));
    }
    crumbs.revalidate();
    crumbs.repaint();
  }

  private AbstractButton createDirButton(Path p) {
    AbstractButton b = new CrumbButton(DirEntry.getDisplayName(p), null);
    if (p.equals(current)) {
      b.setFont(b.getFont().deriveFont(Font.BOLD));
    }
    b.setToolTipText(p.toString());
    b.addActionListener(e -> setCurrentDirectory(p));
    return b;
  }

  // dir == null: lists the root directories (drives)
  private AbstractButton createSeparator(Path dir) {
    CrumbButton b = new CrumbButton(null, new ArrowIcon());
    b.addPopupToggle(() -> DirectoryPopupMenu.ofChildren(dir, current, this::setCurrentDirectory));
    return b;
  }

  private void showContextMenu(MouseEvent e) {
    JPopupMenu popup = new JPopupMenu();
    popup.add("Copy address as text").addActionListener(ev -> {
      StringSelection ss = new StringSelection(current.toString());
      Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, ss);
    });
    popup.add("Edit address").addActionListener(ev -> startEditing());
    popup.show(e.getComponent(), e.getX(), e.getY());
  }

  public void startEditing() {
    field.setText(current.toString());
    cards.show(this, CARD_EDIT);
    field.requestFocusInWindow();
    field.selectAll();
  }

  private void stopEditing() {
    cards.show(this, CARD_CRUMBS);
  }

  private void commitEditing() {
    Path p = parseInput(current, field.getText());
    if (Objects.isNull(p)) {
      Toolkit.getDefaultToolkit().beep();
      field.selectAll();
      return;
    }
    stopEditing();
    setCurrentDirectory(p);
  }

  // Expands a leading "~" to the user's home and resolves a relative path against
  // the base directory. Returns null when the text is not an existing directory
  @SuppressWarnings("PMD.OnlyOneReturn")
  private static Path parseInput(Path base, String text) {
    String s = text.trim();
    if ("~".equals(s) || s.startsWith("~/") || s.startsWith("~" + File.separator)) {
      s = System.getProperty("user.home") + s.substring(1);
    }
    if (s.isEmpty()) {
      return null;
    }
    Path p;
    try {
      p = base.resolve(s).toAbsolutePath().normalize();
    } catch (InvalidPathException ex) {
      return null;
    }
    if (Files.isRegularFile(p)) {
      p = p.getParent();
    }
    return Files.isDirectory(p) ? p : null;
  }
}

// Child order: [root icon][places separator][root label][root separator][ellipsis]
//   ([directory][separator])*
// Hides the leftmost directory buttons that do not fit and shows the ellipsis instead.
class BreadcrumbLayout implements LayoutManager {
  private static final int FIXED_COUNT = 5;
  private static final int ELLIPSIS_INDEX = 4;
  private int hiddenCount;

  public int getHiddenCount() {
    return hiddenCount;
  }

  @Override public void addLayoutComponent(String name, Component comp) {
    // not needed
  }

  @Override public void removeLayoutComponent(Component comp) {
    // not needed
  }

  @Override public Dimension preferredLayoutSize(Container parent) {
    return getLayoutSize(parent, 0);
  }

  @Override public Dimension minimumLayoutSize(Container parent) {
    return getLayoutSize(parent, getPairCount(parent) - 1);
  }

  private static int getPairCount(Container parent) {
    return Math.max(0, parent.getComponentCount() - FIXED_COUNT) / 2;
  }

  // hidden: number of hidden directory buttons (counted from the left)
  @SuppressWarnings("PMD.OnlyOneReturn")
  private static boolean isShown(int index, int hidden) {
    if (index < ELLIPSIS_INDEX) {
      return true;
    } else if (index == ELLIPSIS_INDEX) {
      return hidden > 0;
    }
    int pair = (index - FIXED_COUNT) / 2;
    boolean isButton = (index - FIXED_COUNT) % 2 == 0;
    // The separator of the last hidden pair stays visible next to the ellipsis
    return isButton ? pair >= hidden : pair >= hidden - 1;
  }

  private static Dimension getLayoutSize(Container parent, int hidden) {
    Insets in = parent.getInsets();
    int w = 0;
    int h = 0;
    for (int i = 0; i < parent.getComponentCount(); i++) {
      Dimension d = parent.getComponent(i).getPreferredSize();
      h = Math.max(h, d.height);
      if (isShown(i, hidden)) {
        w += d.width;
      }
    }
    return new Dimension(w + in.left + in.right, h + in.top + in.bottom);
  }

  @SuppressWarnings("PMD.OnlyOneReturn")
  private static int computeHiddenCount(Container parent, int available) {
    Insets in = parent.getInsets();
    int pairs = getPairCount(parent);
    for (int k = 0; k < pairs; k++) {
      if (getLayoutSize(parent, k).width - in.left - in.right <= available) {
        return k;
      }
    }
    return Math.max(0, pairs - 1);
  }

  @Override public void layoutContainer(Container parent) {
    Insets in = parent.getInsets();
    int available = parent.getWidth() - in.left - in.right;
    int height = parent.getHeight() - in.top - in.bottom;
    hiddenCount = computeHiddenCount(parent, available);
    int x = in.left;
    for (int i = 0; i < parent.getComponentCount(); i++) {
      Component c = parent.getComponent(i);
      boolean shown = isShown(i, hiddenCount);
      c.setVisible(shown);
      if (shown) {
        Dimension d = c.getPreferredSize();
        // Clip the last components instead of overflowing the container
        int w = Math.max(0, Math.min(d.width, in.left + available - x));
        c.setBounds(x, in.top + (height - d.height) / 2, w, d.height);
        x += w;
      }
    }
  }
}

// Flat button: paints a highlight only on rollover, press or while its popup is open
class CrumbButton extends JButton {
  private static final int MAX_WIDTH = 160;
  private static final Color ROLLOVER = new Color(0x20_00_00_00, true);
  private static final Color PRESSED = new Color(0x40_00_00_00, true);
  private boolean popupVisible;
  // The popup was just closed by pressing this button: the following action must not reopen it
  private boolean skipNextAction;

  protected CrumbButton(String text, Icon icon) {
    super(text, icon);
  }

  @Override public void updateUI() {
    super.updateUI();
    setContentAreaFilled(false);
    setBorderPainted(false);
    setFocusPainted(false);
    setRolloverEnabled(true);
    setOpaque(false);
    setIconTextGap(2);
    setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
    setCursor(Cursor.getDefaultCursor());
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    d.width = Math.min(d.width, MAX_WIDTH);
    return d;
  }

  // Clicking the button shows the popup created by the factory below the button,
  // and clicking it again while the popup is open closes the popup
  public void addPopupToggle(Supplier<? extends JPopupMenu> factory) {
    addActionListener(e -> togglePopup(factory));
    addMouseListener(new MouseAdapter() {
      @Override public void mouseExited(MouseEvent e) {
        skipNextAction = false;
      }
    });
  }

  private void togglePopup(Supplier<? extends JPopupMenu> factory) {
    if (skipNextAction) {
      skipNextAction = false;
      return;
    }
    JPopupMenu popup = factory.get();
    popup.addPopupMenuListener(new PopupMenuListener() {
      @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        popupVisible = true;
        repaint();
      }

      @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        popupVisible = false;
        skipNextAction = isHandlingMousePress();
        repaint();
      }

      @Override public void popupMenuCanceled(PopupMenuEvent e) {
        // not needed
      }
    });
    popup.show(this, 0, getHeight());
  }

  // True while a mouse press on this button is dispatched, e.g. when the press
  // closes the popup of this button (a popup closed by a key returns false)
  private boolean isHandlingMousePress() {
    AWTEvent e = EventQueue.getCurrentEvent();
    return e instanceof MouseEvent
        && e.getID() == MouseEvent.MOUSE_PRESSED
        && equals(((MouseEvent) e).getComponent());
  }

  @Override protected void paintComponent(Graphics g) {
    ButtonModel m = getModel();
    if (m.isArmed() || m.isRollover() || popupVisible) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setPaint(m.isArmed() || popupVisible ? PRESSED : ROLLOVER);
      g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 4, 4);
      g2.dispose();
    }
    super.paintComponent(g);
    if (isFocusOwner()) {
      g.setColor(Color.GRAY);
      BasicGraphicsUtils.drawDashedRect(g, 1, 1, getWidth() - 2, getHeight() - 2);
    }
  }
}

class ArrowIcon implements Icon {
  private static final int SIZE = 10;
  private static final Stroke STROKE = new BasicStroke(
      1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(c.getForeground());
    g2.setStroke(STROKE);
    Path2D p = new Path2D.Double();
    p.moveTo(x + 3d, y + 2d);
    p.lineTo(x + 6.5, y + SIZE / 2d);
    p.lineTo(x + 3d, y + SIZE - 2d);
    g2.draw(p);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return SIZE;
  }

  @Override public int getIconHeight() {
    return SIZE;
  }
}

@SuppressWarnings("PMD.TooManyMethods")
class DirEntry {
  // Icons of regular files are cached by extension, except for the types
  // whose icon is embedded in each file
  private static final Map<String, Icon> ICON_CACHE = new ConcurrentHashMap<>();
  private static final Set<String> OWN_ICON = new HashSet<>(
      Arrays.asList("", ".exe", ".lnk", ".ico", ".url"));
  private static final String DIR_ICON = "FileView.directoryIcon";
  private static final List<String> HOME_FOLDERS = Arrays.asList(
      "", "Desktop", "Documents", "Downloads", "Music", "Pictures", "Videos");
  private final Path path;
  private final String name;
  private final Icon icon;
  private final boolean directory;

  // FileSystemView#getSystemDisplayName(File) and #getSystemIcon(File) take a few
  // milliseconds per file on Windows, so they are used only where they matter:
  // the system name and icon for the root directories (drives), the system icon
  // for regular files, and the file name with the generic folder icon for directories
  protected DirEntry(Path path, boolean directory) {
    this.path = path;
    this.directory = directory;
    Path fileName = path.getFileName();
    if (Objects.isNull(fileName)) {
      this.name = getDisplayName(path);
      this.icon = FileSystemView.getFileSystemView().getSystemIcon(path.toFile());
    } else {
      this.name = fileName.toString();
      this.icon = directory ? UIManager.getIcon(DIR_ICON) : getFileIcon(path);
    }
  }

  private DirEntry(Path dir, String name, Icon icon) {
    this.path = dir;
    this.name = name;
    this.icon = icon;
    this.directory = true;
  }

  // A directory with its system display name, as the breadcrumb buttons show it
  private static DirEntry withDisplayName(Path dir) {
    Icon icon = Objects.isNull(dir.getFileName())
        ? FileSystemView.getFileSystemView().getSystemIcon(dir.toFile())
        : UIManager.getIcon(DIR_ICON);
    return new DirEntry(dir, getDisplayName(dir), icon);
  }

  public static String getDisplayName(Path p) {
    String name = FileSystemView.getFileSystemView().getSystemDisplayName(p.toFile());
    return name.isEmpty() ? p.toString() : name;
  }

  // The system icon, e.g. the special icon of the Desktop or Downloads folder
  public static Icon getSystemIcon(Path p) {
    Icon icon = FileSystemView.getFileSystemView().getSystemIcon(p.toFile());
    return Objects.isNull(icon) ? UIManager.getIcon(DIR_ICON) : icon;
  }

  // The root directories (drives)
  public static List<DirEntry> listRoots() {
    List<DirEntry> list = new ArrayList<>();
    for (Path p : FileSystems.getDefault().getRootDirectories()) {
      if (Files.isDirectory(p)) {
        list.add(new DirEntry(p, true));
      }
    }
    return list;
  }

  // The subdirectories that are not hidden, sorted by name
  public static List<DirEntry> listSubdirectories(Path dir) throws IOException {
    List<DirEntry> list = new ArrayList<>();
    listAttributes(dir).forEach((p, attrs) -> {
      if (attrs.isDirectory() && !isHidden(p, attrs)) {
        list.add(new DirEntry(p, true));
      }
    });
    list.sort(Comparator.comparing(DirEntry::getName, String.CASE_INSENSITIVE_ORDER));
    return list;
  }

  // The given directories with their system display names
  public static List<DirEntry> listWithDisplayNames(List<Path> dirs) {
    List<DirEntry> list = new ArrayList<>();
    for (Path p : dirs) {
      list.add(withDisplayName(p));
    }
    return list;
  }

  // A place shown with the system name and icon of the file (e.g. a shortcut) that opens dir
  private static DirEntry ofPlace(File file, Path dir) {
    FileSystemView fsv = FileSystemView.getFileSystemView();
    String name = fsv.getSystemDisplayName(file);
    Icon icon = fsv.getSystemIcon(file);
    return new DirEntry(
        dir,
        name.isEmpty() ? dir.toString() : name,
        Objects.isNull(icon) ? UIManager.getIcon(DIR_ICON) : icon);
  }

  // The Desktop and its items that are directories on the file system, like the list
  // next to the address bar icon of Windows Explorer. The virtual folders such as
  // PC, Libraries and Network have no Path and are skipped. Where the root of
  // FileSystemView is not the Desktop but the file system root (e.g. "/"), the user's
  // home and its common subdirectories are listed instead.
  public static List<DirEntry> listPlaces() {
    FileSystemView fsv = FileSystemView.getFileSystemView();
    File[] roots = fsv.getRoots();
    return roots.length == 0 || fsv.isFileSystemRoot(roots[0])
        ? listHomeFolders()
        : listDesktopItems(fsv, roots[0]);
  }

  @SuppressWarnings("PMD.UseConcurrentHashMap")
  private static List<DirEntry> listDesktopItems(FileSystemView fsv, File desktop) {
    Map<Path, DirEntry> places = new LinkedHashMap<>();
    places.put(desktop.toPath(), ofPlace(desktop, desktop.toPath()));
    for (File f : fsv.getFiles(desktop, true)) {
      // Shortcuts are skipped: the path of a shortcut file is not a directory.
      // Java 9+ can resolve them with FileSystemView#isLink(File) and #getLinkLocation(File)
      if (fsv.isFileSystem(f) && Files.isDirectory(f.toPath())) {
        places.putIfAbsent(f.toPath(), ofPlace(f, f.toPath()));
      }
    }
    return new ArrayList<>(places.values());
  }

  private static List<DirEntry> listHomeFolders() {
    Path home = Paths.get(System.getProperty("user.home"));
    List<DirEntry> list = new ArrayList<>();
    for (String s : HOME_FOLDERS) {
      Path p = home.resolve(s);
      if (Files.isDirectory(p)) {
        list.add(ofPlace(p.toFile(), p));
      }
    }
    return list;
  }

  private static Icon getFileIcon(Path path) {
    String n = path.getFileName().toString().toLowerCase(Locale.ROOT);
    int i = n.lastIndexOf('.');
    String ext = i < 0 ? "" : n.substring(i);
    return OWN_ICON.contains(ext)
        ? FileSystemView.getFileSystemView().getSystemIcon(path.toFile())
        : ICON_CACHE.computeIfAbsent(
            ext, k -> FileSystemView.getFileSystemView().getSystemIcon(path.toFile()));
  }

  // Lists the direct children with their attributes. On Windows the attributes
  // come from the directory scan, which is much faster than Files#isDirectory(Path)
  // per child; the returned attributes also report the hidden flag of directories
  // correctly, unlike Files#isHidden(Path)
  @SuppressWarnings("PMD.UseConcurrentHashMap")
  public static Map<Path, BasicFileAttributes> listAttributes(Path dir) throws IOException {
    Map<Path, BasicFileAttributes> map = new LinkedHashMap<>();
    Set<FileVisitOption> options = EnumSet.noneOf(FileVisitOption.class);
    Files.walkFileTree(dir, options, 1, new SimpleFileVisitor<Path>() {
      @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        map.put(file, attrs);
        return FileVisitResult.CONTINUE;
      }

      // Skip unreadable children, but report an unreadable dir itself
      @Override public FileVisitResult visitFileFailed(Path file, IOException ex)
          throws IOException {
        if (file.equals(dir)) {
          throw ex;
        }
        return FileVisitResult.CONTINUE;
      }
    });
    return map;
  }

  public static boolean isHidden(Path path, BasicFileAttributes attrs) {
    return attrs instanceof DosFileAttributes
        ? ((DosFileAttributes) attrs).isHidden()
        : path.toFile().isHidden();
  }

  public Path getPath() {
    return path;
  }

  public String getName() {
    return name;
  }

  public Icon getIcon() {
    return icon;
  }

  public boolean isDirectory() {
    return directory;
  }
}

// Lists directories: the subdirectories of a directory (or the root directories),
// or the given directories such as the hidden breadcrumb ancestors.
// The entries are loaded on a worker thread while a "Loading..." item is shown,
// and shown in a scrollable JList instead of JMenuItems.
// The popup is not focusable, like the JComboBox popup: PopupFactory makes the
// heavyweight popup window focusable when a JPopupMenu contains a component other
// than a MenuElement, and Windows then activates that window for a moment, which
// makes the title bar of the frame flicker. The invoker keeps the focus instead
// and forwards the keys to the list while the popup is open.
@SuppressWarnings("PMD.TooManyMethods")
class DirectoryPopupMenu extends JPopupMenu {
  public static final String NAVIGATE = "navigate";
  private static final String PREFIX = "DirectoryPopupMenu.";
  private static final String CLOSE = "close";
  // Key -> JList action name, or NAVIGATE / CLOSE
  private static final Map<String, String> KEY_ACTIONS = createKeyActions();
  private final transient Callable<List<DirEntry>> loader;
  private final transient Path current;
  private final transient Consumer<Path> navigator;
  private final JMenuItem loading = createInfoItem("Loading...");
  private final transient FocusAdapter focusHandler = new FocusAdapter() {
    @Override public void focusLost(FocusEvent e) {
      if (!e.isTemporary()) {
        setVisible(false);
      }
    }
  };
  private DirectoryList list;

  @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
  protected DirectoryPopupMenu(
      Callable<List<DirEntry>> loader, Path current, Consumer<Path> navigator) {
    super();
    this.loader = loader;
    this.current = current;
    this.navigator = navigator;
    setFocusable(false);
    add(loading);
    addPopupMenuListener(new PopupMenuListener() {
      @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        installKeyBindings();
      }

      @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        uninstallKeyBindings();
      }

      @Override public void popupMenuCanceled(PopupMenuEvent e) {
        // not needed
      }
    });
  }

  // Subdirectories of dir, or the root directories (drives) when dir is null;
  // ancestors of current are shown in bold
  public static DirectoryPopupMenu ofChildren(Path dir, Path current, Consumer<Path> navigator) {
    return new DirectoryPopupMenu(
        () -> Objects.isNull(dir) ? DirEntry.listRoots() : DirEntry.listSubdirectories(dir),
        current, navigator);
  }

  // The given directories with their system display names
  public static DirectoryPopupMenu ofPaths(List<Path> dirs, Consumer<Path> navigator) {
    List<Path> copy = new ArrayList<>(dirs);
    return new DirectoryPopupMenu(() -> DirEntry.listWithDisplayNames(copy), null, navigator);
  }

  // The places such as the Desktop; ancestors of current are shown in bold
  public static DirectoryPopupMenu ofPlaces(Path current, Consumer<Path> navigator) {
    return new DirectoryPopupMenu(DirEntry::listPlaces, current, navigator);
  }

  @SuppressWarnings("PMD.UseConcurrentHashMap")
  private static Map<String, String> createKeyActions() {
    Map<String, String> map = new HashMap<>();
    map.put("UP", "selectPreviousRow");
    map.put("DOWN", "selectNextRow");
    map.put("PAGE_UP", "scrollUp");
    map.put("PAGE_DOWN", "scrollDown");
    map.put("HOME", "selectFirstRow");
    map.put("END", "selectLastRow");
    map.put("ENTER", NAVIGATE);
    map.put("ESCAPE", CLOSE);
    map.put("SPACE", CLOSE);
    return Collections.unmodifiableMap(map);
  }

  // Keeps the popup at least as wide as the "Loading..." item
  // so that the width does not shrink (flicker) when the items are replaced
  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    Insets i = getInsets();
    d.width = Math.max(d.width, loading.getPreferredSize().width + i.left + i.right);
    return d;
  }

  @Override public void show(Component invoker, int x, int y) {
    super.show(invoker, x, y);
    new SwingWorker<List<DirEntry>, Void>() {
      @Override protected List<DirEntry> doInBackground() throws Exception {
        return loader.call();
      }

      @Override protected void done() {
        // Skip when the popup was closed before loading finished
        if (DirectoryPopupMenu.this.isVisible()) {
          updateItems(this);
        }
      }
    }.execute();
  }

  private void updateItems(SwingWorker<List<DirEntry>, ?> worker) {
    removeAll();
    try {
      addEntries(worker.get());
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    } catch (ExecutionException ex) {
      add(createInfoItem("(Access denied)"));
    }
    pack();
  }

  private void addEntries(List<DirEntry> entries) {
    if (entries.isEmpty()) {
      add(createInfoItem("(No subfolders)"));
      return;
    }
    list = new DirectoryList(entries, current, navigator);
    JScrollPane scroll = new JScrollPane(list);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.getVerticalScrollBar().setUnitIncrement(list.getFixedCellHeight());
    scroll.getVerticalScrollBar().setFocusable(false);
    scroll.setFocusable(false);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.setViewportBorder(BorderFactory.createEmptyBorder());
    add(scroll);
  }

  private void installKeyBindings() {
    Component c = getInvoker();
    if (c instanceof JComponent) {
      JComponent invoker = (JComponent) c;
      KEY_ACTIONS.forEach((key, name) -> {
        invoker.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(key), PREFIX + name);
        invoker.getActionMap().put(PREFIX + name, new AbstractAction() {
          @Override public void actionPerformed(ActionEvent e) {
            performKeyAction(name);
          }
        });
      });
      // Closes the popup when the focus moves elsewhere, e.g. with the Tab key
      invoker.addFocusListener(focusHandler);
    }
  }

  private void uninstallKeyBindings() {
    Component c = getInvoker();
    if (c instanceof JComponent) {
      JComponent invoker = (JComponent) c;
      KEY_ACTIONS.forEach((key, name) -> {
        invoker.getInputMap(WHEN_FOCUSED).remove(KeyStroke.getKeyStroke(key));
        invoker.getActionMap().remove(PREFIX + name);
      });
      invoker.removeFocusListener(focusHandler);
    }
  }

  private void performKeyAction(String name) {
    if (CLOSE.equals(name)) {
      setVisible(false);
    } else if (Objects.nonNull(list)) {
      list.performAction(name);
    }
  }

  private static JMenuItem createInfoItem(String text) {
    JMenuItem item = new JMenuItem(text);
    item.setEnabled(false);
    return item;
  }
}

// Scrollable list of directories shown in DirectoryPopupMenu.
// The row under the mouse is selected like a menu item. The list never gets the focus;
// the keys are forwarded by DirectoryPopupMenu from the invoker.
class DirectoryList extends JList<DirEntry> {
  private static final int MAX_ROWS = 12;
  private static final int MAX_WIDTH = 400;
  private final transient Consumer<Path> navigator;
  private transient MouseAdapter handler;

  @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
  protected DirectoryList(List<DirEntry> entries, Path current, Consumer<Path> navigator) {
    super(createModel(entries));
    this.navigator = navigator;
    DirEntryRenderer renderer = new DirEntryRenderer(current);
    setCellRenderer(renderer);
    Component c = renderer.getListCellRendererComponent(this, entries.get(0), 0, false, false);
    setFixedCellHeight(c.getPreferredSize().height);
    setVisibleRowCount(Math.min(entries.size(), MAX_ROWS));
  }

  private static ListModel<DirEntry> createModel(List<DirEntry> entries) {
    DefaultListModel<DirEntry> model = new DefaultListModel<>();
    entries.forEach(model::addElement);
    return model;
  }

  @Override public void updateUI() {
    removeMouseListener(handler);
    removeMouseMotionListener(handler);
    super.updateUI();
    setFocusable(false);
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setSelectionBackground(UIManager.getColor("MenuItem.selectionBackground"));
    setSelectionForeground(UIManager.getColor("MenuItem.selectionForeground"));
    handler = new RolloverHandler();
    addMouseListener(handler);
    addMouseMotionListener(handler);
  }

  @Override public Dimension getPreferredScrollableViewportSize() {
    Dimension d = super.getPreferredScrollableViewportSize();
    d.width = Math.min(d.width, MAX_WIDTH);
    return d;
  }

  // Performs the JList action of the name, or moves to the selected directory
  // for DirectoryPopupMenu.NAVIGATE
  public void performAction(String name) {
    if (DirectoryPopupMenu.NAVIGATE.equals(name)) {
      navigateSelected();
    } else {
      Action a = getActionMap().get(name);
      if (Objects.nonNull(a)) {
        a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, name));
      }
    }
  }

  // Closes the popup and moves to the selected directory
  public void navigateSelected() {
    DirEntry entry = getSelectedValue();
    if (Objects.nonNull(entry)) {
      Container popup = SwingUtilities.getAncestorOfClass(JPopupMenu.class, this);
      if (popup instanceof JPopupMenu) {
        popup.setVisible(false);
      }
      navigator.accept(entry.getPath());
    }
  }

  private final class RolloverHandler extends MouseAdapter {
    private void setRollover(MouseEvent e) {
      Point pt = e.getPoint();
      int index = locationToIndex(pt);
      Rectangle r = getCellBounds(index, index);
      if (Objects.nonNull(r) && r.contains(pt)) {
        setSelectedIndex(index);
      } else {
        clearSelection();
      }
    }

    @Override public void mouseMoved(MouseEvent e) {
      setRollover(e);
    }

    @Override public void mouseDragged(MouseEvent e) {
      setRollover(e);
    }

    @Override public void mouseExited(MouseEvent e) {
      clearSelection();
    }

    @Override public void mouseClicked(MouseEvent e) {
      if (SwingUtilities.isLeftMouseButton(e)) {
        setRollover(e);
        navigateSelected();
      }
    }
  }
}

// Shows the name and icon of a DirEntry; ancestors of the current directory are bold
// (current == null: no bold)
class DirEntryRenderer extends DefaultListCellRenderer {
  private final transient Path current;

  protected DirEntryRenderer(Path current) {
    super();
    this.current = current;
  }

  @Override public Component getListCellRendererComponent(
      JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    super.getListCellRendererComponent(list, value, index, isSelected, false);
    if (value instanceof DirEntry) {
      DirEntry entry = (DirEntry) value;
      setText(entry.getName());
      setIcon(entry.getIcon());
      boolean bold = Objects.nonNull(current) && current.startsWith(entry.getPath());
      setFont(getFont().deriveFont(bold ? Font.BOLD : Font.PLAIN));
    }
    setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 8));
    return this;
  }
}
