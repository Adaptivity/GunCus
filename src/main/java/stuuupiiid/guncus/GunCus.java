package stuuupiiid.guncus;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;
import org.lwjgl.input.Keyboard;

@Mod(modid = "GunCus", name = "Gun Customization", version = "1.7.10 BETA v2")
@NetworkMod(channels = { "guncus" }, clientSideRequired = true, serverSideRequired = false, packetHandler = GunCusPacketHandler.class)
public class GunCus {

	@SidedProxy(clientSide = "assets.guncus.GunCusClientProxy", serverSide = "assets.guncus.GunCusCommonProxy")
	public static GunCusCommonProxy commonProxy = new GunCusCommonProxy();
	public static Configuration config;
	public static boolean blockDamage;
	public static int shootTime = 0;
	public static int switchTime = 0;
	public static double accuracy = 100.0D;
	public static int accuracyReset = 5;
	public static float zoomLevel = 1.0F;
	public static float maxX;
	public static float maxY;
	public static boolean scopingX;
	public static boolean scopingY;
	public static int counter = 0;
	public static boolean startedBreathing;
	public static boolean breathing = false;
	public static int breathCounter = 0;
	public static int reloading = 0;
	public static int hitmarker;
	public static String cameraZoom = "Y";
	public static int actualItemID = 0;
	public static int actual = 0;

	public int[] guns = new int[Item.itemsList.length];
	public int[] gunDelays = new int[Item.itemsList.length];
	public int[] gunShoots = new int[Item.itemsList.length];
	public int[] gunMags = new int[Item.itemsList.length];
	public int[] gunBullets = new int[Item.itemsList.length];
	public int[] gunRecoils = new int[Item.itemsList.length];

	public static int check = 300;

	private List<String> loadedGuns = new ArrayList();
	private List<String> loadedBullets = new ArrayList();
	public static File path;

