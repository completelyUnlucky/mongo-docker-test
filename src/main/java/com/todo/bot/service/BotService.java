package com.todo.bot.service;

import com.todo.bot.config.BotConfig;
import com.todo.bot.model.BotModel;
import com.todo.bot.repos.BotRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

@Slf4j
@Component
public class BotService extends TelegramLongPollingBot {

    private final BotConfig botConfig;

    @Autowired
    BotRepo botRepo;

    public BotService(BotConfig botConfig) {
        this.botConfig = botConfig;
        List<BotCommand> botCommandList = new ArrayList<>();
        botCommandList.add(new BotCommand("/start", "Запустить бота"));
        botCommandList.add(new BotCommand("/add", "Добавить задачу"));
        botCommandList.add(new BotCommand("/list", "Получить список добавленных задач"));
        botCommandList.add(new BotCommand("/complete", "Пометить задание как завершенное"));
        botCommandList.add(new BotCommand("/remove", "Убрать задачу из списка"));
        botCommandList.add(new BotCommand("/help", "Инструкция по использованию"));
        try {
            this.execute(new SetMyCommands(botCommandList, new BotCommandScopeDefault(), "en"));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (botRepo.findById(chatId).isEmpty()) {
                botRepo.save(new BotModel(chatId, new ArrayList<>()));
            }

            if (messageText.equals("/start")) {
                sendMessage(chatId, "Welcome to ToDo Bot!\nUse /add <task> to add tasks.", false);
            } else if(messageText.startsWith("/add ")) {
                String task = messageText.substring(5);
                addTask(chatId, task);
                sendMessage(chatId, "Task added: " + task, false);
            } else if (messageText.equals("/list")) {
                StringBuilder tasks = new StringBuilder("Ваши задачи:\n");

                int count = 1;
                for (String task : botRepo.findById(chatId).get().getToDoList()) {
                    tasks.append(count).append(". ").append(task).append("\n");
                    count++;
                }
                sendMessage(chatId, tasks.toString(), false);
            } else if (messageText.startsWith("/complete ")) {
                String task = messageText.substring(10);
                completeTask(chatId, task);
                sendMessage(chatId, "Task completed: " + task,
                        true);
            } else if (messageText.startsWith("/remove ")) {
                String task = messageText.substring(8);
                removeTask(chatId, task);
                sendMessage(chatId, "Task removed: " + task, false);
            } else if (messageText.equals("/help")) {
                sendMessage(chatId,
                        """
                        • Добавить задачу в список дел - /add <задача>
                        Пример: /add Побегать с утра
                        
                        • Получить список добавленных задач - /list
                        Пример: /list
                        
                        • Пометить задание как завершенное - /complete <задача>
                        Пример: /complete Сделать домашнее задание
                        
                        • Убрать задачу из списка - /remove <задача>
                        Пример: /remove Вкусно покушать
                        
                        """, false);
            }
        }
    }

    private void sendMessage(long chatId, String message, boolean useMarkdown) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        if (useMarkdown) {
            sendMessage.setParseMode("MarkdownV2");
        }
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public void completeTask(Long chatId, String completeTask) {
        Optional<BotModel> entity = botRepo.findById(chatId);
        if (entity.isPresent()) {
            BotModel existingEntity = entity.get();
            List<String> list = existingEntity.getToDoList();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).equals(completeTask)) {
                    list.set(i, completeTask + "✅");
                    break;
                }
            }
            botRepo.save(existingEntity);
        }
    }

    public void removeTask(Long chatId, String task) {
        Optional<BotModel> entity = botRepo.findById(chatId);
        if (entity.isPresent()) {
            BotModel existingEntity = entity.get();
            List<String> list = existingEntity.getToDoList();
            list.remove(task + "✅");
            if (!list.contains(task + "✅")) {
                list.remove(task);
            }
            botRepo.save(existingEntity);
        }
    }

    public void addTask(Long chatId, String task) {

        Optional<BotModel> entity = botRepo.findById(chatId);
        if (entity.isPresent()) {
            BotModel existingEntity = entity.get();
            List<String> list = existingEntity.getToDoList();
            list.add(task);
            botRepo.save(existingEntity);
        }

    }

    @Override
    public String getBotUsername() {
        return botConfig.getUsername();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

}
