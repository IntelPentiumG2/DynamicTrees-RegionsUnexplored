package dtteam.dtru.data;

import com.dtteam.dynamictrees.data.builder.BasicLoaderBuilder;
import com.dtteam.dynamictrees.data.generator.SurfaceRootStateGenerator;
import com.dtteam.dynamictrees.tree.family.Family;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Like Dynamic Trees' surface root generator, but also passes the overlay texture the eucalyptus root
 * model needs.
 */
public class EucalyptusSurfaceRootStateGenerator extends SurfaceRootStateGenerator {

    @Override
    public void generate(BlockModelGenerators generators, Family input, Dependencies dependencies) {
        final Identifier primitiveLogPath = ModelLocationUtils.getModelLocation(dependencies.get(PRIMITIVE_LOG));

        final Map<String, Identifier> textures = new HashMap<>();
        input.addBranchTextures(textures::put, primitiveLogPath, dependencies.get(PRIMITIVE_LOG));
        textures.keySet().retainAll(java.util.Set.of("bark", "overlay"));

        final BasicLoaderBuilder builder = BasicLoaderBuilder.loaderBuilders.get(input.getSurfaceRootLoader())
                .apply(textures, input);

        generators.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(dependencies.get(SURFACE_ROOT), MultiVariant.of(builder)));
    }

}
