package de.ellpeck.naturesaura.items.tools;

import de.ellpeck.naturesaura.Helper;
import de.ellpeck.naturesaura.NaturesAura;
import de.ellpeck.naturesaura.api.NaturesAuraAPI;
import de.ellpeck.naturesaura.api.misc.ILevelData;
import de.ellpeck.naturesaura.data.ItemModelGenerator;
import de.ellpeck.naturesaura.items.ModItems;
import de.ellpeck.naturesaura.misc.LevelData;
import de.ellpeck.naturesaura.reg.ICustomItemModel;
import de.ellpeck.naturesaura.reg.IModItem;
import de.ellpeck.naturesaura.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;

public class ItemPickaxe extends PickaxeItem implements IModItem, ICustomItemModel {

    private final String baseName;

    public ItemPickaxe(String baseName, Tier material, int damage, float speed) {
        super(material, damage, speed, new Properties());
        this.baseName = baseName;
        ModRegistry.ALL_ITEMS.add(this);
    }

    @Override
    public String getBaseName() {
        return this.baseName;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (this == ModItems.INFUSED_IRON_PICKAXE) {
            var player = context.getPlayer();
            var level = context.getLevel();
            var pos = context.getClickedPos();
            var stack = player.getItemInHand(context.getHand());
            var state = level.getBlockState(pos);
            var result = NaturesAuraAPI.BOTANIST_PICKAXE_CONVERSIONS.get(state);
            if (result != null) {
                if (!level.isClientSide) {
                    level.setBlockAndUpdate(pos, result);

                    var data = (LevelData) ILevelData.getLevelData(level);
                    data.addMossStone(pos);
                }
                level.playSound(player, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                stack.hurtAndBreak(15, player, p -> p.broadcastBreakEvent(context.getHand()));
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level levelIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (this == ModItems.SKY_PICKAXE) {
            if (!(entityIn instanceof Player))
                return;
            if (!isSelected || levelIn.isClientSide)
                return;
            var bounds = new AABB(entityIn.blockPosition()).inflate(4);
            for (var item : levelIn.getEntitiesOfClass(ItemEntity.class, bounds)) {
                // only pick up freshly dropped items
                if (item.tickCount >= 5 || !item.isAlive())
                    continue;
                item.setPickUpDelay(0);
                item.playerTouch((Player) entityIn);
            }
        }
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
        if (itemstack.getItem() == ModItems.DEPTH_PICKAXE && Helper.isToolEnabled(itemstack) && player.level().getBlockState(pos).is(Tags.Blocks.ORES)) {
            Helper.mineRecursively(player.level(), pos, pos, itemstack, 5, 5, s -> s.is(Tags.Blocks.ORES));
            return true;
        }
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (stack.getItem() == ModItems.DEPTH_PICKAXE && Helper.toggleToolEnabled(player, stack))
            return InteractionResultHolder.success(stack);
        return super.use(level, player, hand);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return Helper.makeRechargeProvider(stack, true);
    }

    @Override
    public void generateCustomItemModel(ItemModelGenerator generator) {
        if (this == ModItems.DEPTH_PICKAXE)
            return;
        generator.withExistingParent(this.getBaseName(), "item/handheld").texture("layer0", "item/" + this.getBaseName());
    }

}
