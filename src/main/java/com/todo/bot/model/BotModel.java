package com.todo.bot.model;

import lombok.Data;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Setter
@Document
public class BotModel {
    @Id
    private final Long chatId;

    private final List<String> toDoList;

}
