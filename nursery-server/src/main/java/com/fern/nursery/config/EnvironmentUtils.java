/*
 * (c) Copyright 2022 Birch Solutions Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fern.nursery.config;

import java.util.Optional;

public final class EnvironmentUtils {

    private EnvironmentUtils() {}

    public static String readVariableFromEnvironmentOrProperty(String varName) {
        return maybeReadVariableFromEnvironmentOrProperty(varName)
                .orElseThrow(() -> new IllegalStateException(
                        "Expected " + varName + " to be in environment or system properties! Not found."));
    }

    public static Optional<String> maybeReadVariableFromEnvironmentOrProperty(String varName) {
        String envValue = System.getenv(varName);
        if (envValue == null) {
            return Optional.ofNullable(System.getProperty(varName));
        }
        return Optional.of(envValue);
    }
}