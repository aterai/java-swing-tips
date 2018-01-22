package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.ComponentUI;

@SuppressWarnings("PMD.TooManyMethods") // This class has too many methods, consider refactoring it.
public class BasicSearchBarComboBoxUI extends SearchBarComboBoxUI {
    protected PopupMenuListener popupMenuListener;
    protected JButton loupeButton;
    protected Action loupeAction = new AbstractAction() {
        @Override public void actionPerformed(ActionEvent e) {
            comboBox.setPopupVisible(false);
            Object o = listBox.getSelectedValue();
            if (Objects.isNull(o)) {
                o = comboBox.getItemAt(0);
            }
            System.out.println(o + ": " + comboBox.getEditor().getItem());
        }
    };
    public static ComponentUI createUI(JComponent c) {
        return new BasicSearchBarComboBoxUI();
    }
    // protected boolean isEditable = true;
    @Override protected void installDefaults() {
        super.installDefaults();
        // comboBox.setEditable(true);
        comboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        // comboBox.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    }
    @Override protected void installListeners() {
        super.installListeners();
        popupMenuListener = createPopupMenuListener();
        comboBox.addPopupMenuListener(popupMenuListener);
    }
    @Override protected void uninstallListeners() {
        super.uninstallListeners();
        comboBox.removePopupMenuListener(popupMenuListener);
    }
    protected final PopupMenuListener createPopupMenuListener() {
        if (Objects.isNull(popupMenuListener)) {
            popupMenuListener = new PopupMenuListener() {
                private String str;
                @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    JComboBox combo = (JComboBox) e.getSource();
                    str = combo.getEditor().getItem().toString();
                }
                @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    JComboBox combo = (JComboBox) e.getSource();
                    Object o = listBox.getSelectedValue();
                    if (o instanceof SearchEngine) {
                        SearchEngine se = (SearchEngine) o;
                        arrowButton.setIcon(se.favicon);
                        arrowButton.setRolloverIcon(makeRolloverIcon(se.favicon));
                    }
                    combo.getEditor().setItem(str);
                }
                @Override public void popupMenuCanceled(PopupMenuEvent e) { /* not needed */ }
            };
        }
        return popupMenuListener;
    }
//     // NullPointerException at BasicComboBoxUI#isNavigationKey(int keyCode, int modifiers)
//     private static class DummyKeyAdapter extends KeyAdapter { /* dummy */ }
//     @Override protected KeyListener createKeyListener() {
//         if (Objects.isNull(keyListener)) {
//             keyListener = new DummyKeyAdapter();
//         }
//         return keyListener;
//     }
    @Override protected void configureEditor() {
        // super.configureEditor();
        // Should be in the same state as the combobox
        editor.setEnabled(comboBox.isEnabled());
        editor.setFocusable(comboBox.isFocusable());
        editor.setFont(comboBox.getFont());
        // if (Objects.nonNull(focusListener)) {
        //     editor.addFocusListener(focusListener);
        // }
        // editor.addFocusListener(getHandler());
        // comboBox.getEditor().addActionListener(getHandler());
        if (editor instanceof JComponent) {
            // ((JComponent) editor).putClientProperty("doNotCancelPopup", HIDE_POPUP_KEY);
            ((JComponent) editor).setInheritsPopupMenu(true);
        }
        comboBox.configureEditor(comboBox.getEditor(), comboBox.getSelectedItem());
        editor.addPropertyChangeListener(propertyChangeListener);

        ((JComponent) editor).setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        ((JComponent) editor).getActionMap().put("loupe", loupeAction);
        InputMap im = ((JComponent) editor).getInputMap(JComponent.WHEN_FOCUSED);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "loupe");
    }
    @Override protected JButton createArrowButton() {
        return new TriangleArrowButton();
    }
    @Override public void configureArrowButton() {
        super.configureArrowButton();
        if (Objects.nonNull(arrowButton)) {
            arrowButton.setBackground(UIManager.getColor("Panel.background"));
            arrowButton.setHorizontalAlignment(SwingConstants.LEFT);
            arrowButton.setOpaque(true);
            arrowButton.setFocusPainted(false);
            arrowButton.setContentAreaFilled(false);
            arrowButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(127, 157, 185)),
                BorderFactory.createEmptyBorder(1, 1, 1, 1)));
        }
    }
    @Override protected void installComponents() {
        // super.installComponents();
        arrowButton = createArrowButton();
        comboBox.add(arrowButton);
        configureArrowButton();

        loupeButton = createLoupeButton();
        comboBox.add(loupeButton);
        configureLoupeButton();

        // if (comboBox.isEditable())
        addEditor();
        comboBox.add(currentValuePane);
    }
    @Override protected void uninstallComponents() {
        if (Objects.nonNull(loupeButton)) {
            unconfigureLoupeButton();
        }
        loupeButton = null;
        super.uninstallComponents();
    }
    protected final JButton createLoupeButton() {
        JButton button = new JButton(loupeAction);
        ImageIcon loupe = new ImageIcon(BasicSearchBarComboBoxUI.class.getResource("loupe.png"));
        button.setIcon(loupe);
        button.setRolloverIcon(makeRolloverIcon(loupe));
        return button;
    }
    public final void configureLoupeButton() {
        if (Objects.nonNull(loupeButton)) {
            loupeButton.setName("ComboBox.loupeButton");
            loupeButton.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            loupeButton.setEnabled(comboBox.isEnabled());
            loupeButton.setFocusable(comboBox.isFocusable());
            loupeButton.setOpaque(false);
            loupeButton.setRequestFocusEnabled(false);
            loupeButton.setFocusPainted(false);
            loupeButton.setContentAreaFilled(false);
            // loupeButton.addMouseListener(popup.getMouseListener());
            // loupeButton.addMouseMotionListener(popup.getMouseMotionListener());
            loupeButton.resetKeyboardActions();
            // loupeButton.putClientProperty("doNotCancelPopup", HIDE_POPUP_KEY);
            loupeButton.setInheritsPopupMenu(true);
        }
    }
    public final void unconfigureLoupeButton() {
        if (Objects.nonNull(loupeButton)) {
            loupeButton.setAction(null);
            // loupeButton.removeMouseListener(popup.getMouseListener());
            // loupeButton.removeMouseMotionListener(popup.getMouseMotionListener());
        }
    }
    @Override protected ListCellRenderer<Object> createRenderer() {
        return new SearchEngineListCellRenderer();
    }
    @Override protected LayoutManager createLayoutManager() {
        return new SearchBarLayout();
    }
    protected static Icon makeRolloverIcon(Icon srcIcon) {
        RescaleOp op = new RescaleOp(
            new float[] {1.2f, 1.2f, 1.2f, 1f},
            new float[] {0f, 0f, 0f, 0f}, null);
        BufferedImage img = new BufferedImage(srcIcon.getIconWidth(), srcIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.getGraphics();
        // g.drawImage(srcIcon.getImage(), 0, 0, null);
        srcIcon.paintIcon(null, g, 0, 0);
        g.dispose();
        return new ImageIcon(op.filter(img, null));
    }
}

