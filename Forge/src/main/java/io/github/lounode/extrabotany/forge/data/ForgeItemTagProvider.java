package io.github.lounode.extrabotany.forge.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;
import io.github.lounode.extrabotany.common.lib.LibMisc;

import java.util.concurrent.CompletableFuture;

import static io.github.lounode.extrabotany.common.item.ExtraBotanyItems.*;
import static net.minecraftforge.common.Tags.Items;

public class ForgeItemTagProvider extends net.minecraft.data.tags.ItemTagsProvider {
	public ForgeItemTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagsProvider.TagLookup<Block>> blockTagProvider, ExistingFileHelper helper) {
		super(packOutput, lookupProvider, blockTagProvider, LibMisc.MOD_ID, helper);
	}

	@Override
	public String getName() {
		return "ExtraBotany item tags (Forge-specific)";
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		this.generateMaterialTags();
		this.generateToolTags();
		this.generateAccessoryTags();
	}

	private void generateMaterialTags() {
		//Material
		this.tag(forge("ingots/orichalcos")).addTag(ExtraBotanyTags.Items.INGOTS_ORICHALCOS);
		this.tag(forge("ingots/photonium")).addTag(ExtraBotanyTags.Items.INGOTS_PHOTONIUM);
		this.tag(forge("ingots/shadowium")).addTag(ExtraBotanyTags.Items.INGOTS_SHADOWIUM);
		this.tag(forge("ingots/aerialite")).addTag(ExtraBotanyTags.Items.INGOTS_AERIALITE);
		this.tag(Items.INGOTS)
				.addTag(forge("ingots/orichalcos"))
				.addTag(forge("ingots/photonium"))
				.addTag(forge("ingots/shadowium"))
				.addTag(forge("ingots/aerialite"));

		this.tag(forge("nuggets/orichalcos")).addTag(ExtraBotanyTags.Items.NUGGETS_ORICHALCOS);
		this.tag(forge("nuggets/photonium")).addTag(ExtraBotanyTags.Items.NUGGETS_PHOTONIUM);
		this.tag(forge("nuggets/shadowium")).addTag(ExtraBotanyTags.Items.NUGGETS_SHADOWIUM);
		this.tag(forge("nuggets/aerialite")).addTag(ExtraBotanyTags.Items.NUGGETS_AERIALITE);
		this.tag(Items.NUGGETS)
				.addTag(forge("nuggets/orichalcos"))
				.addTag(forge("nuggets/photonium"))
				.addTag(forge("nuggets/shadowium"))
				.addTag(forge("nuggets/aerialite"));

		this.copyToSameName(ForgeBlockTagProvider.ORICHALCOS);
		this.copyToSameName(ForgeBlockTagProvider.PHOTONIUM);
		this.copyToSameName(ForgeBlockTagProvider.SHADOWIUM);
		this.copyToSameName(ForgeBlockTagProvider.AERIALITE);

		this.tag(Items.GEMS_QUARTZ).add(gaiaQuartz, elementiumQuartz);
	}

	private void generateAccessoryTags() {
		tag(accessory("ring")).add(RINGS);
		tag(accessory("necklace")).add(NECKLACES);
		tag(accessory("head")).add(HEAD);
		tag(accessory(("body"))).add(BODY);
		tag(accessory("charm")).add(CHARMS);
		tag(accessory("curio")).add(CURIOS);
		tag(accessory("mount")).add(MOUNTS);
	}

	private void generateToolTags() {
		this.tag(Tags.Items.ARMORS_HELMETS).add(starryIdolHeadgear, pleiadesCombatMaidHeadgear, shadowWarriorHelmet, goblinSlayerHelmet, mikuHelm, shootingGuardianHelm, silentSagesHelm);
		this.tag(Tags.Items.ARMORS_CHESTPLATES).add(starryIdolSuit, pleiadesCombatMaidSuit, sanguinePleiadesCombatMaidSuit, shadowWarriorChestplate, goblinSlayerChestplate, mikuChest, shootingGuardianChest, silentSagesChest);
		this.tag(Tags.Items.ARMORS_LEGGINGS).add(starryIdolSkirt, pleiadesCombatMaidSkirt, shadowWarriorLeggings, goblinSlayerLeggings, mikuLegs, shootingGuardianLegs, silentSagesLegs);
		this.tag(Tags.Items.ARMORS_BOOTS).add(starryIdolBoots, pleiadesCombatMaidBoots, shadowWarriorBoots, goblinSlayerBoots, mikuBoots, shootingGuardianBoots, silentSagesBoots);
		this.tag(Items.TOOLS_SHIELDS).add(SHIELDS);
		this.tag(Items.TOOLS_BOWS).add(BOWS);
	}

	private static TagKey<Item> accessory(String name) {
		return ItemTags.create(ResourceLocation.tryBuild("curios", name));
	}

	private static TagKey<Item> forge(String name) {
		return ItemTags.create(ResourceLocation.tryBuild("forge", name));
	}

	private void copyToSameName(TagKey<Block> source) {
		this.copy(source, ItemTags.create(source.location()));
	}
}
