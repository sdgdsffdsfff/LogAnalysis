package com.hust.software;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by Yan Yu on 2014-05-18.
 */
public class MyQuickFix implements LocalQuickFix {

    private static final Logger LOG = Logger.getInstance("#com.hust.software.MyQuickFix");

    @NotNull
    public String getName() {
        // The test (see the TestThisPlugin class) uses this string to identify the quick fix action.
        return LogAnalysisBundle.message("MyQuickFixName");
    }


    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        if (descriptor.getPsiElement() instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression logErrorCall = (PsiMethodCallExpression) descriptor.getPsiElement();

            PsiMethod method = PsiTreeUtil.getParentOfType(logErrorCall, PsiMethod.class);
            List<PsiReferenceExpression> referenceExpressions = LogAnalysisUtil.findVariablesNeedAdd(method, logErrorCall);

            PsiElementFactory factory = JavaPsiFacade.getInstance(logErrorCall.getProject()).getElementFactory();

            PsiExpression argInFirstBefore = logErrorCall.getArgumentList().getExpressions()[0];

            referenceExpressions.forEach(re -> {
                PsiBinaryExpression binaryExpression = (PsiBinaryExpression) factory.createExpressionFromText("a+b", null);
                PsiLiteralExpression parName = (PsiLiteralExpression) factory.createExpressionFromText("\"\\t"+re.getText()+"=\"", null);

                binaryExpression.getLOperand().replace(parName);
                binaryExpression.getROperand().replace(re);
                argInFirstBefore.add(binaryExpression.getOperationSign());
                argInFirstBefore.add(binaryExpression);
            });
            if(LOG.isDebugEnabled()){
                StringBuilder message = new StringBuilder();
                message.append("applyFix by add\t");
                referenceExpressions
                        .stream()
                        .map(re -> re.getReferenceName())
                        .forEach(refName -> message.append(" " + refName + ","));
                message.setCharAt(message.length() - 1, ' ');
                LOG.debug(message.toString());
            }
        }
    }

    @NotNull
    public String getFamilyName() {
        return getName();
    }
}
