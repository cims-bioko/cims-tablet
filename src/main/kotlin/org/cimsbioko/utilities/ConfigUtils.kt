package org.cimsbioko.utilities

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager.NameNotFoundException
import android.util.Log
import org.cimsbioko.App
import org.cimsbioko.R
import org.cimsbioko.navconfig.NavigatorConfig
import org.cimsbioko.navconfig.NavigatorModule
import org.cimsbioko.navconfig.forms.Binding
import java.util.*

object ConfigUtils {

    var TAG: String = ConfigUtils::class.java.simpleName

    @JvmStatic
    fun getResourceString(context: Context, id: Int): String {
        return context.getString(id)
    }

    @JvmStatic
    fun getSharedPrefs(ctx: Context): SharedPreferences {
        return ctx.getSharedPreferences(ctx.packageName + "_preferences", Context.MODE_PRIVATE)
    }

    @JvmStatic
    fun getPreferenceString(context: Context, key: Int, defaultValue: String?): String? {
        return getSharedPrefs(context).getString(getResourceString(context, key), defaultValue)
    }

    @JvmStatic
    fun getPreferenceString(context: Context, key: String, defaultValue: String?): String? {
        return getSharedPrefs(context).getString(key, defaultValue)
    }

    @JvmStatic
    fun getMultiSelectPreference(context: Context, key: String, defaultValues: Set<String>): Set<String> {
        return getSharedPrefs(context).getStringSet(key, defaultValues)!!
    }

    @JvmStatic
    fun getPreferenceBool(context: Context, key: String, defaultValue: Boolean): Boolean {
        return getSharedPrefs(context).getBoolean(key, defaultValue)
    }

    @Throws(NameNotFoundException::class)
    fun getVersion(ctx: Context): String {
        return ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName
    }

    @JvmStatic
    fun getActiveModules(ctx: Context): Collection<NavigatorModule> = NavigatorConfig.instance.run {
        val activeModuleNames = getMultiSelectPreference(ctx, ctx.getString(R.string.active_modules_key), moduleNames)
        modules.filter { it.name in activeModuleNames }
    }

    @JvmStatic
    fun clearActiveModules() {
        val ctx: Context = App.instance
        getSharedPrefs(ctx).edit().remove(ctx.getString(R.string.active_modules_key)).apply()
        Log.i(TAG, "cleared active modules from preferences")
    }

    @JvmStatic
    fun getActiveModuleForBinding(ctx: Context, binding: Binding): Collection<NavigatorModule> {
        val modules: MutableList<NavigatorModule> = ArrayList()
        for (module in getActiveModules(ctx)) {
            if (module.bindings.containsKey(binding.name)) {
                modules.add(module)
            }
        }
        return modules
    }

    private fun getAppName(context: Context): String {
        return getResourceString(context, R.string.app_name)
    }

    @JvmStatic
    fun getAppFullName(context: Context): String {
        val appName = getAppName(context)
        return try {
            String.format("%s %s", appName, getVersion(context))
        } catch (e: NameNotFoundException) {
            appName
        }
    }
}