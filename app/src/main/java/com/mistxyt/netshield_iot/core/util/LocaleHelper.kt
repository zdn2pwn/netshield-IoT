package com.mistxyt.netshield_iot.core.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import com.mistxyt.netshield_iot.domain.repository.AppLanguage
import java.util.Locale

object LocaleHelper {

    fun getLocalizedContext(context: Context, language: AppLanguage): Context {
        val locale = when (language) {
            AppLanguage.EN -> Locale("en", "US")
            AppLanguage.ID -> Locale("in", "ID")
            AppLanguage.SYSTEM -> return context
        }

        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        val localizedContext = context.createConfigurationContext(config)

        return object : ContextWrapper(context) {
            override fun getResources(): Resources = localizedContext.resources
            override fun getAssets(): AssetManager = localizedContext.assets
        }
    }
}
