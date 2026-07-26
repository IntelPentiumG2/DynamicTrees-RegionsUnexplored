package dtteam.dtru.model;

import com.dtteam.dynamictrees.model.BranchMultiPartHolder.PartMap;
import com.dtteam.dynamictrees.model.ModelHelper;
import com.dtteam.dynamictrees.model.parts.BranchModelPart;
import com.dtteam.dynamictrees.model.parts.SurfaceRootModelPart;
import com.dtteam.dynamictrees.utility.CoordUtils;
import com.google.common.collect.Maps;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Eucalyptus parts are Dynamic Trees' own branch and surface root geometry drawn twice: once with the
 * bark texture, then again with the overlay texture at tint index 0. The colour handler registered in
 * {@code DTRUClient} shifts that second layer through the rainbow by block position.
 *
 * <p>Only the trunk geometry is duplicated here, because Dynamic Trees keeps it private; the core,
 * sleeve and root shapes are inherited so their coordinates stay in one place.</p>
 */
public final class EucalyptusModelParts {

    /** Tint index of the overlay layer, matching the position of the tint source registered for the block. */
    public static final int OVERLAY_TINT = 0;

    private EucalyptusModelParts() {
    }

    public static class OverlaidCore extends BranchModelPart.UnbakedCore {

        private final Material.Baked overlay;

        public OverlaidCore(Material.Baked material, Material.Baked overlay) {
            super(material);
            this.overlay = overlay;
        }

        @Override
        public PartMap<BranchModelPart> bakeAllSides(ModelBaker baker, int radius, Direction.Axis axis) {
            final PartMap<QuadCollection.Builder> builders = createBuilders();

            final CuboidModelElement part = generateCorePart(radius, axis, false);
            ModelParts.addUnculledFaces(baker, builders::get, part, material);
            ModelParts.addUnculledFaces(baker, builders::get, ModelParts.overlay(part, overlay, OVERLAY_TINT), overlay);

            return buildParts(builders, material);
        }
    }

    public static class OverlaidSleeve extends BranchModelPart.UnbakedSleeve {

        private final Material.Baked overlay;

        public OverlaidSleeve(Material.Baked material, Material.Baked overlay) {
            super(material);
            this.overlay = overlay;
        }

        @Override
        public PartMap<BranchModelPart> bakeAllSides(ModelBaker baker, int radius) {
            final PartMap<BranchModelPart> parts = new PartMap<>();

            for (Direction dir : Direction.values()) {
                final CuboidModelElement part = generateSleevePart(radius, dir, false);
                final QuadCollection.Builder builder = new QuadCollection.Builder();

                ModelParts.addUnculledFaces(baker, builder, part, material);
                ModelParts.addUnculledFaces(baker, builder, ModelParts.overlay(part, overlay, OVERLAY_TINT), overlay);

                parts.put(dir, new BranchModelPart(builder.build(), true, material));
            }

            return parts;
        }
    }

    /**
     * Copy of Dynamic Trees' thick trunk geometry with the overlay layer added. The shape is
     * reproduced rather than inherited because its generator is private.
     */
    public static class OverlaidThickTrunk extends BranchModelPart.UnbakedThickTrunk {

        private final Material.Baked overlay;

        public OverlaidThickTrunk(Material.Baked material, Material.Baked overlay, boolean isRings) {
            super(material, isRings);
            this.overlay = overlay;
        }

        @Override
        public PartMap<BranchModelPart> bakeAllSides(ModelBaker baker, int radius) {
            return bakeSides(baker, radius, EnumSet.allOf(Direction.class));
        }

