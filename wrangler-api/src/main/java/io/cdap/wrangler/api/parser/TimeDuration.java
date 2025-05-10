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

import java.math.BigDecimal;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Token class representing time duration values with units (e.g., "5ms", "10s").
 * Parses and stores time durations, providing methods to retrieve the value in
 * milliseconds, seconds, and in a canonical unit.
 */
@PublicEvolving
public class TimeDuration implements Token {

    // Regular expression pattern to identify numerical values and time duration units
    private static final Pattern TIME_PATTERN = Pattern.compile("([0-9]*\.?[0-9]+)\s*([ms])");

    // Mapping of unit types to their respective millisecond multipliers
    private static final Map<String, BigDecimal> UNIT_MULTIPLIERS = Map.of(
        "ms", BigDecimal.valueOf(1),
        "s", BigDecimal.valueOf(1000)
    );

    // Original string representation of the time duration
    private final String originalValue;

    // Total time duration calculated from the original value
    private final BigDecimal totalMilliseconds;

    /**
     * Constructs a TimeDuration object from a string representation.
     *
     * @param originalValue The original string representing the time duration (e.g., "5ms", "10s")
     */
    public TimeDuration(String originalValue) {
        if (originalValue == null || originalValue.isEmpty()) {
            throw new IllegalArgumentException("Time duration value cannot be null or empty");
        }
        this.originalValue = originalValue;
        this.totalMilliseconds = parseAndConvert(originalValue);
    }

    /**
     * Parses and converts the provided string representation into milliseconds.
     *
     * @param timeStr The string containing the numerical value and unit (e.g., "5ms")
     * @return The equivalent duration in milliseconds
     */
    private BigDecimal parseAndConvert(String timeStr) {
        Matcher matcher = TIME_PATTERN.matcher(timeStr.trim());

        // Validate if the string matches the time duration pattern
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid time duration format: " + timeStr);
        }

        // Extract the numerical part and the unit part
        BigDecimal value = new BigDecimal(matcher.group(1));
        String unit = matcher.group(2).toLowerCase();

        // Convert the value to milliseconds using the multiplier map
        return value.multiply(UNIT_MULTIPLIERS.getOrDefault(unit, BigDecimal.ONE));
    }

    @Override
    public String value() {
        return originalValue;
    }

    /**
     * Returns the total duration in milliseconds.
     *
     * @return Duration in milliseconds
     */
    public BigDecimal getMilliseconds() {
        return totalMilliseconds;
    }

    /**
     * Returns the duration in seconds.
     *
     * @return Duration in seconds
     */
    public BigDecimal toSeconds() {
        return totalMilliseconds.divide(BigDecimal.valueOf(1000), BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Retrieves the canonical unit representation (milliseconds) as a long value.
     * @return The time duration as a long value.
     */
    public long getCanonicalMilliseconds() {
        return totalMilliseconds.longValue();
    }

    @Override
    public TokenType type() {
        return TokenType.TIME_DURATION;
    }

    @Override
    public JsonElement toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", TokenType.TIME_DURATION.name());
        object.addProperty("value", originalValue);
        object.addProperty("milliseconds", totalMilliseconds.toString());
        return object;
    }
}
