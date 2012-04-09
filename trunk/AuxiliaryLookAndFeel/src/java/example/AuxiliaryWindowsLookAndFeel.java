package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;

public class AuxiliaryWindowsLookAndFeel extends LookAndFeel {
    @Override public String getName() {
        return "AuxiliaryWindows";
    }
    @Override public String getID() {
        return "Not well known";
    }
    @Override public String getDescription() {
        return "Auxiliary Windows Look and Feel";
    }
    @Override public boolean isSupportedLookAndFeel() {
        return true;
    }
    @Override public boolean isNativeLookAndFeel() {
        return false;
    }
    public UIDefaults getDefaults() {
        UIDefaults table = new UIDefaults() {
            @Override protected void getUIError(String msg) {}
        };
        //final String packageName = "example.AuxiliaryWindows";
        Object[] uiDefaults = {
            "ComboBoxUI", "example.AuxiliaryWindowsComboBoxUI",
        };
        table.putDefaults(uiDefaults);
        return table;
    }
}
