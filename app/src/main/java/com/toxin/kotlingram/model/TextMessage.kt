package com.toxin.kotlingram.model

import java.util.*

data class TextMessage (
    val text: String,
    override val time: Date,
    override val senderId: String,
    override val type: String = MessageType.TEXT,
    override val read: Boolean = false
) : Message {

    constructor() : this("", Date(0), "")

}