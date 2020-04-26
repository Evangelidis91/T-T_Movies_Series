package com.evangelidis.t_tmoviesseries.model

class MessagePost(var email: String, var messageBody: String, var date: String, var name: String) {

    fun toMap(): Map<String, Any> {
        val result = HashMap<String, Any>()
        result["email"] = email
        result["messageBody"] = messageBody
        result["date"] = date
        result["name"] = name
        return result
    }
}