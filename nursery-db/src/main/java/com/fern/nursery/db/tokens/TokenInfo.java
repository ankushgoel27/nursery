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

package com.fern.nursery.db.tokens;

import com.fern.nursery.db.StagedImmutablesStyle;
import java.time.LocalDateTime;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@StagedImmutablesStyle
public interface TokenInfo {

    String tokenId();

    String ownerId();

    Optional<String> description();

    LocalDateTime createdDateTime();

    static ImmutableTokenInfo.TokenIdBuildStage builder() {
        return ImmutableTokenInfo.builder();
    }
}