        @Override
        public PartMap<BranchModelPart> bakeSides(ModelBaker baker, int radius, EnumSet<Direction> sides) {
            final AABB wholeVolume = new AABB(8 - radius, 0, 8 - radius, 8 + radius, 16, 8 + radius);

            final ArrayList<Vec3i> offsets = new ArrayList<>();
            for (CoordUtils.Surround dir : CoordUtils.Surround.values()) {
                offsets.add(dir.getOffset()); // 8 surrounding component pieces
            }
            offsets.add(new Vec3i(0, 0, 0)); //Center

            final PartMap<BranchModelPart> parts = new PartMap<>();

            for (Direction face : sides) {
                final QuadCollection.Builder builder = new QuadCollection.Builder();

                for (CuboidModelElement part : generateTrunkParts(face, offsets, wholeVolume)) {
                    builder.addUnculledFace(ModelHelper.makeBakedQuad(baker, part, part.faces().get(face), material, face));
                    final CuboidModelElement overlaid = ModelParts.overlay(part, overlay, OVERLAY_TINT);
                    builder.addUnculledFace(ModelHelper.makeBakedQuad(baker, overlaid, overlaid.faces().get(face), overlay, face));
                }

                parts.put(face, new BranchModelPart(builder.build(), true, material));
            }

            return parts;
        }

        private List<CuboidModelElement> generateTrunkParts(Direction face, ArrayList<Vec3i> offsets, AABB wholeVolume) {
            final Vec3i dirVector = face.getUnitVec3i();
            final List<CuboidModelElement> cuboidParts = new LinkedList<>();
            for (Vec3i offset : offsets) {
                if (face.getAxis() == Direction.Axis.Y || new Vec3(dirVector.getX(), dirVector.getY(), dirVector.getZ())
                        .add(new Vec3(offset.getX(), offset.getY(), offset.getZ())).lengthSqr() > 2.25) { //This means that the dir and face share a common direction
                    final Vec3 scaledOffset = new Vec3(offset.getX() * 16, offset.getY() * 16, offset.getZ() * 16);//Scale the dimensions to match standard minecraft texels
                    final AABB partBoundary = new AABB(0, 0, 0, 16, 16, 16).move(scaledOffset).intersect(wholeVolume);

                    final Vector3f[] limits = ModelHelper.AABBLimits(partBoundary);

                    final Map<Direction, CuboidFace> mapFacesIn = Maps.newEnumMap(Direction.class);

                    final int wholeVolumeWidth = 48;
                    final float[] uvCoords = isRings
                            ? getUvs(face, partBoundary, wholeVolumeWidth)
                            : ModelHelper.modUV(ModelHelper.getUVs(partBoundary, face));

                    final CuboidFace.UVs uvFace = new CuboidFace.UVs(uvCoords[0], uvCoords[1], uvCoords[2], uvCoords[3]);
                    mapFacesIn.put(face, new CuboidFace(null, -1, material.toString(), uvFace,
                            ModelHelper.getFaceQuadrant(Direction.Axis.Y, face)));

                    cuboidParts.add(new CuboidModelElement(limits[0], limits[1], mapFacesIn));
                }
            }
            return cuboidParts;
        }

        private static float[] getUvs(Direction face, AABB partBoundary, int wholeVolumeWidth) {
            final float textureOffsetX = -16f;
            final float textureOffsetZ = -16f;

            final float minX = ((float) ((partBoundary.minX - textureOffsetX) / wholeVolumeWidth)) * 16f;
            final float maxX = ((float) ((partBoundary.maxX - textureOffsetX) / wholeVolumeWidth)) * 16f;
            float minZ = ((float) ((partBoundary.minZ - textureOffsetZ) / wholeVolumeWidth)) * 16f;
            float maxZ = ((float) ((partBoundary.maxZ - textureOffsetZ) / wholeVolumeWidth)) * 16f;

            if (face == Direction.DOWN) {
                minZ = ((float) ((partBoundary.maxZ - textureOffsetZ) / wholeVolumeWidth)) * 16f;
                maxZ = ((float) ((partBoundary.minZ - textureOffsetZ) / wholeVolumeWidth)) * 16f;
            }

            return new float[]{minX, minZ, maxX, maxZ};
        }
    }

    /**
     * Copy of Dynamic Trees' surface root core geometry; its generator is private, and the enclosing
     * type is a record so it cannot be extended.
     */
    public record OverlaidRootCore(Material.Baked material, Material.Baked overlay) {

        public SurfaceRootModelPart bake(ModelBaker baker, int radius, Direction.Axis axis) {
            return build(baker, generateCorePart(radius, axis), material, overlay);
        }

