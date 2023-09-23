/*
 * Copyright 2020-2022 Siphalor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

package de.siphalor.nbtcrafting.recipe.cauldron;

import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.api.RecipeUtil;
import de.siphalor.nbtcrafting.api.ServerRecipe;
import de.siphalor.nbtcrafting.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting.api.recipe.NBTCRecipe;
import de.siphalor.nbtcrafting.dollar.Dollar;
import de.siphalor.nbtcrafting.dollar.DollarParser;

public class CauldronRecipe implements NBTCRecipe<TemporaryCauldronInventory>, ServerRecipe {
	public final Ingredient ingredient;
	public final ItemStack result;
	public final Identifier fluid;
	public final int levels;
	private final Dollar[] outputDollars;

	public CauldronRecipe(Ingredient ingredient, ItemStack result, Identifier fluid, int levels) {
		this.ingredient = ingredient;
		this.result = result;
		this.fluid = fluid;
		this.levels = levels;
		this.outputDollars = DollarParser.extractDollars(result.getNbt(), false);
	}

	public void write(PacketByteBuf packetByteBuf) {
		ingredient.write(packetByteBuf);
		packetByteBuf.writeItemStack(result);
		packetByteBuf.writeIdentifier(fluid);
		packetByteBuf.writeShort(levels);
	}

	@Override
	public boolean matches(TemporaryCauldronInventory inventory, World world) {
		if (fluid != null && !fluid.equals(inventory.getFluid())) {
			return false;
		}
		if (!ingredient.test(inventory.getStack(0))) {
			return false;
		}
		if (levels >= 0) {
			return inventory.getLevel() >= levels;
		} else {
			return inventory.getMaxLevel() - inventory.getLevel() >= -levels;
		}
	}

	@Override
	public ItemStack craft(TemporaryCauldronInventory inventory, DynamicRegistryManager dynamicRegistryManager) {
		inventory.setLevel(inventory.getLevel() - levels);

		inventory.getStack(0).decrement(1);

		return RecipeUtil.applyDollars(result.copy(), outputDollars, buildDollarReference(inventory));
	}

	@Override
	public boolean fits(int i, int i1) {
		return false;
	}

	@Override
	public ItemStack getResult(DynamicRegistryManager dynamicRegistryManager) {
		return result;
	}

	@Override
	public DefaultedList<Ingredient> getIngredients() {
		return DefaultedList.copyOf(Ingredient.EMPTY, ingredient);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return NbtCrafting.CAULDRON_RECIPE_SERIALIZER;
	}

	@Override
	public RecipeType<?> getType() {
		return NbtCrafting.CAULDRON_RECIPE_TYPE;
	}

	@Override
	public Map<String, Object> buildDollarReference(TemporaryCauldronInventory inv) {
		return Map.of("ingredient", NbtUtil.getTagOrEmpty(inv.getStack(0)));
	}
}
