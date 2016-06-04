package universalcoins.gui;

import java.text.DecimalFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import universalcoins.inventory.ContainerPowerTransmitter;
import universalcoins.tile.TilePowerTransmitter;

public class PowerTransmitterGUI extends GuiContainer {
	private TilePowerTransmitter tEntity;
	private GuiButton coinButton, accessModeButton;
	public static final int idCoinButton = 0;
	public static final int idAccessModeButton = 1;
	DecimalFormat formatter = new DecimalFormat("#,###,###,###");

	public PowerTransmitterGUI(InventoryPlayer inventoryPlayer, TilePowerTransmitter tileEntity) {
		super(new ContainerPowerTransmitter(inventoryPlayer, tileEntity));
		tEntity = tileEntity;

		xSize = 176;
		ySize = 157;
	}

	@Override
	public void initGui() {
		super.initGui();
		coinButton = new GuiSlimButton(idCoinButton, 123 + (width - xSize) / 2, 60 + (height - ySize) / 2, 46, 12,
				StatCollector.translateToLocal("general.button.coin"));
		accessModeButton = new GuiSlimButton(idAccessModeButton, 122 + (width - xSize) / 2, 4 + (height - ySize) / 2,
				50, 12, StatCollector.translateToLocal("general.label.public"));
		buttonList.clear();
		buttonList.add(coinButton);
		buttonList.add(accessModeButton);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		final ResourceLocation texture = new ResourceLocation("universalcoins", "textures/gui/power_transmitter.png");
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
		
		if (tEntity.publicAccess) {
			accessModeButton.displayString = StatCollector.translateToLocal("general.label.public");
		} else {
			accessModeButton.displayString = StatCollector.translateToLocal("general.label.private");
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int param1, int param2) {
		fontRendererObj.drawString(tEntity.getInventoryName(), 6, 5, 4210752);

		fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 6, 63, 4210752);

		// display rf sold
		String formattedkrf = formatter.format(tEntity.krfSold);
		int rfLength = fontRendererObj.getStringWidth(formattedkrf + " kRF");
		String overage = (tEntity.krfSold == Integer.MAX_VALUE ? "+": "");
		fontRendererObj.drawString(formattedkrf + overage +" kRF", 131 - rfLength, 26, 4210752);

		// display coin balance
		String formattedBalance = formatter.format(tEntity.coinSum);
		int balLength = fontRendererObj.getStringWidth(formattedBalance);
		fontRendererObj.drawString(formattedBalance, 131 - balLength, 48, 4210752);
	}

	protected void actionPerformed(GuiButton button) {
		tEntity.sendPacket(button.id, isShiftKeyDown());
	}
}