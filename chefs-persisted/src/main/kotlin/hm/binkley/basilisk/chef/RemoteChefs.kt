package hm.binkley.basilisk.chef

import io.micronaut.http.client.annotation.Client
import io.micronaut.retry.annotation.Retryable

@Client("http://localhost:7372")
@Retryable
interface RemoteChefs : ChefsOperations