        private CuboidModelElement generateCorePart(int radius, Direction.Axis axis) {
            final int diameter = radius * 2;
            final Vector3f posFrom = new Vector3f(8 - radius, 0, 8 - radius);
            final Vector3f posTo = new Vector3f(8 + radius, diameter, 8 + radius);

            final Map<Direction, CuboidFace> mapFacesIn = Maps.newEnumMap(Direction.class);

            for (Direction face : Direction.values()) {
                final CuboidFace.UVs uv;
                if (face.getAxis().isHorizontal()) {
                    final boolean positive = face.getAxisDirection() == Direction.AxisDirection.POSITIVE;
                    uv = new CuboidFace.UVs(positive ? 16 - diameter : 0, 8 - radius, positive ? 16 : diameter, 8 + radius);
                } else {
                    uv = new CuboidFace.UVs(8 - radius, 8 - radius, 8 + radius, 8 + radius);
                }

                mapFacesIn.put(face, new CuboidFace(null, -1, material.toString(), uv,
                        ModelHelper.getFaceQuadrant(axis, face)));
            }

            return new CuboidModelElement(posFrom, posTo, mapFacesIn);
        }
    }

    public record OverlaidRootSleeve(Material.Baked material, Material.Baked overlay) {

        public SurfaceRootModelPart bake(ModelBaker baker, int radius, Direction direction) {
            final CuboidModelElement part = new SurfaceRootModelPart.UnbakedSleeve(material)
                    .generateSleevePart(radius, direction, false);
            return build(baker, part, material, overlay);
        }
    }

    /**
     * Vertical root sections span two blocks, so unlike the other root parts they are assembled from
     * several cuboids and cannot reuse a single element.
     */
    public record OverlaidRootVert(Material.Baked material, Material.Baked overlay) {

        public SurfaceRootModelPart bake(ModelBaker baker, int radius, Direction direction) {
            final int radialHeight = radius * 2;
            final QuadCollection.Builder builder = new QuadCollection.Builder();

            final AABB partBoundary = new AABB(8 - radius, radialHeight, 8 - radius, 8 + radius, 16 + radialHeight, 8 + radius)
                    .move(direction.getStepX() * 7, 0, direction.getStepZ() * 7);

            for (int i = 0; i < 2; i++) {
                final AABB pieceBoundary = partBoundary.intersect(new AABB(0, 0, 0, 16, 16, 16).move(0, 16 * i, 0));

                for (Direction face : Direction.values()) {
                    final Map<Direction, CuboidFace> mapFacesIn = Maps.newEnumMap(Direction.class);

                    final float[] uvCoords = ModelHelper.modUV(ModelHelper.getUVs(pieceBoundary, face));
                    final CuboidFace.UVs uvface = new CuboidFace.UVs(uvCoords[0], uvCoords[1], uvCoords[2], uvCoords[3]);
                    mapFacesIn.put(face, new CuboidFace(face, -1, material.toString(), uvface,
                            ModelHelper.getFaceQuadrant(Direction.Axis.Y, face)));

                    final Vector3f[] limits = ModelHelper.AABBLimits(pieceBoundary);

                    final CuboidModelElement part = new CuboidModelElement(limits[0], limits[1], mapFacesIn);
                    builder.addUnculledFace(ModelHelper.makeBakedQuad(baker, part, part.faces().get(face), material, face));

                    final CuboidModelElement overlaid = ModelParts.overlay(part, overlay, OVERLAY_TINT);
                    builder.addUnculledFace(ModelHelper.makeBakedQuad(baker, overlaid, overlaid.faces().get(face), overlay, face));
                }
            }

            return new SurfaceRootModelPart(builder.build(), true, material);
        }
    }

    private static SurfaceRootModelPart build(ModelBaker baker, CuboidModelElement part,
                                              Material.Baked material, Material.Baked overlay) {
        final QuadCollection.Builder builder = new QuadCollection.Builder();
        ModelParts.addUnculledFaces(baker, builder, part, material);
        ModelParts.addUnculledFaces(baker, builder, ModelParts.overlay(part, overlay, OVERLAY_TINT), overlay);
        return new SurfaceRootModelPart(builder.build(), true, material);
    }

}
