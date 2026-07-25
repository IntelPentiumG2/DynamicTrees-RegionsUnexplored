package dtteam.dtru.model;

import com.dtteam.dynamictrees.model.BranchMultiPartHolder.PartMap;
import com.dtteam.dynamictrees.model.ModelHelper;
import com.dtteam.dynamictrees.model.parts.BranchModelPart;
import com.google.common.collect.Maps;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.cuboid.CuboidRotation;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;

import java.util.Map;

/**
 * Giant bamboo culms are narrower than a regular branch and read their bark from a strip of the
 * bamboo log texture, so they map UVs differently from Dynamic Trees' branches and cannot simply
 * inherit that geometry.
 */
@OnlyIn(Dist.CLIENT)
public final class BambooModelParts {

    private BambooModelParts() {
    }

    public static class Core extends BranchModelPart.UnbakedCore {

        private final boolean isRings;

        public Core(Material.Baked material, boolean isRings) {
            super(material);
            this.isRings = isRings;
        }

        @Override
        public PartMap<BranchModelPart> bakeAllSides(ModelBaker baker, int radius, Direction.Axis axis) {
            final PartMap<QuadCollection.Builder> builders = createBuilders();
            ModelParts.addUnculledFaces(baker, builders::get, generateCorePart(radius, axis, false), material);
            return buildParts(builders, material);
        }

        @Override
        protected CuboidModelElement generateCorePart(int radius, Direction.Axis axis, boolean flipNormals) {
            Vector3f posFrom = new Vector3f(8 - radius, 8 - radius, 8 - radius);
            Vector3f posTo = new Vector3f(8 + radius, 8 + radius, 8 + radius);
            if (flipNormals) {
                final Vector3f aux = posFrom;
                posFrom = posTo;
                posTo = aux;
            }

            final Map<Direction, CuboidFace> mapFacesIn = Maps.newEnumMap(Direction.class);

            for (Direction face : Direction.values()) {
                final CuboidFace.UVs uv = isRings
                        ? new CuboidFace.UVs(12 - radius, 4 - radius, 12 + radius, 4 + radius)
                        : new CuboidFace.UVs(4 - radius, 8 - radius, 4 + radius, 8 + radius);
                mapFacesIn.put(face, new CuboidFace(face, -1, material.toString(), uv,
                        ModelHelper.getFaceQuadrant(axis, face)));
            }

            return new CuboidModelElement(posFrom, posTo, mapFacesIn);
        }
    }

    public static class Sleeve extends BranchModelPart.UnbakedSleeve {

        public Sleeve(Material.Baked material) {
            super(material);
        }

        @Override
        public PartMap<BranchModelPart> bakeAllSides(ModelBaker baker, int radius) {
            final PartMap<BranchModelPart> parts = new PartMap<>();

            for (Direction dir : Direction.values()) {
                final QuadCollection.Builder builder = new QuadCollection.Builder();
                ModelParts.addUnculledFaces(baker, builder, generateSleevePart(radius, dir, false), material);
                parts.put(dir, new BranchModelPart(builder.build(), true, material));
            }

            return parts;
        }

        @Override
        public CuboidModelElement generateSleevePart(int radius, Direction dir, boolean flipNormals) {
            //Work in double units(*2)
            final int diameter = radius * 2;
            final int halfSize = (16 - diameter) / 2;
            final int halfSizeX = dir.getStepX() != 0 ? halfSize : diameter;
            final int halfSizeY = dir.getStepY() != 0 ? halfSize : diameter;
            final int halfSizeZ = dir.getStepZ() != 0 ? halfSize : diameter;
            final int move = 16 - halfSize;
            final int centerX = 16 + (dir.getStepX() * move);
            final int centerY = 16 + (dir.getStepY() * move);
            final int centerZ = 16 + (dir.getStepZ() * move);

            Vector3f posFrom = new Vector3f((centerX - halfSizeX) / 2f, (centerY - halfSizeY) / 2f, (centerZ - halfSizeZ) / 2f);
            Vector3f posTo = new Vector3f((centerX + halfSizeX) / 2f, (centerY + halfSizeY) / 2f, (centerZ + halfSizeZ) / 2f);
            if (flipNormals) {
                final Vector3f aux = posFrom;
                posFrom = posTo;
                posTo = aux;
                dir = dir.getOpposite();
            }

            boolean negative = dir.getAxisDirection() == Direction.AxisDirection.NEGATIVE;
            if (dir.getAxis() == Direction.Axis.Z) {//North/South
                negative = !negative;
            }

            final Map<Direction, CuboidFace> mapFacesIn = Maps.newEnumMap(Direction.class);

            for (Direction face : Direction.values()) {
                if (dir.getOpposite() != face) { //Discard side of sleeve that faces core
                    CuboidFace.UVs uvface = null;
                    if (dir == face) {//Side of sleeve that faces away from core
                        if (radius == 1 || radius == 2) {
                            uvface = new CuboidFace.UVs(12 - radius, 4 - radius, 12 + radius, 4 + radius);
                        }
                    } else { //UV for Bark texture
                        uvface = new CuboidFace.UVs(4 - radius, negative ? 16 - halfSize : 0, 4 + radius, negative ? 16 : halfSize);
                    }
                    if (uvface != null) {
                        mapFacesIn.put(face, new CuboidFace(face, -1, material.toString(), uvface,
                                ModelHelper.getFaceQuadrant(dir.getAxis(), face)));
                    }
                }
            }

            return new CuboidModelElement(posFrom, posTo, mapFacesIn);
        }
    }

    /**
     * Two vertical planes crossed at right angles, drawn on top of a culm that carries foliage.
     */
    public record Leaves(Material.Baked material) {

        public BranchModelPart bake(ModelBaker baker) {
            final QuadCollection.Builder builder = new QuadCollection.Builder();

            addPlane(baker, builder,
                    new Vector3f(0, 0, 2.35f), new Vector3f(0, 16, 20.35f),
                    new CuboidRotation(new Vector3f(0, 0, 0), new CuboidRotation.SingleAxisRotation(Direction.Axis.Y, 45), false));
            addPlane(baker, builder,
                    new Vector3f(11.35f, 0, -9), new Vector3f(11.35f, 16, 9),
                    new CuboidRotation(new Vector3f(0, 0, 0), new CuboidRotation.SingleAxisRotation(Direction.Axis.Y, -45), false));

            return new BranchModelPart(builder.build(), false, material);
        }

        private void addPlane(ModelBaker baker, QuadCollection.Builder builder,
                              Vector3f from, Vector3f to, CuboidRotation rotation) {
            final Map<Direction, CuboidFace> mapFacesIn = Maps.newEnumMap(Direction.class);
            for (Direction face : new Direction[]{Direction.WEST, Direction.EAST}) {
                mapFacesIn.put(face, new CuboidFace(null, -1, material.toString(),
                        new CuboidFace.UVs(0, 0, 16, 16), com.mojang.math.Quadrant.R0));
            }

            final CuboidModelElement part = new CuboidModelElement(from, to, mapFacesIn, rotation, false, 0);

            for (Map.Entry<Direction, CuboidFace> entry : part.faces().entrySet()) {
                final Direction face = entry.getKey();
                builder.addUnculledFace(FaceBakery.bakeQuad(baker, part.from(), part.to(), entry.getValue(),
                        material, face, ModelHelper.noState(), rotation, false, 0));
            }
        }
    }

}
