package com.hust.software;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.TypeConversionUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A tool to help analyse code for log enhance.
 * Created by Yan Yu on 2014-05-18.
 */
public class LogAnalysisUtil {

    private static final Logger LOG = Logger.getInstance("#com.hust.software.LogAnalysisUtil");
//    private static final String ERROR = "error";


    public static PsiMethodCallExpression findLogWithFault(PsiMethod method) {
        PsiMethodCallExpression logErrorCall = findLogErrorMethodCall(method);
        if (logErrorCall == null || !checkLogFault(method, logErrorCall)) {
            return null;
        }
        return logErrorCall;
    }

    private static boolean checkLogFault(PsiMethod method, PsiMethodCallExpression logErrorCall) {
        return !findVariablesNeedAdd(method, logErrorCall).isEmpty();
    }

    private static PsiMethodCallExpression findLogErrorMethodCall(PsiMethod method) {
        if (LOG.isDebugEnabled()) {
            PsiTreeUtil.findChildrenOfType(method, PsiMethodCallExpression.class)
                    .stream()
                    .forEach(ex -> LOG.debug("findLogErrorMethodCall\t"
                            + ex.getText() + "\t" + ex.getMethodExpression().getReferenceName()));
        }

        List<PsiMethodCallExpression> logErrorCalls = PsiTreeUtil.findChildrenOfType(method, PsiMethodCallExpression.class)
                .stream()
                .filter(methodCall -> LogAnalysisInspection.CHECKED_LOG_ERROR_Method.equals(methodCall.getMethodExpression()
                        .getReferenceName()))
                .collect(Collectors.toList());
        if (logErrorCalls.isEmpty()) {
            LOG.info("Log error method call not found in " + method.getName() + ". ");
            return null;
        }
        LOG.info("Log error method call found in " + method.getName() + ". ");
        return logErrorCalls.get(0);
    }


   /* private static void analysisExecutePath(PsiMethod method, PsiStatement errorStatement) {
        List<PsiElement> mustRun = new ArrayList<>();
        PsiElement element = errorStatement;

        LOG.debug("### must run path ###");
        mustRun.stream().map(ele -> ele.toString()).forEach(LOG::debug);
        LOG.debug("### end must run path ###");
    }*/

    public static List<PsiReferenceExpression> findVariablesNeedAdd(PsiMethod method, PsiMethodCallExpression logErrorCall) {

        List<PsiIfStatement> conditionStatementList = new ArrayList<>();
        PsiElement element = logErrorCall;

        for (PsiIfStatement is; (is = PsiTreeUtil.getParentOfType(element, PsiIfStatement.class)) != null; element = is) {
            conditionStatementList.add(is);
        }

        List<PsiReferenceExpression> conditionRefExList = new ArrayList<>();

        conditionStatementList.stream()
                .map(PsiIfStatement::getCondition)
                .forEach((condition) -> {
                            if (condition instanceof PsiReferenceExpression) {
                                conditionRefExList.add((PsiReferenceExpression) condition);
                            }
                            conditionRefExList.addAll(PsiTreeUtil.findChildrenOfType(condition, PsiReferenceExpression.class));
                        }
                );


        List<PsiAssignmentExpression> assignmentExpressionList = PsiTreeUtil.findChildrenOfType(method, PsiReferenceExpression.class)
                .stream()
                .filter(ex ->
                                conditionRefExList.stream()
                                        .anyMatch((conditionRefEx) ->
                                                conditionRefEx.getQualifiedName().equals(ex.getQualifiedName()))
                )
                .map(PsiElement::getParent)
                .filter(par -> par instanceof PsiAssignmentExpression)
                .map(ae -> ((PsiAssignmentExpression) ae))
                .collect(Collectors.toList());

        List<PsiReferenceExpression> refExpHaveAffectList = new ArrayList<>();

        PsiTreeUtil.findChildrenOfType(method, PsiIfStatement.class)
                .stream()
                .filter(ifStatement ->
                        filterIfStatementByAssignInEitherBrench(assignmentExpressionList, ifStatement))
                .map(PsiIfStatement::getCondition)
                .forEach(condition ->
                {
                    if (condition instanceof PsiReferenceExpression) {
                        refExpHaveAffectList.add((PsiReferenceExpression) condition);
                    }
                    refExpHaveAffectList.addAll(PsiTreeUtil.findChildrenOfType(condition, PsiReferenceExpression.class));
                });

        List<PsiReferenceExpression> priOrStrHaveAffectList = refExpHaveAffectList.stream()
                .filter(filed -> {
//                        LOG.debug("filed: "+ filed +"\tref type:"
//                                + (Objects.isNull(filed.getType()) ? null : filed.getType().getPresentableText()));
                    if (Objects.isNull(filed.getType())) {
                        return false;
                    }
                    assert filed.getType() != null;
                    if (!TypeConversionUtil.isPrimitiveAndNotNull(filed.getType())
                            && !filed.getType().getPresentableText().equals("String")) {
                        return false;
                    }
                    return !(filed.getParent() instanceof PsiMethodCallExpression);
                })
                .collect(Collectors.toList());

        Collection<PsiReferenceExpression> refExpAlreadyInLogList =
                PsiTreeUtil.findChildrenOfType(logErrorCall.getArgumentList(), PsiReferenceExpression.class);

        List<PsiReferenceExpression> refExpNeedAddList = priOrStrHaveAffectList.stream()
                .filter(refExpHaveAffect -> {
                    boolean match = refExpAlreadyInLogList.stream()
                            .anyMatch(refExpAlreadyInLog ->
                                            refExpAlreadyInLog.getQualifiedName().equals(refExpHaveAffect.getQualifiedName())
//                                        && refExpAlreadyInLog.getContext().equals(refExpHaveAffect.getContext())
                            );
                    return !match;

                })
                .collect(Collectors.toList());

        if (LOG.isDebugEnabled()) {
            LOG.debug("### start ###");
            refExpHaveAffectList.stream().map(Object::toString).forEach(LOG::debug);
            LOG.debug("### end ###");
        }
        return refExpNeedAddList;
    }

    private static boolean filterIfStatementByAssignInEitherBrench
            (List<PsiAssignmentExpression> assignmentExpressionList, PsiIfStatement psiIfStatement) {

        PsiStatement thenBranch = psiIfStatement.getThenBranch();
        boolean thenHas = Objects.nonNull(thenBranch) && assignmentExpressionList.stream()
                .anyMatch(ae -> {
                    assert thenBranch != null;
                    return thenBranch.equals(PsiTreeUtil.findCommonParent(ae, thenBranch));
                });

        PsiStatement elseBranch = psiIfStatement.getElseBranch();
        boolean elseHas = Objects.nonNull(elseBranch) && assignmentExpressionList.stream()
                .anyMatch(ae -> {
                    assert elseBranch != null;
                    return elseBranch.equals(PsiTreeUtil.findCommonParent(ae, elseBranch));
                });

        return thenHas || elseHas;
    }
}
