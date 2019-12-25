package com.ruinscraft.powder.model.particle;

public enum ParticleName {

	// default enums associated with each character used in Dust and ParticleMatrix

	A("HEART"), // heart
	B("NOTE"), // note
	C("BARRIER"), // barrier block
	D("BLOCK_CRACK"), // purple empty
	E("CLOUD"), // smoke boom
	F("CRIT"), // critical hit
	G("CRIT_MAGIC"), // thingy blue dark
	H("DAMAGE_INDICATOR"), // blak hearts
	I("DRAGON_BREATH"), // purple immediate goes out
	J("DRIP_LAVA"), // tiny sits there little lava thing
	K("DRIP_WATER"), // tiny too but little water thing
	L("ENCHANTMENT_TABLE"), // enchant letters come to you
	M("END_ROD"), // snow ball kinda small flies away
	N("EXPLOSION"), //
	O("EXPLOSION"), //
	P("EXPLOSION"), // explode
	Q("FALLING_DUST"), // black goes into ur head
	R("FIREWORKS_SPARK"), // spark
	S("FLAME"), // everywhere flame
	T("FOOTSTEP"), // a little line
	U("ITEM_CRACK"), // purple black bursts
	V("ITEM_TAKE"), //
	W("LAVA"), // boom lava
	X("MOB_APPEARANCE"), // AAAAAAA
	Y("PORTAL"), // small purple comes to you
	Z("REDSTONE"), // tiny color thing that varies a llot
	a("SLIME"), // slime
	b("SMOKE_LARGE"), // black smoke
	c("SMOKE_NORMAL"), // black smoke goes away smaller
	d("SNOW_SHOVEL"), // yeah goes away
	e("SNOWBALL"), // snowball
	f("SPELL"), // white smoke
	g("SPELL_INSTANT"), // white smoke sparkle
	h("SPELL_MOB"), // cool
	i("SPELL_MOB_AMBIENT"), // cool but fading
	j("SPELL_WITCH"), // purple slow
	k("SPIT"), // spark too but less but goes away faster
	l("SUSPENDED"), //
	m("SUSPENDED_DEPTH"), // tiny gray smoke
	n("SWEEP_ATTACK"), //
	o("TOTEM"), // green dots flying away
	p("TOWN_AURA"), // tiny gray smoke
	q("VILLAGER_ANGRY"), // grr
	r("VILLAGER_HAPPY"), // sparkle
	s("WATER_BUBBLE"), // tiny splash
	t("WATER_DROP"), // again
	u("WATER_SPLASH"), // boom away
	v("WATER_WAKE"), // tiny
	x(""),
	y(""),
	z("");

	private String name;

	ParticleName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}