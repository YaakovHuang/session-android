package org.thoughtcrime.securesms.et

import org.thoughtcrime.securesms.wallet.Token

/**
 * Created by Yaakov on
 * Describe:
 */
data class RefreshEvent(val et: ET?)

data class UserUpdateEvent(val user: User?)

data class TokenUpdateEvent(val token: Token?)