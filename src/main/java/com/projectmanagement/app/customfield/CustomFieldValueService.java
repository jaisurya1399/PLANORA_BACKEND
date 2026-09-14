package com.projectmanagement.app.customfield;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.projectmanagement.app.ticket.Ticket;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomFieldValueService {
    private final CustomFieldValueRepository valueRepo;
    private final CustomFieldRepository fieldRepo;
    private final FieldConfigurationService configService;

    @Transactional(readOnly = true)
    public Map<String, String> getValues(Long ticketId) {
        Map<String, String> m = new LinkedHashMap<>();
        for (CustomFieldValue v : valueRepo.findByTicketId(ticketId))
            m.put(v.getField().getKey(), v.getValue());
        return m;
    }

    public void replaceValues(Ticket ticket, Map<String, String> values, Long projectId, Long typeId) {
        List<FieldConfigurationResponse> configs = configService.effective(projectId, typeId);
        Map<String, String> input = values == null ? Map.of() : values;
        Set<String> allowed = new HashSet<>();
        for (FieldConfigurationResponse c : configs) {
            if (!Boolean.TRUE.equals(c.getVisible()))
                continue;
            allowed.add(c.getFieldKey());
            String v = input.get(c.getFieldKey());
            if (Boolean.TRUE.equals(c.getRequired()) && (v == null || v.isBlank()))
                throw bad("Custom field is required: " + c.getFieldName());
            if (v != null) {
                validate(c, v);
            }
        }
        for (String key : input.keySet())
            if (!allowed.contains(key))
                throw bad("Custom field is not configured for this issue type: " + key);
        valueRepo.deleteByTicketId(ticket.getId());
        for (Map.Entry<String, String> e : input.entrySet()) {
            if (e.getValue() == null)
                continue;
            CustomField f = fieldRepo.findByKey(e.getKey())
                    .orElseThrow(() -> bad("Unknown custom field: " + e.getKey()));
            valueRepo.save(CustomFieldValue.builder().ticket(ticket).field(f).value(e.getValue()).build());
        }
    }

    private void validate(FieldConfigurationResponse c, String v) {
        try {
            switch (c.getFieldType()) {
                case NUMBER -> new java.math.BigDecimal(v);
                case DATE -> LocalDate.parse(v);
                case DATETIME -> LocalDateTime.parse(v);
                case BOOLEAN -> Boolean.parseBoolean(v);
                case URL -> {
                    if (!Pattern.matches("https?://.+", v))
                        throw bad("Invalid URL for " + c.getFieldName());
                }
                case SELECT, MULTI_SELECT, TEXT, TEXTAREA -> {
                }
            }
        } catch (NumberFormatException | DateTimeParseException ex) {
            throw bad("Invalid value for custom field: " + c.getFieldName());
        }
    }

    private ResponseStatusException bad(String m) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, m);
    }
}
