package net.fabric.pickupfilter;

import net.common.pickupfilter.PickUpFilter;

import java.util.Set;
import java.util.HashSet;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Formatting;

public class PickUpFilterFabric implements ModInitializer {
    public static final String MOD_ID = "pick-up-filter-fabric";
    public static Set<Identifier> WHITELIST = new HashSet<>();

    public static boolean filterEnabled = false;

    @Override
    public void onInitialize() {
        PickUpFilter.LOGGER.info("PickUpFilter mod initialized!");
        WHITELIST = WhitelistLoaderFabric.loadWhitelist();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerCommands(dispatcher, "pickupfilter");
            registerCommands(dispatcher, "puf");
        });
    }

    private void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, String name) {
        dispatcher.register(CommandManager.literal(name)
                .then(CommandManager.literal("help")
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            context.getSource().sendMessage(Text.literal("[Pick Up Filter] Available Commands:").styled(style -> style.withColor(Formatting.BLUE)));
                            context.getSource().sendMessage(Text.literal("/" + name + " on - Opens the filter"));
                            context.getSource().sendMessage(Text.literal("/" + name + " off - Close the filter."));
                            context.getSource().sendMessage(Text.literal("/" + name + " add <item> - Adds item to the filter list."));
                            context.getSource().sendMessage(Text.literal("/" + name + " del <item> - Deletes item from the filter list."));
                            context.getSource().sendMessage(Text.literal("/" + name + " reload - Refreshes item filter list."));
                            context.getSource().sendMessage(Text.literal("/" + name + " list - Displays filtered items."));
                            context.getSource().sendMessage(Text.literal("/" + name + " clear - Clears filtered items list."));
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
                                if (!filterEnabled) {
                                    filterEnabled = true;
                                    context.getSource().sendFeedback(() -> Text.literal("[Pick Up Filter] opened!").styled(style -> style.withColor(Formatting.GREEN)), false);
                                } else {
                                    context.getSource().sendError(Text.literal("[Pick Up Filter] already opened!").styled(style -> style.withColor(Formatting.YELLOW)));
                                }
                            } else if (state.equalsIgnoreCase("off")) {
                                if (filterEnabled) {
                                    filterEnabled = false;
                                    context.getSource().sendFeedback(() -> Text.literal("[Pick Up Filter] closed!").styled(style -> style.withColor(Formatting.GREEN)), false);
                                } else {
                                    context.getSource().sendError(Text.literal("[Pick Up Filter] already closed!").styled(style -> style.withColor(Formatting.YELLOW)));
                                }
                            } else if (state.equalsIgnoreCase("reload")) {
                                WHITELIST.clear();
                                WHITELIST.addAll(WhitelistLoaderFabric.loadWhitelist());
                                context.getSource().sendFeedback(() -> Text.literal("[Pick Up Filter] reloaded!").styled(style -> style.withColor(Formatting.GREEN)), false);
                            } else if (state.equalsIgnoreCase("add") || state.equalsIgnoreCase("del")) {
                                context.getSource().sendError(Text.literal("[Pick Up Filter] Please specify an item name with the '" + state + "' command."));
                            } else {
                                context.getSource().sendError(Text.literal("[Pick Up Filter] Invalid parameter!").styled(style -> style.withColor(Formatting.RED)));
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
                                        context.getSource().sendError(Text.literal("[Pick Up Filter] That item does not exist in Minecraft!").copy().styled(style -> style.withColor(Formatting.RED)));
                                        return 0;
                                    }
                                    if (WHITELIST.add(itemId)) {
                                        WhitelistLoaderFabric.saveWhitelist(WHITELIST);
                                        context.getSource().sendFeedback(() -> Text.of("[Pick Up Filter] Item added to whitelist: " + itemDisplayName(Registries.ITEM.get(itemId))), false);
                                    } else {
                                        context.getSource().sendFeedback(() -> Text.of("[Pick Up Filter] Item is already in whitelist.").copy().styled(style -> style.withColor(Formatting.GREEN)), false);
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
                                        context.getSource().sendError(Text.of("[Pick Up Filter] That item does not exist in Minecraft!").copy().styled(style -> style.withColor(Formatting.RED)));
                                        return 0;
                                    }
                                    if (WHITELIST.remove(itemId)) {
                                        WhitelistLoaderFabric.saveWhitelist(WHITELIST);
                                        context.getSource().sendFeedback(() -> Text.of("[Pick Up Filter] Item removed from whitelist: " + itemDisplayName(Registries.ITEM.get(itemId))).copy().styled(style -> style.withColor(Formatting.GREEN)), false);
                                    } else {
                                        context.getSource().sendFeedback(() -> Text.of("[Pick Up Filter] Item was not in whitelist.").copy().styled(style -> style.withColor(Formatting.RED)), false);
                                    }
                                    return 1;
                                })
                        )
                )
                .then(CommandManager.literal("clear")
                        .executes(context -> {
                            WHITELIST.clear();
                            WhitelistLoaderFabric.saveWhitelist(WHITELIST);
                            context.getSource().sendFeedback(() -> Text.literal("[Pick Up Filter] list cleared!").styled(style -> style.withColor(Formatting.GREEN)), false);
                            return 1;
                        })
                )
                .then(CommandManager.literal("list")
                        .executes(context -> {
                            if (WHITELIST.isEmpty()) {
                                context.getSource().sendFeedback(() -> Text.of("[Pick Up Filter] List is empty.").copy().styled(style -> style.withColor(Formatting.RED)), false);
                                return 1;
                            }
                            StringBuilder itemList = new StringBuilder("[Pick Up Filter] Whitelisted Items:\n");
                            WHITELIST.forEach(id -> {
                                itemList.append("- ").append(itemDisplayName(Registries.ITEM.get(id))).append("\n");
                            });
                            context.getSource().sendFeedback(() -> Text.of(itemList.toString()), false);
                            return 1;
                        })
                )
        );
    }

    private String itemDisplayName(Item itemId) {
        return itemId.getName().getString();
    }
}