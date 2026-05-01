package com.example.hr.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * SQLite does not have native date/time types, so we store them as ISO strings.
 * These converters make JPA persist LocalDate/LocalTime as TEXT automatically.
 */
public class JpaConverters {
  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ISO_LOCAL_TIME;

  @Converter(autoApply = true)
  public static class LocalDateConverter implements AttributeConverter<LocalDate, String> {
    @Override
    public String convertToDatabaseColumn(LocalDate attribute) {
      return attribute == null ? null : DATE_FMT.format(attribute);
    }

    @Override
    public LocalDate convertToEntityAttribute(String dbData) {
      if (dbData == null) {
        return null;
      }
      // Backward compatibility: accept epoch millis stored as TEXT.
      if (dbData.chars().allMatch(Character::isDigit)) {
        long epochMillis = Long.parseLong(dbData);
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate();
      }
      return LocalDate.parse(dbData, DATE_FMT);
    }
  }

  @Converter(autoApply = true)
  public static class LocalTimeConverter implements AttributeConverter<LocalTime, String> {
    @Override
    public String convertToDatabaseColumn(LocalTime attribute) {
      return attribute == null ? null : TIME_FMT.format(attribute);
    }

    @Override
    public LocalTime convertToEntityAttribute(String dbData) {
      return dbData == null ? null : LocalTime.parse(dbData, TIME_FMT);
    }
  }
}
