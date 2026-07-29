package top.fifthlight.fabazel.mappingmerger.operation;

import net.fabricmc.mappingio.adapter.MappingDstNsReorder;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import top.fifthlight.fabazel.mappingmerger.context.MergeContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DropNamespacesOperation implements Operation {
    private final Set<String> droppedNamespaces;

    public DropNamespacesOperation(Collection<String> namespaces) {
        this.droppedNamespaces = new HashSet<>(namespaces);
    }

    @Override
    public MemoryMappingTree run(MemoryMappingTree tree, MergeContext context) throws Exception {
        List<String> keptDstNs = new ArrayList<>();
        for (String ns : tree.getDstNamespaces()) {
            if (!droppedNamespaces.contains(ns)) {
                keptDstNs.add(ns);
            }
        }
        var newTree = new MemoryMappingTree();
        tree.accept(new MappingDstNsReorder(newTree, keptDstNs));
        return newTree;
    }
}
