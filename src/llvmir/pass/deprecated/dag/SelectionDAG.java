package llvmir.pass.deprecated.dag;

import llvmir.pass.Pass;
import llvmir.tree.Module;
import llvmir.tree.value.BasicBlock;
import llvmir.tree.value.Value;
import llvmir.tree.value.user.instruction.Instruction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SelectionDAG extends Pass {
    // 外部引用的 Value 对应的节点（包含常量节点、外部引用节点）
    private final List<SDNode> references = new ArrayList<>();
    // 用于记录每个 Value 对应的节点（键中包含所有节点）
    private final Map<Value, Integer> valueToNodeId = new LinkedHashMap<>();
    // 节点编号到节点的映射（键中包含指令节点、常量节点、外部引用节点）
    private final Map<Integer, SDNode> nodeIdToNode = new LinkedHashMap<>();
    // 快查表: 建立指令操作数到节点的映射
    private final Map<List<Integer>, List<SDNode>> codeToNodes = new LinkedHashMap<>();

    private void putNode(Value value, SDNode node, boolean isReference) {
        valueToNodeId.put(value, node.getId());
        if (isReference) {
            // 引用的叶节点
            references.add(node);
            nodeIdToNode.put(node.getId(), node);
        } else if (!nodeIdToNode.containsKey(node.getId())) {
            // 新指令节点
            nodeIdToNode.put(node.getId(), node);
            codeToNodes.merge(node.getOperandIds(), new ArrayList<>(), (old, newOne) -> old).add(node);
        }
    }

    private SDNode findNode(Instruction inst, List<Integer> operandIds) {
        // 遍历所有节点，查找是否有相同的节点
        for (SDNode node : codeToNodes.getOrDefault(operandIds, new ArrayList<>())) {
            // 比较指令节点的指令类型、操作数编号
            Instruction nodeInst = (Instruction) node.getOrigins().get(0);
            if (Instruction.mergeable(inst.getClass()) && nodeInst.getClass().equals(inst.getClass()) &&
                    node.getOperandIds().equals(operandIds)) {
                // 指令类型相同，且指令类型可合并，返回找到的中间节点
                return node;
            }
        }
        return null;
    }

    /*========== 通过建立 DAG 图消除局部公共子表达式 ==========*/
    private static SelectionDAG build(BasicBlock block) {
        // 由于 LLVM IR 指令满足 SSA 形式，因此每个变量的值是唯一的，赋值后不会被更改
        SelectionDAG dag = new SelectionDAG();
        int id = 0;
        for (Instruction inst : block.getAllInstructions()) {
            // 先找到所有的操作数的节点
            List<Integer> operandIds = new ArrayList<>();
            List<SDNode> operands = new ArrayList<>();
            for (Value operand : inst.getOperands()) {
                if (dag.valueToNodeId.containsKey(operand)) {
                    // 如果已经存在了，就直接添加
                    operands.add(dag.nodeIdToNode.get(dag.valueToNodeId.get(operand)));
                    operandIds.add(dag.valueToNodeId.get(operand));
                } else {
                    // 如果不存在，就新建一个节点
                    SDNode node = new SDNode(id++, operand.toString(), operand, new ArrayList<>());
                    dag.putNode(operand, node, true);
                    operands.add(node);
                    operandIds.add(id - 1);
                }
            }
            // 寻找是否已有同样运算的中间节点
            SDNode node = dag.findNode(inst, operandIds);
            if (node == null) {
                // 如果没找到，就新建一个节点
                node = new SDNode(id++, inst.toString(), inst, operands);
            } else {
                // 如果找到了，就将该节点指向合并的节点
                node.getOrigins().add(inst);
            }
            dag.putNode(inst, node, false);
        }
        return dag;
    }

    public void merge(BasicBlock block) {
        SelectionDAG dag = build(block);
        // 根据 DAG 图合并 BasicBlock 中的指令
        for (Value value : dag.valueToNodeId.keySet()) {
            Value nodeValue = dag.nodeIdToNode.get(dag.valueToNodeId.get(value)).getOrigins().get(0);
            if (nodeValue != value) {
                // 该 Value 指向的节点不是该 Value 本身，说明该 Value 被合并了
                value.replaceSelfWith(nodeValue);
                block.removeInst((Instruction) value);
                setChanged();
            }
        }
    }

    @Override
    public void pass(Module module) {
        module.getFunctions().forEach(f -> f.getBlocks().forEach(this::merge));
    }

}
