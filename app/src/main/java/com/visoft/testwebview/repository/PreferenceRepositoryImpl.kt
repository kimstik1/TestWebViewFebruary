package com.visoft.testwebview.repository

import android.content.Context
import android.content.SharedPreferences

class PreferenceRepositoryImpl(context: Context): PreferenceRepository {

    private var shared: SharedPreferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE)


    override suspend fun saveDeeplink(link: String) {
        shared.edit()
                .putString(SHARED_NAME_DEEPLINK, link)
                .apply()
    }

    override suspend fun getDeeplink(): String = shared.getString(SHARED_NAME_DEEPLINK, "") ?: ""


    override suspend fun saveCompany(company: String) {
        shared.edit()
                .putString(SHARED_NAME_COMPANY, company)
                .apply()
    }

    override suspend fun getCompany(): String = shared.getString(SHARED_NAME_COMPANY, "") ?: ""


    override suspend fun saveFinalLink(link: String) {
        shared.edit()
                .putString(SHARED_NAME_FINAL_LINK, link)
                .apply()
    }

    override suspend fun getFinalLink(): String = shared.getString(SHARED_NAME_FINAL_LINK, "") ?: ""


    override suspend fun getFirstVisit(): Boolean {
        val isVisit = shared.getBoolean(SHARED_NAME_VISIT, false)

        if(!isVisit){
            shared.edit()
                    .putBoolean(SHARED_NAME_VISIT, true)
                    .apply()
        }

        return isVisit
    }

    companion object{
        private const val SHARED_NAME = "SharedPreferences.Test.Application"

        private const val SHARED_NAME_DEEPLINK = "SharedPreferences.Test.Application.DeepLink"
        private const val SHARED_NAME_COMPANY = "SharedPreferences.Test.Application.Company"
        private const val SHARED_NAME_VISIT = "SharedPreferences.Test.Application.Visit"
        private const val SHARED_NAME_FINAL_LINK = "SharedPreferences.Test.Application.Final.Link"
    }
}