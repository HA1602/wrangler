/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.cdap.wrangler.api.annotations.PublicEvolving;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Token class representing time duration values with units (e.g., "5s", "10m", "2h").
 * Parses and stores time durations, providing methods to retrieve the value in various time units.
 */
@PublicEvolving
public class TimeDuration implements Token {

    // Regular expression to identify the value and time unit
    private static final Pattern TIME_PATTERN = Pattern.compile("([0-9]+)\s*([smhdwy]|mo)");

    // Original string representation of the time duration
    private final String rawValue;

    // Converted time value in milliseconds
    private final long totalMilliseconds;

    /**
     * Constructs a TimeDuration object from a string representation.
     * @param rawValue The original string representing the time duration (e.g., "5s", "10m", "2h")
     */
    public TimeDuration(String rawValue) {
        if (rawValue == null || rawValue.isEmpty()) {
            throw new IllegalArgumentException("Time duration value cannot be null or empty");
        }
        this.rawValue = rawValue;
        this.totalMilliseconds = parseAndConvert(rawValue);
    }

    /**
     * Parses and converts the provided string representation into milliseconds.
     * @param timeStr The string containing the numerical value and time unit (e.g., "5s")
     * @return The equivalent duration in milliseconds
     */
    private long parseAndConvert(String timeStr) {
        Matcher matcher = TIME_PATTERN.matcher(timeStr.trim());

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid time duration format: " + timeStr);
        }

        long value = Long.parseLong(matcher.group(1));
        String unit = matcher.group(2).toLowerCase();

        return convertToMilliseconds(value, unit);
    }

    /**
     * Converts the parsed value to milliseconds based on the time unit.
     * @param value The numeric value of the duration
     * @param unit The time unit (e.g., "s", "m", "h")
     * @return The duration in milliseconds
     */
    private long convertToMilliseconds(long value, String unit) {
        return switch (unit) {
            case "s" -> TimeUnit.SECONDS.toMillis(value);
            case "m" -> TimeUnit.MINUTES.toMillis(value);
            case "h" -> TimeUnit.HOURS.toMillis(value);
            case "d" -> TimeUnit.DAYS.toMillis(value);
            case "w" -> TimeUnit.DAYS.toMillis(value * 7);
            case "mo" -> TimeUnit.DAYS.toMillis(value * 30);
            case "y" -> TimeUnit.DAYS.toMillis(value * 365);
            default -> throw new IllegalArgumentException("Unsupported time unit: " + unit);
        };
    }

    @Override
    public String value() {
        return rawValue;
    }

    public long getMilliseconds() {
        return totalMilliseconds;
    }

    public double toSeconds() {
        return totalMilliseconds / 1000.0;
    }

    public double toMinutes() {
        return totalMilliseconds / (1000.0 * 60);
    }

    public double toHours() {
        return totalMilliseconds / (1000.0 * 60 * 60);
    }

    public double toDays() {
        return totalMilliseconds / (1000.0 * 60 * 60 * 24);
    }

    @Override
    public TokenType type() {
        return TokenType.TIME_DURATION;
    }

    @Override
    public JsonElement toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", TokenType.TIME_DURATION.name());
        object.addProperty("value", rawValue);
        object.addProperty("milliseconds", totalMilliseconds);
        return object;
    }
}
