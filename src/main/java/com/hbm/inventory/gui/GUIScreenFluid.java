package com.hbm.inventory.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFluidIDMulti;
import com.hbm.lib.RefStrings;
import com.hbm.packet.toserver.NBTItemControlPacket;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;

/** Fluid selection for the multi fluid identifier: search field and the first 9 matches */
public class GUIScreenFluid extends Screen {

	protected static final ResourceLocation texture = RefStrings.loc("textures/gui/machine/gui_fluid.png");
	protected int xSize = 176;
	protected int ySize = 54;
	protected int guiLeft;
	protected int guiTop;
	private EditBox search;

	private final Player player;
	private FluidType primary = Fluids.NONE;
	private FluidType secondary = Fluids.NONE;
	private FluidType[] searchArray = new FluidType[9];

	public GUIScreenFluid(Player player) {
		super(Component.translatable("item.hbm.fluid_identifier_multi"));
		this.player = player;
	}

	private boolean holdsIdentifier() {
		return player.getMainHandItem().is(ModItems.fluid_identifier_multi.get());
	}

	@Override
	protected void init() {
		super.init();
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;

		this.search = new EditBox(this.font, guiLeft + 46, guiTop + 11, 86, 12, Component.empty());
		this.search.setTextColor(-1);
		this.search.setTextColorUneditable(-1);
		this.search.setBordered(false);
		this.search.setResponder(text -> updateSearch());
		this.addRenderableWidget(this.search);
		this.setInitialFocus(this.search);

		if(holdsIdentifier()) {
			this.primary = ItemFluidIDMulti.getType(player.getMainHandItem(), true);
			this.secondary = ItemFluidIDMulti.getType(player.getMainHandItem(), false);
		}

		updateSearch();
	}

	@Override
	public void tick() {
		if(!holdsIdentifier()) this.onClose();
	}

	private boolean isOverResult(int k, double x, double y) {
		return guiLeft + 7 + k * 18 <= x && guiLeft + 7 + k * 18 + 18 > x && guiTop + 29 < y && guiTop + 29 + 18 >= y;
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {

		for(int k = 0; k < this.searchArray.length; k++) {
			if(this.searchArray[k] == null) break;

			if(isOverResult(k, x, y) && (button == 0 || button == 1)) {
				minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
				CompoundTag data = new CompoundTag();
				if(button == 0) {
					this.primary = this.searchArray[k];
					data.putInt("primary", this.primary.getID());
				} else {
					this.secondary = this.searchArray[k];
					data.putInt("secondary", this.secondary.getID());
				}
				NBTItemControlPacket.send(data);
				return true;
			}
		}

		return super.mouseClicked(x, y, button);
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.renderBackground(graphics, mouseX, mouseY, partialTick);

		graphics.blit(texture, guiLeft, guiTop, 0, 0, xSize, ySize);

		if(this.search.isFocused())
			graphics.blit(texture, guiLeft + 43, guiTop + 7, 166, 54, 90, 18);

		for(int k = 0; k < this.searchArray.length; k++) {
			FluidType type = this.searchArray[k];
			if(type == null) break;

			int color = type.getColor();
			graphics.setColor(((color >> 16) & 0xFF) / 255F, ((color >> 8) & 0xFF) / 255F, (color & 0xFF) / 255F, 1.0F);
			graphics.blit(texture, guiLeft + 12 + k * 18, guiTop + 31, 12 + k * 18, 56, 8, 14);
			graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

			if(type == this.primary && type == this.secondary) {
				graphics.blit(texture, guiLeft + 7 + k * 18, guiTop + 29, 176, 36, 18, 18);
			} else if(type == this.primary) {
				graphics.blit(texture, guiLeft + 7 + k * 18, guiTop + 29, 176, 0, 18, 18);
			} else if(type == this.secondary) {
				graphics.blit(texture, guiLeft + 7 + k * 18, guiTop + 29, 176, 18, 18, 18);
			}
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);

		for(int k = 0; k < this.searchArray.length; k++) {
			if(this.searchArray[k] == null) break;

			if(isOverResult(k, mouseX, mouseY)) {
				List<String> info = new ArrayList<>();
				this.searchArray[k].addInfo(info);
				List<Component> tooltip = new ArrayList<>();
				tooltip.add(Component.translatable(this.searchArray[k].getConditionalName()));
				for(String line : info) tooltip.add(Component.literal(line));
				graphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
			}
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void updateSearch() {
		this.searchArray = new FluidType[9];

		int next = 0;
		String subs = this.search.getValue().toLowerCase(Locale.US);

		for(FluidType type : Fluids.getInNiceOrder()) {
			String name = type.getLocalizedName().toLowerCase(Locale.US);

			if(name.contains(subs) && !type.hasNoID()) {
				this.searchArray[next] = type;
				next++;

				if(next >= 9)
					return;
			}
		}
	}
}
