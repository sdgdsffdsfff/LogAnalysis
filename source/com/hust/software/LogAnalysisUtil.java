package com.hust.software;

import com.intellij.codeInsight.highlighting.HighlightManager;
import com.intellij.codeInspection.dataFlow.StandardDataFlowRunner;
import com.intellij.codeInspection.dataFlow.StandardInstructionVisitor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.*;
import com.intellij.psi.controlFlow.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.TypeConversionUtil;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        highlightMethodCaller(method);
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
//        while(true);
        // todo these code is just for test
        if (LOG.isDebugEnabled()) {
            LOG.debug("### Instruction ###");
            try {
                ControlFlow controlFlow = ControlFlowFactory.getInstance(method.getProject())
                        .getControlFlow(method.getBody(), LocalsOrMyInstanceFieldsControlFlowPolicy.getInstance(), true, true);
                ControlFlowUtil.findCodeFragment(method);
                controlFlow.getInstructions()
                        .stream()
                        .forEach((com.intellij.psi.controlFlow.Instruction ins) -> LOG.debug(ins.toString()));
            } catch (AnalysisCanceledException e) {
                e.printStackTrace();
            }
            LOG.debug("### end Instruction ###");

//            new ControlFlowBuilder().build(dataFlowRunner, method.getBody());

            LOG.debug("### Instruction2 ###");
            StandardDataFlowRunner standardDataFlowRunner = new StandardDataFlowRunner(method.getBody());
            standardDataFlowRunner.analyzeMethod(method.getBody(), new StandardInstructionVisitor());
            com.intellij.codeInspection.dataFlow.instructions.Instruction[] instructions
                    = standardDataFlowRunner.getInstructions();
//            DfaValueFactory dfaValueFactory = standardDataFlowRunner.getFactory();
//            Pair<Set<Instruction>, Set<Instruction>> constConditionalExpressions
//                    = standardDataFlowRunner.getConstConditionalExpressions();

            Stream.of(instructions)
                    .forEach(instruction -> LOG.debug(instruction.getIndex() + "\t" + instruction.toString()));
            LOG.debug("### end Instruction2 ###");
        }

        LOG.info("Log error method call found in " + method.getName() + ". ");
        return logErrorCalls.get(0);
    }

    public static void highlightMethodCaller(PsiMethod method) {

        /**
         * search function call relationship
         */
        Collection<PsiReference> refMethodCaller = ReferencesSearch.search(method).findAll();
        highlightElement(refMethodCaller
                .stream()
                .map(ref ->
                        PsiTreeUtil.getParentOfType(ref.getElement(), PsiMethodCallExpression.class))
                .collect(Collectors.toList()));

        if (LOG.isDebugEnabled()) {
            LOG.debug("### method call relationship ###");
            refMethodCaller.stream()
                    .map((PsiReference ref) -> ref.getElement().toString() + "\t" + ref.getElement().getParent())
                    .forEach(LOG::debug);

            LOG.debug("### end method call relationship ###");
        }
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
        highlightElement(refExpNeedAddList.stream().map(PsiReference::getElement).collect(Collectors.toList()));

        return refExpNeedAddList;
    }

    private static void highlightElement(@NotNull List<PsiElement> elementList) {
        if (elementList.size() == 0) {
            return;
        }
        final PsiElement[] elements = elementList.toArray(new PsiElement[elementList.size()]);
        final PsiElement element = elements[0];

        ApplicationManager.getApplication().invokeLater(() ->
                        ApplicationManager.getApplication().runWriteAction(() -> {
                            final Project project = element.getProject();
                            final FileEditorManager editorManager =
                                    FileEditorManager.getInstance(project);
                            final HighlightManager highlightManager =
                                    HighlightManager.getInstance(project);
                            final EditorColorsManager editorColorsManager =
                                    EditorColorsManager.getInstance();
                            final Editor editor = editorManager.getSelectedTextEditor();
//                final EditorColorsScheme globalScheme =
//                        editorColorsManager.getGlobalScheme();
//        ColorKey.createColorKey()
        /*final TextAttributes textattributes =
                globalScheme.getAttributes(
                        EditorColors.SEARCH_RESULT_ATTRIBUTES);
                        */
                            final TextAttributes textattributes =
                                    new TextAttributes(null, JBColor.GREEN, null, EffectType.LINE_UNDERSCORE, Font.PLAIN);


                            if (editor != null) {
                                highlightManager.addOccurrenceHighlights(
                                        editor, elements, textattributes, true, null);
                                final WindowManager windowManager = WindowManager.getInstance();
                                final StatusBar statusBar = windowManager.getStatusBar(project);
                                if (statusBar != null) {
                                    statusBar.setInfo("Press Esc to remove highlighting");
                                }
                            }

                        })
        );

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
