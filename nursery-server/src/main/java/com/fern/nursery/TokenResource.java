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

package com.fern.nursery;

import com.fern.nursery.api.model.token.CreateTokenRequest;
import com.fern.nursery.api.model.token.CreateTokenResponse;
import com.fern.nursery.api.model.token.GetTokenMetadataRequest;
import com.fern.nursery.api.model.token.OwnerId;
import com.fern.nursery.api.model.token.TokenId;
import com.fern.nursery.api.model.token.TokenMetadata;
import com.fern.nursery.api.model.token.TokenNotFoundError;
import com.fern.nursery.api.model.token.TokenNotFoundErrorBody;
import com.fern.nursery.api.model.token.TokenStatus;
import com.fern.nursery.api.server.token.TokenService;
import com.fern.nursery.db.NurseryDatabase;
import com.fern.nursery.db.tokens.CreatedToken;
import com.fern.nursery.db.tokens.TokenInfo;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class TokenResource implements TokenService {

    private final NurseryDatabase nurseryDatabase;

    @Inject
    public TokenResource(NurseryDatabase nurseryDatabase) {
        this.nurseryDatabase = nurseryDatabase;
    }

    @Override
    public CreateTokenResponse create(CreateTokenRequest request) {
        CreatedToken createdToken = nurseryDatabase.inTransactionResult(nurseryDao ->
                nurseryDao.tokenDao().createToken(request.getOwnerId().get(), request.getDescription()));
        return CreateTokenResponse.builder()
                .token(createdToken.token())
                .tokenId(TokenId.of(createdToken.tokenId()))
                .build();
    }

    @Override
    public TokenMetadata getTokenMetadata(GetTokenMetadataRequest request) throws TokenNotFoundError {
        return nurseryDatabase
                .inTransactionResult(nurseryDao -> nurseryDao.tokenDao().getToken(request.getToken()))
                .map(TokenResource::convertToTokenMetadata)
                .orElseThrow(() -> new TokenNotFoundError(TokenNotFoundErrorBody.of()));
    }

    @Override
    public List<TokenMetadata> getTokensForOwner(OwnerId ownerId) {
        return nurseryDatabase
                .inTransactionResult(nurseryDao -> nurseryDao.tokenDao().getTokensForOwner(ownerId.get()))
                .stream()
                .map(TokenResource::convertToTokenMetadata)
                .collect(Collectors.toList());
    }

    private static TokenMetadata convertToTokenMetadata(TokenInfo tokenInfo) {
        return TokenMetadata.builder()
                .tokenId(TokenId.of(tokenInfo.tokenId()))
                .ownerId(OwnerId.of(tokenInfo.ownerId()))
                .createdTime(
                        tokenInfo.createdDateTime().format(DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.of("UTC"))))
                .status(TokenStatus.active())
                .description(tokenInfo.description())
                .build();
    }
}
