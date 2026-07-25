package dtteam.dtru.data;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.data.builder.BasicLoaderBuilder;
import com.dtteam.dynamictrees.data.generator.StrippedBranchStateGenerator;
import com.dtteam.dynamictrees.tree.family.Family;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.function.BiFunction;

/**
 * Generates a stripped branch using Dynamic Trees' own branch model rather than the family's.
 *
 * <p>Eucalyptus needs this: its overlay texture only exists for the unstripped log, so a stripped
 * branch has nothing for the overlay layer to draw.</p>
 */
public class PlainStrippedBranchStateGenerator extends StrippedBranchStateGenerator {

    @Override
    protected BiFunction<Map<String, Identifier>, Family, BasicLoaderBuilder> getBranchLoader(Family input) {
        return BasicLoaderBuilder.loaderBuilders.get(DynamicTrees.location("branch"));
    }

}
