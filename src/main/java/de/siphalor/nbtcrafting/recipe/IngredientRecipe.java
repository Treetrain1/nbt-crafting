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

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import de.siphalor.nbtcrafting.api.RecipeUtil;
import de.siphalor.nbtcrafting.api.ServerRecipe;
import de.siphalor.nbtcrafting.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting.api.recipe.NBTCRecipe;
import de.siphalor.nbtcrafting.dollar.Dollar;
import de.siphalor.nbtcrafting.dollar.DollarParser;

public class IngredientRecipe<I extends Inventory> implements NBTCRecipe<I>, ServerRecipe {
	protected final Ingredient base;
	protected final Ingredient ingredient;
	protected final ItemStack result;
	protected final Dollar[] resultDollars;
	protected final RecipeType<? extends IngredientRecipe<I>> recipeType;
	protected final RecipeSerializer<? extends IngredientRecipe<I>> serializer;

	public IngredientRecipe(Ingredient base, Optional<Ingredient> ingredient, ItemStack result, RecipeType<? extends IngredientRecipe<I>> recipeType, RecipeSerializer<? extends IngredientRecipe<I>> serializer) {
		this.base = base;
		this.ingredient = ingredient.orElse(Ingredient.EMPTY);
		this.result = result;
		this.resultDollars = DollarParser.extractDollars(result.getNbt(), false);
		this.recipeType = recipeType;
		this.serializer = serializer;
	}

	@Override
	public boolean matches(I inv, World world) {
		if (ingredient != null && ingredient.test(inv.getStack(1))) {
			return base.test(inv.getStack(0));
		}
		return false;
	}

	@Override
	public boolean fits(int width, int height) {
		return false;
	}

	@Override
	public ItemStack craft(I inv, DynamicRegistryManager dynamicRegistryManager) {
		return RecipeUtil.applyDollars(result.copy(), resultDollars, buildDollarReference(inv));
	}

	@Override
	public ItemStack getResult(DynamicRegistryManager dynamicRegistryManager) {
		return result;
	}

	public Ingredient getBase() {
		return base;
	}

	public Ingredient getIngredient() {
		return ingredient;
	}

	@Override
	public DefaultedList<Ingredient> getIngredients() {
		return DefaultedList.copyOf(Ingredient.EMPTY, base, ingredient);
	}

	@Override
	public RecipeType<?> getType() {
		return recipeType;
	}

	@Override
	public RecipeSerializer<? extends IngredientRecipe<I>> getSerializer() {
		return serializer;
	}

	public Map<String, Object> buildDollarReference(I inv) {
		return ImmutableMap.of(
				"base", NbtUtil.getTagOrEmpty(inv.getStack(0)),
				"ingredient", NbtUtil.getTagOrEmpty(inv.getStack(1))
		);
	}

	public void readCustomData(JsonObject json) {
	}

	public void readCustomData(PacketByteBuf buf) {
	}

	public void writeCustomData(PacketByteBuf buf) {
	}

	public interface Factory<R extends IngredientRecipe<?>> {
		R create(Ingredient base, Optional<Ingredient> ingredient, ItemStack result, Serializer<R> serializer);
	}

	public static class Serializer<R extends IngredientRecipe<?>> implements RecipeSerializer<R> {
		private final Factory<R> factory;
		private final Codec<R> codec;

		public Serializer(Factory<R> factory) {
			this.factory = factory;
			this.codec = RecordCodecBuilder.create(instance -> instance.group(
					Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("base").forGetter(recipe -> recipe.base),
					Ingredient.DISALLOW_EMPTY_CODEC.optionalFieldOf("ingredient").forGetter(recipe -> Optional.of(recipe.ingredient)),
					Registries.ITEM.getCodec().xmap(ItemStack::new, ItemStack::getItem).fieldOf("result").forGetter(recipe -> recipe.result)
			).apply(instance, (base, ingredient, result) -> factory.create(base, ingredient, result, this)));
		}

		@Override
		public Codec<R> codec() {
			return this.codec;
		}

		@Override
		public R read(PacketByteBuf buf) {
			Ingredient base = Ingredient.fromPacket(buf);
			Optional<Ingredient> ingredient = Optional.of(Optional.of(Ingredient.fromPacket(buf)).orElse(Ingredient.EMPTY));
			ItemStack result = buf.readItemStack();
			R recipe = factory.create(base, ingredient, result, this);
			recipe.readCustomData(buf);
			return recipe;
		}

		@Override
		public void write(PacketByteBuf buf, R recipe) {
			recipe.base.write(buf);
			recipe.ingredient.write(buf);
			buf.writeItemStack(recipe.result);
			recipe.writeCustomData(buf);
		}
	}
}
