package mod.heimrarnadalr.kagic.networking;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import mod.akrivus.kagic.client.gui.GUIWarpPadSelection;
import mod.akrivus.kagic.init.KAGIC;
import mod.heimrarnadalr.kagic.worlddata.WarpPadDataEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PadDataMessage implements IMessage {
	private NBTTagCompound padDataCompound;
	private int x;
	private int y;
	private int z;
	
	public PadDataMessage() {}

	public PadDataMessage(NBTTagCompound padDataCompound, int x, int y, int z) {
		this.padDataCompound = padDataCompound;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		// TODO Auto-generated method stub
		int count = buf.readInt();
		byte[] dataBytes = new byte[count];
		buf.readBytes(dataBytes);
		ByteArrayInputStream padDataStream = new ByteArrayInputStream(dataBytes);
		try {
			this.padDataCompound = CompressedStreamTools.readCompressed(padDataStream);
		} catch (IOException e) {
			KAGIC.instance.chatInfoMessage("IOException in toBytes");
			e.printStackTrace();
		}
		this.x = buf.readInt();
		this.y = buf.readInt();
		this.z = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteArrayOutputStream padDataStream = new ByteArrayOutputStream();
		try {
			CompressedStreamTools.writeCompressed(padDataCompound, padDataStream);
			buf.writeInt(padDataStream.size());
			buf.writeBytes(padDataStream.toByteArray());
			buf.writeInt(this.x);
			buf.writeInt(this.y);
			buf.writeInt(this.z);
		} catch (IOException e) {
			KAGIC.instance.chatInfoMessage("IOException in toBytes");
			e.printStackTrace();
		}
	}
	
	public static class PadDataMessageHandler implements IMessageHandler<PadDataMessage, IMessage> {
		@Override
		public IMessage onMessage(PadDataMessage message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}
		
		private LinkedHashMap<BlockPos, WarpPadDataEntry> decodePadData(NBTTagCompound padDataCompound) {
			LinkedHashMap<BlockPos, WarpPadDataEntry> padDataMap = new LinkedHashMap<BlockPos, WarpPadDataEntry>();
			NBTTagList list = padDataCompound.getTagList("pads", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < list.tagCount(); ++i) {
				NBTTagCompound tc = list.getCompoundTagAt(i);
				BlockPos pos = new BlockPos(tc.getInteger("x"), tc.getInteger("y"), tc.getInteger("z"));
				String name = tc.getString("name");
				boolean valid = tc.getBoolean("valid");
				boolean clear = tc.getBoolean("clear");
				padDataMap.put(pos, new WarpPadDataEntry(name, valid, clear));
			}
			return padDataMap;
		}

		private void handle(PadDataMessage message, MessageContext ctx) {
			LinkedHashMap<BlockPos, WarpPadDataEntry> padData = this.decodePadData(message.padDataCompound);
			KAGIC.proxy.openWarpPadSelectionGUI(padData, message.x, message.y, message.z);
		}
	}
}
