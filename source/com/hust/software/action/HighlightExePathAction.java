package com.hust.software.action;

import com.hust.software.LogAnalysisUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.psi.PsiFile;

import javax.swing.*;

/**
 * highlight execute paths for method contains log.error
 * Created by Yan Yu on 2014-06-01.
 */
public class HighlightExePathAction extends AnAction {
    private ImageIcon myIcon;

    public HighlightExePathAction() {
        super("ExePath", "Highlight Exe Path", null);
    }

    public void actionPerformed(AnActionEvent event) {
        PsiFile psiFile = event.getData(LangDataKeys.PSI_FILE);
        LogAnalysisUtil.highlightControlFlowForFile(psiFile);
    }

    public void update(AnActionEvent event) {
        super.update(event);
        Presentation presentation = event.getPresentation();
        if (ActionPlaces.MAIN_TOOLBAR.equals(event.getPlace())) {
            if (myIcon == null) {
                java.net.URL resource = HighlightExePathAction.class.getResource("/icons/garbage.png");
                myIcon = new ImageIcon(resource);
            }
            presentation.setIcon(myIcon);
        }
    }
}
