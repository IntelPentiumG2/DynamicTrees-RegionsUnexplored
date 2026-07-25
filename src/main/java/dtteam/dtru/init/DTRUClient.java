package dtteam.dtru.init;

import com.dtteam.dynamictrees.tree.family.Family;
import dtteam.dtru.DynamicTreesRU;
import dtteam.dtru.tree.EucalyptusFamily;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import java.util.List;

@EventBusSubscriber(modid = DynamicTreesRU.MOD_ID, value = Dist.CLIENT)
public class DTRUClient {

    /**
     * Registers the shifting colour of eucalyptus bark. The overlay layer of the branch and root
     * models carries tint index 0, which resolves to the first source registered for the block.
     *
     * <p>Render layers are no longer assigned here: since 26.1 they follow from the textures a model
     * uses rather than from a per-block registration.</p>
     */
    @SubscribeEvent
    public static void registerBlockColorHandlers(RegisterColorHandlersEvent.BlockTintSources event) {
        Family.REGISTRY.getAll().stream()
                .filter(EucalyptusFamily.class::isInstance)
                .map(EucalyptusFamily.class::cast)
                .forEach(family -> {
                    final List<BlockTintSource> sources = List.of(new EucalyptusTintSource(family));
                    family.getBranch().ifPresent(branch -> event.register(sources, branch));
                    family.getSurfaceRoot().ifPresent(root -> event.register(sources, root));
                });
    }

    private record EucalyptusTintSource(EucalyptusFamily family) implements BlockTintSource {

        /** Out of world there is no position to derive a hue from, so the overlay is left unshaded. */
        private static final int NO_POSITION = 0xFFFFFFFF;

        @Override
        public int color(BlockState state) {
            return NO_POSITION;
        }

        @Override
        public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            return family.branchColorMultiplier(state, level, pos);
        }
    }
}
