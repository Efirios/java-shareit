package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Service
public class BookingClient extends BaseClient {

    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + "/bookings"))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build());
    }

    public ResponseEntity<Object> create(long userId, BookingCreateDto dto) {
        return post("", userId, dto);
    }

    public ResponseEntity<Object> approve(long userId, long bookingId, boolean approved) {
        return patch("/{bookingId}?approved={approved}", userId,
                Map.of("bookingId", bookingId, "approved", approved), null);
    }

    public ResponseEntity<Object> getById(long userId, long bookingId) {
        return get("/{bookingId}", userId, Map.of("bookingId", bookingId));
    }

    public ResponseEntity<Object> getByBooker(long userId, String state, int from, int size) {
        return get("?state={state}&from={from}&size={size}", userId,
                Map.of("state", state, "from", from, "size", size));
    }

    public ResponseEntity<Object> getByOwner(long userId, String state, int from, int size) {
        return get("/owner?state={state}&from={from}&size={size}", userId,
                Map.of("state", state, "from", from, "size", size));
    }
}