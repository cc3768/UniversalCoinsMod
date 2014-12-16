package universalcoins.net;

import universalcoins.tile.TileCardStation;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class UCCardStationServerMessage  implements IMessage, IMessageHandler<UCCardStationServerMessage, IMessage> {
	private int x, y, z, withdrawalAmount;

    public UCCardStationServerMessage() {}

    public UCCardStationServerMessage(int x, int y, int z, int withdrawalAmount) { 
    	this.x = x;
    	this.y = y;
    	this.z = z;
        this.withdrawalAmount = withdrawalAmount;
    }

    @Override
    public void toBytes(ByteBuf buf) { 
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(withdrawalAmount);
    }

    @Override
    public void fromBytes(ByteBuf buf) { 
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.withdrawalAmount = buf.readInt();
	}

	@Override
	public IMessage onMessage(UCCardStationServerMessage message, MessageContext ctx) {
		World world = ctx.getServerHandler().playerEntity.worldObj;

		TileEntity tileEntity = world.getTileEntity(message.x, message.y, message.z);
		if (tileEntity instanceof TileCardStation) {
			((TileCardStation) tileEntity).coinWithdrawalAmount = message.withdrawalAmount;
			}
			return null;
	}
}
