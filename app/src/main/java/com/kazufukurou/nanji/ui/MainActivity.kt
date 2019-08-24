/*
 * Copyright 2019 Artyom Mironov
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

package com.kazufukurou.nanji.ui

import android.app.Dialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kazufukurou.anyadapter.AnyAdapter
import com.kazufukurou.nanji.model.Prefs
import com.kazufukurou.nanji.R
import com.kazufukurou.nanji.model.Language
import com.kazufukurou.nanji.year
import java.util.Calendar
import java.util.TimeZone

class MainActivity : AppCompatActivity() {
  private val prefs by lazy { Prefs(PreferenceManager.getDefaultSharedPreferences(this)) }
  private val diffUtilItemCallback = object : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
      return oldItem is Item && newItem is Item && oldItem.title == newItem.title
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean = false
  }
  private val myAdapter = AnyAdapter(diffUtilItemCallback)
    .map(::ActionHolder)
    .map(::SwitchHolder)
    .map { EditHolder(it, ::showDialog) }
    .map { SelectorHolder<String>(it, ::showDialog, {}) }
    .map { SelectorHolder<Language>(it, ::showDialog, ::render) }
  private var currentDialog: Dialog? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(
      RecyclerView(this).apply {
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context)
        adapter = myAdapter
      }
    )
    render()
  }

  override fun onPause() {
    super.onPause()
    val ids = AppWidgetManager.getInstance(this).getAppWidgetIds(ComponentName(this, WidgetProvider::class.java))
    sendBroadcast(
      Intent(this, WidgetProvider::class.java)
        .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
    )
    currentDialog?.dismiss()
  }

  private fun render() {
    val languages = Language.values().toList()
    val languageToString: (Language) -> String = { getString(it.title) }
    val timeZones = listOf("") + TimeZone.getAvailableIDs()
    val timeZoneToString: (String) -> String = { if (it.isEmpty()) getString(R.string.languageSystem) else it }
    val messageReplaceDigits = String.format(TEXT_REPLACE_DIGITS_EXAMPLE, getString(R.string.prefsExamples))

    myAdapter.submitList(
      listOfNotNull(
        ActionItem(R.string.appearance, ::goAppearance),
        SelectorItem(R.string.language, languages, prefs::language, languageToString),
        SwitchItem(R.string.prefsTwentyFour, prefs::twentyFour),
        SwitchItem(R.string.prefsShowBattery, prefs::showBattery),
        SwitchItem(R.string.prefsOpenClock, prefs::openClock),
        SwitchItem(R.string.japaneseEra, prefs::japaneseEra).takeIf { prefs.language == Language.ja },
        SelectorItem(R.string.prefsTimeZone, timeZones, prefs::timeZone, timeZoneToString),
        EditItem(R.string.prefsReplaceDigits, messageReplaceDigits, prefs::customSymbols),
        ActionItem(R.string.about, ::showAboutAlert)
      )
    )
  }

  private fun showDialog(dialog: Dialog) {
    currentDialog?.dismiss()
    currentDialog = dialog
    currentDialog?.show()
  }

  private fun goAppearance() {
    startActivity(Intent(this, AppearanceActivity::class.java))
  }

  private fun goEmail(appName: String, appVersion: String) {
    val deviceInfo = "${Build.BRAND} ${Build.MODEL} (${Build.VERSION.SDK_INT})"
    Intent(Intent.ACTION_SEND)
      .setType("plain/text")
      .putExtra(Intent.EXTRA_EMAIL, arrayOf("artyommironov@gmail.com"))
      .putExtra(Intent.EXTRA_SUBJECT, "$appName $appVersion $deviceInfo")
      .takeIf { it.resolveActivity(packageManager) != null }
      ?.let(::startActivity)
  }

  private fun showAboutAlert() {
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    val appName = getString(R.string.appName)
    val appVersion = packageInfo.versionName
    val year = Calendar.getInstance().year
    AlertDialog.Builder(this)
      .setTitle("$appName $appVersion")
      .setMessage(String.format(LICENSE, year))
      .setPositiveButton(android.R.string.ok, null)
      .setNegativeButton(R.string.prefsFeedback) { _, _ -> goEmail(appName, appVersion) }
      .create()
      .let(::showDialog)
  }
}

private const val TEXT_REPLACE_DIGITS_EXAMPLE =  "ABCDEF = A->B C->D E->F\n%s\n零〇~♥\n一壱二弐三参十拾"

private const val LICENSE = """Copyright %d Artyom Mironov

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
"""