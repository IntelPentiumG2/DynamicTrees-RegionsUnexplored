package dtteam.dtru.tree;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.utility.IdentifierUtils;
import dtteam.dtru.DynamicTreesRU;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.regions_unexplored.config.RUConfigHandler;
import net.regions_unexplored.config.state.client.RUClientConfig;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class EucalyptusFamily extends Family {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(EucalyptusFamily::new);

    /** Texture override key for the tinted layer drawn over the bark. */
    public static final String OVERLAY = "overlay";

    public static final Identifier PLAIN_STRIPPED_BRANCH_GENERATOR = DynamicTreesRU.location("plain_stripped_branch");
    public static final Identifier SURFACE_ROOT_GENERATOR = DynamicTreesRU.location("eucalyptus_surface_root");

    public EucalyptusFamily(Identifier name) {
        super(name);
    }

    @Override
    public Identifier getBranchLoader() {
        return DynamicTreesRU.EUCALYPTUS;
    }

    @Override
    public Identifier getSurfaceRootLoader() {
        return DynamicTreesRU.EUCALYPTUS_SURFACE_ROOT;
    }

    @Override
    public void addBranchTextures(BiConsumer<String, Identifier> textureConsumer, Identifier primitiveLogLocation, Block sourceBlock) {
        final Map<String, Identifier> textures = new HashMap<>();
        super.addBranchTextures(textures::put, primitiveLogLocation, sourceBlock);
        textures.put(OVERLAY, getTexturePath(OVERLAY)
                .orElseGet(() -> IdentifierUtils.suffix(textures.get("bark"), "_overlay")));
        textures.forEach(textureConsumer);
    }

    /**
     * Only the unstripped log has an overlay texture, so the stripped branch falls back to Dynamic
     * Trees' plain branch model.
     */
    @Override
    public List<Identifier> getBlockModelGenerators() {
        final List<Identifier> generators = new LinkedList<>(super.getBlockModelGenerators());
        if (generators.remove(DynamicTrees.location("stripped_branch"))) {
            generators.add(PLAIN_STRIPPED_BRANCH_GENERATOR);
        }
        if (generators.remove(DynamicTrees.location("surface_root"))) {
            generators.add(SURFACE_ROOT_GENERATOR);
        }
        return generators;
    }

    @OnlyIn(Dist.CLIENT)
    public int branchColorMultiplier(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        // Regions Unexplored 0.7.0 dropped the ConfigValue-based client config; the values now live
        // on a plain codec-backed object held by RUConfigHandler.
        final RUClientConfig.EucalyptusColors colors = RUConfigHandler.CLIENT.eucalyptusColors;
        Color rainbow = Color.getHSBColor(
                ((float) pos.getX() + (float) pos.getY() + (float) pos.getZ()) / (float) colors.transitionSize,
                (float) colors.saturation,
                (float) colors.brightness);
        return rainbow.getRGB();
    }
}
