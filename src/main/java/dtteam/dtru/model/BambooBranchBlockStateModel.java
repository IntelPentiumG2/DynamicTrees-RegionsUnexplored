package dtteam.dtru.model;

import com.dtteam.dynamictrees.api.network.Connections;
import com.dtteam.dynamictrees.model.BlockStateModelWithConnectionData;
import com.dtteam.dynamictrees.model.ModelConnections;
import com.dtteam.dynamictrees.model.ModelHelper;
import com.dtteam.dynamictrees.model.blockstate.BranchBlockStateModel;
import com.dtteam.dynamictrees.model.parts.BranchModelPart;
import com.dtteam.dynamictrees.tree.TreeHelper;
import dtteam.dtru.block.BambooBranchBlock;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;

import java.util.List;

/**
 * A regular branch with foliage added to culms that carry it. Bamboo never grows past radius 4, so
 * anything wider is left unrendered rather than falling back to the thick trunk shape.
 */
@OnlyIn(Dist.CLIENT)
public record BambooBranchBlockStateModel(
        BranchBlockStateModel branch,
        BranchModelPart leaves
) implements DynamicBlockStateModel, BlockStateModelWithConnectionData {

    public static final int MAX_RADIUS = 4;

    @Override
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return ModelHelper.getModelConnections(level, pos, state);
    }

    @Override
    public void collectParts(BlockState state, List<BlockStateModelPart> parts, Connections connectionsData) {
        if (TreeHelper.getRadius(state) > MAX_RADIUS) return;

        branch.collectParts(state, parts, connectionsData);

        if (!hasLeaves(state) || !(connectionsData instanceof ModelConnections)) return;

        final int[] connections = connectionsData.getAllRadii();
        if (BranchBlockStateModel.countConnections(connections) == 0
                && ((ModelConnections) connectionsData).getRingOnly() != null) {
            return;
        }

        // Foliage only hangs off culms whose source is above or below them.
        final Direction sourceDir = BranchBlockStateModel.get3DSourceDir(TreeHelper.getRadius(state), connections);
        if (sourceDir != null && sourceDir.getAxis() == Direction.Axis.Y) {
            parts.add(leaves);
        }
    }

    private static boolean hasLeaves(BlockState state) {
        return state.hasProperty(BambooBranchBlock.LEAVES) && state.getValue(BambooBranchBlock.LEAVES);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        collectParts(state, parts, ModelHelper.getModelConnections(level, pos, state));
    }

    @Override
    public Material.Baked particleMaterial() {
        return branch.particleMaterial();
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return branch.materialFlags() | leaves.materialFlags();
    }

}
