package com.hust.software;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Inspection (probable) log issue, log error without enough variables to
 * make the assignments of parameters which in condition certain.
 * Created by Yan Yu on 2014-05-18.
 */
public class LogAnalysisInspection extends BaseJavaLocalInspectionTool {
    private static final Logger LOG = Logger.getInstance("#com.hust.software.LogAnalysisInspection");

    private final LocalQuickFix myQuickFix = new MyQuickFix();

    @SuppressWarnings({"WeakerAccess"})
    @NonNls
    public static String CHECKED_LOG_ERROR_Method = "error";

    @NonNls
    private static final String DESCRIPTION_TEMPLATE = LogAnalysisBundle.message("DESCRIPTION_TEMPLATE");

    @NotNull
    public final String getDisplayName() {
        return LogAnalysisBundle.message("DisplayName");
    }

    @Override
    @NotNull
    public String getGroupDisplayName() {
        return GroupNames.LOGGING_GROUP_NAME;
    }

    @NotNull
    public String getShortName() {
        return "LogAnalysis";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethod(PsiMethod method) {
                PsiMethodCallExpression logErrorCall = LogAnalysisUtil.findLogWithFault(method);
                if(LOG.isDebugEnabled()){
                    LOG.debug((Objects.isNull(logErrorCall) ? "no " : "")
                            +"log error call with fault found in " + method.getName() + ". ");
                }
                if (logErrorCall != null) {
                    holder.registerProblem(logErrorCall, DESCRIPTION_TEMPLATE, myQuickFix);
                }
            }
        };
    }

    @Override
    public JComponent createOptionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JTextField checkedLogErrorMethod = new JTextField(CHECKED_LOG_ERROR_Method);
        checkedLogErrorMethod.getDocument().addDocumentListener(new DocumentAdapter() {
            public void textChanged(DocumentEvent event) {
                CHECKED_LOG_ERROR_Method = checkedLogErrorMethod.getText();
            }
        });

        panel.add(checkedLogErrorMethod);
        return panel;
    }

    @Override
    public boolean isEnabledByDefault() {
        return false;
    }
}
