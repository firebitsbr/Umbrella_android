package org.secfirst.umbrella.feature.form

import org.secfirst.umbrella.data.database.form.Answer
import org.secfirst.umbrella.data.database.form.Item
import org.secfirst.umbrella.data.database.form.Option

enum class FieldType(val value: String) {
    TEXT_INPUT("text_input"),
    LABEL("label"),
    TEXT_AREA("text_area"),
    MULTIPLE_CHOICE("multiple_choice"),
    SINGLE_CHOICE("single_choice"),
    HINT("hint")
}

fun Option.hasAnswer(answers: List<Answer>?): Answer {
    answers?.forEach { answer ->
        if (this.id == answer.optionId)
            return answer
    }
    return Answer()
}

fun Item.hasAnswer(answers: List<Answer>?): Answer {
    answers?.forEach { answer ->
        if (this.id == answer.itemId)
            return answer
    }
    return Answer()
}
