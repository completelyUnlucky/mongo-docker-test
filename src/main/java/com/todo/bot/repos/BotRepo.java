package com.todo.bot.repos;

import com.todo.bot.model.BotModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BotRepo extends MongoRepository<BotModel, Long> {

}
