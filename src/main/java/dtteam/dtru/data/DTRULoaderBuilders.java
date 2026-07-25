package dtteam.dtru.data;

import com.dtteam.dynamictrees.data.builder.BasicLoaderBuilder;
import dtteam.dtru.DynamicTreesRU;
import dtteam.dtru.model.UnbakedBambooBranchModel;
import dtteam.dtru.model.UnbakedEucalyptusBranchModel;
import dtteam.dtru.model.UnbakedEucalyptusSurfaceRootModel;

import java.util.Optional;

/**
 * Teaches Dynamic Trees' branch generators how to emit this add-on's block state model types, so a
 * family only has to name one from {@code getBranchLoader}.
 *
 * <p>Kept apart from the mod class and called only while gathering data: {@link BasicLoaderBuilder}
 * descends from a client-only data generation type, and the JVM verifies a whole class when it links
 * it, so naming it from any class the mod loader touches would break a dedicated server.</p>
 */
public class DTRULoaderBuilders {

    public static void register() {
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
