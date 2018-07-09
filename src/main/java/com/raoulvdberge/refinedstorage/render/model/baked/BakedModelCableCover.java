package com.raoulvdberge.refinedstorage.render.model.baked;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.apiimpl.network.node.cover.Cover;
import com.raoulvdberge.refinedstorage.apiimpl.network.node.cover.CoverManager;
import com.raoulvdberge.refinedstorage.block.BlockCable;
import com.raoulvdberge.refinedstorage.render.CubeBuilder;
import com.raoulvdberge.refinedstorage.render.collision.constants.ConstantsCable;
import com.raoulvdberge.refinedstorage.util.RenderUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.List;

public class BakedModelCableCover implements IBakedModel {
    private static TextureAtlasSprite GREY_SPRITE;

    private IBakedModel base;

    public BakedModelCableCover(IBakedModel base) {
        this.base = base;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        List<BakedQuad> quads = new ArrayList<>(base.getQuads(state, side, rand));

        if (state != null) {
            IExtendedBlockState s = (IExtendedBlockState) state;

            boolean hasUp = s.getValue(BlockCable.COVER_UP) != null;
            boolean hasDown = s.getValue(BlockCable.COVER_DOWN) != null;

            boolean hasEast = s.getValue(BlockCable.COVER_EAST) != null;
            boolean hasWest = s.getValue(BlockCable.COVER_WEST) != null;

            addCover(quads, s.getValue(BlockCable.COVER_NORTH), EnumFacing.NORTH, side, rand, hasUp, hasDown, hasEast, hasWest, true);
            addCover(quads, s.getValue(BlockCable.COVER_SOUTH), EnumFacing.SOUTH, side, rand, hasUp, hasDown, hasEast, hasWest, true);
            addCover(quads, s.getValue(BlockCable.COVER_EAST), EnumFacing.EAST, side, rand, hasUp, hasDown, hasEast, hasWest, true);
            addCover(quads, s.getValue(BlockCable.COVER_WEST), EnumFacing.WEST, side, rand, hasUp, hasDown, hasEast, hasWest, true);
            addCover(quads, s.getValue(BlockCable.COVER_DOWN), EnumFacing.DOWN, side, rand, hasUp, hasDown, hasEast, hasWest, true);
            addCover(quads, s.getValue(BlockCable.COVER_UP), EnumFacing.UP, side, rand, hasUp, hasDown, hasEast, hasWest, true);
        }

        return quads;
    }

    protected static void addCover(List<BakedQuad> quads, @Nullable Cover cover, EnumFacing coverSide, EnumFacing side, long rand, boolean hasUp, boolean hasDown, boolean hasEast, boolean hasWest, boolean handle) {
        if (cover == null) {
            return;
        }

        IBlockState coverState = CoverManager.getBlockState(cover.getStack());

        if (coverState == null) {
            return;
        }

        TextureAtlasSprite sprite = RenderUtils.getSprite(Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(coverState), coverState, side, rand);

        switch (cover.getType()) {
            case NORMAL:
                addNormalCover(quads, sprite, coverSide, hasUp, hasDown, hasEast, hasWest, handle);
                break;
            case HOLLOW:
                addHollowCover(quads, sprite, coverSide, hasUp, hasDown, hasEast, hasWest);
                break;
            case HOLLOW_WIDE:
                addHollowWideCover(quads, sprite, coverSide, hasUp, hasDown, hasEast, hasWest);
                break;
        }
    }

