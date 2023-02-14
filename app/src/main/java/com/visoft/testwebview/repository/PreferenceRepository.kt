package com.visoft.testwebview.repository

interface PreferenceRepository {

    suspend fun saveDeeplink(link: String)
    suspend fun getDeeplink(): String

    suspend fun saveCompany(company: String)
    suspend fun getCompany(): String

    suspend fun saveFinalLink(link: String)
    suspend fun getFinalLink(): String

    suspend fun getFirstVisit(): Boolean
}