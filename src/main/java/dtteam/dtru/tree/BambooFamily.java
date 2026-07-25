package dtteam.dtru.tree;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.api.voxmap.BlockPosBounds;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.utility.IdentifierUtils;
import dtteam.dtru.DynamicTreesRU;
import dtteam.dtru.block.BambooBranchBlock;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class BambooFamily extends Family {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(BambooFamily::new);

    /** Texture override key for the foliage planes drawn on leafy culms. */
    public static final String LEAVES = "leaves";

    public BambooFamily(Identifier name) {
        super(name);
    }

    @Override
    protected BranchBlock createBranch(Identifier name, BlockBehaviour.Properties properties) {
        return new BambooBranchBlock(name, properties);
    }

    @Override
    public Identifier getBranchLoader() {
        return DynamicTreesRU.BAMBOO;
    }

    @Override
    public void addBranchTextures(BiConsumer<String, Identifier> textureConsumer, Identifier primitiveLogLocation, Block sourceBlock) {
        final Map<String, Identifier> textures = new HashMap<>();
        super.addBranchTextures(textures::put, primitiveLogLocation, sourceBlock);
        textures.put(LEAVES, leavesTexture(textures.get("bark"), sourceBlock));
        textures.forEach(textureConsumer);
    }

    /** Stripped culms keep no foliage, so they take the blank texture Dynamic Trees provides. */
    private Identifier leavesTexture(Identifier bark, Block sourceBlock) {
        if (getPrimitiveStrippedLog().map(sourceBlock::equals).orElse(false)) {
            return DynamicTrees.location("block/air");
        }
        return getTexturePath(LEAVES).orElseGet(() -> IdentifierUtils.suffix(bark, "_leaves"));
    }

    public BlockPosBounds expandLeavesBlockBounds(BlockPosBounds bounds) {
        return bounds.expand(3).expand(Direction.DOWN, 3);
    }

}
