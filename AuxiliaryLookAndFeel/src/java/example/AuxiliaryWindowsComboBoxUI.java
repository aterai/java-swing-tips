package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

import com.sun.java.swing.plaf.windows.WindowsComboBoxUI;

// https://docs.oracle.com/javase/8/docs/api/javax/swing/plaf/multi/doc-files/multi_tsc.html
// ???: Don't extend visual look and feels.
public class AuxiliaryWindowsComboBoxUI extends WindowsComboBoxUI {
    public static ComponentUI createUI(JComponent c) {
        return new AuxiliaryWindowsComboBoxUI();
    }
    @Override protected ComboPopup createPopup() {
        // System.out.println("AuxiliaryWindowsComboBoxUI#createPopup");
        return new BasicComboPopup2(comboBox);
    }
    // // ???: Use the installUI method to perform all initialization, and the uninstallUI method to perform all cleanup.
    // @Override public void installUI(JComponent c) {
    //     // super.installUI(c);
    // }
    // @Override public void uninstallUI(JComponent c) {
    //     // super.uninstallUI(c);
    // }
    // Override all UI-specific methods your UI classes inherit.
    @Override protected void configureEditor() { /* Override all UI-specific methods your UI classes inherit. */ }
    @Override protected void unconfigureEditor() { /* Override all UI-specific methods your UI classes inherit. */ }
    @Override public void removeEditor() { /* Override all UI-specific methods your UI classes inherit. */ }
    @Override public void addEditor() {
        removeEditor();
        Optional.ofNullable(comboBox.getEditor()).ifPresent(cbe -> {
            editor = cbe.getEditorComponent();
            Optional.ofNullable(editor).ifPresent(ec -> {
                configureEditor();
                comboBox.add(ec);
                if (comboBox.isFocusOwner()) {
                    // Switch focus to the editor component
                    ec.requestFocusInWindow();
                }
            });
        });
    }
    // @Override public void unconfigureArrowButton() {}
    // @Override public void configureArrowButton() {}
    @Override public void update(Graphics g, JComponent c) { /* Override all UI-specific methods your UI classes inherit. */ }
    @Override public void paint(Graphics g, JComponent c) { /* Override all UI-specific methods your UI classes inherit. */ }
    @Override public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) { /* Override all UI-specific methods your UI classes inherit. */ }
    @Override public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) { /* Override all UI-specific methods your UI classes inherit. */ }
}

class BasicComboPopup2 extends BasicComboPopup {
    private transient MouseListener handler2;
    protected BasicComboPopup2(JComboBox<?> combo) {
        super(combo);
    }
    @Override public void uninstallingUI() {
        super.uninstallingUI();
        handler2 = null;
    }
    @Override protected MouseListener createListMouseListener() {
        // if (Objects.isNull(handler2)) {
        //     handler2 = new Handler2();
        // }
        handler2 = Optional.ofNullable(handler2).orElseGet(Handler2::new);
        return handler2;
    }
    private class Handler2 extends MouseAdapter {
        @Override public void mouseReleased(MouseEvent e) {
            if (!Objects.equals(e.getSource(), list)) {
                return;
            }
            if (list.getModel().getSize() > 0) {
                // <ins>
                if (!SwingUtilities.isLeftMouseButton(e) || !comboBox.isEnabled()) {
                    return;
                }
                // </ins>
                // JList mouse listener
                if (comboBox.getSelectedIndex() == list.getSelectedIndex()) {
                    comboBox.getEditor().setItem(list.getSelectedValue());
                }
                comboBox.setSelectedIndex(list.getSelectedIndex());
            }
            comboBox.setPopupVisible(false);
            // workaround for cancelling an edited item (bug 4530953)
            if (comboBox.isEditable() && Objects.nonNull(comboBox.getEditor())) {
                comboBox.configureEditor(comboBox.getEditor(), comboBox.getSelectedItem());
            }
        }
    }
}
