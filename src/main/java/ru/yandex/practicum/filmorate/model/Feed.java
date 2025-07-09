package ru.yandex.practicum.filmorate.model;

import java.sql.Timestamp;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.filmorate.model.enums.EventTypes;
import ru.yandex.practicum.filmorate.model.enums.OperationTypes;

@Getter
@Setter
public class Feed {

    private Long eventId;
    private Long entityId;
    private Long userId;
    private Timestamp timestamp = Timestamp.from(Instant.now());
    private EventTypes eventType;
    private OperationTypes operation;
}
