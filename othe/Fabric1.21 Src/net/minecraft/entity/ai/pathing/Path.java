/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.pathing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.TargetPathNode;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class Path {
    private final List<PathNode> nodes;
    @Nullable
    private DebugNodeInfo debugNodeInfos;
    private int currentNodeIndex;
    private final BlockPos target;
    private final float manhattanDistanceFromTarget;
    private final boolean reachesTarget;

    public Path(List<PathNode> nodes, BlockPos target, boolean reachesTarget) {
        this.nodes = nodes;
        this.target = target;
        this.manhattanDistanceFromTarget = nodes.isEmpty() ? Float.MAX_VALUE : this.nodes.get(this.nodes.size() - 1).getManhattanDistance(this.target);
        this.reachesTarget = reachesTarget;
    }

    public void next() {
        ++this.currentNodeIndex;
    }

    public boolean isStart() {
        return this.currentNodeIndex <= 0;
    }

    public boolean isFinished() {
        return this.currentNodeIndex >= this.nodes.size();
    }

    @Nullable
    public PathNode getEnd() {
        if (!this.nodes.isEmpty()) {
            return this.nodes.get(this.nodes.size() - 1);
        }
        return null;
    }

    public PathNode getNode(int index) {
        return this.nodes.get(index);
    }

    public void setLength(int length) {
        if (this.nodes.size() > length) {
            this.nodes.subList(length, this.nodes.size()).clear();
        }
    }

    public void setNode(int index, PathNode node) {
        this.nodes.set(index, node);
    }

    public int getLength() {
        return this.nodes.size();
    }

    public int getCurrentNodeIndex() {
        return this.currentNodeIndex;
    }

    public void setCurrentNodeIndex(int nodeIndex) {
        this.currentNodeIndex = nodeIndex;
    }

    public Vec3d getNodePosition(Entity entity, int index) {
        PathNode pathNode = this.nodes.get(index);
        double d = (double)pathNode.x + (double)((int)(entity.getWidth() + 1.0f)) * 0.5;
        double e = pathNode.y;
        double f = (double)pathNode.z + (double)((int)(entity.getWidth() + 1.0f)) * 0.5;
        return new Vec3d(d, e, f);
    }

    public BlockPos getNodePos(int index) {
        return this.nodes.get(index).getBlockPos();
    }

    public Vec3d getNodePosition(Entity entity) {
        return this.getNodePosition(entity, this.currentNodeIndex);
    }

    public BlockPos getCurrentNodePos() {
        return this.nodes.get(this.currentNodeIndex).getBlockPos();
    }

    public PathNode getCurrentNode() {
        return this.nodes.get(this.currentNodeIndex);
    }

    @Nullable
    public PathNode getLastNode() {
        return this.currentNodeIndex > 0 ? this.nodes.get(this.currentNodeIndex - 1) : null;
    }

    public boolean equalsPath(@Nullable Path o) {
        if (o == null) {
            return false;
        }
        if (o.nodes.size() != this.nodes.size()) {
            return false;
        }
        for (int i = 0; i < this.nodes.size(); ++i) {
            PathNode pathNode = this.nodes.get(i);
            PathNode pathNode2 = o.nodes.get(i);
            if (pathNode.x == pathNode2.x && pathNode.y == pathNode2.y && pathNode.z == pathNode2.z) continue;
            return false;
        }
        return true;
    }

    public boolean reachesTarget() {
        return this.reachesTarget;
    }

    @Debug
    void setDebugInfo(PathNode[] debugNodes, PathNode[] debugSecondNodes, Set<TargetPathNode> debugTargetNodes) {
        this.debugNodeInfos = new DebugNodeInfo(debugNodes, debugSecondNodes, debugTargetNodes);
    }

    @Nullable
    public DebugNodeInfo getDebugNodeInfos() {
        return this.debugNodeInfos;
    }

    public void toBuf(PacketByteBuf buf2) {
        if (this.debugNodeInfos == null || this.debugNodeInfos.targetNodes.isEmpty()) {
            return;
        }
        buf2.writeBoolean(this.reachesTarget);
        buf2.writeInt(this.currentNodeIndex);
        buf2.writeBlockPos(this.target);
        buf2.writeCollection(this.nodes, (buf, node) -> node.write((PacketByteBuf)buf));
        this.debugNodeInfos.write(buf2);
    }

    public static Path fromBuf(PacketByteBuf buf) {
        boolean bl = buf.readBoolean();
        int i = buf.readInt();
        BlockPos blockPos = buf.readBlockPos();
        List<PathNode> list = buf.readList(PathNode::fromBuf);
        DebugNodeInfo debugNodeInfo = DebugNodeInfo.fromBuf(buf);
        Path path = new Path(list, blockPos, bl);
        path.debugNodeInfos = debugNodeInfo;
        path.currentNodeIndex = i;
        return path;
    }

    public String toString() {
        return "Path(length=" + this.nodes.size() + ")";
    }

    public BlockPos getTarget() {
        return this.target;
    }

    public float getManhattanDistanceFromTarget() {
        return this.manhattanDistanceFromTarget;
    }

    static PathNode[] nodesFromBuf(PacketByteBuf buf) {
        PathNode[] pathNodes = new PathNode[buf.readVarInt()];
        for (int i = 0; i < pathNodes.length; ++i) {
            pathNodes[i] = PathNode.fromBuf(buf);
        }
        return pathNodes;
    }

    static void write(PacketByteBuf buf, PathNode[] nodes) {
        buf.writeVarInt(nodes.length);
        for (PathNode pathNode : nodes) {
            pathNode.write(buf);
        }
    }

    public Path copy() {
        Path path = new Path(this.nodes, this.target, this.reachesTarget);
        path.debugNodeInfos = this.debugNodeInfos;
        path.currentNodeIndex = this.currentNodeIndex;
        return path;
    }

    public record DebugNodeInfo(PathNode[] openSet, PathNode[] closedSet, Set<TargetPathNode> targetNodes) {
        public void write(PacketByteBuf buf2) {
            buf2.writeCollection(this.targetNodes, (buf, node) -> node.write((PacketByteBuf)buf));
            Path.write(buf2, this.openSet);
            Path.write(buf2, this.closedSet);
        }

        public static DebugNodeInfo fromBuf(PacketByteBuf buf) {
            HashSet hashSet = buf.readCollection(HashSet::new, TargetPathNode::fromBuffer);
            PathNode[] pathNodes = Path.nodesFromBuf(buf);
            PathNode[] pathNodes2 = Path.nodesFromBuf(buf);
            return new DebugNodeInfo(pathNodes, pathNodes2, hashSet);
        }
    }
}

