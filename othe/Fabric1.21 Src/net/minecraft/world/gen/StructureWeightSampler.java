/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.structure.JigsawJunction;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.StructureTerrainAdaptation;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

/**
 * Applies weights to noise values if they are near structures, placing terrain under them and hollowing out the space above them.
 */
public class StructureWeightSampler
implements DensityFunctionTypes.Beardifying {
    public static final int INDEX_OFFSET = 12;
    private static final int EDGE_LENGTH = 24;
    private static final float[] STRUCTURE_WEIGHT_TABLE = Util.make(new float[13824], array -> {
        for (int i = 0; i < 24; ++i) {
            for (int j = 0; j < 24; ++j) {
                for (int k = 0; k < 24; ++k) {
                    array[i * 24 * 24 + j * 24 + k] = (float)StructureWeightSampler.calculateStructureWeight(j - 12, k - 12, i - 12);
                }
            }
        }
    });
    private final ObjectListIterator<Piece> pieceIterator;
    private final ObjectListIterator<JigsawJunction> junctionIterator;

    public static StructureWeightSampler createStructureWeightSampler(StructureAccessor world, ChunkPos pos) {
        int i = pos.getStartX();
        int j = pos.getStartZ();
        ObjectArrayList objectList = new ObjectArrayList(10);
        ObjectArrayList objectList2 = new ObjectArrayList(32);
        world.getStructureStarts(pos, structure -> structure.getTerrainAdaptation() != StructureTerrainAdaptation.NONE).forEach(start -> {
            StructureTerrainAdaptation structureTerrainAdaptation = start.getStructure().getTerrainAdaptation();
            for (StructurePiece structurePiece : start.getChildren()) {
                if (!structurePiece.intersectsChunk(pos, 12)) continue;
                if (structurePiece instanceof PoolStructurePiece) {
                    PoolStructurePiece poolStructurePiece = (PoolStructurePiece)structurePiece;
                    StructurePool.Projection projection = poolStructurePiece.getPoolElement().getProjection();
                    if (projection == StructurePool.Projection.RIGID) {
                        objectList.add(new Piece(poolStructurePiece.getBoundingBox(), structureTerrainAdaptation, poolStructurePiece.getGroundLevelDelta()));
                    }
                    for (JigsawJunction jigsawJunction : poolStructurePiece.getJunctions()) {
                        int i = jigsawJunction.getSourceX();
                        int j = jigsawJunction.getSourceZ();
                        if (i <= i - 12 || j <= j - 12 || i >= i + 15 + 12 || j >= j + 15 + 12) continue;
                        objectList2.add(jigsawJunction);
                    }
                    continue;
                }
                objectList.add(new Piece(structurePiece.getBoundingBox(), structureTerrainAdaptation, 0));
            }
        });
        return new StructureWeightSampler((ObjectListIterator<Piece>)objectList.iterator(), (ObjectListIterator<JigsawJunction>)objectList2.iterator());
    }

    @VisibleForTesting
    public StructureWeightSampler(ObjectListIterator<Piece> pieceIterator, ObjectListIterator<JigsawJunction> junctionIterator) {
        this.pieceIterator = pieceIterator;
        this.junctionIterator = junctionIterator;
    }

    @Override
    public double sample(DensityFunction.NoisePos pos) {
        int m;
        int l;
        int i = pos.blockX();
        int j = pos.blockY();
        int k = pos.blockZ();
        double d = 0.0;
        while (this.pieceIterator.hasNext()) {
            Piece piece = (Piece)this.pieceIterator.next();
            BlockBox blockBox = piece.box();
            l = piece.groundLevelDelta();
            m = Math.max(0, Math.max(blockBox.getMinX() - i, i - blockBox.getMaxX()));
            int n = Math.max(0, Math.max(blockBox.getMinZ() - k, k - blockBox.getMaxZ()));
            int o = blockBox.getMinY() + l;
            int p = j - o;
            int q = switch (piece.terrainAdjustment()) {
                default -> throw new MatchException(null, null);
                case StructureTerrainAdaptation.NONE -> 0;
                case StructureTerrainAdaptation.BURY, StructureTerrainAdaptation.BEARD_THIN -> p;
                case StructureTerrainAdaptation.BEARD_BOX -> Math.max(0, Math.max(o - j, j - blockBox.getMaxY()));
                case StructureTerrainAdaptation.ENCAPSULATE -> Math.max(0, Math.max(blockBox.getMinY() - j, j - blockBox.getMaxY()));
            };
            d += (switch (piece.terrainAdjustment()) {
                default -> throw new MatchException(null, null);
                case StructureTerrainAdaptation.NONE -> 0.0;
                case StructureTerrainAdaptation.BURY -> StructureWeightSampler.getMagnitudeWeight(m, (double)q / 2.0, n);
                case StructureTerrainAdaptation.BEARD_THIN, StructureTerrainAdaptation.BEARD_BOX -> StructureWeightSampler.getStructureWeight(m, q, n, p) * 0.8;
                case StructureTerrainAdaptation.ENCAPSULATE -> StructureWeightSampler.getMagnitudeWeight((double)m / 2.0, (double)q / 2.0, (double)n / 2.0) * 0.8;
            });
        }
        this.pieceIterator.back(Integer.MAX_VALUE);
        while (this.junctionIterator.hasNext()) {
            JigsawJunction jigsawJunction = (JigsawJunction)this.junctionIterator.next();
            int r = i - jigsawJunction.getSourceX();
            l = j - jigsawJunction.getSourceGroundY();
            m = k - jigsawJunction.getSourceZ();
            d += StructureWeightSampler.getStructureWeight(r, l, m, l) * 0.4;
        }
        this.junctionIterator.back(Integer.MAX_VALUE);
        return d;
    }

    @Override
    public double minValue() {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double maxValue() {
        return Double.POSITIVE_INFINITY;
    }

    private static double getMagnitudeWeight(double x, double y, double z) {
        double d = MathHelper.magnitude(x, y, z);
        return MathHelper.clampedMap(d, 0.0, 6.0, 1.0, 0.0);
    }

    /**
     * Gets the structure weight from the array from the given position, or 0 if the position is out of bounds.
     */
    private static double getStructureWeight(int x, int y, int z, int yy) {
        int i = x + 12;
        int j = y + 12;
        int k = z + 12;
        if (!(StructureWeightSampler.indexInBounds(i) && StructureWeightSampler.indexInBounds(j) && StructureWeightSampler.indexInBounds(k))) {
            return 0.0;
        }
        double d = (double)yy + 0.5;
        double e = MathHelper.squaredMagnitude(x, d, z);
        double f = -d * MathHelper.fastInverseSqrt(e / 2.0) / 2.0;
        return f * (double)STRUCTURE_WEIGHT_TABLE[k * 24 * 24 + i * 24 + j];
    }

    private static boolean indexInBounds(int i) {
        return i >= 0 && i < 24;
    }

    /**
     * Calculates the structure weight for the given position.
     * <p>The weight increases as x and z approach {@code (0, 0)}, and positive y values make the weight negative while negative y values make the weight positive.
     */
    private static double calculateStructureWeight(int x, int y, int z) {
        return StructureWeightSampler.structureWeight(x, (double)y + 0.5, z);
    }

    private static double structureWeight(int x, double y, int z) {
        double d = MathHelper.squaredMagnitude(x, y, z);
        double e = Math.pow(Math.E, -d / 16.0);
        return e;
    }

    @VisibleForTesting
    public record Piece(BlockBox box, StructureTerrainAdaptation terrainAdjustment, int groundLevelDelta) {
    }
}

