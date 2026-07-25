package dtteam.dtru.model;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.model.blockstate.UnbakedBranchModel;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.utility.IdentifierUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

import java.util.Optional;

/**
 * Eucalyptus branches, which are Dynamic Trees' branches plus a tinted overlay layer.
 *
 * <p>Replaces the model loader and geometry pair used before 26.1: block state definitions now name
 * the model type directly, so a codec over the textures is all that is needed.</p>
 */
@OnlyIn(Dist.CLIENT)
public record UnbakedEucalyptusBranchModel(Identifier barkTexture, Identifier ringsTexture,
                                           Identifier overlayTexture,
                                           Optional<Family> family) implements CustomUnbakedBlockStateModel {

    public static final String BARK_TEXTURE = "bark";
    public static final String RINGS_TEXTURE = "rings";
    public static final String OVERLAY_TEXTURE = "overlay";
    public static final String TEXTURES = "textures";
    public static final String FAMILY = "family";

    private record BranchTextures(Identifier bark, Identifier rings, Identifier overlay) {
        static final MapCodec<BranchTextures> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Identifier.CODEC.fieldOf(BARK_TEXTURE).forGetter(BranchTextures::bark),
                Identifier.CODEC.fieldOf(RINGS_TEXTURE).forGetter(BranchTextures::rings),
                Identifier.CODEC.fieldOf(OVERLAY_TEXTURE).forGetter(BranchTextures::overlay)
        ).apply(i, BranchTextures::new));
    }

    public static final MapCodec<UnbakedEucalyptusBranchModel> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BranchTextures.CODEC.codec().fieldOf(TEXTURES)
                    .forGetter(m -> new BranchTextures(m.barkTexture(), m.ringsTexture(), m.overlayTexture())),
            Family.CODEC.optionalFieldOf(FAMILY).forGetter(UnbakedEucalyptusBranchModel::family)
    ).apply(i, (textures, family) ->
            new UnbakedEucalyptusBranchModel(textures.bark(), textures.rings(), textures.overlay(), family)));

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return CODEC;
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
    }

    @Override
    public BlockStateModel bake(ModelBaker baker) {
        final Material.Baked bark = material(baker, barkTexture);
        final Material.Baked rings = material(baker, ringsTexture);
        final Material.Baked overlay = material(baker, overlayTexture);

        final var regular = UnbakedBranchModel.bakeBasic(baker,
                new EucalyptusModelParts.OverlaidCore(bark, overlay),
                new EucalyptusModelParts.OverlaidSleeve(bark, overlay),
                new EucalyptusModelParts.OverlaidCore(rings, overlay),
                null);

        if (family.isPresent() && family.get().isThick()) {
            final Identifier thickRingsTexture = getThickRingsTexture(ringsTexture);
            final Material.Baked thickRings = material(baker, thickRingsTexture);

            return UnbakedBranchModel.bakeThick(baker, regular,
                    new EucalyptusModelParts.OverlaidThickTrunk(bark, overlay, false),
                    new EucalyptusModelParts.OverlaidThickTrunk(thickRings, overlay, true));
        }
        return regular;
    }

    private static Material.Baked material(ModelBaker baker, Identifier texture) {
        return baker.materials().get(new Material(texture), texture::toDebugFileName);
    }

    private static Identifier getThickRingsTexture(Identifier ringsTexture) {
        if (ringsTexture.equals(DynamicTrees.location("block/air")))
            return ringsTexture;
        return IdentifierUtils.suffix(ringsTexture, "_thick");
    }

}