    private static void addNormalCover(List<BakedQuad> quads, TextureAtlasSprite sprite, EnumFacing coverSide, boolean hasUp, boolean hasDown, boolean hasEast, boolean hasWest, boolean handle) {
        AxisAlignedBB bounds = ConstantsCable.getCoverBounds(coverSide);

        Vector3f from = new Vector3f((float) bounds.minX * 16, (float) bounds.minY * 16, (float) bounds.minZ * 16);
        Vector3f to = new Vector3f((float) bounds.maxX * 16, (float) bounds.maxY * 16, (float) bounds.maxZ * 16);

        if (coverSide == EnumFacing.NORTH) {
            if (hasWest) {
                from.setX(2);
            }

            if (hasEast) {
                to.setX(14);
            }
        } else if (coverSide == EnumFacing.SOUTH) {
            if (hasWest) {
                from.setX(2);
            }

            if (hasEast) {
                to.setX(14);
            }
        }

        if (coverSide.getAxis() != EnumFacing.Axis.Y) {
            if (hasDown) {
                from.setY(2);
            }

            if (hasUp) {
                to.setY(14);
            }
        }

        quads.addAll(new CubeBuilder().from(from.getX(), from.getY(), from.getZ()).to(to.getX(), to.getY(), to.getZ()).addFaces(face -> new CubeBuilder.Face(face, sprite)).bake());

        if (handle) {
            if (GREY_SPRITE == null) {
                GREY_SPRITE = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(RS.ID + ":blocks/generic_grey");
            }

            bounds = ConstantsCable.getHolderBounds(coverSide);

            from = new Vector3f((float) bounds.minX * 16, (float) bounds.minY * 16, (float) bounds.minZ * 16);
            to = new Vector3f((float) bounds.maxX * 16, (float) bounds.maxY * 16, (float) bounds.maxZ * 16);

            quads.addAll(new CubeBuilder().from(from.getX(), from.getY(), from.getZ()).to(to.getX(), to.getY(), to.getZ()).addFaces(face -> new CubeBuilder.Face(face, GREY_SPRITE)).bake());
        }
    }

    private static void addHollowCover(List<BakedQuad> quads, TextureAtlasSprite sprite, EnumFacing coverSide, boolean hasUp, boolean hasDown, boolean hasEast, boolean hasWest) {
        AxisAlignedBB bounds = ConstantsCable.getCoverBounds(coverSide);

        Vector3f from = new Vector3f((float) bounds.minX * 16, (float) bounds.minY * 16, (float) bounds.minZ * 16);
        Vector3f to = new Vector3f((float) bounds.maxX * 16, (float) bounds.maxY * 16, (float) bounds.maxZ * 16);

        if (coverSide.getAxis() != EnumFacing.Axis.Y) {
            if (hasDown) {
                from.setY(2);
            }

            if (hasUp) {
                to.setY(14);
            }
        }

        // Right
        if (coverSide == EnumFacing.NORTH) {
            if (hasWest) {
                from.setX(2);
            } else {
                from.setX(0);
            }

            to.setX(6);
        } else if (coverSide == EnumFacing.SOUTH) {
            if (hasEast) {
                to.setX(14);
            } else {
                to.setX(16);
            }

            from.setX(10);
        } else if (coverSide == EnumFacing.EAST) {
            from.setZ(0);
            to.setZ(6);
        } else if (coverSide == EnumFacing.WEST) {
            from.setZ(10);
            to.setZ(16);
        } else if (coverSide == EnumFacing.DOWN || coverSide == EnumFacing.UP) {
            from.setZ(10);
            to.setZ(16);
        }

        quads.addAll(new CubeBuilder()
            .from(from.getX(), from.getY(), from.getZ())
            .to(to.getX(), to.getY(), to.getZ())
            .addFaces(face -> new CubeBuilder.Face(face, sprite))
            .bake()
        );

        // Left
        if (coverSide == EnumFacing.NORTH) {
            if (hasEast) {
                to.setX(14);
            } else {
                to.setX(16);
            }

            from.setX(10);
        } else if (coverSide == EnumFacing.SOUTH) {
            if (hasWest) {
                from.setX(2);
            } else {
                from.setX(0);
            }

            to.setX(6);
        } else if (coverSide == EnumFacing.EAST) {
            to.setZ(16);
            from.setZ(10);
        } else if (coverSide == EnumFacing.WEST) {
            from.setZ(0);
            to.setZ(6);
        } else if (coverSide == EnumFacing.DOWN || coverSide == EnumFacing.UP) {
            from.setZ(0);
            to.setZ(6);
        }

        quads.addAll(new CubeBuilder()
            .from(from.getX(), from.getY(), from.getZ())
            .to(to.getX(), to.getY(), to.getZ())
            .addFaces(face -> new CubeBuilder.Face(face, sprite))
            .bake()
        );

        // Bottom
        if (coverSide == EnumFacing.NORTH) {
            from.setX(6);
            to.setX(10);

            if (hasDown) {
                from.setY(2);
            } else {
                from.setY(0);
            }

            to.setY(6);
        } else if (coverSide == EnumFacing.SOUTH) {
            from.setX(6);
            to.setX(10);

            if (hasDown) {
                from.setY(2);
            } else {
                from.setY(0);
            }

            to.setY(6);
        } else if (coverSide == EnumFacing.EAST) {
            from.setZ(6);
            to.setZ(10);

            if (hasDown) {
                from.setY(2);
            } else {
                from.setY(0);
            }

            to.setY(6);
        } else if (coverSide == EnumFacing.WEST) {
            from.setZ(6);
            to.setZ(10);

            if (hasDown) {
                from.setY(2);
            } else {
                from.setY(0);
            }

            to.setY(6);
        } else if (coverSide == EnumFacing.DOWN || coverSide == EnumFacing.UP) {
            from.setZ(6);
            to.setZ(10);

            from.setX(0);
            to.setX(6);
        }

        quads.addAll(new CubeBuilder()
            .from(from.getX(), from.getY(), from.getZ())
            .to(to.getX(), to.getY(), to.getZ())
            .addFaces(face -> new CubeBuilder.Face(face, sprite))
            .bake()
        );

        // Up
        if (coverSide == EnumFacing.NORTH) {
            from.setX(6);
            to.setX(10);

            if (hasUp) {
                to.setY(14);
            } else {
                to.setY(16);
            }

            from.setY(10);
        } else if (coverSide == EnumFacing.SOUTH) {
            from.setX(6);
            to.setX(10);

            if (hasUp) {
                to.setY(14);
            } else {
                to.setY(16);
            }

            from.setY(10);
        } else if (coverSide == EnumFacing.EAST) {
            from.setZ(6);
            to.setZ(10);

            if (hasUp) {
                to.setY(14);
            } else {
                to.setY(16);
            }

            from.setY(10);
        } else if (coverSide == EnumFacing.WEST) {
            from.setZ(6);
            to.setZ(10);

            if (hasUp) {
                to.setY(14);
            } else {
                to.setY(16);
            }

            from.setY(10);
        } else if (coverSide == EnumFacing.DOWN || coverSide == EnumFacing.UP) {
            from.setZ(6);
            to.setZ(10);

            from.setX(10);
            to.setX(16);
        }

        quads.addAll(new CubeBuilder()
            .from(from.getX(), from.getY(), from.getZ())
            .to(to.getX(), to.getY(), to.getZ())
            .addFaces(face -> new CubeBuilder.Face(face, sprite))
            .bake()
        );
    }

