package dtteam.dtru.worldgen;

import com.dtteam.dynamictrees.api.worldgen.BiomePropertySelectors;
import com.dtteam.dynamictrees.worldgen.featurecancellation.TreeFeatureCanceller;
import dev.worldgen.lithostitched.api.util.Weighted;
import dev.worldgen.lithostitched.worldgen.feature.config.SimplePlacedConfig;
import dev.worldgen.lithostitched.worldgen.feature.config.WeightedSelectorConfig;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.stream.Stream;

/**
 * A tree canceller that can see through Lithostitched's selector features.
 *
 * <p>Regions Unexplored 0.7.0 groups nearly all of its biome trees behind
 * {@code lithostitched:placed} and {@code lithostitched:weighted_selector}. Neither config implements
 * {@link FeatureConfiguration#getSubFeatures()}, so Dynamic Trees cannot tell that they contain trees,
 * and every grouped tree generated in its static form beside the dynamic ones.</p>
 *
 * <p>Subclasses that need their own matching rule should override {@link #matches}, not
 * {@code shouldCancel}, so that they keep the unwrapping.</p>
 */
public class LithostitchedTreeFeatureCanceller<T extends FeatureConfiguration> extends TreeFeatureCanceller<T> {

    public LithostitchedTreeFeatureCanceller(Identifier registryName, Class<T> treeFeatureConfigClass) {
        super(registryName, treeFeatureConfigClass);
    }

    @Override
    public final boolean shouldCancel(ConfiguredFeature<?, ?> configuredFeature, BiomePropertySelectors.NormalFeatureCancellation featureCancellations) {
        if (matches(configuredFeature, featureCancellations)) {
            return true;
        }
        return unwrap(configuredFeature.config())
                .flatMap(placed -> placed.getFeatures().map(Holder::value))
                .anyMatch(nested -> shouldCancel(nested, featureCancellations));
    }

    /**
     * Whether this configured feature is itself something to cancel, ignoring anything it nests.
     */
    protected boolean matches(ConfiguredFeature<?, ?> configuredFeature, BiomePropertySelectors.NormalFeatureCancellation featureCancellations) {
        return super.shouldCancel(configuredFeature, featureCancellations);
    }

    private static Stream<PlacedFeature> unwrap(FeatureConfiguration config) {
        if (config instanceof SimplePlacedConfig placed) {
            return Stream.of(placed.feature().value());
        }
        if (config instanceof WeightedSelectorConfig selector) {
            return selector.features().unwrap().stream()
                    .map(Weighted::value)
                    .map(Holder::value);
        }
        return Stream.empty();
    }

}
