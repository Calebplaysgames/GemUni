package mod.heimrarnadalr.kagic.networking;

import io.netty.buffer.ByteBuf;
import mod.akrivus.kagic.init.KAGIC;
import mod.heimrarnadalr.kagic.worlddata.WorldDataWarpPad;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PadDataRequestMessage implements IMessage {
	private int x;
	private int y;
	private int z;
	
	public PadDataRequestMessage() {}
	
	public PadDataRequestMessage(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		this.x = buf.readInt();
		this.y = buf.readInt();
		this.z = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(this.x);
		buf.writeInt(this.y);
		buf.writeInt(this.z);
	}

	public static class PadDataRequestMessageHandler implements IMessageHandler<PadDataRequestMessage, IMessage> {
		@Override
		public IMessage onMessage(PadDataRequestMessage message, MessageContext ctx) {
			((WorldServer) ctx.getServerHandler().playerEntity.world).addScheduledTask(() -> handle(message, ctx));
			return null;
		}
		
		private void handle(PadDataRequestMessage message, MessageContext ctx) {
			EntityPlayerMP playerEntity = ctx.getServerHandler().playerEntity;
			World world = playerEntity.getEntityWorld();
			NBTTagCompound data = new NBTTagCompound();
			WorldDataWarpPad padData = WorldDataWarpPad.get(world);
			data = padData.writeToNBT(data);
			KTPacketHandler.INSTANCE.sendTo(new PadDataMessage(data, message.x, message.y, message.z), ctx.getServerHandler().playerEntity);
		}
	}
}