	@Mod.Instance("GunCus")
	public static GunCus instance;
	public GunCusGuiHandler guiHandler = new GunCusGuiHandler();
	public static Item quickKnife;
	public static CreativeTabs gcTab;
	public static Block blockWeapon;
	public static Block blockMag;
	public static Block blockBullet;
	public static Block blockAmmo;
	public static Block blockGun;
	public static Item magFill;
	public static Item part;
	public static GunCusItemScope scope;
	public static GunCusItemMetadata barrel;
	public static GunCusItemMetadata attachment;
	public static Item ammoM320;
	public static int knifeTime = 0;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent preEvent) {
		config = new Configuration(preEvent.getSuggestedConfigurationFile());

		Configuration config1 = this.config;

		config1.load();
		int i1 = config1.get("Gun Customization IDs", "Quick Knife", 13000).getInt(13000);

		quickKnife = new GunCusItemKnife(i1).setUnlocalizedName("quickKnife").setFull3D();
		config1.save();

		gcTab = new GunCusCreativeTab("Gun Customization Modification", quickKnife.itemID);
		quickKnife.setCreativeTab(gcTab);

		config1.load();
		blockDamage = config1.get("Gun Customization", "Enable Block Damage", true).getBoolean(true);

		int b1 = config1.get("Gun Customization IDs", "Weapon Box", 500).getInt(500);
		int b2 = config1.get("Gun Customization IDs", "Mag Box", 501).getInt(501);
		int b3 = config1.get("Gun Customization IDs", "Bullet Box", 502).getInt(502);
		int b4 = config1.get("Gun Customization IDs", "Ammo Box", 503).getInt(503);
		int b5 = config1.get("Gun Customization IDs", "Gun Box", 504).getInt(504);

		int i2 = config1.get("Gun Customization IDs", "Manual Mag Filler", 13001).getInt(13001);
		int i3 = config1.get("Gun Customization IDs", "Box Part", 13002).getInt(13002);

		int s = config1.get("Gun Customization IDs", "Scopes", 13003).getInt(13003);
		int b = config1.get("Gun Customization IDs", "Barrels", 13004).getInt(13004);
		int a = config1.get("Gun Customization IDs", "Attachments", 13005).getInt(13005);

		int ammo1 = config1.get("Gun Customization IDs", "GC40x46mm SR Frag", 13006).getInt(13006);
		config1.save();

		blockWeapon = new GunCusBlockWeapon(b1);
		blockMag = new GunCusBlockMag(b2);
		blockBullet = new GunCusBlockBullet(b3);
		blockAmmo = new GunCusBlockAmmo(b4);
		blockGun = new GunCusBlockGun(b5);

		magFill = new GunCusItemMagFill(i2);
		part = new GunCusItem(i3, "guncus:boxpart", "Box Part", "boxpart");

		scope = new GunCusItemScope(s, "scope", "scope", new GunCusScope[] {
				new GunCusScope("Reflex (RDS) Scope", "reflex", 1.0F, 1),
				new GunCusScope("Kobra (RDS) Scope", "kobra", 1.0F, 2),
				new GunCusScope("Holographic (Holo) Scope", "holographic", 1.0F, 3),
				new GunCusScope("PKA-S (Holo) Scope", "pka-s", 1.0F, 4),
				new GunCusScope("M145 (3.4x) Scope", "m145", 3.4F, 5),
				new GunCusScope("PK-A (3.4x) Scope", "pk-a", 3.4F, 6),
				new GunCusScope("ACOG (4x) Scope", "acog", 4.0F, 7),
				new GunCusScope("PSO-1 (4x) Scope", "pso-1", 4.0F, 8),
				new GunCusScope("Rifle (6x) Scope", "rifle", 6.0F, 9),
				new GunCusScope("PKS-07 (7x) Scope", "pks-07", 7.0F, 10),
				new GunCusScope("Rifle (8x) Scope", "rifle", 8.0F, 11),
				new GunCusScope("Ballistic (12x) Scope", "ballistic", 4.0F, 12),
				new GunCusScope("Ballistic (20x) Scope", "ballistic", 20.0F, 13) });
		barrel = new GunCusItemMetadata(b, "barrel", "barrel", new GunCusCustomizationPart[] {
				new GunCusCustomizationPart("Silencer", "-sln", 1),
				new GunCusCustomizationPart("Heavy Barrel", "-hbl", 2),
				new GunCusCustomizationPart("Rifled Barrel", "-rbl", 3),
				new GunCusCustomizationPart("Polygonal Barrel", "-pbl", 4) });
		attachment = new GunCusItemAttachment(a, "attachment", "attachment", new GunCusCustomizationPart[] {
				new GunCusCustomizationPart("Straight Pull Bolt", "-spb", 1),
				new GunCusCustomizationPart("Bipod", "-bpd", 2), new GunCusCustomizationPart("Foregrip", "-grp", 3),
				new GunCusCustomizationPart("M320", "-320", 4),
				new GunCusCustomizationPart("Strong Spiral Spring", "-sss", 5),
				new GunCusCustomizationPart("Improved Grip", "-img", 6),
				new GunCusCustomizationPart("Laser Pointer", "-ptr", 7) });

		ammoM320 = new GunCusItem(ammo1, "guncus:ammoM320", "GC 40x46mm SR Frag", "ammoM320").setMaxStackSize(8);

		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			registerTickHandlers();
		}

		path = new File(preEvent.getModConfigurationDirectory().getParentFile().getAbsolutePath() + "/GunCus");

		if (!path.exists()) {
			path.mkdirs();
			log("Created the GunCus directory!");
			log("You should install some gun packs now!");
		}

		loadGunPacks(path);
	}

	@SideOnly(Side.CLIENT)
	private boolean registerTickHandlers() {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			TickRegistry.registerScheduledTickHandler(new GunCusTickHandlerClient(), Side.CLIENT);
			TickRegistry.registerScheduledTickHandler(new GunCusTickHandlerRender(), Side.CLIENT);
		}
		return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		commonProxy.render();
		commonProxy.sound();
		instance = this;
		LanguageRegistry.addName(quickKnife, "Quick Knife");
		EntityRegistry.registerModEntity(GunCusEntityBullet.class, "guncusbullet", 200, this, 500, 1, true);

		EntityRegistry.registerModEntity(GunCusEntityAT.class, "guncusat", 201, this, 500, 1, true);
		NetworkRegistry.instance().registerGuiHandler(this, this.guiHandler);

		GameRegistry.registerBlock(blockGun, blockGun.getUnlocalizedName());
		GameRegistry.registerBlock(blockAmmo, blockAmmo.getUnlocalizedName());
		GameRegistry.registerBlock(blockMag, blockMag.getUnlocalizedName());
		GameRegistry.registerBlock(blockBullet, blockBullet.getUnlocalizedName());
		GameRegistry.registerBlock(blockWeapon, blockWeapon.getUnlocalizedName());

		GameRegistry.addShapedRecipe(
				new ItemStack(part),
				new Object[] { "ABA", "BCB", "ABA", Character.valueOf('A'), new ItemStack(Item.ingotIron),
						Character.valueOf('B'), new ItemStack(Item.redstone), Character.valueOf('C'),
						new ItemStack(Item.ingotGold) });
		GameRegistry.addShapedRecipe(new ItemStack(magFill), new Object[] { "ABA", "BAB", "ABA",
				Character.valueOf('B'), new ItemStack(Item.ingotIron), Character.valueOf('A'),
				new ItemStack(Item.redstone) });
		GameRegistry.addShapedRecipe(new ItemStack(blockAmmo),
				new Object[] { "BBB", "ABA", "BCB", Character.valueOf('A'), new ItemStack(part),
						Character.valueOf('B'), new ItemStack(Item.ingotIron), Character.valueOf('C'),
						new ItemStack(Block.blockIron) });
		GameRegistry.addShapedRecipe(new ItemStack(blockBullet),
				new Object[] { "BAB", "AAA", "BCB", Character.valueOf('A'), new ItemStack(part),
						Character.valueOf('B'), new ItemStack(Item.ingotIron), Character.valueOf('C'),
						new ItemStack(Block.blockIron) });
		GameRegistry.addShapedRecipe(new ItemStack(blockMag),
				new Object[] { "BAB", "ABA", "BCB", Character.valueOf('A'), new ItemStack(part),
						Character.valueOf('B'), new ItemStack(Item.ingotIron), Character.valueOf('C'),
						new ItemStack(Block.blockIron) });
		GameRegistry.addShapedRecipe(new ItemStack(blockGun),
				new Object[] { "BAB", "AAA", "BAB", Character.valueOf('A'), new ItemStack(part),
						Character.valueOf('B'), new ItemStack(Item.ingotIron) });
		GameRegistry.addShapedRecipe(new ItemStack(blockWeapon),
				new Object[] { "ABA", "ABA", "BCB", Character.valueOf('A'), new ItemStack(part),
						Character.valueOf('B'), new ItemStack(Item.ingotIron), Character.valueOf('C'),
						new ItemStack(Block.blockIron) });

		GameRegistry.addShapedRecipe(
				new ItemStack(scope, 1, 0),
				new Object[] { " IG", "IRI", Character.valueOf('G'), new ItemStack(Block.thinGlass, 1),
						Character.valueOf('I'), new ItemStack(Item.ingotIron, 1), Character.valueOf('R'),
						new ItemStack(Item.redstone, 1) });
		GameRegistry.addShapedRecipe(
				new ItemStack(scope, 1, 1),
				new Object[] { "IG ", "IRI", Character.valueOf('G'), new ItemStack(Block.thinGlass, 1),
						Character.valueOf('I'), new ItemStack(Item.ingotIron, 1), Character.valueOf('R'),
						new ItemStack(Item.redstone, 1) });
		GameRegistry.addShapedRecipe(new ItemStack(scope, 1, 2),
				new Object[] { " I ", "GRG", "I I", Character.valueOf('G'), new ItemStack(Block.thinGlass, 1),
						Character.valueOf('I'), new ItemStack(Item.ingotIron, 1), Character.valueOf('R'),
						new ItemStack(Item.redstone, 1) });
		GameRegistry.addShapedRecipe(new ItemStack(scope, 1, 3),
				new Object[] { "I I", "GRG", " I ", Character.valueOf('G'), new ItemStack(Block.thinGlass, 1),
						Character.valueOf('I'), new ItemStack(Item.ingotIron, 1), Character.valueOf('R'),
						new ItemStack(Item.redstone, 1) });
		GameRegistry.addShapedRecipe(new ItemStack(scope, 1, 4),
				new Object[] { " I ", "GDG", "I I", Character.valueOf('G'), new ItemStack(Block.thinGlass, 1),
						Character.valueOf('I'), new ItemStack(Item.ingotIron, 1), Character.valueOf('D'),
						new ItemStack(Item.diamond, 1) });
		GameRegistry.addShapedRecipe(new ItemStack(scope, 1, 5),
				new Object[] { "I I", "GDG", " I ", Character.valueOf('G'), new ItemStack(Block.thinGlass, 1),
						Character.valueOf('I'), new ItemStack(Item.ingotIron, 1), Character.valueOf('D'),
						new ItemStack(Item.diamond, 1) });
		GameRegistry.addShapedRecipe(new ItemStack(scope, 1, 6),
				new Object[] { "I I", "GDG", "I I", Character.valueOf('G'), new ItemStack(Block.thinGlass, 1),
						Character.valueOf('I'), new ItemStack(Item.ingotIron, 1), Character.valueOf('D'),
						new ItemStack(Item.diamond, 1) });
		GameRegistry.addShapedRecipe(new ItemStack(scope, 1, 7),
				new Object[] { "I I", "GDG", " II", Character.valueOf('G'), new ItemStack(Block.thinGlass, 1),
						Character.valueOf('I'), new ItemStack(Item.ingotIron, 1), Character.valueOf('D'),
						new ItemStack(Item.diamond, 1) });
		GameRegistry.addShapedRecipe(new ItemStack(scope, 1, 8),
				new Object[] { "III", "GDG", "I I", Character.valueOf('G'), new ItemStack(Block.thinGlass, 1),
						Character.valueOf('I'), new ItemStack(Item.ingotIron, 1), Character.valueOf('D'),
						new ItemStack(Item.diamond, 1) });
		GameRegistry.addShapedRecipe(new ItemStack(scope, 1, 9),
				new Object[] { "I I", "D8G", "I I", Character.valueOf('G'), new ItemStack(Block.thinGlass, 1),
						Character.valueOf('I'), new ItemStack(Item.ingotIron, 1), Character.valueOf('D'),
						new ItemStack(Item.diamond, 1), Character.valueOf('8'), new ItemStack(scope, 1, 7) });
		GameRegistry.addShapedRecipe(
				new ItemStack(scope, 1, 10),
				new Object[] { "D9G", " I ", Character.valueOf('G'), new ItemStack(Block.thinGlass, 1),
						Character.valueOf('I'), new ItemStack(Item.ingotIron, 1), Character.valueOf('D'),
						new ItemStack(Item.diamond, 1), Character.valueOf('9'), new ItemStack(scope, 1, 8) });
		GameRegistry.addShapedRecipe(new ItemStack(scope, 1, 11),
				new Object[] { "GIG", "DDD", "III", Character.valueOf('G'), new ItemStack(Block.thinGlass, 1),
						Character.valueOf('I'), new ItemStack(Item.ingotIron, 1), Character.valueOf('D'),
						new ItemStack(Item.diamond, 1) });
		GameRegistry.addShapedRecipe(new ItemStack(scope, 1, 12),
				new Object[] { " I ", "DBG", "I I", Character.valueOf('G'), new ItemStack(Block.thinGlass, 1),
						Character.valueOf('I'), new ItemStack(Item.ingotIron, 1), Character.valueOf('D'),
						new ItemStack(Item.diamond, 1), Character.valueOf('B'), new ItemStack(scope, 1, 11) });

		GameRegistry.addShapedRecipe(
				new ItemStack(ammoM320),
				new Object[] { "GI ", "IGI", " IG", Character.valueOf('I'), new ItemStack(Item.ingotIron, 1),
						Character.valueOf('G'), new ItemStack(Item.gunpowder, 1) });
		GameRegistry.addShapedRecipe(new ItemStack(attachment, 1, 0),
				new Object[] { "I  ", " I ", "I I", Character.valueOf('I'), new ItemStack(Item.ingotIron, 1) });
		GameRegistry.addShapedRecipe(new ItemStack(attachment, 1, 1),
				new Object[] { " I ", "I I", "I I", Character.valueOf('I'), new ItemStack(Item.ingotIron, 1) });
		GameRegistry.addShapedRecipe(new ItemStack(attachment, 1, 2),
				new Object[] { "II ", " I ", " II", Character.valueOf('I'), new ItemStack(Item.ingotIron, 1) });
		GameRegistry.addShapedRecipe(
				new ItemStack(attachment, 1, 3),
				new Object[] { " II", "IRR", "I I", Character.valueOf('I'), new ItemStack(Item.ingotIron, 1),
						Character.valueOf('R'), new ItemStack(Item.redstone, 1) });
		GameRegistry.addShapedRecipe(
				new ItemStack(attachment, 1, 4),
				new Object[] { "I  ", "IGI", "  I", Character.valueOf('I'), new ItemStack(Item.ingotIron, 1),
						Character.valueOf('G'), new ItemStack(Item.ingotGold, 1) });
		GameRegistry.addShapedRecipe(
				new ItemStack(attachment, 1, 5),
				new Object[] { " L ", "LGL", " L ", Character.valueOf('L'), new ItemStack(Item.leather, 1),
						Character.valueOf('G'), new ItemStack(attachment, 1, 2) });
		GameRegistry.addShapedRecipe(
				new ItemStack(attachment, 1, 6),
				new Object[] { "II ", "RRI", "II ", Character.valueOf('I'), new ItemStack(Item.ingotIron, 1),
						Character.valueOf('R'), new ItemStack(Item.redstone, 1) });

		GameRegistry.addShapedRecipe(
				new ItemStack(barrel, 1, 0),
				new Object[] { "SI ", "ISI", " IS", Character.valueOf('I'), new ItemStack(Item.ingotIron, 1),
						Character.valueOf('S'), new ItemStack(Item.slimeBall, 1) });
		GameRegistry.addShapedRecipe(new ItemStack(barrel, 1, 1),
				new Object[] { "II ", "II ", "  I", Character.valueOf('I'), new ItemStack(Item.ingotIron, 1) });
		GameRegistry.addShapedRecipe(
				new ItemStack(barrel, 1, 2),
				new Object[] { "GI ", "IGI", " IG", Character.valueOf('I'), new ItemStack(Item.ingotIron, 1),
						Character.valueOf('G'), new ItemStack(Item.ingotGold, 1) });
		GameRegistry.addShapedRecipe(
				new ItemStack(barrel, 1, 3),
				new Object[] { "II ", "IDI", " II", Character.valueOf('I'), new ItemStack(Item.ingotIron, 1),
						Character.valueOf('D'), new ItemStack(Item.diamond, 1) });
	}

	public static void createExplosionServer(Entity entity, double x, double y, double z, float str) {
		if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
			World world = entity.worldObj;
			world.createExplosion(entity, x, y, z, str, blockDamage);
		}
	}

	public static void removeBlockServer(Entity entity, int x, int y, int z) {
		if ((FMLCommonHandler.instance().getEffectiveSide().isServer()) && (blockDamage)) {
			World world = entity.worldObj;
			world.setBlock(x, y, z, 0);
		}
	}

	@SideOnly(Side.CLIENT)
	public static void doKnife() {
		if ((knifeTime <= 0) && (FMLClientHandler.instance().getClient().thePlayer != null)
				&& (FMLClientHandler.instance().getClient().theWorld != null)) {
			if (((Keyboard.isKeyDown(29)) || (Keyboard.isKeyDown(157))) && (Keyboard.isKeyDown(33))
					&& (FMLClientHandler.instance().getClient().currentScreen == null)) {
				knifeTime += 25;
				shootTime += 24;
				ByteArrayDataOutput bytes = ByteStreams.newDataOutput();
				bytes.writeInt(13);
				bytes.writeInt(0);
				PacketDispatcher.sendPacketToServer(new Packet250CustomPayload("guncus", bytes.toByteArray()));
			}
		}
	}

	private void loadGunPacks(File path1) {
		ClassLoader classloader = MinecraftServer.class.getClassLoader();
		Method method = null;
		try {
			method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
			method.setAccessible(true);
		} catch (Exception e) {
			log("Failed to get the classloader; the textures wont work!");
			e.printStackTrace();
		}

		GunCusInjector injector = new GunCusInjector(classloader, method);
		defaultPack(path1.getAbsolutePath());

		for (File pack : path1.listFiles()) {
			if ((pack.isDirectory()) && (!pack.getName().equals("default"))) {
				bullets(pack.getAbsolutePath(), pack.getName());
				guns(pack.getAbsolutePath(), pack.getName());
				sounds(pack.getAbsolutePath());

				injector.addToClassPath(pack);
			}
		}

		if (this.loadedBullets.size() > 0) {
			System.out.println("");
			log("The GunCus addon found the following bullet files:");

			for (int v1 = 0; v1 < this.loadedBullets.size(); v1++) {
				log(this.loadedBullets.get(v1));
			}
		}

		if (this.loadedGuns.size() > 0) {
			System.out.println("");
			log("The GunCus addon found the following gun files:");

			for (int v1 = 0; v1 < this.loadedGuns.size(); v1++) {
				log(this.loadedGuns.get(v1));
			}
		}

		if ((this.loadedBullets.size() > 0) || (this.loadedGuns.size() > 0)) {
			System.out.println("");
		}
	}

	private void defaultPack(String pathS) {
		File path1 = new File(pathS + "/default");
		if (!path1.exists()) {
			path1.mkdirs();
		}

		File defaultFile = new File(path1.getAbsolutePath() + "/bullets/default.cfg");
		Configuration config1 = new Configuration(defaultFile);
		Property idProp = config1.get("general", "ID", 1010);
		idProp.comment = "The item ID of the bullet";

		Property bulProp = config1.get("general", "BulletID", 1);
		bulProp.comment = "The bullet ID of the bullet";

		Property ironProp = config1.get("general", "Iron", 1);
		ironProp.comment = "How much iron you need to craft this bullet type";

		Property sulProp = config1.get("general", "Gunpowder", 3);
		sulProp.comment = "How much gunpowder you need to craft this bullet type";

		Property stackProp = config1.get("general", "stackSize", 4);
		stackProp.comment = "How much bullets you get at a time by crafting this bullet type";

		Property nameProp = config1.get("general", "Name", "default");
		nameProp.comment = "The name of the bullet";

		Property iconProp = config1.get("general", "Icon", "");
		iconProp.comment = "The texture of this bullet. Leave blanc for default";

		Property splitProp = config1.get("general", "Split", 1);
		splitProp.comment = "How much bullets being shot at a time.";

		Property sprayProp = config1.get("general", "Spray", 100);
		sprayProp.comment = "The maximum accuracy by using this bullet. 100 = 100% accuracy. 30 = shotgun spray.";

		Property onImpactProp = config1.get("general", "Impact", "");
		onImpactProp.comment = "Impact Effects. \"X\" is a modifier. 1:X = Poision (X = time in seconds) | 2:X = Nausea (X = time in seconds) | 3:X = Fire (X = time in seconds) | 4:X = Explosion (X = Strength, Example Strength: 7 = RPG, 4.5 = M320) | 5:X = Explosion without Block Damage (X = Strength, Example Strength: 7 = RPG, 4.5 = M320) | 6:X = Heal (X = heal amount) | 7:X = Blindness (X = time in seconds) . Use semicolons. Example: \"1:3;2:3;4:1.0;5:3.5;7:10\"";

		Property gravityProp = config1.get("general", "GravityModifier", 1.0D);
		gravityProp.comment = "Modifies the applied gravity of a bullet. | Gravity x GravityModifier = Applied Gravity";

		Property damageProp = config1.get("general", "Damage Modifier", 1.0D);
		damageProp.comment = "Gun Damage * Damage Modifier = Applied Damage";
		config1.save();

		File defaultFile2 = new File(path1 + "/guns/default.cfg");
		Configuration config2 = new Configuration(defaultFile2);

		Property idMagProp = config2.get("general", "Mag ID", 1000);
		idMagProp.comment = "The ID of the magazines | Should be 1 lower than the gun's ID";

		Property idProp2 = config2.get("general", "ID", 1001);
		idProp2.comment = "The ID of the gun";

		Property shootTypeProp = config2.get("general", "Shoot", 2);
		shootTypeProp.comment = "0 = Single Shooting | 1 = Burst Shooting | 2 = Auto Shooting";

		Property delayProp = config2.get("general", "Delay", 3);
		delayProp.comment = "The delay between shots of the gun";

		Property magProp = config2.get("general", "Magsize", 1);
		magProp.comment = "The size of the magazines";

		Property magIngotProp = config2.get("general", "Mag Ingots", 1);
		magIngotProp.comment = "The number of iron ingots a mag needs to be crafted";

		Property ingotProp = config2.get("general", "Iron Ingots", 1);
		ingotProp.comment = "The number of iron ingots this gun needs to be crafted";

		Property redProp = config2.get("general", "Redstone", 1);
		redProp.comment = "The number of redstone this gun needs to be crafted";

		Property nameProp2 = config2.get("general", "Name", "default");
		nameProp2.comment = "The name of the gun";

		Property bulletProp = config2.get("general", "Bullets", "1");
		bulletProp.comment = "The bullet IDs of all bullets this gun is using. You may type more than 1 bullet ID if this gun doesnt use magazines!. Use semicolons.";

		Property usingMagProp = config2.get("general", "UsingMags", true);
		usingMagProp.comment = "Does this gun use magazines? False, if the gun is for example a shotgun.";

		Property iconProp2 = config2.get("general", "Texture", "");
		iconProp2.comment = "The texture of the gun. Leave blanc for default";

		Property recProp = config2.get("general", "RecoilModifier", 1.0D);
		recProp.comment = "This modifies the recoil. | Recoil x RecoilModifier = Applied Recoil";

		Property sound_normalP = config2.get("general", "NormalSound", "Sound_DERP2");
		sound_normalP.comment = "The sound being used when shooting the gun. Only .ogg or .wav!!! Leave blanc for default";

		Property sound_silencedP = config2.get("general", "SilencedSound", "");
		sound_silencedP.comment = "The sound being used when shooting the gun that has a silencer. Only .ogg or .wav!!! Leave blanc for default";

		Property sndProp = config2.get("general", "SoundModifier", 1.0D);
		sndProp.comment = "Modifies the sound volume (does not affect the volume of silenced shots). | Default Sound Volume x SoundModifier = Used Sound Volume";

		Property extra1Prop = config2.get("general", "Attachments", "1;3;2;6");
		extra1Prop.comment = "1 = Straight Pull Bolt | 2 = Bipod | 3 = Foregrip | 4 = M320 | 5 = Strong Spiral Spring | 6 = Improved Grip | 7 = Laser Pointer . Type all attachments that should be able to be attatched on the gun. Use semicolons.";

		Property bar1Prop = config2.get("general", "Barrels", "1;2;3");
		bar1Prop.comment = "1 = Silencer | 2 = Heavy Barrel | 3 = Rifled Barrel | 4 = Polygonal Barrel . Type all barrels that should be able to be attatched on the gun. Use semicolons.";

		Property scopesProp = config2.get("general", "Scopes", "1;2;3;4;5;6;7;8;9;10;11;12;13");
		scopesProp.comment = "1 = Reflex | 2 = Kobra | 3 = Holographic | 4 = PKA-S | 5 = M145 | 6 = PK-A | 7 = ACOG | 8 = PSO-1 | 9 = Rifle 6x | 10 = PKS-07 | 11 = Rifle 8x | 12 = Ballistic 12x | 13 = Ballistic 20x . Type all scopes that should be able to be attached on the gun. Use semicolons.";

		Property defaultZoomProp = config2.get("general", "Zoom", 1.0D);
		defaultZoomProp.comment = "The zoom factor without any scope. Default 1.0";

		Property damageProp2 = config2.get("general", "Damage", 6);
		damageProp2.comment = "The damage. 1 = a half heart";
		config2.save();

		File textures = new File(path1.getAbsolutePath() + "/assets/minecraft/textures");
		if (!textures.exists()) {
			textures.mkdirs();
		}
		File items = new File(textures.getAbsolutePath() + "/items");
		if (!items.exists()) {
			items.mkdirs();
		}
		File blocks = new File(textures.getAbsolutePath() + "/blocks");
		if (!blocks.exists()) {
			blocks.mkdirs();
		}
		File sounds = new File(path1.getAbsolutePath() + "/assets/minecraft/sound");
		if (!sounds.exists()) {
			sounds.mkdirs();
		}
	}

	private void bullets(String packPath, String pack) {
		File file = new File(packPath + "/bullets");
		file.mkdirs();
		File[] filesFound = file.listFiles();
		ArrayList files = new ArrayList();

		for (int v1 = 0; v1 < filesFound.length; v1++) {
			if (filesFound[v1].getAbsolutePath().endsWith(".cfg")) {
				files.add(filesFound[v1]);
			}
		}

		for (int v1 = 0; v1 < files.size(); v1++) {
			Configuration config1 = new Configuration((File) files.get(v1));
			config1.load();

			Property idProp = config1.get("general", "ID", 1010);
			idProp.comment = "The item ID of the bullet";

			Property bulProp = config1.get("general", "Bullet ID", 1);
			bulProp.comment = "The bullet ID of the bullet";

			Property ironProp = config1.get("general", "Iron", 1);
			ironProp.comment = "How much iron you need to craft this bullet type";

			Property sulProp = config1.get("general", "Gunpowder", 3);
			sulProp.comment = "How much gunpowder you need to craft this bullet type";

			Property stackProp = config1.get("general", "stackSize", 4);
			stackProp.comment = "How much bullets you get at a time by crafting this bullet type";

			Property nameProp = config1.get("general", "Name", "default");
			nameProp.comment = "The name of the bullet";

			Property iconProp = config1.get("general", "Icon", "");
			iconProp.comment = "The texture of this bullet. Leave blanc for default";

			Property splitProp = config1.get("general", "Split", 1);
			splitProp.comment = "How much bullets being shot at a time.";

			Property sprayProp = config1.get("general", "Spray", 100);
			sprayProp.comment = "The maximum accuracy by using this bullet. 100 = 100% accuracy. 30 = shotgun spray.";

			Property onImpactProp = config1.get("general", "Impact", "");
			onImpactProp.comment = "Impact Effects. \"X\" is a modifier. 1:X = Poision (X = time in seconds) | 2:X = Nausea (X = time in seconds) | 3:X = Fire (X = time in seconds) | 4:X = Explosion (X = Strength, Example Strength: 7 = RPG, 4.5 = M320) | 5:X = Explosion without Block Damage (X = Strength, Example Strength: 7 = RPG, 4.5 = M320) | 6:X = Heal (X = heal amount) | 7:X = Blindness (X = time in seconds) . Use semicolons. Example: \"1:3;2:3;4:1.0;5:3.5;7:10\"";

			Property gravityProp = config1.get("general", "GravityModifier", 1.0D);
			gravityProp.comment = "Modifies the applied gravity of a bullet. | Gravity x GravityModifier = Applied Gravity";

			Property damageProp = config1.get("general", "Damage Modifier", 1.0D);
			damageProp.comment = "Gun Damage * Damage Modifier = Applied Damage";

			float damage = (float) damageProp.getDouble(1.0D);
			int id = idProp.getInt(1010);
			int bul = bulProp.getInt(1);
			int iron = ironProp.getInt(1);
			int sul = sulProp.getInt(3);
			int stack = stackProp.getInt(4);
			String name = nameProp.getString();
			String icon = iconProp.getString();
			String[] effects = onImpactProp.getString().split(";");
			int split = splitProp.getInt(1);
			int spray = sprayProp.getInt(100);
			double gravity = gravityProp.getDouble(1.0D);

			if (!GunCusItemBullet.bulletsList.containsKey(pack)) {
				GunCusItemBullet.bulletsList.put(pack, new ArrayList());
			}

			if ((name != null)
					&& (id > 0)
					&& (bul > 0)
					&& (iron >= 0)
					&& (sul >= 0)
					&& ((iron > 0) || (sul > 0))
					&& (stack > 0)
					&& (((((List) GunCusItemBullet.bulletsList.get(pack)).size() > bul) && (((List) GunCusItemBullet.bulletsList
							.get(pack)).get(bul) == null)) || ((((List) GunCusItemBullet.bulletsList.get(pack)).size() <= bul) && (Item.itemsList[(id + 256)] == null)))) {
				if ((icon.equals("")) || (icon.equals(" "))) {
					icon = "guncus:bullet";
				} else {
					icon = "minecraft:bullets/" + icon;
				}

				GunCusItemBullet bullet = new GunCusItemBullet(id, name, bul, sul, iron, stack, pack, icon, damage)
						.setSplit(split).setGravityModifier(gravity).setSpray(spray);

				for (String effect : effects) {
					try {
						if (effect.contains(":")) {
							String[] effect2 = effect.split(":");

							if (effect2.length == 2) {
								bullet.addEffect(Integer.parseInt(effect2[0]), Float.parseFloat(effect2[1]));
							}
						}
					} catch (Exception e) {
						log("[" + pack + "] Something went wrong while trying to add the effect \"" + effect
								+ "\" to the gun \"" + name + "\"!");
					}
				}

				this.loadedBullets.add(" - " + name + " (ID:" + id + ", Bullet ID:" + bul + ", Pack:" + pack + ")");
			} else if (Item.itemsList[(id + 256)] != null) {
				log("[" + pack + "] Conflict while trying to add \"" + name + "\" bullets: The ID \"" + id
						+ "\" is already occupied!");
			} else {
				log("[" + pack + "] Something went wrong while initializing the bullet \"" + name
						+ "\"! Ignoring this bullet!");
			}

			config1.save();
		}
	}

	private void guns(String packPath, String path1) {
		File file = new File(packPath + "/guns");
		file.mkdirs();
		File[] filesFound = file.listFiles();
		ArrayList files = new ArrayList();

		for (int v1 = 0; v1 < filesFound.length; v1++) {
			if (filesFound[v1].getAbsolutePath().endsWith(".cfg")) {
				files.add(filesFound[v1]);
			}
		}

		for (int v1 = 0; v1 < files.size(); v1++) {
			Configuration config1 = new Configuration((File) files.get(v1));
			config1.load();

			int bullets = -1;

			Property idMagProp = config1.get("general", "Mag ID", 1000);
			idMagProp.comment = "The ID of the magazines | Should be 1 lower than the gun's ID";

			Property idProp = config1.get("general", "ID", 1001);
			idProp.comment = "The ID of the gun";

			Property shootTypeProp = config1.get("general", "Shoot", 2);
			shootTypeProp.comment = "0 = Single Shooting | 1 = Burst Shooting | 2 = Auto Shooting";

			Property delayProp = config1.get("general", "Delay", 3);
			delayProp.comment = "The delay between shots of the gun";

			Property magProp = config1.get("general", "Magsize", 1);
			magProp.comment = "The size of the magazines";

			Property magIngotProp = config1.get("general", "Mag Ingots", 1);
			magIngotProp.comment = "The number of iron ingots a mag needs to be crafted";

			Property ingotProp = config1.get("general", "Iron Ingots", 1);
			ingotProp.comment = "The number of iron ingots this gun needs to be crafted";

			Property redProp = config1.get("general", "Redstone", 1);
			redProp.comment = "The number of redstone this gun needs to be crafted";

			Property nameProp = config1.get("general", "Name", "default");
			nameProp.comment = "The name of the gun";

			Property bulletProp = config1.get("general", "Bullets", "1");
			bulletProp.comment = "The bullet IDs of all bullets this gun is using. You may type more than 1 bullet ID if this gun doesnt use magazines!. Use semicolons.";

			Property usingMagProp = config1.get("general", "UsingMags", true);
			usingMagProp.comment = "Does this gun use magazines? False, if the gun is for example a shotgun.";

			Property iconProp = config1.get("general", "Texture", "");
			iconProp.comment = "The texture of the gun. Leave blanc for default";

			Property recProp = config1.get("general", "RecoilModifier", 1.0D);
			recProp.comment = "This modifies the recoil. | Recoil x RecoilModifier = Applied Recoil";

			Property sound_normalP = config1.get("general", "NormalSound", "Sound_DERP2");
			sound_normalP.comment = "The sound being used when shooting the gun. Only .ogg or .wav!!! Leave blanc for default";

			Property sound_silencedP = config1.get("general", "SilencedSound", "");
			sound_silencedP.comment = "The sound being used when shooting the gun that has a silencer. Only .ogg or .wav!!! Leave blanc for default";

			Property sndProp = config1.get("general", "SoundModifier", 1.0D);
			sndProp.comment = "Modifies the sound volume (does not affect the volume of silenced shots). | Default Sound Volume x SoundModifier = Used Sound Volume";

			Property extra1Prop = config1.get("general", "Attachments", "1;3;2;6");
			extra1Prop.comment = "1 = Straight Pull Bolt | 2 = Bipod | 3 = Foregrip | 4 = M320 | 5 = Strong Spiral Spring | 6 = Improved Grip | 7 = Laser Pointer . Type all attachments that should be able to be attatched on the gun. Use semicolons.";

			Property bar1Prop = config1.get("general", "Barrels", "1;2;3");
			bar1Prop.comment = "1 = Silencer | 2 = Heavy Barrel | 3 = Rifled Barrel | 4 = Polygonal Barrel . Type all barrels that should be able to be attatched on the gun. Use semicolons.";

			Property scopesProp = config1.get("general", "Scopes", "1;2;3;4;5;6;7;8;9;10;11;12;13");
			scopesProp.comment = "1 = Reflex | 2 = Kobra | 3 = Holographic | 4 = PKA-S | 5 = M145 | 6 = PK-A | 7 = ACOG | 8 = PSO-1 | 9 = Rifle 6x | 10 = PKS-07 | 11 = Rifle 8x | 12 = Ballistic 12x | 13 = Ballistic 20x . Type all scopes that should be able to be attached on the gun. Use semicolons.";

			Property defaultZoomProp = config1.get("general", "Zoom", 1.0D);
			defaultZoomProp.comment = "The zoom factor without any scope. Default 1.0";

			Property damageProp = config1.get("general", "Damage", 6);
			damageProp.comment = "The damage. 1 = a half heart";

			int id = idProp.getInt(-1);
			int shootType = shootTypeProp.getInt(2);
			int delay = delayProp.getInt(3);
			int magSize = magProp.getInt(1);
			int magId = idMagProp.getInt(1000);
			String[] bullets2 = bulletProp.getString().split(";");
			int ingotsMag = magIngotProp.getInt(1);
			int ingots = ingotProp.getInt(1);
			int red = redProp.getInt(1);
			String name = nameProp.getString();
			String icon2 = iconProp.getString();
			double recModify = recProp.getDouble(1.0D);
			double sndModify = sndProp.getDouble(1.0D);
			String snormal = sound_normalP.getString();
			String ssln = sound_silencedP.getString();
			String[] attach1 = extra1Prop.getString().split(";");
			String[] bar1 = bar1Prop.getString().split(";");
			String[] scopes1 = scopesProp.getString().split(";");

			float zoom = (float) defaultZoomProp.getDouble(1.1D);
			boolean usingMag = usingMagProp.getBoolean(true);

			int damage = damageProp.getInt(6);

			if (sndModify < 1.E-005D) {
				sndModify = 1.E-005D;
			}

			if (sndModify > 20.0D) {
				sndModify = 20.0D;
			}

			boolean errored = false;
			String pack = new File(packPath).getName();
			int[] bulletsArray;
			if (!usingMag) {
				bullets = -1;
				bulletsArray = new int[bullets2.length];
				magId = -1;
				for (int v2 = 0; v2 < bullets2.length; v2++) {
					try {
						bulletsArray[v2] = Integer.parseInt(bullets2[v2]);
					} catch (Exception e) {
						log("[" + pack + "] Something went wrong while initializing bullets of the gun \"" + name
								+ "\"! Caused by: \"" + bullets2[v2] + "\"!");
						errored = true;
					}
				}
			} else {
				bulletsArray = new int[0];
				try {
					bullets = Integer.parseInt(bullets2[0]);
				} catch (Exception e) {
					log("[" + pack + "] Something went wrong while initializing bullets of the gun \"" + name
							+ "\"! Caused by: \"" + bullets2[0] + "\"!");
					errored = true;
				}
			}

			if ((!errored)
					&& (name != null)
					&& (icon2 != null)
					&& (shootType >= 0)
					&& (shootType < 3)
					&& (delay >= 0)
					&& (magSize >= 1)
					&& (magId != id)
					&& (ingots > 0)
					&& (ingotsMag >= 0)
					&& (red >= 0)
					&& ((ingots > 0) || (red > 0))
					&& (Item.itemsList[(id + 256)] == null)
					&& (((usingMag) && (Item.itemsList[(magId + 256)] == null)) || ((!usingMag)
							&& (bulletsArray.length >= 1) && (GunCusItemBullet.bulletsList.get(pack) != null) && ((!usingMag) || ((usingMag)
							&& (((List) GunCusItemBullet.bulletsList.get(pack)).size() > bullets) && (((List) GunCusItemBullet.bulletsList
							.get(pack)).get(bullets) != null)))))) {
				boolean def = false;
				String icon;
				if ((icon2.equals("")) || (icon2.equals(" "))) {
					log("[" + pack + "] The texture of the gun \"" + name + "\" is missing!");
					icon = "guncus:gun_default/";
					def = true;
				} else {
					icon = "minecraft:gun_" + icon2 + "/";
				}
				try {
					int[] attach;
					if ((attach1.length > 0) && (!attach1[0].replace(" ", "").equals(""))) {
						attach = new int[attach1.length];
						for (int v2 = 0; v2 < attach1.length; v2++) {
							attach[v2] = Integer.parseInt(attach1[v2]);
						}
					} else {
						attach = new int[0];
					}
					int[] bar;
					if ((bar1.length > 0) && (!bar1[0].replace(" ", "").equals(""))) {
						bar = new int[bar1.length];
						for (int v2 = 0; v2 < bar1.length; v2++) {
							bar[v2] = Integer.parseInt(bar1[v2]);
						}
					} else {
						bar = new int[0];
					}
					int[] scopes;
					if ((scopes1.length > 0) && (!scopes1[0].replace(" ", "").equals(""))) {
						scopes = new int[scopes1.length];
						for (int v2 = 0; v2 < scopes1.length; v2++) {
							scopes[v2] = Integer.parseInt(scopes1[v2]);
						}
					} else {
						scopes = new int[0];
					}

					GunCusItemGun gun = new GunCusItemGun(id, damage, shootType, delay, name, icon, magSize, magId,
							bullets, ingotsMag, ingots, red, pack, false, attach, bar, scopes, !usingMag, bulletsArray)
							.setRecoilModifier(recModify).setSoundModifier(sndModify).defaultTexture(def).setZoom(zoom);

					if ((!snormal.isEmpty()) && (!snormal.equals(" "))) {
						gun.setNormalSound("minecraft:" + snormal);
					}

					if ((!ssln.isEmpty()) && (!ssln.equals(" "))) {
						gun.setSLNSound("minecraft:" + ssln);
					}
				} catch (Exception e) {
					log("[" + pack + "] Error while trying to add the gun \"" + name
							+ "\": ! Pls check the attachments, barrels and scopes of it!");
				}

				this.guns[id] = 1;
				this.gunDelays[id] = delay;
				this.gunShoots[id] = shootType;
				this.gunMags[id] = magId;
				this.gunBullets[id] = bullets;
				this.gunRecoils[id] = MathHelper.floor_double(recModify);

				this.loadedGuns.add(" - " + name + " (ID:" + id + ", Pack:" + pack + ")");
			} else if ((id + 256 >= Item.itemsList.length) || (Item.itemsList[(id + 256)] != null)
					|| (Item.itemsList[(magId + 256)] != null) || (!GunCusItemBullet.bulletsList.containsKey(pack))
					|| (((List) GunCusItemBullet.bulletsList.get(pack)).size() <= bullets)
					|| (((List) GunCusItemBullet.bulletsList.get(pack)).get(bullets) == null)) {
				if (id + 256 >= Item.itemsList.length) {
					log("[" + pack + "] Conflict while trying to add the gun \"" + name + "\": The ID \"" + id
							+ "\" is too high!");
				}
				if (Item.itemsList[(id + 256)] != null) {
					log("[" + pack + "] Conflict while trying to add the gun \"" + name + "\": The ID \"" + id
							+ "\" is already occupied!");
				}
				if (Item.itemsList[(magId + 256)] != null) {
					log("[" + pack + "] Conflict while trying to add the magazine of the gun \"" + name
							+ "\": The ID \"" + magId + "\" is already occupied!");
				}
				if ((bullets >= 0)
						&& ((!GunCusItemBullet.bulletsList.containsKey(pack))
								|| (((List) GunCusItemBullet.bulletsList.get(pack)).size() <= bullets) || (((List) GunCusItemBullet.bulletsList
								.get(pack)).get(bullets) == null))) {
					log("[" + pack + "] The bullets of the gun \"" + name + "\" do not exist (Bullet ID:" + bullets
							+ ")! Ignoring this gun!");
				}
			} else {
				log("[" + pack + "] Something went wrong while initializing the gun \"" + name
						+ "\"! Ignoring this gun!");
			}
			config1.save();
		}
	}

	private void sounds(String path1) {
		File file = new File(path1 + "/assets/minecraft/sound");

		if ((file != null) && (file.exists())) {
			for (File f : file.listFiles()) {
				if ((f.getName().endsWith(".ogg")) || ((f.getName().endsWith(".wav")) && (!f.getName().contains(" ")))) {
					GunCusSound.addSound(f.getName());
				}
			}
		} else {
			log("Could not load sounds!");
		}
	}

	public static void log(Object s) {
		System.out.println("[GunCus] " + s);
	}
}