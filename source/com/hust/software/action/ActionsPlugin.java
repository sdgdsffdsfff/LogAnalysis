package com.hust.software.action;

import com.intellij.openapi.components.ApplicationComponent;

/**
 * this plugin is for add my menu to intellij
 * Created by Yan Yu on 2014-06-01.
 */
public class ActionsPlugin implements ApplicationComponent {
    /**
     * Method is called after plugin is already created and configured. Plugin can start to communicate with
     * other plugins only in this method.
     */
    public void initComponent() {
    }

    /**
     * This method is called on plugin disposal.
     */
    public void disposeComponent() {
    }

    /**
     * Returns   the name of component
     *
     * @return String representing component name. Use PluginName.ComponentName notation
     * to avoid conflicts.
     */
    public String getComponentName() {
        return "LogAnalysis.ActionsPlugin";
    }
}
