package de.ellpeck.naturesaura.packet;

import de.ellpeck.naturesaura.NaturesAura;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.api.distmarker.Dist;

public final class PacketHandler {

    private static SimpleNetworkWrapper network;

    public static void init() {
        network = new SimpleNetworkWrapper(NaturesAura.MOD_ID);
        network.registerMessage(PacketParticleStream.Handler.class, PacketParticleStream.class, 0, Dist.CLIENT);
        network.registerMessage(PacketParticles.Handler.class, PacketParticles.class, 1, Dist.CLIENT);
        network.registerMessage(PacketAuraChunk.Handler.class, PacketAuraChunk.class, 2, Dist.CLIENT);
        network.registerMessage(PacketClient.Handler.class, PacketClient.class, 3, Dist.CLIENT);
    }

    public static void sendToAllLoaded(World world, BlockPos pos, IMessage message) {
        network.sendToAllTracking(message, new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 0));
    }

    public static void sendToAllAround(World world, BlockPos pos, int range, IMessage message) {
        network.sendToAllAround(message, new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), range));
    }

    public static void sendTo(PlayerEntity player, IMessage message) {
        network.sendTo(message, (ServerPlayerEntity) player);
    }
}
