package dtteam.dtru.model;

import com.dtteam.dynamictrees.model.blockstate.BranchBlockStateModel;
import com.dtteam.dynamictrees.model.blockstate.UnbakedBranchModel;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

/**
 * Giant bamboo culms, with their own UV mapping and an optional foliage layer.
 */
public record UnbakedBambooBranchModel(Identifier barkTexture, Identifier ringsTexture,
                                       Identifier leavesTexture) implements CustomUnbakedBlockStateModel {

    public static final String BARK_TEXTURE = "bark";
    public static final String RINGS_TEXTURE = "rings";
    public static final String LEAVES_TEXTURE = "leaves";
    public static final String TEXTURES = "textures";

    private record BambooTextures(Identifier bark, Identifier rings, Identifier leaves) {
        static final MapCodec<BambooTextures> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Identifier.CODEC.fieldOf(BARK_TEXTURE).forGetter(BambooTextures::bark),
                Identifier.CODEC.fieldOf(RINGS_TEXTURE).forGetter(BambooTextures::rings),
                Identifier.CODEC.fieldOf(LEAVES_TEXTURE).forGetter(BambooTextures::leaves)
        ).apply(i, BambooTextures::new));
    }

    public static final MapCodec<UnbakedBambooBranchModel> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BambooTextures.CODEC.codec().fieldOf(TEXTURES)
                    .forGetter(m -> new BambooTextures(m.barkTexture(), m.ringsTexture(), m.leavesTexture()))
    ).apply(i, textures -> new UnbakedBambooBranchModel(textures.bark(), textures.rings(), textures.leaves())));

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
        final Material.Baked leaves = material(baker, leavesTexture);

        final BranchBlockStateModel branch = UnbakedBranchModel.bakeBasic(baker,
                new BambooModelParts.Core(bark, false),
                new BambooModelParts.Sleeve(bark),
                new BambooModelParts.Core(rings, true),
                null);

        return new BambooBranchBlockStateModel(branch, new BambooModelParts.Leaves(leaves).bake(baker));
    }

    private static Material.Baked material(ModelBaker baker, Identifier texture) {
        return baker.materials().get(new Material(texture), texture::toDebugFileName);
    }

}
