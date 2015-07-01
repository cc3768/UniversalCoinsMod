package universalcoins.tile;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;
import universalcoins.net.UCButtonMessage;
import universalcoins.util.UniversalAccounts;

public class TilePackager extends TileEntity implements IInventory, ISidedInventory {

	private ItemStack[] inventory = new ItemStack[11];
	public static final int[] itemPackageSlot = { 0, 1, 2, 3, 4, 5, 6, 7 };
	public static final int itemCardSlot = 8;
	public static final int itemCoinSlot = 9;
	public static final int itemOutputSlot = 10;
	private static final int[] multiplier = new int[] { 1, 9, 81, 729, 6561 };
	private static final Item[] coins = new Item[] { UniversalCoins.proxy.itemCoin,
			UniversalCoins.proxy.itemSmallCoinStack, UniversalCoins.proxy.itemLargeCoinStack,
			UniversalCoins.proxy.itemSmallCoinBag, UniversalCoins.proxy.itemLargeCoinBag };
	public int coinSum = 0;
	public boolean cardAvailable = false;
	public String customName = "";
	public String playerName = "";
	public boolean inUse = false;
	public int packageSize = 0;
	public int[] packageCost = { UniversalCoins.smallPackagePrice, UniversalCoins.medPackagePrice,
			UniversalCoins.largePackagePrice };

	public TilePackager() {
		super();
	}

	public void onButtonPressed(int buttonId) {
		if (buttonId == 0) {
			if (inventory[itemOutputSlot] == null) {

				NBTTagList itemList = new NBTTagList();
				NBTTagCompound tagCompound = new NBTTagCompound();
				for (int i = 0; i < itemPackageSlot.length; i++) {
					ItemStack invStack = inventory[i];
					if (invStack != null && invStack.getItem() != UniversalCoins.proxy.itemPackage) {
						NBTTagCompound tag = new NBTTagCompound();
						tag.setByte("Slot", (byte) i);
						invStack.writeToNBT(tag);
						itemList.appendTag(tag);
						inventory[i] = null;
					}
				}
				if (itemList.tagCount() > 0) {
					inventory[itemOutputSlot] = new ItemStack(UniversalCoins.proxy.itemPackage);
					tagCompound.setTag("Inventory", itemList);
					inventory[itemOutputSlot].setTagCompound(tagCompound);
					if (cardAvailable) {
						String account = inventory[itemCardSlot].getTagCompound().getString("accountNumber");
						UniversalAccounts.getInstance().debitAccount(worldObj, account, packageCost[packageSize]);
					} else {
						coinSum -= packageCost[packageSize];

					}
				}

			}
		}
		if (buttonId == 1) {
			fillOutputSlot();
		}
		if (buttonId == 2) {
			packageSize = 0;
			for (int i = 0; i < 4; i++) {
				if (inventory[i] != null) {
					if (worldObj.getPlayerEntityByName(playerName).inventory.getFirstEmptyStack() != -1) {
						worldObj.getPlayerEntityByName(playerName).inventory.addItemStackToInventory(inventory[i]);
					} else {
						// spawn in world
						EntityItem entityItem = new EntityItem(worldObj, xCoord, yCoord, zCoord, inventory[i]);
						worldObj.spawnEntityInWorld(entityItem);
					}
					inventory[i] = null;
				}
			}
		}
		if (buttonId == 3) {
			packageSize = 1;
			for (int i = 0; i < 2; i++) {
				if (inventory[i] != null) {
					if (worldObj.getPlayerEntityByName(playerName).inventory.getFirstEmptyStack() != -1) {
						worldObj.getPlayerEntityByName(playerName).inventory.addItemStackToInventory(inventory[i]);
					} else {
						// spawn in world
						EntityItem entityItem = new EntityItem(worldObj, xCoord, yCoord, zCoord, inventory[i]);
						worldObj.spawnEntityInWorld(entityItem);
					}
					inventory[i] = null;
				}
			}
		}
		if (buttonId == 4) {
			packageSize = 2;
		}
	}

