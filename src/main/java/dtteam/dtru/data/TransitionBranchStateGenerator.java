package dtteam.dtru.data;

import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.data.builder.BasicLoaderBuilder;
import com.dtteam.dynamictrees.data.generator.BranchStateGenerator;
import com.dtteam.dynamictrees.tree.family.Family;
import dtteam.dtru.block.TransitionLogBranchBlock;
import dtteam.dtru.tree.TransitionLogFamily;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

/**
 * Emits both transition states of a {@link TransitionLogBranchBlock} rather than a single unqualified
 * variant. The two states differ only in the bark texture.
 */
public class TransitionBranchStateGenerator extends BranchStateGenerator {

    @Override
    protected void acceptOutput(BlockModelGenerators generators, Family input, Dependencies dependencies,
                                BranchBlock branch, BasicLoaderBuilder branchBuilder) {
        if (!(input instanceof TransitionLogFamily family)) {
            super.acceptOutput(generators, input, dependencies, branch, branchBuilder);
            return;
        }

        final Block primitiveLog = dependencies.get(PRIMITIVE_LOG);
        final Identifier primitiveLogPath = ModelLocationUtils.getModelLocation(primitiveLog);

        final Map<String, Identifier> transitionTextures = new HashMap<>();
        addTextures(input, transitionTextures, primitiveLogPath, primitiveLog);
        transitionTextures.put("bark", family.getTransitionBarkTexture(primitiveLogPath));

        final BasicLoaderBuilder transitionBuilder = getBranchLoader(input).apply(transitionTextures, input);

        generators.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(branch)
                        .with(PropertyDispatch.initial(TransitionLogBranchBlock.TRANSITION)
                                .select(false, MultiVariant.of(branchBuilder))
                                .select(true, MultiVariant.of(transitionBuilder))));
    }

}
