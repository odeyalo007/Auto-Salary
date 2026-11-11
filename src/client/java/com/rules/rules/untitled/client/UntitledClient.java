package com.rules.rules.untitled.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.text.Text;
import com.mojang.brigadier.arguments.StringArgumentType;

public class UntitledClient implements ClientModInitializer {
    private static int ticks = 0;
    private static boolean isRunning = true;

    @Override
    public void onInitializeClient() {
        System.out.println("Клиентский таймер запущен!");

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (isRunning && client.player != null) {
                ticks++;

                if (ticks >= 36000) { // 30 минут
                    ticks = 0;
                    client.player.networkHandler.sendChatCommand("salary");
                    client.player.sendMessage(Text.literal("§e Зарплата получена!"), false);
                }

                if (ticks % 6000 == 0 && ticks > 0) { // Каждые 5 минут
                    int mins = (36000 - ticks) / 1200;
                    client.player.sendMessage(Text.literal("§eЗарплата через " + mins + " мин"), false);
                }
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {

            dispatcher.register(ClientCommandManager.literal("stime")
                    .executes(context -> {
                        int totalSeconds = (36000 - ticks) / 20;
                        int minutes = totalSeconds / 60;
                        int seconds = totalSeconds % 60;
                        context.getSource().sendFeedback(Text.literal("§bОсталось: §f" + minutes + " : " + seconds));
                        return 1;
                    }));

            dispatcher.register(ClientCommandManager.literal("ssettime")
                    .then(ClientCommandManager.argument("time", StringArgumentType.string())
                            .executes(context -> {
                                String timeArg = StringArgumentType.getString(context, "time");
                                try {
                                    int minutes = Integer.parseInt(timeArg.replace("m", ""));
                                    int newTicks = 36000 - (minutes * 1200);
                                    if (newTicks < 0) newTicks = 0;
                                    ticks = newTicks;
                                    context.getSource().sendFeedback(Text.literal("§aТаймер установлен на " + minutes + " минут"));
                                } catch (NumberFormatException e) {
                                    context.getSource().sendFeedback(Text.literal("§cОшибка! Введи число и 'm', например: /ssettime 20m"));
                                }
                                return 1;
                            })));

            dispatcher.register(ClientCommandManager.literal("sstart")
                    .executes(context -> {
                        isRunning = true;
                        context.getSource().sendFeedback(Text.literal("§aТаймер ВКЛЮЧЕН!"));
                        return 1;
                    }));

            dispatcher.register(ClientCommandManager.literal("sstop")
                    .executes(context -> {
                        isRunning = false;
                        context.getSource().sendFeedback(Text.literal("§cТаймер ВЫКЛЮЧЕН!"));
                        return 1;
                    }));

            dispatcher.register(ClientCommandManager.literal("sreset")
                    .executes(context -> {
                        if (context.getSource().getClient().player != null) {
                            context.getSource().getClient().player.networkHandler.sendChatCommand("salary");
                            ticks = 0;
                            context.getSource().sendFeedback(Text.literal("§a Зарплата выдана, таймер сброшен!"));
                        }
                        return 1;

                    }));
        });
    }
}