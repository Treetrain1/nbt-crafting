package de.siphalor.nbtcrafting.brewing;

import de.siphalor.nbtcrafting.Core;
import de.siphalor.nbtcrafting.dollar.Dollar;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.ingredient.IIngredient;
import de.siphalor.nbtcrafting.util.RecipeUtil;
import de.siphalor.nbtcrafting.util.ServerRecipe;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.*;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Iterator;

public class BrewingRecipe implements Recipe<BrewingStandBlockEntity>, ServerRecipe {
	private Identifier id;
	public Ingredient base;
	public Ingredient ingredient;
	private ItemStack output;
	private Dollar[] outputDollars;

	public BrewingRecipe(Identifier id, Ingredient base, Ingredient ingredient, ItemStack output) {
		this.id = id;
		this.base = base;
		this.ingredient = ingredient;
		this.output = output;
		this.outputDollars = DollarParser.extractDollars(output.getOrCreateTag().copy());
	}

	public static boolean existsMatchingIngredient(ItemStack stack, RecipeManager recipeManager) {
		Iterator<BrewingRecipe> iterator = recipeManager.values().stream().filter(recipe -> recipe.getType() == Core.BREWING_RECIPE_TYPE).map(recipe -> (BrewingRecipe) recipe).iterator();
		while(iterator.hasNext()) {
			if(iterator.next().ingredientMatches(stack)) {
				return true;
			}
		}
		return false;
	}

	public boolean ingredientMatches(ItemStack stack) {
		return ingredient.test(stack);
	}

	public static boolean existsMatchingBase(ItemStack stack, RecipeManager recipeManager) {
		Iterator<BrewingRecipe> iterator = recipeManager.values().stream().filter(recipe -> recipe.getType() == Core.BREWING_RECIPE_TYPE).map(recipe -> (BrewingRecipe) recipe).iterator();
		while(iterator.hasNext()) {
			if(iterator.next().baseMatches(stack)) {
				return true;
			}
		}
		return false;
	}

	public boolean baseMatches(ItemStack stack) {
		return base.test(stack);
	}

	public void write(PacketByteBuf buffer) {
		buffer.writeIdentifier(id);
		base.write(buffer);
		ingredient.write(buffer);
		buffer.writeItemStack(output);
	}

	public static BrewingRecipe from(PacketByteBuf buffer) {
		Identifier id = buffer.readIdentifier();
		Ingredient base = Ingredient.fromPacket(buffer);
		Ingredient ingredient = Ingredient.fromPacket(buffer);
		ItemStack output = buffer.readItemStack();
		return new BrewingRecipe(id, base, ingredient, output);
	}

	@Override
	public boolean matches(BrewingStandBlockEntity brewingStandBlockEntity, World var2) {
		if(!ingredient.test(brewingStandBlockEntity.getInvStack(3))) {
			return false;
		}
		for(byte i = 0; i < 3; i++) {
			if(base.test(brewingStandBlockEntity.getInvStack(i)))
				return true;
		}
		return false;
	}

	@Override
	public ItemStack craft(BrewingStandBlockEntity brewingStandBlockEntity) {
		ItemStack ingredientStack = brewingStandBlockEntity.getInvStack(3);
		ingredientStack.split(1);
		HashMap<String, CompoundTag> map = new HashMap<>(1);
		map.put("ingredient", ingredientStack.getOrCreateTag());
		ItemStack remainder = ((IIngredient)(Object) base).getRecipeRemainder(ingredientStack, map);
		if(!ingredientStack.isEmpty()) {
			ItemScatterer.spawn(brewingStandBlockEntity.getWorld(), brewingStandBlockEntity.getPos(), DefaultedList.copyOf(remainder));
		} else if(remainder != null) {
			brewingStandBlockEntity.setInvStack(3, remainder);
		}

		for(byte i = 0; i < 3; i++) {
			ItemStack baseStack = brewingStandBlockEntity.getInvStack(i);
			if(base.test(baseStack)) {
				map.replace("base", baseStack.getOrCreateTag());
				brewingStandBlockEntity.setInvStack(i, RecipeUtil.applyDollars(output.copy(), outputDollars, map));
			}
		}
		return null;
	}

	@Override
	public boolean fits(int var1, int var2) {
		return true;
	}

	@Override
	public ItemStack getOutput() {
		return output;
	}

	@Override
	public Identifier getId() {
		return id;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return Core.BREWING_RECIPE_SERIALIZER;
	}

	@Override
	public RecipeType<?> getType() {
		return Core.BREWING_RECIPE_TYPE;
	}
}
