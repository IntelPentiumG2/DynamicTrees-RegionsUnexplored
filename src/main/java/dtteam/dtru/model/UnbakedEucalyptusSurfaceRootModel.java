package dtteam.dtru.model;

import com.dtteam.dynamictrees.model.blockstate.SurfaceRootBlockStateModel;
import com.dtteam.dynamictrees.model.parts.SurfaceRootModelPart;
import com.dtteam.dynamictrees.utility.CoordUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

/**
 * Eucalyptus surface roots, carrying the same tinted overlay layer as the branches.
 */
@OnlyIn(Dist.CLIENT)
public record UnbakedEucalyptusSurfaceRootModel(Identifier barkTexture,
                                                Identifier overlayTexture) implements CustomUnbakedBlockStateModel {

    public static final String TEXTURES = "textures";
    public static final String BARK_TEXTURE = "bark";
    public static final String OVERLAY_TEXTURE = "overlay";

    private record Textures(Identifier bark, Identifier overlay) {
        static final MapCodec<Textures> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Identifier.CODEC.fieldOf(BARK_TEXTURE).forGetter(Textures::bark),
                Identifier.CODEC.fieldOf(OVERLAY_TEXTURE).forGetter(Textures::overlay)
        ).apply(i, Textures::new));
    }

    public static final MapCodec<UnbakedEucalyptusSurfaceRootModel> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Textures.CODEC.codec().fieldOf(TEXTURES).forGetter(m -> new Textures(m.barkTexture(), m.overlayTexture()))
    ).apply(i, textures -> new UnbakedEucalyptusSurfaceRootModel(textures.bark(), textures.overlay())));

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return CODEC;
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
    }

    @Override
    public BlockStateModel bake(ModelBaker baker) {
        final SurfaceRootModelPart[][] sleeves = new SurfaceRootModelPart[4][7];
        final SurfaceRootModelPart[][] cores = new SurfaceRootModelPart[2][8]; //8 Cores for 2 axis(X, Z) with the bark texture on all 6 sides rotated appropriately.
        final SurfaceRootModelPart[][] verts = new SurfaceRootModelPart[4][8];

        final Material.Baked bark = baker.materials().get(new Material(barkTexture, false), barkTexture::toDebugFileName);
        final Material.Baked overlay = baker.materials().get(new Material(overlayTexture, false), overlayTexture::toDebugFileName);

        final var unbakedCores = new EucalyptusModelParts.OverlaidRootCore(bark, overlay);
        final var unbakedSleeves = new EucalyptusModelParts.OverlaidRootSleeve(bark, overlay);
        final var unbakedVerts = new EucalyptusModelParts.OverlaidRootVert(bark, overlay);

        for (int r = 0; r < 8; r++) {
            final int radius = r + 1;
            if (radius < 8) {
                for (Direction dir : CoordUtils.HORIZONTALS) {
                    final int horIndex = dir.get2DDataValue();
                    sleeves[horIndex][r] = unbakedSleeves.bake(baker, radius, dir);
                    verts[horIndex][r] = unbakedVerts.bake(baker, radius, dir);
                }
            }
            cores[0][r] = unbakedCores.bake(baker, radius, Direction.Axis.Z); //NORTH<->SOUTH
            cores[1][r] = unbakedCores.bake(baker, radius, Direction.Axis.X); //WEST<->EAST
        }

        return new SurfaceRootBlockStateModel(cores, sleeves, verts, bark);
    }

}
