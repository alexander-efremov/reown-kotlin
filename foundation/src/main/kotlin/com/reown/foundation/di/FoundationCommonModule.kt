package com.reown.foundation.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tinder.scarlet.utils.getRawType
import com.reown.foundation.common.adapters.SubscriptionIdAdapter
import com.reown.foundation.common.adapters.TopicAdapter
import com.reown.foundation.common.adapters.TtlAdapter
import com.reown.foundation.common.model.SubscriptionId
import com.reown.foundation.common.model.Topic
import com.reown.foundation.common.model.Ttl
import com.reown.foundation.util.Logger
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.reflect.jvm.jvmName

fun foundationCommonModule() = module {

    single {
        KotlinJsonAdapterFactory()
    }

    single<Moshi>(named(FoundationDITags.MOSHI)) {
        Moshi.Builder()
            .add { type, _, _ ->
                when (type.getRawType().name) {
                    SubscriptionId::class.jvmName -> SubscriptionIdAdapter
                    Topic::class.jvmName -> TopicAdapter
                    Ttl::class.jvmName -> TtlAdapter
                    else -> null
                }
            }
            .addLast(get<KotlinJsonAdapterFactory>())
            .build()
    }

    single<Logger> {
        object : Logger {
            override fun log(logMsg: String?) {
                println(logMsg)
            }

            override fun log(throwable: Throwable?) {
                println(throwable?.stackTraceToString() ?: throwable?.message!!)
            }

            override fun error(errorMsg: String?) {
                println(errorMsg)
            }

            override fun error(throwable: Throwable?) {
                println(throwable?.stackTraceToString() ?: throwable?.message!!)
            }
        }
    }
}