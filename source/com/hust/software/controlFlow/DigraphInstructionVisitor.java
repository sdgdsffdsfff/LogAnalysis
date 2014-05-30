package com.hust.software.controlFlow;

import com.intellij.codeInspection.dataFlow.DataFlowRunner;
import com.intellij.codeInspection.dataFlow.DfaInstructionState;
import com.intellij.codeInspection.dataFlow.DfaMemoryState;
import com.intellij.codeInspection.dataFlow.StandardInstructionVisitor;
import com.intellij.codeInspection.dataFlow.instructions.BinopInstruction;
import com.intellij.codeInspection.dataFlow.instructions.ConditionalGotoInstruction;
import com.intellij.codeInspection.dataFlow.instructions.InstanceofInstruction;
import com.intellij.codeInspection.dataFlow.instructions.Instruction;
import com.intellij.openapi.diagnostic.Logger;

import java.util.stream.Stream;

/**
 * enhance StandardInstructionVisitor for generating graph for instructions
 * Created by Yan Yu on 2014-05-29.
 */
public class DigraphInstructionVisitor extends StandardInstructionVisitor {
    private static final Logger LOG = Logger.getInstance("#com.hust.software.controlFlow.GraphInstructionVisitor");

    private GraphUtil<Instruction> graph;

    public DigraphInstructionVisitor(GraphUtil<Instruction> graph) {
        super();
        this.graph = graph;
    }

    public GraphUtil getGraphUtil() {
        return graph;
    }

    private DfaInstructionState[] addEdges(Instruction instruction, DfaInstructionState[] dfaInstructionStates) {

        Stream.of(dfaInstructionStates)
                .map(DfaInstructionState::getInstruction)
                .forEach(succIns -> {
                    graph.addEdge(instruction, succIns);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("graph " + graph.toString() + " add edge " + instruction + "\t->\t" + succIns);
                    }
                });
        return dfaInstructionStates;
    }

    @Override
    public DfaInstructionState[] visitConditionalGoto(ConditionalGotoInstruction instruction, DataFlowRunner runner, DfaMemoryState memState) {
        return addEdges(instruction, super.visitConditionalGoto(instruction, runner, memState));
    }

    @Override
    public DfaInstructionState[] visitBinop(BinopInstruction instruction, DataFlowRunner runner, DfaMemoryState memState) {
        return addEdges(instruction, super.visitBinop(instruction, runner, memState));
    }

    @Override
    public DfaInstructionState[] visitInstanceof(InstanceofInstruction instruction, DataFlowRunner runner, DfaMemoryState memState) {
        return addEdges(instruction, super.visitInstanceof(instruction, runner, memState));
    }

   /* @Override
    public DfaInstructionState[] visitCast(MethodCallInstruction instruction, DataFlowRunner runner, DfaMemoryState memState) {
        return addEdges(instruction, super.visitCast(instruction, runner, memState));
    }

    @Override
    public DfaInstructionState[] visitEmptyInstruction(EmptyInstruction instruction, DataFlowRunner runner, DfaMemoryState before) {
        return addEdges(instruction, super.visitEmptyInstruction(instruction, runner, before));
    }

    @Override
    public DfaInstructionState[] visitEmptyStack(EmptyStackInstruction instruction, DataFlowRunner runner, DfaMemoryState memState) {
        return addEdges(instruction, super.visitEmptyStack(instruction, runner, memState));
    }

    @Override
    public DfaInstructionState[] visitFlushVariable(FlushVariableInstruction instruction, DataFlowRunner runner, DfaMemoryState memState) {
        return addEdges(instruction, super.visitFlushVariable(instruction, runner, memState));
    }

    @Override
    public DfaInstructionState[] visitLambdaExpression(LambdaInstruction instruction, DataFlowRunner runner, DfaMemoryState memState) {
        return addEdges(instruction, super.visitLambdaExpression(instruction, runner, memState));
    }

    @Override
    public DfaInstructionState[] visitNot(NotInstruction instruction, DataFlowRunner runner, DfaMemoryState memState) {
        return addEdges(instruction, super.visitNot(instruction, runner, memState));
    }

    @Override
    public DfaInstructionState[] visitTypeCast(TypeCastInstruction instruction, DataFlowRunner runner, DfaMemoryState memState) {
        return addEdges(instruction, super.visitTypeCast(instruction, runner, memState));
    }

    @Override
    public DfaInstructionState[] visitAssign(AssignInstruction instruction, DataFlowRunner runner, DfaMemoryState memState) {
        return addEdges(instruction, super.visitAssign(instruction, runner, memState));
    }

    @Override
    public DfaInstructionState[] visitCheckReturnValue(CheckReturnValueInstruction instruction, DataFlowRunner runner, DfaMemoryState memState) {
        return addEdges(instruction, super.visitCheckReturnValue(instruction, runner, memState));
    }

    @Override
    public DfaInstructionState[] visitFieldReference(FieldReferenceInstruction instruction, DataFlowRunner runner, DfaMemoryState memState) {
        return addEdges(instruction, super.visitFieldReference(instruction, runner, memState));
    }

    @Override
    public DfaInstructionState[] visitMethodCall(MethodCallInstruction instruction, DataFlowRunner runner, DfaMemoryState memState) {
        return addEdges(instruction, super.visitMethodCall(instruction, runner, memState));
    }

    @Override
    public DfaInstructionState[] visitPush(PushInstruction instruction, DataFlowRunner runner, DfaMemoryState memState) {
        return addEdges(instruction, super.visitPush(instruction, runner, memState));
    }*/


}
