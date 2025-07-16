package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.enums.EventTypes;
import ru.yandex.practicum.filmorate.model.enums.OperationTypes;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FeedRowMapper implements RowMapper<Feed> {


    @Override
    public Feed mapRow(ResultSet rs, int rowNum) throws SQLException {
        Feed feed = new Feed();
        feed.setTimestamp(rs.getTimestamp("timestamp").getTime());
        feed.setUserId(rs.getInt("user_id"));
        feed.setEventType(EventTypes.valueOf(rs.getString("event_type")));
        feed.setOperation(OperationTypes.valueOf(rs.getString("operation_type")));
        feed.setEventId(rs.getLong("event_id"));
        feed.setEntityId(rs.getLong("entity_id"));
        return feed;
    }
}
