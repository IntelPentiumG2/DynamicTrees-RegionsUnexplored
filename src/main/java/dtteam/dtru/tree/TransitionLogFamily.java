package dtteam.dtru.tree;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.utility.IdentifierUtils;
import dtteam.dtru.DynamicTreesRU;
import dtteam.dtru.block.TransitionLogBranchBlock;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.LinkedList;
import java.util.List;

public class TransitionLogFamily extends Family {

    public static final TypedRegistry.EntryType<Family> TYPE_STRIPPED = TypedRegistry.newType(res -> new TransitionLogFamily(res, true, false));
    public static final TypedRegistry.EntryType<Family> TYPE_BASE = TypedRegistry.newType(res -> new TransitionLogFamily(res, false, true));

    /**
     * Texture override key for the bark shown while {@link TransitionLogBranchBlock#TRANSITION} is
     * set. Defaults to the primitive log suffixed with {@code _transition}.
     */
    public static final String TRANSITION_BRANCH = "transition_branch";

    public static final Identifier TRANSITION_BRANCH_GENERATOR = DynamicTreesRU.location("transition_branch");

    public TransitionLogFamily(Identifier name, boolean stripped, boolean base) {
        super(name);
        transitionOnStripped = stripped;
        transitionOnBase = base;
    }

    boolean transitionOnStripped;
    boolean transitionOnBase;

    @Override
    protected BranchBlock createBranch(Identifier name, BlockBehaviour.Properties properties) {
        return new TransitionLogBranchBlock(name, properties, transitionOnStripped, transitionOnBase);
    }

    /**
     * Replaces the stock branch generator with one that dispatches on the transition property. Before
     * 26.1 the transition variants were hand-written into the generated block states, so every data
     * generation run silently reverted them.
     */
    @Override
    public List<Identifier> getBlockModelGenerators() {
        final List<Identifier> generators = new LinkedList<>(super.getBlockModelGenerators());
        generators.remove(DynamicTrees.location("branch"));
        generators.add(TRANSITION_BRANCH_GENERATOR);
        return generators;
    }

    public Identifier getTransitionBarkTexture(Identifier primitiveLogLocation) {
        return getTexturePath(TRANSITION_BRANCH)
                .orElseGet(() -> IdentifierUtils.suffix(primitiveLogLocation, "_transition"));
    }

}
