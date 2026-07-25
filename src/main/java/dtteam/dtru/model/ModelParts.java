package dtteam.dtru.model;

import com.dtteam.dynamictrees.model.ModelHelper;
import com.google.common.collect.Maps;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Map;
import java.util.function.Function;

/**
 * Shared plumbing for this add-on's block state model parts.
 *
 * <p>Dynamic Trees keeps the equivalent helpers private, so they are mirrored here rather than
 * reimplemented differently.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class ModelParts {

    private ModelParts() {
    }

    /**
     * Copies a cuboid onto a second texture and tint index, giving a second layer over identical
     * geometry. Eucalyptus draws its animated overlay this way: the bark layer is untinted and the
     * overlay layer takes tint index 0, which the block colour handler resolves per position.
     */
    public static CuboidModelElement overlay(CuboidModelElement source, Material.Baked material, int tintIndex) {
        final Map<Direction, CuboidFace> faces = Maps.newEnumMap(Direction.class);
        source.faces().forEach((direction, face) ->
                faces.put(direction, new CuboidFace(
                        face.cullForDirection(), tintIndex, material.toString(), face.uvs(), face.rotation())));
        return new CuboidModelElement(source.from(), source.to(), faces);
    }

    public static void addUnculledFaces(ModelBaker baker, QuadCollection.Builder builder,
                                        CuboidModelElement part, Material.Baked material) {
        addUnculledFaces(baker, direction -> builder, part, material);
    }

    public static void addUnculledFaces(ModelBaker baker, Function<Direction, QuadCollection.Builder> builders,
                                        CuboidModelElement part, Material.Baked material) {
        for (Map.Entry<Direction, CuboidFace> entry : part.faces().entrySet()) {
            final Direction face = entry.getKey();
            builders.apply(face).addUnculledFace(
                    ModelHelper.makeBakedQuad(baker, part, entry.getValue(), material, face));
        }
    }

}
