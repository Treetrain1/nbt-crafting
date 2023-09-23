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

package de.siphalor.nbtcrafting.recipe;

import com.google.gson.JsonObject;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import de.siphalor.nbtcrafting.NbtCrafting;

import java.util.Optional;

public class AnvilRecipe extends IngredientRecipe<Inventory> {
	protected int levels = 0;

	public static final IngredientRecipe.Serializer<AnvilRecipe> SERIALIZER = new IngredientRecipe.Serializer<>((AnvilRecipe::new));

	public AnvilRecipe(Ingredient base, Optional<Ingredient> ingredient, ItemStack result, Serializer<AnvilRecipe> serializer) {
		super(base, ingredient, result, NbtCrafting.ANVIL_RECIPE_TYPE, SERIALIZER);
	}

	public int getLevels() {
		return levels;
	}

	@Override
	public void readCustomData(PacketByteBuf buf) {
		super.readCustomData(buf);
		levels = buf.readVarInt();
	}

	@Override
	public void writeCustomData(PacketByteBuf buf) {
		super.writeCustomData(buf);
		buf.writeVarInt(levels);
	}
}
