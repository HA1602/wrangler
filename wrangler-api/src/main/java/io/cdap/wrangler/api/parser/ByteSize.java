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
 * Token class representing byte size values with units (e.g., "10KB", "5MB").
 * Parses and stores byte sizes, providing methods to retrieve the value in
 * bytes, kilobytes, megabytes, and gigabytes.
 */
@PublicEvolving
public class ByteSize implements Token {

    // Regular expression pattern to identify numerical values and byte size units
    private static final Pattern BYTE_PATTERN = Pattern.compile("([0-9]*\.?[0-9]+)\s*([kKmMgGtTpP]?[bB]?)");

    // Mapping of unit types to their respective byte multipliers
    private static final Map<String, BigDecimal> UNIT_MULTIPLIERS = Map.of(
        "B", BigDecimal.valueOf(1),
        "KB", BigDecimal.valueOf(1024),
        "MB", BigDecimal.valueOf(1024 * 1024),
        "GB", BigDecimal.valueOf(1024 * 1024 * 1024),
        "TB", BigDecimal.valueOf(1024L * 1024 * 1024 * 1024),
        "PB", BigDecimal.valueOf(1024L * 1024 * 1024 * 1024 * 1024)
    );

    // Original string representation of the byte size
    private final String originalValue;

    // Total byte size calculated from the original value
    private final BigDecimal totalBytes;

    /**
     * Constructs a ByteSize object from a string representation.
     *
     * @param originalValue The original string representing the byte size (e.g., "10KB", "1.5MB")
     */
    public ByteSize(String originalValue) {
        if (originalValue == null || originalValue.isEmpty()) {
            throw new IllegalArgumentException("Byte size value cannot be null or empty");
        }
        this.originalValue = originalValue;
        this.totalBytes = convertToBytes(originalValue);
    }

    /**
     * Converts the provided string representation into bytes.
     *
     * @param sizeStr The string containing the numerical value and unit (e.g., "10KB")
     * @return The equivalent size in bytes
     */
    private BigDecimal convertToBytes(String sizeStr) {
        Matcher matcher = BYTE_PATTERN.matcher(sizeStr.trim());

        // Validate if the string matches the byte size pattern
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid byte size format: " + sizeStr);
        }

        // Extract the numerical part and the unit part
        BigDecimal value = new BigDecimal(matcher.group(1));
        String unit = matcher.group(2).toUpperCase();

        // Convert the value to bytes using the multiplier map
        return value.multiply(UNIT_MULTIPLIERS.getOrDefault(unit, BigDecimal.ONE));
    }

    /**
     * Returns the original string representation of the byte size.
     */
    @Override
    public String value() {
        return originalValue;
    }

    /**
     * Returns the total size in bytes.
     *
     * @return Size in bytes
     */
    public BigDecimal getBytes() {
        return totalBytes;
    }

    /**
     * Converts the byte size to kilobytes (KB).
     *
     * @return Size in kilobytes
     */
    public BigDecimal toKilobytes() {
        return totalBytes.divide(BigDecimal.valueOf(1024), BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Converts the byte size to megabytes (MB).
     *
     * @return Size in megabytes
     */
    public BigDecimal toMegabytes() {
        return totalBytes.divide(BigDecimal.valueOf(1024 * 1024), BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Converts the byte size to gigabytes (GB).
     *
     * @return Size in gigabytes
     */
    public BigDecimal toGigabytes() {
        return totalBytes.divide(BigDecimal.valueOf(1024 * 1024 * 1024), BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Defines the type of the token as BYTE_SIZE.
     *
     * @return The TokenType as BYTE_SIZE
     */
    @Override
    public TokenType type() {
        return TokenType.BYTE_SIZE;
    }

    /**
     * Converts the ByteSize instance to a JSON representation.
     *
     * @return A JSON element containing type, value, and byte size
     */
    @Override
    public JsonElement toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", TokenType.BYTE_SIZE.name());
        object.addProperty("value", originalValue);
        object.addProperty("bytes", totalBytes.toString());
        return object;
    }
}
