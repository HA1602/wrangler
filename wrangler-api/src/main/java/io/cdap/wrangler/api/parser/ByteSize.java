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
 * bytes.
 */
@PublicEvolving
public class ByteSize implements Token {
    private static final Pattern BYTE_PATTERN = Pattern.compile("([0-9]*\.?[0-9]+)\s*([kKmMgGtTpP]?[bB]?)");
    private static final Map<String, BigDecimal> UNIT_MULTIPLIERS = Map.of(
        "B", BigDecimal.valueOf(1),
        "KB", BigDecimal.valueOf(1024),
        "MB", BigDecimal.valueOf(1024 * 1024),
        "GB", BigDecimal.valueOf(1024 * 1024 * 1024),
        "TB", BigDecimal.valueOf(1024L * 1024 * 1024 * 1024),
        "PB", BigDecimal.valueOf(1024L * 1024 * 1024 * 1024 * 1024)
    );

    private final String originalValue;
    private final BigDecimal totalBytes;

    public ByteSize(String originalValue) {
        if (originalValue == null || originalValue.isEmpty()) {
            throw new IllegalArgumentException("Byte size value cannot be null or empty");
        }
        this.originalValue = originalValue;
        this.totalBytes = convertToBytes(originalValue);
    }

    private BigDecimal convertToBytes(String sizeStr) {
        Matcher matcher = BYTE_PATTERN.matcher(sizeStr.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid byte size format: " + sizeStr);
        }

        BigDecimal value = new BigDecimal(matcher.group(1));
        String unit = matcher.group(2).toUpperCase();

        return value.multiply(UNIT_MULTIPLIERS.getOrDefault(unit, BigDecimal.ONE));
    }

    @Override
    public String value() {
        return originalValue;
    }

    public BigDecimal getBytes() {
        return totalBytes;
    }

    public BigDecimal toKilobytes() {
        return totalBytes.divide(BigDecimal.valueOf(1024), BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal toMegabytes() {
        return totalBytes.divide(BigDecimal.valueOf(1024 * 1024), BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal toGigabytes() {
        return totalBytes.divide(BigDecimal.valueOf(1024 * 1024 * 1024), BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public TokenType type() {
        return TokenType.BYTE_SIZE;
    }

    @Override
    public JsonElement toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", TokenType.BYTE_SIZE.name());
        object.addProperty("value", originalValue);
        object.addProperty("bytes", totalBytes.toString());
        return object;
    }
}
