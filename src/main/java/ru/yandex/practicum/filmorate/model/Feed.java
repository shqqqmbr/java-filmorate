package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.filmorate.model.enums.EventTypes;
import ru.yandex.practicum.filmorate.model.enums.OperationTypes;

@Getter
@Setter
public class Feed {

    private Long eventId;
    private Long entityId;
    private int userId;
    private long timestamp;
    private EventTypes eventType;
    private OperationTypes operation;
}
