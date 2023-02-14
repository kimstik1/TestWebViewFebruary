package com.visoft.testwebview

object Utils {

    fun nakedLinkToFinalLink(nakedLink: String, company: String, deeplink: String): String{

        val companyList: List<String> = company.split("_")
        val deeplinkList: List<String> = deeplink.split("/")

        var finaList: String = nakedLink.split("?")[0]

        for (i in companyList.indices){
            if (i != 0) finaList += "&"
            finaList += "sub${i+1}=${companyList[i]}"
        }

        var deeplinkCounter = 0

        for(i in companyList.size until companyList.size + deeplinkList.size){

            finaList += "sub${i+1}=${deeplinkList[deeplinkCounter]}"
            deeplinkCounter++
        }

        return finaList
    }
}