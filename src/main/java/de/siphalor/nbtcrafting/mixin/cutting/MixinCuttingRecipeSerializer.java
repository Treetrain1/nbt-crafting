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

package de.siphalor.nbtcrafting.mixin.cutting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.mojang.serialization.Codec;

import com.mojang.serialization.JsonOps;

import de.siphalor.nbtcrafting.NbtCrafting;

import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.CuttingRecipe;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.dynamic.Codecs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CuttingRecipe.Serializer.class)
public class MixinCuttingRecipeSerializer {
	private static ItemStack nbtCrafting_resultStack;

	@Redirect(
			method = "read(Lnet/minecraft/network/PacketByteBuf;)Lnet/minecraft/recipe/CuttingRecipe;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;readString()Ljava/lang/String;")
	)
	public String getResultId(PacketByteBuf instance) {
		nbtCrafting_resultStack = null;
		String key = "group";
		String original = instance.readString();
		try {
			var codec = Codecs.createStrictOptionalFieldCodec(Codec.STRING, key, "").codec();
			var jsonObject = (JsonObject) codec.encodeStart(JsonOps.INSTANCE, original).get().left().get();
			nbtCrafting_resultStack = NbtCrafting.outputFromJson(jsonObject);
			return Registries.ITEM.getId(nbtCrafting_resultStack.getItem()).toString();
		} catch (Exception e) {
			return original;
		}
	}

	@Redirect(
			method = "read(Lnet/minecraft/network/PacketByteBuf;)Lnet/minecraft/recipe/CuttingRecipe;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/PacketByteBuf;readItemStack()Lnet/minecraft/item/ItemStack;"
			)
	)
	public ItemStack createStack(PacketByteBuf instance) {
		if (nbtCrafting_resultStack == null)
			return instance.readItemStack();
		return nbtCrafting_resultStack;
	}
}
