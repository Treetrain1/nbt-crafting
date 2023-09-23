package de.siphalor.nbtcrafting.mixin.network;

import de.siphalor.nbtcrafting.network.ServerNetworkHandlerAccess;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayNetworkHandler extends ServerCommonNetworkHandler implements ServerNetworkHandlerAccess {

	private MixinServerPlayNetworkHandler(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
		super(server, connection, clientData);
	}

	@Override
	public ClientConnection nbtCrafting$getConnection() {
		return this.connection;
	}
}
