package dtteam.dtru.data;

import com.dtteam.dynamictrees.data.generator.BranchStateGenerator;
import com.dtteam.dynamictrees.tree.family.Family;
import dtteam.dtru.tree.BrimwoodFamily;

/**
 * Generates the block state for Brimwood's third branch variant, which carries its own primitive log.
 */
public class MagmaBranchStateGenerator extends BranchStateGenerator {

    @Override
    public Dependencies gatherDependencies(Family input) {
        if (input instanceof BrimwoodFamily brimwood) {
            return new Dependencies()
                    .append(BRANCH, brimwood.getMagmaBranch())
                    .append(PRIMITIVE_LOG, brimwood.getPrimitiveMagmaLog());
        }
        return new Dependencies();
    }

}
