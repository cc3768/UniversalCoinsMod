package universalcoins.util;

import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import universalcoins.UniversalCoins;

public class RecipePlankTextureChange implements IRecipe {

	private ItemStack newStack;
	private ItemStack plankStack;

	@Override
	public boolean matches(InventoryCrafting inventorycrafting, World world) {
		this.newStack = null;
		boolean hasItem = false;
		boolean hasPlank = false;
		for (int j = 0; j < inventorycrafting.getSizeInventory(); j++) {
			if (inventorycrafting.getStackInSlot(j) != null && !hasPlank && 
					isWoodPlank(inventorycrafting.getStackInSlot(j))) {
					hasPlank = true;
					plankStack = inventorycrafting.getStackInSlot(j);
					continue;
			}
			if (inventorycrafting.getStackInSlot(j) != null && !hasItem && 
					(inventorycrafting.getStackInSlot(j).getItem() == UniversalCoins.proxy.itemUCSign ||
					Block.getBlockFromItem(inventorycrafting.getStackInSlot(j).getItem()) == UniversalCoins.proxy.blockVendorFrame)) {
				hasItem = true;
				newStack = inventorycrafting.getStackInSlot(j);
				continue;
			}
			if (inventorycrafting.getStackInSlot(j) != null) {
				return false;
			}
		}
		
		if (!hasPlank || !hasItem)
			return false;
		else
			return true;
	}

	private boolean isWoodPlank(ItemStack stack) {
		for (ItemStack oreStack : OreDictionary.getOres("plankWood")) {
			if (OreDictionary.itemMatches(oreStack, stack, false)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting var1) {
		//newStack = new ItemStack(inventorycrafting.getStackInSlot(itemIndex).getItem());
		String blockIcon = plankStack.getIconIndex().getIconName();

		// the iconIndex function does not work with BOP so we have to do a
		// bit of a hack here
		if (blockIcon.startsWith("biomesoplenty")) {
			String[] iconInfo = blockIcon.split(":");
			String[] blockName = plankStack.getUnlocalizedName().split("\\.", 3);
			String woodType = blockName[2].replace("Plank", "");
			// hellbark does not follow the same naming convention
			if (woodType.contains("hell"))
				woodType = "hell_bark";
			blockIcon = iconInfo[0] + ":" + "plank_" + woodType;
			// bamboo needs a hack too
			if (blockIcon.contains("bamboo"))
				blockIcon = blockIcon.replace("plank_bambooThatching",
						"bamboothatching");
			// I feel dirty now :(
		}
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("BlockIcon", blockIcon);
		this.newStack.setTagCompound(tag);
		return newStack;
	}

	@Override
	public int getRecipeSize() {
		return 2;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return newStack;
	}

}
