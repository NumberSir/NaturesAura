package de.ellpeck.naturesaura.blocks;

import de.ellpeck.naturesaura.Helper;
import de.ellpeck.naturesaura.api.NaturesAuraAPI;
import de.ellpeck.naturesaura.data.BlockStateGenerator;
import de.ellpeck.naturesaura.reg.*;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockGoldenLeaves extends LeavesBlock implements IModItem, IColorProvidingBlock, IColorProvidingItem, ICustomBlockState {

    public static final int HIGHEST_STAGE = 3;
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, BlockGoldenLeaves.HIGHEST_STAGE);

    public BlockGoldenLeaves() {
        super(Properties.of().mapColor(MapColor.GOLD).strength(0.2F).randomTicks().noOcclusion().sound(SoundType.GRASS));
        ModRegistry.ALL_ITEMS.add(this);
    }

    public static boolean convert(Level level, BlockPos pos) {
        var state = level.getBlockState(pos);
        if (state.getBlock() instanceof LeavesBlock && !(state.getBlock() instanceof BlockAncientLeaves || state.getBlock() instanceof BlockGoldenLeaves)) {
            if (!level.isClientSide) {
                level.setBlockAndUpdate(pos, ModBlocks.GOLDEN_LEAVES.defaultBlockState()
                        .setValue(LeavesBlock.DISTANCE, state.hasProperty(LeavesBlock.DISTANCE) ? state.getValue(LeavesBlock.DISTANCE) : 1)
                        .setValue(LeavesBlock.PERSISTENT, state.hasProperty(LeavesBlock.PERSISTENT) ? state.getValue(LeavesBlock.PERSISTENT) : false));
            }
            return true;
        }
        return false;
    }

    @Override
    public String getBaseName() {
        return "golden_leaves";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, Level levelIn, BlockPos pos, RandomSource rand) {
        if (stateIn.getValue(BlockGoldenLeaves.STAGE) == BlockGoldenLeaves.HIGHEST_STAGE && rand.nextFloat() >= 0.75F)
            NaturesAuraAPI.instance().spawnMagicParticle(
                    pos.getX() + rand.nextFloat(),
                    pos.getY() + rand.nextFloat(),
                    pos.getZ() + rand.nextFloat(),
                    0F, 0F, 0F,
                    0xF2FF00, 0.5F + rand.nextFloat(), 50, 0F, false, true);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockGoldenLeaves.STAGE);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BlockColor getBlockColor() {
        return (state, levelIn, pos, tintIndex) -> {
            var color = 0xF2FF00;
            if (state != null && levelIn != null && pos != null) {
                var foliage = BiomeColors.getAverageFoliageColor(levelIn, pos);
                return Helper.blendColors(color, foliage, state.getValue(BlockGoldenLeaves.STAGE) / (float) BlockGoldenLeaves.HIGHEST_STAGE);
            } else {
                return color;
            }
        };
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ItemColor getItemColor() {
        return (stack, tintIndex) -> 0xF2FF00;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel levelIn, BlockPos pos, RandomSource random) {
        super.randomTick(state, levelIn, pos, random);
        if (!levelIn.isClientSide) {
            int stage = state.getValue(BlockGoldenLeaves.STAGE);
            if (stage < BlockGoldenLeaves.HIGHEST_STAGE) {
                levelIn.setBlockAndUpdate(pos, state.setValue(BlockGoldenLeaves.STAGE, stage + 1));
            }

            if (stage > 1) {
                var offset = pos.relative(Direction.getRandom(random));
                if (levelIn.isLoaded(offset))
                    BlockGoldenLeaves.convert(levelIn, offset);
            }
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState p_54449_) {
        return true;
    }

    @Override
    public void generateCustomBlockState(BlockStateGenerator generator) {
        generator.simpleBlock(this, generator.models().getExistingFile(generator.modLoc(this.getBaseName())));
    }
}
