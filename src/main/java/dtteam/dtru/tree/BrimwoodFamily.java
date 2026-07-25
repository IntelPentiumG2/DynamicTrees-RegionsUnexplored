package dtteam.dtru.tree;

import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.ThickBranchBlock;
import com.dtteam.dynamictrees.tree.BranchEntry;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.utility.IdentifierUtils;
import dtteam.dtru.DynamicTreesRU;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class BrimwoodFamily extends TransitionLogFamily {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(BrimwoodFamily::new);

    /** Follows {@code AltBranchFamily}, which claims index 2 for its own extra branch. */
    public static final int MAGMA_BRANCH_INDEX = 2;

    public static final Identifier MAGMA_BRANCH_GENERATOR = DynamicTreesRU.location("magma_branch");

    /** Texture override keys for the magma branch. */
    public static final String MAGMA_BRANCH = "magma_branch";
    public static final String MAGMA_BRANCH_TOP = "magma_branch_top";

    public BrimwoodFamily(Identifier name) {
        super(name, false, false);
    }

    @Override
    public void setupBlocks() {
        super.setupBlocks();

        addBranch(MAGMA_BRANCH_INDEX, new BranchEntry(this, getBranchName("magma_"))
                .setBlockProperties(getBranchProperties().lightLevel(state -> 4))
                .CreateBlock(this::createMagmaBranch));
    }

    protected BranchBlock createMagmaBranch(Identifier name, BlockBehaviour.Properties properties) {
        return new ThickBranchBlock(name, properties) {
            @Override
            public Optional<Block> getPrimitiveLog() {
                if (getFamily() instanceof BrimwoodFamily magmaLogFamily)
                    return magmaLogFamily.getPrimitiveMagmaLog();
                return super.getPrimitiveLog();
            }
        };
    }

    public Family setPrimitiveMagmaLog(Block primitiveLog) {
        branches.get(MAGMA_BRANCH_INDEX).setPrimitiveBlock(primitiveLog);
        return this;
    }

    public Optional<BranchBlock> getMagmaBranch() {
        return getBranchBlock(MAGMA_BRANCH_INDEX);
    }

    public Optional<Block> getPrimitiveMagmaLog() {
        return getPrimitiveLog(MAGMA_BRANCH_INDEX);
    }

    @Override
    public List<Identifier> getBlockModelGenerators() {
        final List<Identifier> generators = new LinkedList<>(super.getBlockModelGenerators());
        generators.add(MAGMA_BRANCH_GENERATOR);
        return generators;
    }

    /**
     * The magma log has no matching {@code _top} texture, so the magma branch borrows the plain
     * brimwood one for its rings unless overridden.
     */
    @Override
    public void addBranchTextures(BiConsumer<String, Identifier> textureConsumer, Identifier primitiveLogLocation, Block sourceBlock) {
        if (getPrimitiveMagmaLog().map(sourceBlock::equals).orElse(false)) {
            textureConsumer.accept("bark", getTexturePath(MAGMA_BRANCH)
                    .orElse(primitiveLogLocation));
            textureConsumer.accept("rings", getTexturePath(MAGMA_BRANCH_TOP)
                    .orElseGet(() -> IdentifierUtils.suffix(primitiveLogLocation, "_top")));
            return;
        }
        super.addBranchTextures(textureConsumer, primitiveLogLocation, sourceBlock);
    }

}
