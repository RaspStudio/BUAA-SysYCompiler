package backend.regalloc;

import backend.translate.MipsMapper;
import backend.value.MipsBlock;
import backend.value.MipsFunction;
import backend.value.inst.MipsInst;
import backend.value.inst.atype.MipsLoadWord;
import backend.value.inst.atype.MipsSaveWord;
import backend.value.meta.MipsAddr;
import backend.value.meta.MipsReg;
import backend.value.meta.MipsRegs;
import util.Graph;

import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class Allocator extends RegAllocator<MipsInst, MipsReg, MipsBlock, MipsFunction> {
    private final MipsMapper mapper;

    /**
     * @param functions 待分配寄存器的函数列表
     * @param validColors 可用于分配的寄存器编号列表
     */
    public Allocator(List<MipsFunction> functions, List<Integer> validColors, MipsMapper mapper) {
        super(functions, validColors);
        this.mapper = mapper;
    }

    @Override
    protected void allocate(MipsFunction function) {
        final Map<MipsReg, Integer> regToColor = newMap();
        while (true) {
            Graph<MipsReg> graph = buildConflictGraph(function);
            Set<MipsReg> spilled = assignColors(graph, regToColor);
            if (spilled.isEmpty()) {
                // 分配成功
                break;
            } else {
                // 分配失败，需要溢出并重新分配
                regToColor.clear();
                spilled.forEach(r -> spill(function, r));
            }
        }
        regToColor.forEach(MipsReg::addColor);
        mapper.putSaveRegs(function, getUsedColors(regToColor));
    }

    private Set<MipsReg> assignColors(Graph<MipsReg> graph, Map<MipsReg, Integer> regToColor) {
        Set<MipsReg> spilled = newSet();
        // 第一步 通过图着色算法筛选第一遍需要溢出的节点
        Graph<MipsReg> graphCopy = graph.copy();
        Stack<MipsReg> waitToColor = new Stack<>();
        while (!graphCopy.isEmpty()) {
            // 当图非空时，遍历所有节点找出可以着色的节点
            boolean found = true;
            while (found) {
                found = false;
                Set<MipsReg> needRemove = newSet();
                for (MipsReg reg : graphCopy.getNodes()) {
                    if (graphCopy.getDegree(reg) < validColors.size()) {
                        waitToColor.push(reg);
                        needRemove.add(reg);
                        found = true;
                    }
                }
                needRemove.forEach(graphCopy::removeNode);
            }
            // 如果没有可以着色的节点，说明图中存在冲突，需要溢出
            if (!graphCopy.isEmpty()) {
                MipsReg toSpill = graphCopy.getNodes().last();
                spilled.add(toSpill);
                graphCopy.removeNode(toSpill);
            }
        }

        // 第二步 把预着色的节点弄进去
        ListIterator<MipsReg> filterPreColor = waitToColor.listIterator();
        while (filterPreColor.hasNext()) {
            MipsReg reg = filterPreColor.next();
            if (reg.hasColor()) {
                regToColor.put(reg, reg.getColor());
                filterPreColor.remove();
            }
        }

        // 第三步 把不是预着色的节点弄出来筛选出第二遍需要溢出的节点
        while (!waitToColor.isEmpty()) {
            MipsReg reg = waitToColor.pop();
            // 筛选当前节点能够使用的颜色
            Set<Integer> usedColors = newSet();
            for (MipsReg neighbor : graph.getNeighbors(reg)) {
                if (regToColor.containsKey(neighbor)) {
                    usedColors.add(regToColor.get(neighbor));
                }
            }
            List<Integer> availableColors = listOf(validColors);
            availableColors.removeAll(usedColors);
            // 为当前节点选择一个颜色并着色
            if (availableColors.isEmpty()) {
                // 如果没有可用的颜色，说明当前节点需要溢出
                spilled.add(reg);// todo: 是否能够再优化：每溢出一个节点重来一次
            } else {
                // 如果有可用的颜色，选择一个颜色并着色
                regToColor.put(reg, availableColors.get(0));
            }
        }

        return spilled;
    }

    @Override
    protected void spill(MipsFunction function, MipsReg toSpill) {
        // 为溢出的寄存器分配一个栈空间
        MipsAddr spillTo = function.stack().allocData(4);
        int counter = 0;
        for (MipsBlock block : function.getBlocks()) {
            ListIterator<MipsInst> insts = block.getInsts().listIterator();
            while (insts.hasNext()) {
                MipsInst inst = insts.next();
                if (inst.getDefs().contains(toSpill)) {
                    // 如果当前指令定义了需要溢出的寄存器，需要将该指令存下来
                    insts.add(new MipsSaveWord(block, toSpill, spillTo));
                }
                if (inst.getUses().contains(toSpill)) {
                    // 如果当前指令使用了需要溢出的寄存器，需要将该指令读出来
                    MipsReg load = new MipsReg(toSpill.name() + "_" + (counter++));
                    inst.replaceUse(toSpill, load);
                    insts.previous();
                    insts.add(new MipsLoadWord(block, load, spillTo));
                    insts.next();
                }
            }
        }
    }

    private static List<Integer> getUsedColors(Map<MipsReg, Integer> regToColor) {
        Set<Integer> forAlloc = new HashSet<>(MipsRegs.forAlloc());
        Set<Integer> usedColors = new HashSet<>(regToColor.values());
        usedColors.removeIf(o -> !forAlloc.contains(o));
        return listOf(usedColors);
    }
}