    private static void addHollowWideCover(List<BakedQuad> quads, TextureAtlasSprite sprite, EnumFacing coverSide, boolean hasUp, boolean hasDown, boolean hasEast, boolean hasWest) {
        AxisAlignedBB bounds = ConstantsCable.getCoverBounds(coverSide);

        Vector3f from = new Vector3f((float) bounds.minX * 16, (float) bounds.minY * 16, (float) bounds.minZ * 16);
        Vector3f to = new Vector3f((float) bounds.maxX * 16, (float) bounds.maxY * 16, (float) bounds.maxZ * 16);

        if (coverSide.getAxis() != EnumFacing.Axis.Y) {
            if (hasDown) {
                from.setY(2);
            }

            if (hasUp) {
                to.setY(14);
            }
        }

        // Right
        if (coverSide == EnumFacing.NORTH) {
            if (hasWest) {
                from.setX(2);
            } else {
                from.setX(0);
            }

            to.setX(3);
        } else if (coverSide == EnumFacing.SOUTH) {
            if (hasEast) {
                to.setX(14);
            } else {
                to.setX(16);
            }

            from.setX(13);
        } else if (coverSide == EnumFacing.EAST) {
            from.setZ(0);
            to.setZ(3);
        } else if (coverSide == EnumFacing.WEST) {
            from.setZ(13);
            to.setZ(16);
        } else if (coverSide == EnumFacing.DOWN || coverSide == EnumFacing.UP) {
            from.setZ(13);
            to.setZ(16);
        }

        quads.addAll(new CubeBuilder()
            .from(from.getX(), from.getY(), from.getZ())
            .to(to.getX(), to.getY(), to.getZ())
            .addFaces(face -> new CubeBuilder.Face(face, sprite))
            .bake()
        );

        // Left
        if (coverSide == EnumFacing.NORTH) {
            if (hasEast) {
                to.setX(14);
            } else {
                to.setX(16);
            }

            from.setX(13);
        } else if (coverSide == EnumFacing.SOUTH) {
            if (hasWest) {
                from.setX(2);
            } else {
                from.setX(0);
            }

            to.setX(3);
        } else if (coverSide == EnumFacing.EAST) {
            to.setZ(16);
            from.setZ(13);
        } else if (coverSide == EnumFacing.WEST) {
            from.setZ(0);
            to.setZ(3);
        } else if (coverSide == EnumFacing.DOWN || coverSide == EnumFacing.UP) {
            from.setZ(0);
            to.setZ(3);
        }

        quads.addAll(new CubeBuilder()
            .from(from.getX(), from.getY(), from.getZ())
            .to(to.getX(), to.getY(), to.getZ())
            .addFaces(face -> new CubeBuilder.Face(face, sprite))
            .bake()
        );

        // Bottom
        if (coverSide == EnumFacing.NORTH) {
            from.setX(3);
            to.setX(13);

            if (hasDown) {
                from.setY(2);
            } else {
                from.setY(0);
            }

            to.setY(3);
        } else if (coverSide == EnumFacing.SOUTH) {
            from.setX(3);
            to.setX(13);

            if (hasDown) {
                from.setY(2);
            } else {
                from.setY(0);
            }

            to.setY(3);
        } else if (coverSide == EnumFacing.EAST) {
            from.setZ(3);
            to.setZ(13);

            if (hasDown) {
                from.setY(2);
            } else {
                from.setY(0);
            }

            to.setY(3);
        } else if (coverSide == EnumFacing.WEST) {
            from.setZ(3);
            to.setZ(13);

            if (hasDown) {
                from.setY(2);
            } else {
                from.setY(0);
            }

            to.setY(3);
        } else if (coverSide == EnumFacing.DOWN || coverSide == EnumFacing.UP) {
            from.setZ(3);
            to.setZ(13);

            from.setX(0);
            to.setX(3);
        }

        quads.addAll(new CubeBuilder()
            .from(from.getX(), from.getY(), from.getZ())
            .to(to.getX(), to.getY(), to.getZ())
            .addFaces(face -> new CubeBuilder.Face(face, sprite))
            .bake()
        );

        // Up
        if (coverSide == EnumFacing.NORTH) {
            from.setX(3);
            to.setX(13);

            if (hasUp) {
                to.setY(14);
            } else {
                to.setY(16);
            }

            from.setY(13);
        } else if (coverSide == EnumFacing.SOUTH) {
            from.setX(3);
            to.setX(13);

            if (hasUp) {
                to.setY(14);
            } else {
                to.setY(16);
            }

            from.setY(13);
        } else if (coverSide == EnumFacing.EAST) {
            from.setZ(3);
            to.setZ(13);

            if (hasUp) {
                to.setY(14);
            } else {
                to.setY(16);
            }

            from.setY(13);
        } else if (coverSide == EnumFacing.WEST) {
            from.setZ(3);
            to.setZ(13);

            if (hasUp) {
                to.setY(14);
            } else {
                to.setY(16);
            }

            from.setY(13);
        } else if (coverSide == EnumFacing.DOWN || coverSide == EnumFacing.UP) {
            from.setZ(3);
            to.setZ(13);

            from.setX(13);
            to.setX(16);
        }

        quads.addAll(new CubeBuilder()
            .from(from.getX(), from.getY(), from.getZ())
            .to(to.getX(), to.getY(), to.getZ())
            .addFaces(face -> new CubeBuilder.Face(face, sprite))
            .bake()
        );
    }

    @Override
    @SuppressWarnings("deprecation")
    public ItemCameraTransforms getItemCameraTransforms() {
        return base.getItemCameraTransforms();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return base.getOverrides();
    }

    @Override
    public boolean isAmbientOcclusion(IBlockState state) {
        return base.isAmbientOcclusion(state);
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        return base.handlePerspective(cameraTransformType);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return base.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return base.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return base.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return base.getParticleTexture();
    }
}