class SearchEngineListCellRenderer extends DefaultListCellRenderer {
    @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof SearchEngine) {
            SearchEngine se = (SearchEngine) value;
            l.setIcon(se.favicon);
            l.setToolTipText(se.url);
        }
        return l;
    }
}

class SearchBarLayout implements LayoutManager {
    @Override public void addLayoutComponent(String name, Component comp) { /* not needed */ }
    @Override public void removeLayoutComponent(Component comp) { /* not needed */ }
    @Override public Dimension preferredLayoutSize(Container parent) {
        return parent.getPreferredSize();
    }
    @Override public Dimension minimumLayoutSize(Container parent) {
        return parent.getMinimumSize();
    }
    @Override public void layoutContainer(Container parent) {
        if (!(parent instanceof JComboBox)) {
            return;
        }
        JComboBox cb = (JComboBox) parent;
        int width = cb.getWidth();
        int height = cb.getHeight();
        Insets insets = cb.getInsets();
        int buttonHeight = height - insets.top - insets.bottom;
        int buttonWidth = buttonHeight;
        int loupeWidth = buttonHeight;

        JButton arrowButton = (JButton) cb.getComponent(0);
        if (Objects.nonNull(arrowButton)) {
            Insets arrowInsets = arrowButton.getInsets();
            buttonWidth = arrowButton.getPreferredSize().width + arrowInsets.left + arrowInsets.right;
            arrowButton.setBounds(insets.left, insets.top, buttonWidth, buttonHeight);
        }
        JButton loupeButton = null;
        for (Component c: cb.getComponents()) {
            if ("ComboBox.loupeButton".equals(c.getName())) {
                loupeButton = (JButton) c;
                break;
            }
        }
        // = (JButton) cb.getComponent(3);
        if (Objects.nonNull(loupeButton)) {
            // Insets loupeInsets = loupeButton.getInsets();
            // loupeWidth = loupeButton.getPreferredSize().width + loupeInsets.left + loupeInsets.right;
            loupeButton.setBounds(width - insets.right - loupeWidth, insets.top, loupeWidth, buttonHeight);
        }
        JTextField editor = (JTextField) cb.getEditor().getEditorComponent();
        // JTextField editor = (JTextField) cb.getComponent(1);
        if (Objects.nonNull(editor)) {
            editor.setBounds(insets.left + buttonWidth, insets.top,
                             width  - insets.left - insets.right - buttonWidth - loupeWidth,
                             height - insets.top  - insets.bottom);
        }
    }
}
