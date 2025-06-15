package net.neoforge.pickupfilter;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Mod(PickUpFilterNeoForge.MOD_ID)
public class PickUpFilterNeoForge {
    public static final String MOD_ID = "pickupfilter";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static Set<ResourceLocation> WHITELIST = new HashSet<>();
    public static boolean filterEnabled = false;

    public PickUpFilterNeoForge() {
        LOGGER.info("PickUpFilter mod initialized!");
        WHITELIST = WhitelistLoaderNeoForge.loadWhitelist();
        IEventBus bus = NeoForge.EVENT_BUS;
        bus.register(this);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        registerCommands(event.getDispatcher(), "pickupfilter");
        registerCommands(event.getDispatcher(), "puf");
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, String name) {
        dispatcher.register(Commands.literal(name)
                .then(Commands.literal("help")
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            source.sendSuccess(() -> Component.literal("[Pick Up Filter] Available Commands:"), false);
                            source.sendSuccess(() -> Component.literal("/" + name + " on - Opens the filter"), false);
                            source.sendSuccess(() -> Component.literal("/" + name + " off - Close the filter."), false);
                            source.sendSuccess(() -> Component.literal("/" + name + " add <item> - Adds item to the filter list."), false);
                            source.sendSuccess(() -> Component.literal("/" + name + " del <item> - Deletes item from the filter list."), false);
                            source.sendSuccess(() -> Component.literal("/" + name + " reload - Refreshes item filter list."), false);
                            source.sendSuccess(() -> Component.literal("/" + name + " list - Displays filtered items."), false);
                            source.sendSuccess(() -> Component.literal("/" + name + " clear - Clears filtered items list."), false);
                            return 1;
                        })
                )
                .then(Commands.argument("state", StringArgumentType.word())
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
                            switch (state.toLowerCase()) {
                                case "on" -> {
                                    if (!filterEnabled) {
                                        filterEnabled = true;
                                        context.getSource().sendSuccess(() -> Component.literal("[Pick Up Filter] opened!").withStyle(ChatFormatting.GREEN), false);
                                    } else {
                                        context.getSource().sendFailure(Component.literal("[Pick Up Filter] already opened!"));
                                    }
                                }
                                case "off" -> {
                                    if (filterEnabled) {
                                        filterEnabled = false;
                                        context.getSource().sendSuccess(() -> Component.literal("[Pick Up Filter] closed!").withStyle(ChatFormatting.GREEN), false);
                                    } else {
                                        context.getSource().sendFailure(Component.literal("[Pick Up Filter] already closed!"));
                                    }
                                }
                                case "reload" -> {
                                    WHITELIST.clear();
                                    WHITELIST.addAll(WhitelistLoaderNeoForge.loadWhitelist());
                                    context.getSource().sendSuccess(() -> Component.literal("[Pick Up Filter] reloaded!").withStyle(ChatFormatting.GREEN), false);
                                }
                                case "add", "del" -> {
                                    context.getSource().sendFailure(Component.literal("[Pick Up Filter] Please specify an item name with the '" + state + "' command."));
                                }
                                default -> {
                                    context.getSource().sendFailure(Component.literal("[Pick Up Filter] Invalid parameter!"));
                                }
                            }
                            return 1;
                        })
                )
                .then(Commands.literal("add")
                        .then(Commands.argument("item_name", StringArgumentType.greedyString())
                                .suggests((context, builder) -> {
                                    BuiltInRegistries.ITEM.keySet().forEach(id -> {
                                        if (id.toString().startsWith(builder.getRemaining().toLowerCase())) {
                                            builder.suggest(id.toString());
                                        }
                                    });
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String itemName = StringArgumentType.getString(context, "item_name");
                                    ResourceLocation itemId = ResourceLocation.tryParse(itemName);
                                    if (itemId == null || !itemId.getNamespace().equals("minecraft") || !BuiltInRegistries.ITEM.containsKey(itemId)) {
                                        context.getSource().sendFailure(Component.literal("[Pick Up Filter] That item does not exist in Minecraft!"));
                                        return 0;
                                    }
                                    if (WHITELIST.add(itemId)) {
                                        WhitelistLoaderNeoForge.saveWhitelist(WHITELIST);
                                        context.getSource().sendSuccess(() -> Component.literal("[Pick Up Filter] Item added to whitelist: " + itemDisplayName(BuiltInRegistries.ITEM.get(itemId))), false);
                                    } else {
                                        context.getSource().sendSuccess(() -> Component.literal("[Pick Up Filter] Item is already in whitelist."), false);
                                    }
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("del")
                        .then(Commands.argument("item_name", StringArgumentType.greedyString())
                                .suggests((context, builder) -> {
                                    BuiltInRegistries.ITEM.keySet().forEach(id -> {
                                        if (id.toString().startsWith(builder.getRemaining().toLowerCase())) {
                                            builder.suggest(id.toString());
                                        }
                                    });
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String itemName = StringArgumentType.getString(context, "item_name");
                                    ResourceLocation itemId = ResourceLocation.tryParse(itemName);
                                    if (itemId == null || !itemId.getNamespace().equals("minecraft") || !BuiltInRegistries.ITEM.containsKey(itemId)) {
                                        context.getSource().sendFailure(Component.literal("[Pick Up Filter] That item does not exist in Minecraft!"));
                                        return 0;
                                    }
                                    if (WHITELIST.remove(itemId)) {
                                        WhitelistLoaderNeoForge.saveWhitelist(WHITELIST);
                                        context.getSource().sendSuccess(() -> Component.literal("[Pick Up Filter] Item removed from whitelist: " + itemDisplayName(BuiltInRegistries.ITEM.get(itemId))), false);
                                    } else {
                                        context.getSource().sendSuccess(() -> Component.literal("[Pick Up Filter] Item was not in whitelist."), false);
                                    }
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("clear")
                        .executes(context -> {
                            WHITELIST.clear();
                            WhitelistLoaderNeoForge.saveWhitelist(WHITELIST);
                            context.getSource().sendSuccess(() -> Component.literal("[Pick Up Filter] list cleared!").withStyle(ChatFormatting.GREEN), false);
                            return 1;
                        })
                )
                .then(Commands.literal("list")
                        .executes(context -> {
                            if (WHITELIST.isEmpty()) {
                                context.getSource().sendSuccess(() -> Component.literal("[Pick Up Filter] List is empty."), false);
                                return 1;
                            }
                            StringBuilder itemList = new StringBuilder("[Pick Up Filter] Whitelisted Items:\n");
                            WHITELIST.forEach(id -> {
                                itemList.append("- ").append(itemDisplayName(BuiltInRegistries.ITEM.get(id))).append("\n");
                            });
                            context.getSource().sendSuccess(() -> Component.literal(itemList.toString()), false);
                            return 1;
                        })
                )
        );
    }

    private static String itemDisplayName(Optional<Holder.Reference<Item>> optionalItem) {
        if (optionalItem == null || !optionalItem.isPresent()) {
            return "[Unknown Item]";
        }
        Item item = optionalItem.get().value();  // Holder içinden gerçek Item'ı alıyoruz
        return new ItemStack(item).getDisplayName().getString();
    }
}