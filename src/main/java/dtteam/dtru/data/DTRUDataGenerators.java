package dtteam.dtru.data;

import com.dtteam.dynamictrees.data.builder.BasicLoaderBuilder;
import com.dtteam.dynamictrees.data.generator.DataGenerators;
import com.dtteam.dynamictrees.tree.family.Family;
import dtteam.dtru.DynamicTreesRU;
import dtteam.dtru.model.UnbakedBambooBranchModel;
import dtteam.dtru.model.UnbakedEucalyptusBranchModel;
import dtteam.dtru.model.UnbakedEucalyptusSurfaceRootModel;
import dtteam.dtru.tree.BrimwoodFamily;
import dtteam.dtru.tree.EucalyptusFamily;
import dtteam.dtru.tree.TransitionLogFamily;

import java.util.Optional;

public class DTRUDataGenerators {

    public static void register() {
        DataGenerators.registerBlockModelGenerator(Family.class,
                TransitionLogFamily.TRANSITION_BRANCH_GENERATOR, TransitionBranchStateGenerator::new);
        DataGenerators.registerBlockModelGenerator(Family.class,
                BrimwoodFamily.MAGMA_BRANCH_GENERATOR, MagmaBranchStateGenerator::new);
        DataGenerators.registerBlockModelGenerator(Family.class,
                EucalyptusFamily.PLAIN_STRIPPED_BRANCH_GENERATOR, PlainStrippedBranchStateGenerator::new);
        DataGenerators.registerBlockModelGenerator(Family.class,
                EucalyptusFamily.SURFACE_ROOT_GENERATOR, EucalyptusSurfaceRootStateGenerator::new);

        registerLoaderBuilders();
    }

    /**
     * Teaches Dynamic Trees' branch generators how to emit this add-on's block state model types, so
     * a family only has to name one from {@code getBranchLoader}.
     */
    private static void registerLoaderBuilders() {
        BasicLoaderBuilder.loaderBuilders.put(DynamicTreesRU.BAMBOO, (textures, family) ->
                new BasicLoaderBuilder(() -> new UnbakedBambooBranchModel(
                        textures.get("bark"),
                        textures.get("rings"),
                        textures.get("leaves"))));

        BasicLoaderBuilder.loaderBuilders.put(DynamicTreesRU.EUCALYPTUS, (textures, family) ->
                new BasicLoaderBuilder(() -> new UnbakedEucalyptusBranchModel(
                        textures.get("bark"),
                        textures.get("rings"),
                        textures.get("overlay"),
                        Optional.ofNullable(family))));

        BasicLoaderBuilder.loaderBuilders.put(DynamicTreesRU.EUCALYPTUS_SURFACE_ROOT, (textures, family) ->
                new BasicLoaderBuilder(() -> new UnbakedEucalyptusSurfaceRootModel(
                        textures.get("bark"),
                        textures.get("overlay"))));
    }

}
