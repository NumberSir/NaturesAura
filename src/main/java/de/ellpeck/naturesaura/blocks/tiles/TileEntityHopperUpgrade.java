package de.ellpeck.naturesaura.blocks.tiles;

import de.ellpeck.naturesaura.api.aura.chunk.IAuraChunk;
import de.ellpeck.naturesaura.packet.PacketHandler;
import de.ellpeck.naturesaura.packet.PacketParticles;
import net.minecraft.block.HopperBlock;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class TileEntityHopperUpgrade extends TileEntityImpl implements ITickable {
    @Override
    public void update() {
        if (!this.world.isRemote && this.world.getTotalWorldTime() % 10 == 0) {
            if (IAuraChunk.getAuraInArea(this.world, this.pos, 25) < 100000)
                return;
            TileEntity tile = this.world.getTileEntity(this.pos.down());
            if (!isValidHopper(tile))
                return;
            if (!tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP))
                return;
            IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP);
            if (handler == null)
                return;

            List<ItemEntity> items = this.world.getEntitiesWithinAABB(ItemEntity.class,
                    new AxisAlignedBB(this.pos).grow(7));
            if (items.isEmpty())
                return;

            for (ItemEntity item : items) {
                if (item.isDead || item.cannotPickup())
                    continue;
                ItemStack stack = item.getItem();
                if (stack.isEmpty())
                    continue;
                ItemStack copy = stack.copy();

                for (int i = 0; i < handler.getSlots(); i++) {
                    copy = handler.insertItem(i, copy, false);
                    if (copy.isEmpty()) {
                        break;
                    }
                }

                if (!ItemStack.areItemStacksEqual(stack, copy)) {
                    item.setItem(copy);
                    if (copy.isEmpty())
                        item.setDead();

                    BlockPos spot = IAuraChunk.getHighestSpot(this.world, this.pos, 25, this.pos);
                    IAuraChunk.getAuraChunk(this.world, spot).drainAura(spot, 500);

                    PacketHandler.sendToAllAround(this.world, this.pos, 32,
                            new PacketParticles((float) item.posX, (float) item.posY, (float) item.posZ, 10));
                }
            }
        }
    }

    private static boolean isValidHopper(TileEntity tile) {
        if (tile instanceof HopperTileEntity)
            return HopperBlock.isEnabled(tile.getBlockMetadata());
        if (tile instanceof TileEntityGratedChute)
            return ((TileEntityGratedChute) tile).redstonePower <= 0;
        return false;
    }
}
