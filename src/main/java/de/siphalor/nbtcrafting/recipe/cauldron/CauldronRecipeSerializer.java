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

import com.mojang.serialization.Codec;

import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class CauldronRecipeSerializer<T extends CauldronRecipe> implements RecipeSerializer<T> {
	private final RecipeFactory<T> recipeFactory;
	private final Codec<T> codec;

	public CauldronRecipeSerializer(RecipeFactory<T> recipeFactory) {
		this.recipeFactory = recipeFactory;
		this.codec = RecordCodecBuilder.create(instance -> instance.group(
				Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("ingredient").forGetter(recipe -> recipe.ingredient),
				Registries.ITEM.getCodec().xmap(ItemStack::new, ItemStack::getItem).fieldOf("result").forGetter(recipe -> recipe.result),
				Identifier.CODEC.fieldOf("fluid").forGetter(recipe -> recipe.fluid),
				Codec.INT.fieldOf("levels").forGetter(recipe -> recipe.levels)
		).apply(instance, recipeFactory::create));
	}

	@Override
	public Codec<T> codec() {
		return this.codec;
	}

	@Override
	public T read(PacketByteBuf packet) {
		Ingredient ingredient = Ingredient.fromPacket(packet);
		ItemStack result = packet.readItemStack();
		Identifier fluid = packet.readIdentifier();
		int levels = packet.readInt();
		return this.recipeFactory.create(ingredient, result, fluid, levels);
	}

	@Override
	public void write(PacketByteBuf packetByteBuf, CauldronRecipe cauldronRecipe) {
		cauldronRecipe.write(packetByteBuf);
	}

	@FunctionalInterface
	public interface RecipeFactory<T extends CauldronRecipe> {
		T create(Ingredient ingredient, ItemStack result, Identifier fluid, int levels);
	}
}
