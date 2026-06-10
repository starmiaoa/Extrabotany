package io.github.lounode.extrabotany.forge;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import org.apache.commons.lang3.tuple.Pair;

import vazkii.botania.xplat.XplatAbstractions;

import io.github.lounode.extrabotany.common.block.flower.functional.*;
import io.github.lounode.extrabotany.common.block.flower.generating.*;
import io.github.lounode.extrabotany.common.lib.LibMisc;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = LibMisc.MOD_ID)
public class ForgeExtrabotanyConfig {
	private static class Client implements ExtraBotanyConfig.ClientConfigAccess {
		public final ModConfigSpec.BooleanValue otakuMode;

		public Client(ModConfigSpec.Builder builder) {
			builder.push("client");

			otakuMode = builder
					.comment("""
							设为 true 来开启二刺螈模式
							（将会启用一些浓度较高、发癫的文本显示）
							
							Set true to enable Otaku Mode.
							(Enables otaku-style text display)""")
					.define("otakuMode", false);

			builder.pop();
		}

		@Override
		public boolean otakuMode() {
			return otakuMode.get();
		}
	}

	public static final Client CLIENT;
	public static final ModConfigSpec CLIENT_SPEC;
	static {
		final Pair<Client, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Client::new);
		CLIENT_SPEC = specPair.getRight();
		CLIENT = specPair.getLeft();
	}

	private static class Common implements ExtraBotanyConfig.ConfigAccess {
		public final ModConfigSpec.BooleanValue disableGaiaDisArm;
		public final ModConfigSpec.BooleanValue enableTelemetry;
		public final ModConfigSpec.ConfigValue<String> telemetryUUID;
		public final ModConfigSpec.ConfigValue<String> fakePlayerId;
		public final ModConfigSpec.BooleanValue enableCandyBagMobSpawn;
		public final ModConfigSpec.DoubleValue candyBagMobSpawnChance;
		public final ModConfigSpec.BooleanValue enableManaGenerator;
		public final ModConfigSpec.IntValue manaGeneratorMaxEnergy;
		public final ModConfigSpec.IntValue manaGeneratorConvertMana;
		public final ModConfigSpec.IntValue manaGeneratorTransferSpeed;
		public final ModConfigSpec.BooleanValue enableManaLiquefaction;
		public final ModConfigSpec.IntValue manaLiquefactionMaxMana;
		public final ModConfigSpec.IntValue manaLiquefactionStorageDrain;
		public final ModConfigSpec.IntValue manaLiquefactionStorageDrainContainer;
		public final ModConfigSpec.IntValue manaLiquefactionStoragePump;
		public final ModConfigSpec.IntValue manaLiquefactionStoragePumpContainer;
		public final ModConfigSpec.IntValue manaLiquefactionManaReceive;
		public final ModConfigSpec.IntValue manaLiquefactionEnergyLoss;
		public final ModConfigSpec.IntValue manaLiquefactionManaGive;
		public final ModConfigSpec.IntValue manaLiquefactionEnergyGain;
		public final ModConfigSpec.ConfigValue<List<? extends Integer>> woodieniaRange;
		public final ModConfigSpec.IntValue woodieniaCooldown;
		public final ModConfigSpec.IntValue woodieniaMaxMana;
		public final ModConfigSpec.IntValue woodieniaWorkManaCost;
		public final ModConfigSpec.IntValue reikarlilyMaxMana;
		public final ModConfigSpec.IntValue reikarlilyProduceCooldown;
		public final ModConfigSpec.IntValue reikarlilyProduceMana;
		public final ModConfigSpec.IntValue reikarlilySpawnLightningCooldown;
		public final ModConfigSpec.IntValue reikarlilyPassiveGenerateTime;
		public final ModConfigSpec.IntValue reikarlilyPassiveGenerateMana;
		public final ModConfigSpec.IntValue tradeOrchidMaxMana;
		public final ModConfigSpec.IntValue tradeOrchidManaCost;
		public final ModConfigSpec.IntValue tradeOrchidCooldown;
		public final ModConfigSpec.DoubleValue tradeOrchidDiscountPercentage;
		public final ModConfigSpec.IntValue bellflowerMaxMana;
		public final ModConfigSpec.DoubleValue bellflowerGenerateModify;
		public final ModConfigSpec.IntValue annoyingflowerMaxMana;
		public final ModConfigSpec.IntValue annoyingflowerFishingCost;
		public final ModConfigSpec.IntValue annoyingflowerCooldown;
		public final ModConfigSpec.IntValue annoyingflowerFoodBoostMax;
		public final ModConfigSpec.IntValue annoyingflowerFoodBoostTimes;
		public final ModConfigSpec.DoubleValue annoyingflowerFoodBoostCooldownMultiplier;
		public final ModConfigSpec.IntValue stonesiaMaxMana;
		public final ModConfigSpec.IntValue stonesiaCooldown;
		public final ModConfigSpec.IntValue edelweissMaxMana;
		public final ModConfigSpec.IntValue edelweissCooldown;
		public final ModConfigSpec.IntValue resoncundMaxMana;
		public final ModConfigSpec.IntValue resoncundLossPerHeard;
		public final ModConfigSpec.IntValue sunshineLilyMaxMana;
		public final ModConfigSpec.IntValue sunshineLilyProduceMana;
		public final ModConfigSpec.IntValue moonlightLilyMaxMana;
		public final ModConfigSpec.IntValue moonlightLilyProduceMana;
		public final ModConfigSpec.IntValue twinstarMaxMana;
		public final ModConfigSpec.IntValue twinstarMaxTemperature;
		public final ModConfigSpec.IntValue twinstarMinTemperature;
		public final ModConfigSpec.IntValue omnivioletMaxMana;
		public final ModConfigSpec.IntValue tinkleMaxMana;
		public final ModConfigSpec.IntValue tinkleProduceMana;
		public final ModConfigSpec.IntValue bloodEnchantressMaxMana;
		public final ModConfigSpec.IntValue bloodEnchantressProduceMana;
		public final ModConfigSpec.IntValue serenitianRange;
		public final ModConfigSpec.IntValue mirrowtuniaMaxMana;
		public final ModConfigSpec.IntValue mirrowtuniaEffectCost;
		public final ModConfigSpec.IntValue necrofleurMaxMana;
		public final ModConfigSpec.IntValue manalinkTransferSpeed;
		public final ModConfigSpec.IntValue enchanterTransformCost;
		public final ModConfigSpec.IntValue enchanterConsumeSpeed;
		public final ModConfigSpec.IntValue enchanterTransformRange;
		public final ModConfigSpec.IntValue stardustLotusMaxMana;
		public final ModConfigSpec.IntValue stardustLotusBaseCost;
		public final ModConfigSpec.IntValue stardustLotusCostPerBlock;
		public final ModConfigSpec.IntValue stardustLotusConsumeSpeed;
		public final ModConfigSpec.ConfigValue<List<? extends String>> gaiaSpawnUnCheckList;

		public Common(ModConfigSpec.Builder builder) {

			builder.push("server");

			builder.push("telemetry");
			enableTelemetry = builder
					.comment("""
							我们使用遥测数据来提供更好的游玩体验。
							以下数据在您的游玩过程中将被收集：
								- 盖亚三通过率
								- 其他...
							
							您可以在 https://github.com/Lounode/Extrabotany 上找到更多
							如果您不想被收集这些数据，在下方将配置项改为 false
							
							We use telemetry data to provide a better gameplay experience.
							The following data will be collected during your play session:
								- Gaia III completion rate
								- etc...
							
							Find more on: https://github.com/Lounode/Extrabotany
							If you prefer not to participate, set the option below to false""")
					.define("enableTelemetry", true);

			telemetryUUID = builder
					.comment("""
							遥测数据UUID
							The UUID of the telemetry data""")
					.define("telemetryUUID", UUID.randomUUID().toString());
			builder.pop();//End telemetry

			builder.push("gaia");
			disableGaiaDisArm = builder
					.comment("""
							设为 true 来禁用盖亚的缴械技能
							Set true to disable Gaia's disarm""")
					.define("disableGaiaDisarm", false);
			gaiaSpawnUnCheckList = builder
					.comment("""
							盖亚三生成时不检查的ModID或者物品
							示例：minecraft, sophisticatedbackpacks:backpack
							Items or ModIDs that gaia ignore to check when spawn
							e.g. minecraft, sophisticatedbackpacks:backpack""")
					.defineList("gaiaSpawnUnCheckList", List.of("minecraft", "botania", "extrabotany"), o -> o instanceof String);
			builder.pop();//End gaia

			builder.push("fakePlayer");
			fakePlayerId = builder
					.comment("""
							假玩家ID（用于权限配置）
							Fake Player ID (for permission configuration)""")
					.define("fakePlayerId", "[Extrabotany]");
			builder.pop();//End fakePlayer

			builder.push("rewardBag");
			enableCandyBagMobSpawn = builder
					.comment("""
							设为 true 时，僵尸和骷髅生成时有概率在副手携带万圣糖惊魂夜。
							Set true to allow zombies and skeletons to spawn with Halloween Candy Night in their offhand.""")
					.define("enableCandyBagMobSpawn", true);
			candyBagMobSpawnChance = builder
					.comment("""
							僵尸和骷髅生成时副手携带万圣糖惊魂夜的概率。旧版默认值为 2%。
							Chance for zombies and skeletons to spawn with Halloween Candy Night in their offhand. The legacy default is 2%.""")
					.defineInRange("candyBagMobSpawnChance", 0.02D, 0.0D, 1.0D);
			builder.pop();//End rewardBag

			builder.push("machine");
			builder.push("manaGenerator");
			enableManaGenerator = builder
					.comment("""
							设为 true 启用 FE 转换器。
							Set true to enable the Flux Manafield.""")
					.define("enableManaGenerator", true);
			manaGeneratorMaxEnergy = builder
					.comment("""
							FE 转换器最大 FE 存储量。旧版默认值为 40000。
							Maximum FE stored by the Flux Manafield. Legacy default: 40000.""")
					.defineInRange("maxEnergy", 40_000, 1, Integer.MAX_VALUE);
			manaGeneratorConvertMana = builder
					.comment("""
							每 1000 FE 转换为多少魔力。旧版默认值为 99。
							Mana generated per 1000 FE. Legacy default: 99.""")
					.defineInRange("manaPer1000FE", 99, 1, Integer.MAX_VALUE);
			manaGeneratorTransferSpeed = builder
					.comment("""
							每 tick 输出到相邻魔力发射器的最大魔力量。旧版默认值为 200。
							Maximum mana sent to adjacent spreaders each tick. Legacy default: 200.""")
					.defineInRange("transferSpeed", 200, 1, Integer.MAX_VALUE);
			builder.pop();//End manaGenerator
			builder.push("manaLiquefaction");
			enableManaLiquefaction = builder
					.comment("""
							设为 true 启用液态魔力转化器。
							Set true to enable the Mana Liquefaction Device.""")
					.define("enableManaLiquefaction", true);
			manaLiquefactionMaxMana = builder
					.comment("""
							液态魔力转化器最大魔力存储。旧版默认值为 1000000。
							Maximum mana stored by the Mana Liquefaction Device. Legacy default: 1000000.""")
					.defineInRange("maxMana", 1_000_000, 1, Integer.MAX_VALUE);
			manaLiquefactionStorageDrain = builder
					.comment("""
							无红石时每次从液态魔力容器抽取成功增加的内部液体能量。旧版默认值为 1。
							Fluid energy gained per successful drain without redstone. Legacy default: 1.""")
					.defineInRange("storageDrain", 1, 1, Integer.MAX_VALUE);
			manaLiquefactionStorageDrainContainer = builder
					.comment("""
							无红石时每次从相邻容器抽取的液态魔力量。旧版默认值为 1。
							Fluided Mana drained from adjacent containers each attempt without redstone. Legacy default: 1.""")
					.defineInRange("storageDrainContainer", 1, 1, Integer.MAX_VALUE);
			manaLiquefactionStoragePump = builder
					.comment("""
							有红石时每次向容器泵出液态魔力消耗的内部液体能量。旧版默认值为 25。
							Fluid energy consumed per pump with redstone. Legacy default: 25.""")
					.defineInRange("storagePump", 25, 1, Integer.MAX_VALUE);
			manaLiquefactionStoragePumpContainer = builder
					.comment("""
							有红石时每次向相邻容器填充的液态魔力量。旧版默认值为 25。
							Fluided Mana filled into adjacent containers each attempt with redstone. Legacy default: 25.""")
					.defineInRange("storagePumpContainer", 25, 1, Integer.MAX_VALUE);
			manaLiquefactionManaReceive = builder
					.comment("""
							无红石时每次由内部液体能量转化出的魔力。旧版默认值为 2000。
							Mana produced from internal fluid energy without redstone. Legacy default: 2000.""")
					.defineInRange("manaReceive", 2_000, 1, Integer.MAX_VALUE);
			manaLiquefactionEnergyLoss = builder
					.comment("""
							无红石时每次生成魔力消耗的内部液体能量。旧版默认值为 2。
							Fluid energy consumed per mana generation without redstone. Legacy default: 2.""")
					.defineInRange("energyLoss", 2, 1, Integer.MAX_VALUE);
			manaLiquefactionManaGive = builder
					.comment("""
							有红石时每次转化为内部液体能量消耗的魔力。旧版默认值为 2000。
							Mana consumed per fluid energy generation with redstone. Legacy default: 2000.""")
					.defineInRange("manaGive", 2_000, 1, Integer.MAX_VALUE);
			manaLiquefactionEnergyGain = builder
					.comment("""
							有红石时每次消耗魔力获得的内部液体能量。旧版默认值为 2。
							Fluid energy gained per mana conversion with redstone. Legacy default: 2.""")
					.defineInRange("energyGain", 2, 1, Integer.MAX_VALUE);
			builder.pop();//End manaLiquefaction
			builder.pop();//End machine

			builder.push("flower");
			builder.comment("""
					商友兰
					Trade Orchid""");
			builder.push("tradeOrchid");
			tradeOrchidMaxMana = builder
					.comment("""
							最大魔力值
							Maximum mana""")
					.defineInRange("maxMana", TradeOrchidBlockEntity.MAX_MANA, 0, Integer.MAX_VALUE);
			tradeOrchidManaCost = builder
					.comment("""
							每只村民消耗的魔力量
							Mana cost per villager""")
					.defineInRange("manaCost", TradeOrchidBlockEntity.MANA_PER_USE, 0, Integer.MAX_VALUE);
			tradeOrchidCooldown = builder
					.comment("""
							冷却时间(ticks)
							Cooldown time in ticks""")
					.defineInRange("cooldown", TradeOrchidBlockEntity.COOLDOWN, 0, Integer.MAX_VALUE);
			tradeOrchidDiscountPercentage = builder
					.comment("""
							折扣百分比(仅支持精确到两位小数)
							(例如：0.85 = 八五折)
							Discount percentage (max precision: 0.01)
							(e.g., 0.85 = 15% off)""")
					.defineInRange("discountPercentage", TradeOrchidBlockEntity.DISCOUNT_RATE, 0, 1.0D);
			builder.pop();//End tradeOrchid
			builder.comment("""
					伐木花
					Woodienia""");
			builder.push("woodienia");
			woodieniaMaxMana = builder
					.comment("""
							最大魔力
							Maximum Mana""")
					.defineInRange("maxMana", 10_000, 0, Integer.MAX_VALUE);
			woodieniaWorkManaCost = builder
					.comment("""
							破坏原木的魔力消耗
							Cost when break Logs""")
					.defineInRange("workManaCost", 200, 0, Integer.MAX_VALUE);
			woodieniaRange = builder
					.comment("""
							以自身为中心的工作范围（±X轴，+Y轴，±Z轴）
							Working range centered on self (±X axis, +Y axis, ±Z axis)""")
					.defineList("range", List.of(8, 16, 8), o -> o instanceof Integer i && i > 0 && i < Integer.MAX_VALUE);
			woodieniaCooldown = builder
					.comment("""
							工作间隔
							Cooldown interval""")
					.defineInRange("cooldown", 10, 0, Integer.MAX_VALUE);
			builder.pop();//End woodienia
			builder.comment("""
					雷卡兰
					Reikarlily""");
			builder.push("reikarlily");
			reikarlilyMaxMana = builder
					.comment("""
							最大魔力
							Maximum Mana""")
					.defineInRange("maxMana", ReikarlilyBlockEntity.MAX_MANA, 0, Integer.MAX_VALUE);
			reikarlilyProduceCooldown = builder
					.comment("""
							雷击后再次产出魔力的冷却时间
							Cooldown time for regenerating mana after a lightning strike""")
					.defineInRange("produceCooldown", ReikarlilyBlockEntity.COOLDOWN, 0, Integer.MAX_VALUE);
			reikarlilyProduceMana = builder
					.comment("""
							雷击生成的魔力量
							Mana generated per lightning strike""")
					.defineInRange("produceMana", ReikarlilyBlockEntity.PRODUCE_MANA, 0, Integer.MAX_VALUE);
			reikarlilyPassiveGenerateTime = builder
					.comment("""
							雷击后被动生成魔力的时间
							Passive mana generation duration after lightning strike""")
					.defineInRange("passiveGenerateTime", ReikarlilyBlockEntity.RESIDUAL_HEAT_AFTER_PRODUCE, 0, Integer.MAX_VALUE);
			reikarlilyPassiveGenerateMana = builder
					.comment("""
							雷击后每Tick被动生成的魔力量
							Mana generated per passive tick""")
					.defineInRange("passiveGenerateMana", ReikarlilyBlockEntity.RESIDUAL_HEAT_PRODUCE_MANA, 0, Integer.MAX_VALUE);
			reikarlilySpawnLightningCooldown = builder
					.comment("""
							雨天生成闪电的冷却时间
							Cooldown for spawning lightning when raining""")
					.defineInRange("spawnLightningCooldown", ReikarlilyBlockEntity.SPAWN_LIGHTNING_COOLDOWN, 0, Integer.MAX_VALUE);
			builder.pop();//End reikarlily
			builder.comment("""
					风铃草
					Bellflower""");
			builder.push("bellflower");
			bellflowerMaxMana = builder
					.comment("""
							最大魔力值
							Maximum mana""")
					.defineInRange("maxMana", BellflowerBlockEntity.MAX_MANA, 0, Integer.MAX_VALUE);
			bellflowerGenerateModify = builder
					.comment("""
							魔力生成修正值
							Mana generation modifier""")
					.defineInRange("generateModify", BellflowerBlockEntity.GENERATE_MODIFY, 0, Integer.MAX_VALUE);
			builder.pop(); // End bellflower
			builder.comment("""
					神烦花
					Annoying Flower""");
			builder.push("annoyingflower");
			annoyingflowerMaxMana = builder
					.comment("""
							最大魔力值
							Maximum mana""")
					.defineInRange("maxMana", AnnoyingFlowerBlockEntity.MAX_MANA, 0, Integer.MAX_VALUE);
			annoyingflowerFishingCost = builder
					.comment("""
							钓鱼消耗的魔力量
							Mana cost per fishing""")
					.defineInRange("fishingCost", AnnoyingFlowerBlockEntity.FISHING_COST, 0, Integer.MAX_VALUE);
			annoyingflowerCooldown = builder
					.comment("""
							工作冷却时间(ticks)
							Cooldown time in ticks""")
					.defineInRange("cooldown", AnnoyingFlowerBlockEntity.COOLDOWN_AFTER_WORK, 0, Integer.MAX_VALUE);
			annoyingflowerFoodBoostMax = builder
					.comment("""
							最大食物加成次数
							Maximum food boost""")
					.defineInRange("foodBoostMax", AnnoyingFlowerBlockEntity.FOOD_BOOST_MAX, 0, Integer.MAX_VALUE);
			annoyingflowerFoodBoostTimes = builder
					.comment("""
							每次进食增加的次数
							Boost added per food consumed""")
					.defineInRange("foodBoostPerEat", AnnoyingFlowerBlockEntity.FOOD_BOOST_TIMES, 0, Integer.MAX_VALUE);
			annoyingflowerFoodBoostCooldownMultiplier = builder
					.comment("""
							食物加成时的冷却时间乘数
							Cooldown multiplier when food boosted""")
					.defineInRange("foodBoostCooldownMultiplier", AnnoyingFlowerBlockEntity.FOOD_BOOST_COOLDOWN_MULTIPLIER, 0, 1.0D);
			builder.pop(); // End annoyingflower
			builder.comment("""
					石中姬
					Stonesia""");
			builder.push("stonesia");
			stonesiaMaxMana = builder
					.comment("""
							最大魔力值
							Maximum mana""")
					.defineInRange("maxMana", StonesiaBlockEntity.MAX_MANA, 0, Integer.MAX_VALUE);
			stonesiaCooldown = builder
					.comment("""
							冷却时间(ticks)
							Cooldown time in ticks""")
					.defineInRange("cooldown", StonesiaBlockEntity.COOLDOWN, 0, Integer.MAX_VALUE);
			builder.pop(); // End stonesia

			builder.comment("""
					雪绒花
					Edelweiss""");
			builder.push("edelweiss");
			edelweissMaxMana = builder
					.comment("""
							最大魔力值
							Maximum mana""")
					.defineInRange("maxMana", EdelweissBlockEntity.MAX_MANA, 0, Integer.MAX_VALUE);
			edelweissCooldown = builder
					.comment("""
							冷却时间(ticks)
							Cooldown time in ticks""")
					.defineInRange("cooldown", EdelweissBlockEntity.COOLDOWN, 0, Integer.MAX_VALUE);
			builder.pop(); // End edelweiss
			builder.comment("""
					回音花
					Resoncund""");
			builder.push("resoncund");
			resoncundMaxMana = builder
					.comment("""
							最大魔力值
							Maximum mana""")
					.defineInRange("maxMana", ResoncundBlockEntity.MAX_MANA, 0, Integer.MAX_VALUE);
			resoncundLossPerHeard = builder
					.comment("""
							同种声音重复听到后的魔力生产衰减
							Mana produce decreases per same sound heard""")
					.defineInRange("lossPerHeard", ResoncundBlockEntity.MANA_LOSS_PER_HEARD, 0, Integer.MAX_VALUE);
			builder.pop(); // End resoncund
			builder.comment("""
					日曜百合
					Sunshine Lily""");
			builder.push("sunshineLily");
			sunshineLilyMaxMana = builder
					.comment("""
							最大魔力值
							Maximum mana""")
					.defineInRange("maxMana", SunshineLilyBlockEntity.MAX_MANA, 0, Integer.MAX_VALUE);
			sunshineLilyProduceMana = builder
					.comment("""
							每次生成的魔力量
							Mana generated per produce""")
					.defineInRange("produceMana", SunshineLilyBlockEntity.MANA_PER_GENERATE, 0, Integer.MAX_VALUE);
			builder.pop();//End sunshineLily
			builder.comment("""
					月耀百合
					Moonlight Lily""");
			builder.push("moonlightLily");
			moonlightLilyMaxMana = builder
					.comment("""
							最大魔力值
							Maximum mana""")
					.defineInRange("maxMana", MoonlightLilyBlockEntity.MAX_MANA, 0, Integer.MAX_VALUE);
			moonlightLilyProduceMana = builder
					.comment("""
							每次生成的魔力量
							Mana generated per produce""")
					.defineInRange("produceMana", MoonlightLilyBlockEntity.MANA_PER_GENERATE, 0, Integer.MAX_VALUE);
			builder.pop();//End moonlightLily
			builder.comment("""
					双子兰
					Twinstar""");
			builder.push("twinstar");
			twinstarMaxMana = builder
					.comment("""
							最大魔力值
							Maximum mana""")
					.defineInRange("maxMana", TwinstarBlockEntity.MAX_MANA, 0, Integer.MAX_VALUE);
			twinstarMaxTemperature = builder
					.comment("""
							最大接受流体温度
							Maximum accepted temperature cap for liquid""")
					.defineInRange("maxTemperatureCap", TwinstarBlockEntity.TEMPERATURE_MAX, 0, Integer.MAX_VALUE);
			twinstarMinTemperature = builder
					.comment("""
							最低接受流体温度
							Minimum accepted temperature cap for liquid""")
					.defineInRange("minTemperatureCap", TwinstarBlockEntity.TEMPERATURE_MIN, 0, Integer.MAX_VALUE);
			builder.pop();//End twinstar
			builder.comment("""
					全知瑾
					Omniviolet""");
			builder.push("omniviolet");
			omnivioletMaxMana = builder
					.comment("""
							最大魔力值
							Maximum mana""")
					.defineInRange("maxMana", OmnivioletBlockEntity.MAX_MANA, 0, Integer.MAX_VALUE);
			builder.pop();//End omniviolet
			builder.comment("""
					叮当花
					Tinkle""");
			builder.push("tinkle");
			tinkleMaxMana = builder
					.comment("""
							最大魔力值
							Maximum mana""")
					.defineInRange("maxMana", TinkleBlockEntity.MAX_MANA, 0, Integer.MAX_VALUE);
			tinkleProduceMana = builder
					.comment("""
							每次生成的魔力量
							Mana generated per produce""")
					.defineInRange("produceMana", TinkleBlockEntity.PRODUCE_MANA, 0, Integer.MAX_VALUE);
			builder.pop();//End tinkle
			builder.comment("""
					鲜血妖姬
					Blood Enchantress""");
			builder.push("bloodEnchantress");
			bloodEnchantressMaxMana = builder
					.comment("""
							最大魔力值
							Maximum mana""")
					.defineInRange("maxMana", BloodEnchantressBlockEntity.MAX_MANA, 0, Integer.MAX_VALUE);
			bloodEnchantressProduceMana = builder
					.comment("""
							每次生成的魔力量
							Mana generated per produce""")
					.defineInRange("produceMana", BloodEnchantressBlockEntity.PRODUCE_MANA, 0, Integer.MAX_VALUE);
			builder.pop();//End bloodEnchantress
			builder.comment("""
					永寂龙胆
					Serenitian""");
			builder.push("serenitian");
			serenitianRange = builder
					.comment("""
							作用范围(方块半径)
							Working range in blocks""")
					.defineInRange("range", SerenitianBlockEntity.RANGE, 1, 16);
			builder.pop();//End serenitian
			builder.comment("""
					镜姬
					Mirrowtunia""");
			builder.push("mirrowtunia");
			mirrowtuniaMaxMana = builder
					.comment("""
							最大魔力值
							Maximum mana""")
					.defineInRange("maxMana", MirrowtuniaBlockEntity.MAX_MANA, 0, Integer.MAX_VALUE);
			mirrowtuniaEffectCost = builder
					.comment("""
							施加效果消耗的魔力量
							Mana cost per effect""")
					.defineInRange("effectCost", MirrowtuniaBlockEntity.EFFECT_COST, 0, Integer.MAX_VALUE);
			builder.pop(); // End mirrowtunia

			builder.comment("""
					死之花
					Necrofleur""");
			builder.push("necrofleur");
			necrofleurMaxMana = builder
					.comment("""
							最大魔力值
							Maximum mana""")
					.defineInRange("maxMana", NecrofleurBlockEntity.MAX_MANA, 0, Integer.MAX_VALUE);
			builder.pop(); // End necrofleur

			builder.comment("""
					魔链星
					Manalink""");
			builder.push("manalink");
			manalinkTransferSpeed = builder
					.comment("""
							魔力传输速度
							Mana transfer speed""")
					.defineInRange("transferSpeed", ManalinkBlockEntity.TRANSFER_SPEED, 0, Integer.MAX_VALUE);
			builder.pop(); // End manalink

			builder.comment("""
					蕴魔瑾
					Enchanter""");
			builder.push("enchanter");
			enchanterTransformCost = builder
					.comment("""
							转换草方块为蕴魔土的总消耗
							Total mana cost to transform grass to enchanted soil""")
					.defineInRange("transformCost", EnchanterBlockEntity.TRANSFORM_COST, 0, Integer.MAX_VALUE);
			enchanterConsumeSpeed = builder
					.comment("""
							魔力消耗速度
							Mana consume speed""")
					.defineInRange("consumeSpeed", EnchanterBlockEntity.CONSUME_SPEED, 0, Integer.MAX_VALUE);
			enchanterTransformRange = builder
					.comment("""
							转化草方块的扫描范围。旧版默认值为 4。
							Range to scan for grass blocks. Legacy default: 4.""")
					.defineInRange("transformRange", EnchanterBlockEntity.RANGE, 1, Integer.MAX_VALUE);
			builder.pop(); // End enchanter

			builder.comment("""
					星尘莲
					Stardust Lotus""");
			builder.push("stardustLotus");
			stardustLotusMaxMana = builder
					.comment("""
							最大魔力值。旧版默认值为 100000。
							Maximum mana. Legacy default: 100000.""")
					.defineInRange("maxMana", StardustLotusBlockEntity.MAX_MANA, 0, Integer.MAX_VALUE);
			stardustLotusBaseCost = builder
					.comment("""
							传送基础魔力消耗。旧版默认值为 20000。
							Base mana cost for teleportation. Legacy default: 20000.""")
					.defineInRange("baseCost", StardustLotusBlockEntity.BASE_COST, 0, Integer.MAX_VALUE);
			stardustLotusCostPerBlock = builder
					.comment("""
							每格距离追加的魔力消耗。旧版默认值为 150。
							Additional mana cost per block of distance. Legacy default: 150.""")
					.defineInRange("costPerBlock", StardustLotusBlockEntity.COST_PER_BLOCK, 0, Integer.MAX_VALUE);
			stardustLotusConsumeSpeed = builder
					.comment("""
							传送充能时每 tick 消耗魔力。旧版默认值为 800。
							Mana consumed per tick while charging teleportation. Legacy default: 800.""")
					.defineInRange("consumeSpeed", StardustLotusBlockEntity.CONSUME_SPEED, 0, Integer.MAX_VALUE);
			builder.pop(); // End stardustLotus

			builder.pop();//End flower
			builder.pop();//End server
		}

		@Override
		public boolean disableGaiaDisArm() {
			return disableGaiaDisArm.get();
		}

		@Override
		public boolean enableTelemetry() {
			return enableTelemetry.get();
		}

		@Override
		public String fakePlayerId() {
			return fakePlayerId.get();
		}

		@Override
		public String telemetryUUID() {
			return telemetryUUID.get();
		}

		@Override
		public boolean enableCandyBagMobSpawn() {
			return enableCandyBagMobSpawn.get();
		}

		@Override
		public double candyBagMobSpawnChance() {
			return candyBagMobSpawnChance.get();
		}

		@Override
		public boolean enableManaGenerator() {
			return enableManaGenerator.get();
		}

		@Override
		public int manaGeneratorMaxEnergy() {
			return manaGeneratorMaxEnergy.get();
		}

		@Override
		public int manaGeneratorConvertMana() {
			return manaGeneratorConvertMana.get();
		}

		@Override
		public int manaGeneratorTransferSpeed() {
			return manaGeneratorTransferSpeed.get();
		}

		@Override
		public boolean enableManaLiquefaction() {
			return enableManaLiquefaction.get();
		}

		@Override
		public int manaLiquefactionMaxMana() {
			return manaLiquefactionMaxMana.get();
		}

		@Override
		public int manaLiquefactionStorageDrain() {
			return manaLiquefactionStorageDrain.get();
		}

		@Override
		public int manaLiquefactionStorageDrainContainer() {
			return manaLiquefactionStorageDrainContainer.get();
		}

		@Override
		public int manaLiquefactionStoragePump() {
			return manaLiquefactionStoragePump.get();
		}

		@Override
		public int manaLiquefactionStoragePumpContainer() {
			return manaLiquefactionStoragePumpContainer.get();
		}

		@Override
		public int manaLiquefactionManaReceive() {
			return manaLiquefactionManaReceive.get();
		}

		@Override
		public int manaLiquefactionEnergyLoss() {
			return manaLiquefactionEnergyLoss.get();
		}

		@Override
		public int manaLiquefactionManaGive() {
			return manaLiquefactionManaGive.get();
		}

		@Override
		public int manaLiquefactionEnergyGain() {
			return manaLiquefactionEnergyGain.get();
		}

		@Override
		public int[] woodieniaRange() {
			return woodieniaRange.get().stream()
					.mapToInt(Integer::intValue)
					.toArray();
		}

		@Override
		public int woodieniaCooldown() {
			return woodieniaCooldown.get();
		}

		@Override
		public int woodieniaMaxMana() {
			return woodieniaMaxMana.get();
		}

		@Override
		public int woodieniaWorkManaCost() {
			return woodieniaWorkManaCost.get();
		}

		@Override
		public int reikarlilyMaxMana() {
			return reikarlilyMaxMana.get();
		}

		@Override
		public int reikarlilyProduceCooldown() {
			return reikarlilyProduceCooldown.get();
		}

		@Override
		public int reikarlilyProduceMana() {
			return reikarlilyProduceMana.get();
		}

		@Override
		public int reikarlilySpawnLightningCooldown() {
			return reikarlilySpawnLightningCooldown.get();
		}

		@Override
		public int reikarlilyPassiveGenerateTime() {
			return reikarlilyPassiveGenerateTime.get();
		}

		@Override
		public int reikarlilyPassiveGenerateMana() {
			return reikarlilyPassiveGenerateMana.get();
		}

		@Override
		public int tradeOrchidMaxMana() {
			return tradeOrchidMaxMana.get();
		}

		@Override
		public int tradeOrchidManaCost() {
			return tradeOrchidManaCost.get();
		}

		@Override
		public int tradeOrchidCooldown() {
			return tradeOrchidCooldown.get();
		}

		@Override
		public double tradeOrchidDiscountPercentage() {
			return tradeOrchidDiscountPercentage.get();
		}

		@Override
		public int bellflowerMaxMana() {
			return bellflowerMaxMana.get();
		}

		@Override
		public double bellflowerGenerateModify() {
			return bellflowerGenerateModify.get();
		}

		@Override
		public int annoyingflowerMaxMana() {
			return annoyingflowerMaxMana.get();
		}

		@Override
		public int annoyingflowerFishingCost() {
			return annoyingflowerFishingCost.get();
		}

		@Override
		public int annoyingflowerCooldown() {
			return annoyingflowerCooldown.get();
		}

		@Override
		public int annoyingflowerFoodBoostMax() {
			return annoyingflowerFoodBoostMax.get();
		}

		@Override
		public int annoyingflowerFoodBoostTimes() {
			return annoyingflowerFoodBoostTimes.get();
		}

		@Override
		public double annoyingflowerFoodBoostCooldownMultiplier() {
			return annoyingflowerFoodBoostCooldownMultiplier.get();
		}

		@Override
		public int stonesiaMaxMana() {
			return stonesiaMaxMana.get();
		}

		@Override
		public int stonesiaCooldown() {
			return stonesiaCooldown.get();
		}

		@Override
		public int edelweissMaxMana() {
			return edelweissMaxMana.get();
		}

		@Override
		public int edelweissCooldown() {
			return edelweissCooldown.get();
		}

		@Override
		public int resoncundMaxMana() {
			return resoncundMaxMana.get();
		}

		@Override
		public int resoncundLossPerHeard() {
			return resoncundLossPerHeard.get();
		}

		@Override
		public int sunshineLilyMaxMana() {
			return sunshineLilyMaxMana.get();
		}

		@Override
		public int sunshineLilyProduceMana() {
			return sunshineLilyProduceMana.get();
		}

		@Override
		public int moonlightLilyMaxMana() {
			return moonlightLilyMaxMana.get();
		}

		@Override
		public int moonlightLilyProduceMana() {
			return moonlightLilyProduceMana.get();
		}

		@Override
		public int twinstarMaxMana() {
			return twinstarMaxMana.get();
		}

		@Override
		public int twinstarMaxTemperature() {
			return twinstarMaxTemperature.get();
		}

		@Override
		public int twinstarMinTemperature() {
			return twinstarMinTemperature.get();
		}

		@Override
		public int omnivioletMaxMana() {
			return omnivioletMaxMana.get();
		}

		@Override
		public int tinkleMaxMana() {
			return tinkleMaxMana.get();
		}

		@Override
		public int tinkleProduceMana() {
			return tinkleProduceMana.get();
		}

		@Override
		public int bloodEnchantressMaxMana() {
			return bloodEnchantressMaxMana.get();
		}

		@Override
		public int bloodEnchantressProduceMana() {
			return bloodEnchantressProduceMana.get();
		}

		@Override
		public int serenitianRange() {
			return serenitianRange.get();
		}

		@Override
		public int mirrowtuniaMaxMana() {
			return mirrowtuniaMaxMana.get();
		}

		@Override
		public int mirrowtuniaEffectCost() {
			return mirrowtuniaEffectCost.get();
		}

		@Override
		public int necrofleurMaxMana() {
			return necrofleurMaxMana.get();
		}

		@Override
		public int manalinkTransferSpeed() {
			return manalinkTransferSpeed.get();
		}

		@Override
		public int enchanterTransformCost() {
			return enchanterTransformCost.get();
		}

		@Override
		public int enchanterConsumeSpeed() {
			return enchanterConsumeSpeed.get();
		}

		@Override
		public int enchanterTransformRange() {
			return enchanterTransformRange.get();
		}

		@Override
		public int stardustLotusMaxMana() {
			return stardustLotusMaxMana.get();
		}

		@Override
		public int stardustLotusBaseCost() {
			return stardustLotusBaseCost.get();
		}

		@Override
		public int stardustLotusCostPerBlock() {
			return stardustLotusCostPerBlock.get();
		}

		@Override
		public int stardustLotusConsumeSpeed() {
			return stardustLotusConsumeSpeed.get();
		}

		@Override
		@SuppressWarnings("unchecked")
		public List<String> gaiaSpawnUnCheckList() {
			return (List<String>) gaiaSpawnUnCheckList.get();
		}
	}

	private static final Common COMMON;
	private static final ModConfigSpec COMMON_SPEC;
	static {
		final Pair<Common, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Common::new);
		COMMON_SPEC = specPair.getRight();
		COMMON = specPair.getLeft();
	}

	public static void setup(ModContainer modContainer) {
		modContainer.registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
		ExtraBotanyConfig.setCommon(COMMON);

		if (XplatAbstractions.INSTANCE.isPhysicalClient()) {
			modContainer.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
			ExtraBotanyConfig.setClient(CLIENT);
		}
	}

	@SubscribeEvent
	public static void onConfigLoad(ModConfigEvent.Loading evt) {
		var config = evt.getConfig();
		if (config.getType() == ModConfig.Type.COMMON && config.getModId().equals(LibMisc.MOD_ID)) {
			ExtraBotanyConfig.resetPatchouliFlags();
		}
	}

	@SubscribeEvent
	public static void onConfigLoad(ModConfigEvent.Reloading evt) {
		var config = evt.getConfig();
		if (config.getType() == ModConfig.Type.COMMON && config.getModId().equals(LibMisc.MOD_ID)) {
			ExtraBotanyConfig.resetPatchouliFlags();
		}
	}
}
