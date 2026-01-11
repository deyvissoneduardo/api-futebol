package br.com.futebol.infrastructure.user;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Duration;

/**
 * Converter para mapear INTERVAL do PostgreSQL para Duration do Java.
 * O PostgreSQL retorna INTERVAL como String via JDBC, então fazemos o parse manualmente.
 */
@Converter(autoApply = false)
public class DurationIntervalConverter implements AttributeConverter<Duration, String> {

    /**
     * Converte Duration para String no formato PostgreSQL INTERVAL (HH:MM:SS).
     */
    @Override
    public String convertToDatabaseColumn(Duration duration) {
        if (duration == null || duration.isZero()) {
            return "00:00:00";
        }
        
        long totalSeconds = duration.getSeconds();
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * Converte String do banco (formato INTERVAL) para Duration.
     */
    @Override
    public Duration convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Duration.ZERO;
        }
        
        try {
            // PostgreSQL retorna INTERVAL em vários formatos possíveis:
            // "HH:MM:SS" ou "X days HH:MM:SS" ou "HH:MM:SS.microseconds"
            String cleaned = dbData.trim();
            
            // Se contém "days", extrair dias primeiro
            long days = 0;
            if (cleaned.contains("days")) {
                String[] parts = cleaned.split(" days ");
                if (parts.length > 0) {
                    days = Long.parseLong(parts[0].trim());
                    cleaned = parts.length > 1 ? parts[1] : "00:00:00";
                }
            }
            
            // Parse do formato HH:MM:SS ou HH:MM:SS.microseconds
            String[] timeParts = cleaned.split(":");
            if (timeParts.length >= 3) {
                int hours = Integer.parseInt(timeParts[0].trim());
                int minutes = Integer.parseInt(timeParts[1].trim());
                
                // Tratar segundos que podem ter microssegundos
                String secondsStr = timeParts[2].trim();
                double seconds = Double.parseDouble(secondsStr);
                
                return Duration.ofDays(days)
                        .plusHours(hours)
                        .plusMinutes(minutes)
                        .plusSeconds((long) seconds);
            }
            
            return Duration.ZERO;
        } catch (Exception e) {
            // Em caso de erro no parse, retornar zero
            return Duration.ZERO;
        }
    }
}

