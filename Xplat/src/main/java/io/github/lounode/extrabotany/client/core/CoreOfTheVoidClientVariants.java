package io.github.lounode.extrabotany.client.core;

import io.github.lounode.extrabotany.api.ExtraBotanyAPI;
import io.github.lounode.extrabotany.common.item.relic.voidcore.variants.Flandre;
import io.github.lounode.extrabotany.common.item.relic.voidcore.variants.Herrscher;
import io.github.lounode.extrabotany.common.item.relic.voidcore.variants.Jim;
import io.github.lounode.extrabotany.common.item.relic.voidcore.variants.Rainbow;
import io.github.lounode.extrabotany.common.item.relic.voidcore.variants.Steampunk;

public final class CoreOfTheVoidClientVariants {
	private CoreOfTheVoidClientVariants() {}

	public static void register() {
		ExtraBotanyAPI.instance().registerCOVVariant(new Herrscher());
		ExtraBotanyAPI.instance().registerCOVVariant(new Flandre());
		ExtraBotanyAPI.instance().registerCOVVariant(new Rainbow());
		ExtraBotanyAPI.instance().registerCOVVariant(new Jim());
		ExtraBotanyAPI.instance().registerCOVVariant(new Steampunk());
	}
}
