package dtteam.dtru.data;

import com.dtteam.dynamictrees.data.generator.DataGenerators;
import com.dtteam.dynamictrees.tree.family.Family;
import dtteam.dtru.tree.BrimwoodFamily;
import dtteam.dtru.tree.EucalyptusFamily;
import dtteam.dtru.tree.TransitionLogFamily;

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
    }

}