	public void inUseCleanup() {
		if (worldObj.isRemote)
			return;
		inUse = false;
		updateTE();
	}

	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.customName : UniversalCoins.proxy.blockPackager.getLocalizedName();
	}

	public void setInventoryName(String name) {
		customName = name;
	}

	public boolean isInventoryNameLocalized() {
		return false;
	}

	@Override
	public boolean hasCustomInventoryName() {
		return this.customName != null && this.customName.length() > 0;
	}

	private int getCoinType(Item item) {
		for (int i = 0; i < 5; i++) {
			if (item == coins[i]) {
				return i;
			}
		}
		return -1;
	}

	public void checkCard() {
		cardAvailable = false;
		if (inventory[itemCardSlot] != null) {
			String account = inventory[itemCardSlot].getTagCompound().getString("accountNumber");
			int accountBalance = UniversalAccounts.getInstance().getAccountBalance(worldObj, account);
			if (accountBalance > packageCost[packageSize]) {
				cardAvailable = true;
			}
		}
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
				&& entityplayer.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
	}

	public void sendPacket(int button, boolean shiftPressed) {
		UniversalCoins.snw.sendToServer(new UCButtonMessage(xCoord, yCoord, zCoord, button, shiftPressed));
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		readFromNBT(pkt.func_148857_g());
	}

	public void updateTE() {
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public void writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		NBTTagList itemList = new NBTTagList();
		for (int i = 0; i < inventory.length; i++) {
			ItemStack stack = inventory[i];
			if (stack != null) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setByte("Slot", (byte) i);
				stack.writeToNBT(tag);
				itemList.appendTag(tag);
			}
		}
		tagCompound.setTag("Inventory", itemList);
		tagCompound.setInteger("coinSum", coinSum);
		tagCompound.setBoolean("cardAvailable", cardAvailable);
		tagCompound.setString("customName", customName);
		tagCompound.setBoolean("inUse", inUse);
		tagCompound.setInteger("packageSize", packageSize);
		tagCompound.setInteger("smallPrice", packageCost[0]);
		tagCompound.setInteger("medPrice", packageCost[1]);
		tagCompound.setInteger("largePrice", packageCost[2]);
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);

		NBTTagList tagList = tagCompound.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound tag = (NBTTagCompound) tagList.getCompoundTagAt(i);
			byte slot = tag.getByte("Slot");
			if (slot >= 0 && slot < inventory.length) {
				inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
			}
		}
		try {
			coinSum = tagCompound.getInteger("coinSum");
		} catch (Throwable ex2) {
			coinSum = 0;
		}
		try {
			cardAvailable = tagCompound.getBoolean("cardAvailable");
		} catch (Throwable ex2) {
			cardAvailable = false;
		}
		try {
			customName = tagCompound.getString("customName");
		} catch (Throwable ex2) {
			customName = "";
		}
		try {
			packageSize = tagCompound.getInteger("packageSize");
		} catch (Throwable ex2) {
			packageSize = 0;
		}
		try {
			packageCost[0] = tagCompound.getInteger("smallPrice");
		} catch (Throwable ex2) {
			packageCost[0] = UniversalCoins.smallPackagePrice;
		}
		try {
			packageCost[1] = tagCompound.getInteger("medPrice");
		} catch (Throwable ex2) {
			packageCost[1] = UniversalCoins.medPackagePrice;
		}
		try {
			packageCost[2] = tagCompound.getInteger("largePrice");
		} catch (Throwable ex2) {
			packageCost[2] = UniversalCoins.largePackagePrice;
		}
	}

	@Override
	public int getSizeInventory() {
		return inventory.length;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		if (i >= inventory.length) {
			return null;
		}
		return inventory[i];
	}

	@Override
	public ItemStack decrStackSize(int slot, int size) {
		ItemStack stack = getStackInSlot(slot);
		if (stack != null) {
			if (stack.stackSize <= size) {
				inventory[slot] = null;
			} else {
				stack = stack.splitStack(size);
				if (stack.stackSize == 0) {
					inventory[slot] = null;
				}
			}
			if (slot == itemCardSlot) {
				checkCard();
			}
		}
		return stack;
	}

	public void fillOutputSlot() {
		inventory[itemOutputSlot] = null;
		if (coinSum > 0) {
			// use logarithm to find largest cointype for the balance
			int logVal = Math.min((int) (Math.log(coinSum) / Math.log(9)), 4);
			int stackSize = Math.min((int) (coinSum / Math.pow(9, logVal)), 64);
			// add a stack to the slot
			inventory[itemOutputSlot] = new ItemStack(coins[logVal], stackSize);
			int itemValue = multiplier[logVal];
			int debitAmount = 0;
			debitAmount = Math.min(stackSize, (Integer.MAX_VALUE - coinSum) / itemValue);
			if (!worldObj.isRemote) {
				coinSum -= debitAmount * itemValue;
			}
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		return getStackInSlot(i);
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		inventory[slot] = stack;
		if (stack != null) {
			if (slot == itemCoinSlot) {
				int coinType = getCoinType(stack.getItem());
				if (coinType != -1) {
					int itemValue = multiplier[coinType];
					int depositAmount = Math.min(stack.stackSize, (Integer.MAX_VALUE - coinSum) / itemValue);
					coinSum += depositAmount * itemValue;
					inventory[slot].stackSize -= depositAmount;
					if (inventory[slot].stackSize == 0) {
						inventory[slot] = null;
					}
				}
			}
			if (slot == itemCardSlot) {
				checkCard();
			}
		}
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public boolean isItemValidForSlot(int var1, ItemStack var2) {
		return false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1) {
		//return all slots so connections can go anywhere
		if (packageSize == 0) return new int[] { 4, 5, 6, 7, 8, 9, 10 };
		if (packageSize == 1) return new int[] { 2, 3, 4, 5, 6, 7, 8, 9, 10 };
		if (packageSize == 2) return new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
		return null;
	}

	@Override
	public boolean canInsertItem(int var1, ItemStack var2, int var3) {
		// first check if items inserted are coins. put them in the coin input slot if they are.
		if (var1 == itemCoinSlot
				&& (var2.getItem() == (UniversalCoins.proxy.itemCoin)
						|| var2.getItem() == (UniversalCoins.proxy.itemSmallCoinStack)
						|| var2.getItem() == (UniversalCoins.proxy.itemLargeCoinStack)
						|| var2.getItem() == (UniversalCoins.proxy.itemSmallCoinBag) || var2.getItem() == (UniversalCoins.proxy.itemLargeCoinBag))) {
			return true;
			// put everything else in the item input slot
		} else if (var1 < itemPackageSlot.length) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean canExtractItem(int var1, ItemStack var2, int var3) {
		// allow pulling items from output slot only
		if (var1 == itemOutputSlot) {
			return true;
		} else {
			return false;
		}
	}

}
