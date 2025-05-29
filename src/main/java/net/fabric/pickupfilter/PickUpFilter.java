package net.fabric.pickupfilter;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.HashSet;
import java.util.Set;

public class PickUpFilter implements ModInitializer {
	public static final String MOD_ID = "pick-up-filter";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static Set<Identifier> WHITELIST = new HashSet<>();

	public static boolean filterEnabled = false;

	@Override
	public void onInitialize() {
		LOGGER.info("PickUpFilter mod initialized!");
		WHITELIST = WhitelistLoader.loadWhitelist();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			registerCommands(dispatcher);
		});
	}

	private void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("pickupfilter")
				.then(CommandManager.literal("help")
						.executes(context -> {
							ServerCommandSource source = context.getSource();
							context.getSource().sendMessage(Text.literal("/pickupfilter on - Opens the filter"));
							context.getSource().sendMessage(Text.literal("/pickupfilter off - Close the filter."));
							context.getSource().sendMessage(Text.literal("/pickupfilter add <item> - Adds item to the filter."));
							context.getSource().sendMessage(Text.literal("/pickupfilter del <item> - Deletes item from the filter."));
							context.getSource().sendMessage(Text.literal("/pickupfilter reload - Refreshes item filter."));
							context.getSource().sendMessage(Text.literal("/pickupfilter list - Displays filtered items."));
							context.getSource().sendMessage(Text.literal("/pickupfilter clear - Clears filtered items."));
							return 1;
						})
				)
				.then(CommandManager.argument("state", StringArgumentType.word())
						.suggests((context, builder) -> {
							builder.suggest("help");
							builder.suggest("on");
							builder.suggest("off");
							builder.suggest("add");
							builder.suggest("del");
							builder.suggest("reload");
							builder.suggest("list");
							builder.suggest("clear");
							return builder.buildFuture();
						})
						.executes(context -> {
							String state = StringArgumentType.getString(context, "state");
							if (state.equalsIgnoreCase("on")) {
								filterEnabled = true;
								context.getSource().sendFeedback(() -> Text.of("Pick Up Filter opened!"), false);
								LOGGER.info("Pick Up Filter opened!");
							} else if (state.equalsIgnoreCase("off")) {
								filterEnabled = false;
								context.getSource().sendFeedback(() -> Text.of("Pick Up Filter closed!"), false);
								LOGGER.info("Pick Up Filter closed!");
							} else if (state.equalsIgnoreCase("reload")) {
								WHITELIST.clear();
								WHITELIST.addAll(WhitelistLoader.loadWhitelist());
								context.getSource().sendFeedback(() -> Text.of("Pick Up Filter reloaded!"), false);
								LOGGER.info("Pick Up Filter reloaded!");
							} else if (state.equalsIgnoreCase("add") || state.equalsIgnoreCase("del")) {
								context.getSource().sendError(Text.literal("Please specify an item name with the '" + state + "' command."));
							} else {
								context.getSource().sendError(Text.literal("Pick Up Filter: Invalid parameter!"));
							}
							return 1;
						})
				)
				.then(CommandManager.literal("add")
						.then(CommandManager.argument("item_name", StringArgumentType.greedyString())
								.suggests((context, builder) -> {
									Registries.ITEM.getIds().forEach(id -> {
										if (id.toString().startsWith(builder.getRemaining().toLowerCase())) {
											builder.suggest(id.toString());
										}
									});
									return builder.buildFuture();
								})
								.executes(context -> {
									String itemName = StringArgumentType.getString(context, "item_name");
									Identifier itemId = Identifier.tryParse(itemName);
									if (itemId == null || !itemId.getNamespace().equals("minecraft") || !Registries.ITEM.containsId(itemId)) {
										context.getSource().sendError(Text.of("That item does not exist in Minecraft!"));
										return 0;
									}
									if (WHITELIST.add(itemId)) {
										WhitelistLoader.saveWhitelist(WHITELIST);
										context.getSource().sendFeedback(() -> Text.of("Item added to whitelist: " + itemName), false);
									} else {
										context.getSource().sendFeedback(() -> Text.of("Item is already in whitelist."), false);
									}
									return 1;
								})
						)
				)
				.then(CommandManager.literal("del")
						.then(CommandManager.argument("item_name", StringArgumentType.greedyString())
								.suggests((context, builder) -> {
									Registries.ITEM.getIds().forEach(id -> {
										if (id.toString().startsWith(builder.getRemaining().toLowerCase())) {
											builder.suggest(id.toString());
										}
									});
									return builder.buildFuture();
								})
								.executes(context -> {
									String itemName = StringArgumentType.getString(context, "item_name");
									Identifier itemId = Identifier.tryParse(itemName);
									if (itemId == null || !itemId.getNamespace().equals("minecraft") || !Registries.ITEM.containsId(itemId)) {
										context.getSource().sendError(Text.of("That item does not exist in Minecraft!"));
										return 0;
									}
									if (WHITELIST.remove(itemId)) {
										WhitelistLoader.saveWhitelist(WHITELIST);
										context.getSource().sendFeedback(() -> Text.of("Item removed from whitelist: " + itemName), false);
									} else {
										context.getSource().sendFeedback(() -> Text.of("Item was not in whitelist."), false);
									}
									return 1;
								})
						)
				)
				.then(CommandManager.literal("clear")
						.executes(context -> {
							WHITELIST.clear();
							WhitelistLoader.saveWhitelist(WHITELIST);
							context.getSource().sendFeedback(() -> Text.of("Pick Up Filter list cleared!"), false);
							LOGGER.info("Pick Up Filter list cleared!");
							return 1;
						})
				)
				.then(CommandManager.literal("list")
						.executes(context -> {
							if (WHITELIST.isEmpty()) {
								context.getSource().sendFeedback(() -> Text.of("List is empty."), false);
								return 1;
							}
							StringBuilder itemList = new StringBuilder("Whitelisted Items:\n");
							WHITELIST.forEach(id -> itemList.append("- ").append(id.toString()).append("\n"));
							context.getSource().sendFeedback(() -> Text.of(itemList.toString()), false);
							return 1;
						})
				)
		);
	}
}